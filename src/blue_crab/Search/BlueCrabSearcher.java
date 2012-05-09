package blue_crab.Search;

import blue_crab.Storage.BlueCrabIndexingPersistentStorage;
import blue_crab.Scribe.BlueCrabMessagingClient;
import blue_crab.Scribe.BlueCrabSearchResult;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.lucene.queryParser.ParseException;

import rice.p2p.commonapi.Id;
import rice.p2p.commonapi.Node;

public class BlueCrabSearcher {
	private BlueCrabIndexingPersistentStorage storage;
	private BlueCrabMessagingClient messenger;
	private HashMap<byte[], ArrayList<BlueCrabSearchResult>> outstandingSearches;
	
	public BlueCrabSearcher(Node node, BlueCrabIndexingPersistentStorage storage) {
		this.storage = storage;
		this.outstandingSearches = new HashMap<byte[], ArrayList<BlueCrabSearchResult>>();
		this.messenger = new BlueCrabMessagingClient(node, this);
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
	
	public byte[] globalSearch(String query) throws NoSuchAlgorithmException {
		byte[] key = messenger.sendSearchRequest(query);
		this.outstandingSearches.put(key, new ArrayList<BlueCrabSearchResult>());
		return key;
	}
	
	public boolean addSearchResults(byte[] key, ArrayList<BlueCrabSearchResult> results) {
		if (this.outstandingSearches.containsKey(key)) {
			this.outstandingSearches.get(key).addAll(results);
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
	
	public ArrayList<BlueCrabSearchResult> getSearchResults(byte[] search_key) {
		if (this.outstandingSearches.containsKey(search_key)) {
			return this.outstandingSearches.get(search_key);
		} else {
			return null;
		}
	}
}
