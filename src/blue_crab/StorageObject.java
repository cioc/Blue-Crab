package blue_crab;

import rice.p2p.commonapi.Id;
import rice.p2p.past.ContentHashPastContent;

public class StorageObject extends ContentHashPastContent{
	private String content;
	/*
	 * TODO: BE ABLE TO CONVERT A FILE TO STRING FOR STORAGE
	 */
	public StorageObject(Id id, String content) {
		super(id);
		this.content = content;
	}
	public String toString(){
		return "StorageObject: "+content;
	}
	
}
