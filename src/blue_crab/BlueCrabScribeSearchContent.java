package blue_crab;

import rice.p2p.commonapi.NodeHandle;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Calendar;

public class BlueCrabScribeSearchContent extends BlueCrabScribeContent{
	private String query;
	private byte[] search_key;
	
	/*
	 * For constructing a search query message
	 */
	public BlueCrabScribeSearchContent(NodeHandle from, String query) throws NoSuchAlgorithmException{
		this.from = from;
		this.type = BlueCrabMessageType.SEARCH;
		this.query = query;
		MessageDigest md = MessageDigest.getInstance("SHA-1"); 
		Calendar cal_for_time = Calendar.getInstance();
		this.search_key = md.digest((query+cal_for_time.getTimeInMillis()).getBytes());
	}
	
	public String query(){
		return this.query;
	}
	
	public byte[] search_key() {
		return this.search_key;
	}
	
	public String toString(){
		return "Blue Crab Scribe Content: "+this.search_key.toString()+" | query: "+this.query;
	}
}
