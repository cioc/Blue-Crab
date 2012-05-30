package blue_crab.Scribe;


import rice.p2p.commonapi.Id;
import rice.p2p.commonapi.NodeHandle;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Calendar;

import org.bouncycastle.util.encoders.Base64;


public class BlueCrabFileRequestContent extends BlueCrabScribeContent{
	/*
	 * For constructing as file request
	 */
	
	private static final long serialVersionUID = 8922L;
	private Id file_request;

	public BlueCrabFileRequestContent(NodeHandle from, Id file_request){
		this.from = from;
		this.type = BlueCrabMessageType.FILE_REQUEST;
		this.file_request = file_request;
	}
	
	public Id getRequest(){
		return this.file_request;
	}
	
	public String toString(){
		return "Blue Crab File Request Content: "+this.search_key.toString()+" | : "+this.file_request.toStringFull();
	}
}
