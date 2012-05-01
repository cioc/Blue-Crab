package blue_crab;

import java.io.IOException;
import java.net.*;
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
import rice.p2p.past.PastException;
import rice.persistence.*;

public class BlueCrab {
	private Vector<Past> nodes;
	private final Environment env;
	private NodeIdFactory node_id_factory;
	private PastryNodeFactory pastry_node_factory;
	private PastryIdFactory local_factory;
	private int number_of_nodes; 
	
	public BlueCrab(int replicas, int port, String hostname, int node_count, String storage_directory) throws Exception {
		env = new Environment();
		nodes = new Vector<Past>();
		node_id_factory = new RandomNodeIdFactory(env);
		pastry_node_factory = new SocketPastryNodeFactory(node_id_factory, port, env);
		local_factory = new rice.pastry.commonapi.PastryIdFactory(env);
		number_of_nodes = node_count;
		InetAddress bootaddr;
		/*
		 * START HERE 
		 */
		if (hostname == null) {
			//THIS IS A BIT OF A HACK TO START A NEW RING CORRECTLY
			/*
			 * MAKE THIS WORK ON WIRELESS CONNECTIONS
			 * MAKE THIS WORK ON MULTIPLE OPERATING SYSTEMS
			 */
			String addrString = "";
			for (Enumeration<NetworkInterface> net_interfaces = NetworkInterface.getNetworkInterfaces(); net_interfaces.hasMoreElements();) {
				NetworkInterface curr = net_interfaces.nextElement();
				System.out.println(curr.getName());
				int addr_count = 0;
				for (Enumeration<InetAddress> addresses = curr.getInetAddresses(); addresses.hasMoreElements();){
					InetAddress curr_addr = addresses.nextElement();
					if (curr.getName().equals("eth0") && addr_count == 2) {
						addrString = curr_addr.toString();
					}
					addr_count += 1;
				}
			}
			bootaddr = InetAddress.getByName(addrString.substring(1));
		} else {
			bootaddr = InetAddress.getByName(hostname);
		}
		InetSocketAddress bootaddress = new InetSocketAddress(bootaddr, port);
			
		for (int i = 0; i < node_count; ++i) {
			PastryNode node = this.pastry_node_factory.newNode();
			PastryIdFactory idf = new rice.pastry.commonapi.PastryIdFactory(env);
			String storageDirectory = storage_directory+node.getId().hashCode();
			Storage stor = new PersistentStorage(idf, storageDirectory, 4 * 1024 * 1024, node.getEnvironment());
			Past past = new PastImpl(node, new StorageManagerImpl(idf, stor, new LRUCache(new MemoryStorage(idf), 512 * 1024, node.getEnvironment())), replicas,"");
			nodes.add(past);
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
		//WAIT SO THAT WE KNOW GET GOT ALL OUR NODES STARTED
		env.getTimeSource().sleep(5000);
	}
	private Id set(final StorageObject storageObj) throws Exception{
		Past p = (Past)this.nodes.get(env.getRandomSource().nextInt(number_of_nodes));
		
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
	
	public String getSafe(final Id key) {
		try {
			return this.get(key);
		} 
		catch (Exception e) {
			System.err.println("Exception occured in getSafe: "+e.getMessage());
		}
		return null;
	}
	
	private String get(final Id key) throws Exception {		
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
			return c.getResult().toString();
		} else {
			return null;
		}
	}
	
	public static void main(String[] args) throws Exception {
		//TESTING PARAMETERS
		int replicas = 4;
		String storage_directory = "/home/charles/bluecrabstorage";
		int test_node_count = 10;
		int test_port = 9001;
		try {
			BlueCrab crab = new BlueCrab(replicas, test_port, null, test_node_count, storage_directory);
			Repl repl = new Repl(crab);
			repl.start();
			System.exit(0);
		} catch (Exception e) {
			System.err.println("Error occured in BlueCrab constructor: "+e.getMessage());
		}
	}
}
