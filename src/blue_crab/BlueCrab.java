package blue_crab;

import java.io.IOException;
import java.net.*;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Vector;
import java.util.Collections;
import java.util.Enumeration;

import rice.Continuation;
import rice.environment.Environment;
import rice.p2p.commonapi.Id;
import rice.p2p.commonapi.NodeHandle;
import rice.p2p.past.*;
import rice.pastry.*;
import rice.pastry.commonapi.PastryIdFactory;
import rice.pastry.socket.SocketPastryNodeFactory;
import rice.pastry.standard.RandomNodeIdFactory;
import rice.persistence.*;

import java.lang.IndexOutOfBoundsException;
import java.util.HashMap;
import org.apache.lucene.queryParser.ParseException;

import blue_crab.Storage.BlueCrabIndexingPersistentStorage;
import blue_crab.Storage.StorageObject;
import blue_crab.Storage.StorageObjectFactory;

import blue_crab.Scribe.BlueCrabSearchResult;
import blue_crab.Search.BlueCrabSearcher;

public class BlueCrab {
	private Vector<Past> nodes;
	private Vector<BlueCrabSearcher> search_nodes;
	//private Vector<>
	private final Environment env;
	private NodeIdFactory node_id_factory;
	private PastryNodeFactory pastry_node_factory;
	private PastryIdFactory local_factory;
	private int number_of_nodes; 
	
	public BlueCrab(int replicas, int port, String hostname, int node_count, String storage_directory) throws Exception {
		env = new Environment();
		nodes = new Vector<Past>();
		this.search_nodes = new Vector<BlueCrabSearcher>();
		node_id_factory = new RandomNodeIdFactory(env);
		pastry_node_factory = new SocketPastryNodeFactory(node_id_factory, port, env);
		local_factory = new rice.pastry.commonapi.PastryIdFactory(env);
		number_of_nodes = node_count;
		InetAddress bootaddr;
		if (hostname == null) {
			bootaddr = BlueCrabIPDetector.findBootAddr();
			if (bootaddr == null) {
				System.err.println("Unable to get bootaddr");	
				throw new Exception();
			}
		} else {
			bootaddr = InetAddress.getByName(hostname);
		}
		InetSocketAddress bootaddress = new InetSocketAddress(bootaddr, port);
			
		for (int i = 0; i < node_count; ++i) {
			PastryNode node = this.pastry_node_factory.newNode();
			PastryIdFactory idf = new rice.pastry.commonapi.PastryIdFactory(env);
			String storageDirectory = storage_directory+node.getId().hashCode();
			Storage stor = new BlueCrabIndexingPersistentStorage(idf, storageDirectory, 4 * 1024 * 1024, node.getEnvironment());
			Past past = new PastImpl(node, new StorageManagerImpl(idf, stor, new LRUCache(new MemoryStorage(idf), 512 * 1024, node.getEnvironment())), replicas,"");
			BlueCrabSearcher searcher = new BlueCrabSearcher(node, (BlueCrabIndexingPersistentStorage)stor);
			nodes.add(past);
			search_nodes.add(searcher);
			if (i == 0){
				node.boot(Collections.EMPTY_LIST);
			} else {
				node.boot(bootaddress);
			}
			synchronized(node){
				while(!node.isReady() && !node.joinFailed()){
					node.wait(500);
					
					if (node.joinFailed()){
						throw new IOException("Could not join the FreePastry Ring. Reason"+node.joinFailedReason());
					}
				}
			}
			
			System.out.println("Finished creating new Node "+i + " | " + node);
		}
		
		System.out.println("Waiting for system to fully boot...");
		env.getTimeSource().sleep(5000);
		
		//GET ALL OF OUR SEARCH NODES GOING
		Iterator<BlueCrabSearcher> i = search_nodes.iterator();
		while (i.hasNext()) {
			BlueCrabSearcher t = i.next();
			t.subscribe();
		}
	}
	private Id set(final StorageObject storageObj) throws Exception{
		PastImpl p = (PastImpl)this.nodes.get(env.getRandomSource().nextInt(number_of_nodes));
		
		BlueCrabContinuation<Boolean[], Exception> c = new BlueCrabContinuation<Boolean[], Exception>(){
			public void receiveResult(Boolean[] results){
				this.received_response = true;
				this.success = true;
				int numSuccessfulStores = 0;
				for (int ctr = 0; ctr < results.length; ctr++){
					if (results[ctr].booleanValue())
						numSuccessfulStores++;
				}
			}
			public void receiveException(Exception result){
				this.received_response = true;
				this.success = false;
				System.out.println("Error storing "+storageObj);
				result.printStackTrace();
			}
		};
		p.insert(storageObj, c);	
		while (!c.receivedResponse()){
			env.getTimeSource().sleep(50);
		}
		if (c.wasSuccessful()) {
			//BlueCrabIndexingPersistentStorage storage_reference = (BlueCrabIndexingPersistentStorage)p.getStorageManager().getStorage();
			//storage_reference.indexDocuments();
			return storageObj.getId();
		} else {
			return null;
		}
	}

	
	public Id setFromFile(String path) throws Exception {
		final StorageObject storageObj = StorageObjectFactory.fromFile(this.local_factory, path);
		return this.set(storageObj);
	}
	
