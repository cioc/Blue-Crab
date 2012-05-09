package blue_crab.Scribe;

import rice.p2p.commonapi.NodeHandle;
import rice.p2p.commonapi.Id;

import java.util.ArrayList;


public class BlueCrabScribeSearchResultContent extends BlueCrabScribeContent{
	private ArrayList<BlueCrabSearchResult> results;

	public BlueCrabScribeSearchResultContent(NodeHandle from, String search_key){
		this.from = from;
		this.type = BlueCrabMessageType.RESULT_RESPONSE;
		this.search_key = search_key;
		this.results = new ArrayList<BlueCrabSearchResult>();
	}
	
	public void addResult(Id id, double score, String digest){
		this.results.add(new BlueCrabSearchResult(id, score, digest));
	}
	
	public void setResults(ArrayList<BlueCrabSearchResult> results) {
		this.results = results;
	}
	
	public ArrayList<BlueCrabSearchResult> getResults(){
		return this.results;
	}
	
	public String toString(){
		return "Blue Crab Scribe Search Result Content: "+this.search_key.toString()+" from "+from;
	}
}
