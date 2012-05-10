package blue_crab.Search;

import blue_crab.Storage.BlueCrabIndexingPersistentStorage;
import blue_crab.Scribe.BlueCrabMessagingClient;
import blue_crab.Scribe.BlueCrabScribeSearchContent;
import blue_crab.Scribe.BlueCrabSearchResult;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.lucene.queryParser.ParseException;

import rice.p2p.commonapi.Id;
import rice.p2p.commonapi.Node;
import rice.pastry.PastryNode;

public class BlueCrabSearcher {
	private BlueCrabIndexingPersistentStorage storage;
	private BlueCrabMessagingClient messenger;
	private HashMap<String, ArrayList<BlueCrabSearchResult>> outstandingSearches;
	
	public BlueCrabSearcher(PastryNode node, BlueCrabIndexingPersistentStorage storage) {
		this.storage = storage;
		this.outstandingSearches = new HashMap<String, ArrayList<BlueCrabSearchResult>>();
		this.messenger = new BlueCrabMessagingClient(node, this);
	}
	
	public void subscribe() {
		this.messenger.subscribe();
	}
	
	public ArrayList<BlueCrabSearchResult> localSearch(String query) throws ParseException, IOException {
		HashMap<Id, String> results = storage.search(query);
		ArrayList<BlueCrabSearchResult> output = new ArrayList<BlueCrabSearchResult>();
		for (Id id : results.keySet()) {
			output.add(new BlueCrabSearchResult(id, 0.0, results.get(id)));
		}	
		return output;
	}
	
	public String globalSearch(String query) throws NoSuchAlgorithmException {
		String key = BlueCrabScribeSearchContent.genKey(query);
		this.outstandingSearches.put(key, new ArrayList<BlueCrabSearchResult>());	
		this.messenger.sendSearchRequest(query, key);
		return key;
	}
	
	public boolean addSearchResults(String key, ArrayList<BlueCrabSearchResult> results) {
		/*
		System.out.println("IN ADD RESULTS!!!");
		for (BlueCrabSearchResult r : results) {
			System.out.println(this.hashCode()+" : addSearchResults: "+r.id+" | "+r.digest+ " with key: "+key);
		}
		*/
		if (this.outstandingSearches.containsKey(key)) {
			//System.out.println("WE HAVE FOUND THE KEY CONTAINING NODE");
			ArrayList<BlueCrabSearchResult> arrT = this.outstandingSearches.get(key);
			arrT.addAll(results);
			this.outstandingSearches.put(key, arrT);
			return true;
		} else {
			return false;
		}
	}
	
	public void removeSearchKey(byte[] key) {
		if (this.outstandingSearches.containsKey(key)) {
			this.outstandingSearches.remove(key);
		}
	}
	
	public ArrayList<BlueCrabSearchResult> getSearchResults(String search_key) {
		if (this.outstandingSearches.containsKey(search_key)) {
			return this.outstandingSearches.get(search_key);
		} else {
			return null;
		}
	}
}