	public Id setFromString(final String val) throws Exception {
		final StorageObject storageObj = new StorageObject(this.local_factory.buildId(val), val);
		return this.set(storageObj);
	}
	
	//@Deprecated
	/*
	public HashMap<Id, String> search(int node, String query) throws IndexOutOfBoundsException, IOException, ParseException{
		if (node >= 0 && node < number_of_nodes) {
			PastImpl p = (PastImpl)this.nodes.get(node);
			BlueCrabIndexingPersistentStorage storage_reference = (BlueCrabIndexingPersistentStorage)p.getStorageManager().getStorage();
			return storage_reference.search(query);
		} else {
			throw new IndexOutOfBoundsException(); 
		}
	}
	*/
	
	public HashMap<Id, String> search(String query, int wait_time) throws NoSuchAlgorithmException, InterruptedException {
		BlueCrabSearcher searcher = this.search_nodes.get(env.getRandomSource().nextInt(number_of_nodes));
		System.out.println("Searching...");
		//System.out.println("SEARCHED TO INITIATE REQUEST: "+searcher.hashCode());
		String key = searcher.globalSearch(query);
		//System.out.println("INITIAL REQUEST QUERY: "+key.toString());
		env.getTimeSource().sleep(wait_time);
		ArrayList<BlueCrabSearchResult> results = searcher.getSearchResults(key);
		HashMap<Id, String>output = new HashMap<Id, String>();
		for (BlueCrabSearchResult r : results) {
			if ((r != null) && !(output.containsKey(r.id))) {
				output.put(r.id, r.digest);
			}
		}
		return output;
	}
	
	public String get(final Id key) throws Exception {		
		Past p = (Past)this.nodes.get(env.getRandomSource().nextInt(number_of_nodes));
		
		BlueCrabContinuation<PastContent, Exception> c = new BlueCrabContinuation<PastContent, Exception>(){
			public void receiveResult(PastContent result){
				this.received_response = true;
				this.success = true;
				this.result = result;
			}
			public void receiveException(Exception result){
				System.out.println("Error looking up. "+key);
				this.received_response = true;
				this.success = false;
				result.printStackTrace();
			}
		};
		p.lookup(key, c);
		while (!c.receivedResponse()){
			env.getTimeSource().sleep(50);
		}
		if (c.wasSuccessful()) {
			return ((StorageObject)(c.getResult())).getContent();
		} else {
			return null;
		}
	}
	
	public static void main(String[] args) throws Exception {
		//TESTING PARAMETERS
		int replicas = 4;
		String storage_directory = "/home/charles/bluecrab/";
		int test_node_count = 10;
		int test_port = 9001;
		try {
			BlueCrab crab = new BlueCrab(replicas, test_port, null, test_node_count, storage_directory);
			Repl repl = new Repl(crab);
			repl.start();
			System.exit(0);
		} catch (Exception e) {
			System.err.println("Error occured in BlueCrab constructor: "+e.getMessage());
			System.exit(1);
		}
	}
}
