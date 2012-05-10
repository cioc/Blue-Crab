package blue_crab.Scribe;

import java.io.Serializable;

import rice.p2p.commonapi.Id;

public class BlueCrabSearchResult implements Serializable {
	private static final long serialVersionUID = 456L;
	public Id id;
	public double score;
	public String digest;
	
	public BlueCrabSearchResult(Id id, double score, String digest){
		this.id = id;
		this.score = score;
		this.digest = digest;
	}
}
