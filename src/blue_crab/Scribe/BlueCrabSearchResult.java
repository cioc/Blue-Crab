package blue_crab.Scribe;

import rice.p2p.commonapi.Id;

public class BlueCrabSearchResult {
	public Id id;
	public double score;
	public String digest;
	
	public BlueCrabSearchResult(Id id, double score, String digest){
		this.id = id;
		this.score = score;
		this.digest = digest;
	}
}
