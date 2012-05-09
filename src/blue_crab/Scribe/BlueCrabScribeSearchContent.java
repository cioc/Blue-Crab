package blue_crab.Scribe;

import rice.p2p.commonapi.NodeHandle;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Calendar;

import org.bouncycastle.util.encoders.Base64;


public class BlueCrabScribeSearchContent extends BlueCrabScribeContent{
	private String query;	
	/*
	 * For constructing a search query message
	 */
	public BlueCrabScribeSearchContent(NodeHandle from, String query, String key){
		this.from = from;
		this.type = BlueCrabMessageType.SEARCH;
		this.query = query;
		this.search_key = key;
	}
	
	public static String genKey(String query) throws NoSuchAlgorithmException {
		MessageDigest md = MessageDigest.getInstance("SHA-1"); 
		Calendar cal_for_time = Calendar.getInstance();
		return new String(Base64.encode(md.digest((query+cal_for_time.getTimeInMillis()).getBytes())));
	}
	
	public String query(){
		return this.query;
	}
	
	public String toString(){
		return "Blue Crab Scribe Content: "+this.search_key.toString()+" | query: "+this.query;
	}
}
