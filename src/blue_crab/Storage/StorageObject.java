package blue_crab.Storage;

import rice.p2p.commonapi.Id;
import rice.p2p.past.ContentHashPastContent;

public class StorageObject extends ContentHashPastContent{
	private String content;
	private String path;
	public StorageObject(Id id, String content) {
		super(id);
		this.content = content;
		this.path = null;
	}
	public StorageObject(Id id, String content, String path) {
		super(id);
		this.content = content;
		this.path = path;
	}
	public String getContent(){
		return this.content;
	}
	public String toString(){
		if (path == null) {
			return content;
		} else {
			return path;
		}
	}
	
}
