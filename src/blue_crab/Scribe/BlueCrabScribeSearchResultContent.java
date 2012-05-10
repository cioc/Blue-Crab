package blue_crab.Scribe;

import rice.p2p.commonapi.Message;
import rice.p2p.commonapi.NodeHandle;
import rice.p2p.commonapi.Id;
import rice.p2p.commonapi.rawserialization.OutputBuffer;
import rice.p2p.commonapi.rawserialization.RawMessage;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;


public class BlueCrabScribeSearchResultContent extends BlueCrabScribeContent{
	private BlueCrabSearchResult[] _results;
	private static final short TYPE = 1;

	public BlueCrabScribeSearchResultContent(NodeHandle from, String search_key){
		this.from = from;
		this.type = BlueCrabMessageType.RESULT_RESPONSE;
		this.search_key = search_key;
	}
	
	public void setResults(ArrayList<BlueCrabSearchResult> results) {
		this._results = new BlueCrabSearchResult[results.toArray().length];
		int index = 0;
		for (BlueCrabSearchResult r: results) {
			if (r != null) {
				this._results[index] = r;
			}
		}
	}
	
	public ArrayList<BlueCrabSearchResult> getResults(){
		ArrayList<BlueCrabSearchResult> output = new ArrayList<BlueCrabSearchResult>();
		int l = this._results.length;
		for (int i = 0; i < l; ++i) {
			output.add(this._results[i]);
		}
		return output;
	}
	
	public String toString(){
		return "Blue Crab Scribe Search Result Content: "+this.search_key.toString()+" from "+from;
	}
}
