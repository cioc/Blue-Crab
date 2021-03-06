package blue_crab.Scribe;

/*
 * The starting point of this class is MyScribeContent.java
 * https://svn.mpi-sb.mpg.de/trac/DS/freepastry/browser/trunk/pastry/src/rice/tutorial/scribe/MyScribeContent.java
 */

import rice.p2p.commonapi.NodeHandle;
import rice.p2p.scribe.ScribeContent;

public abstract class BlueCrabScribeContent implements ScribeContent{
	protected BlueCrabMessageType type;
	protected NodeHandle from;
	protected String search_key;
	
	public BlueCrabMessageType getType(){
		return this.type;
	}
	
	public NodeHandle from() {
		return this.from;
	}
	
	public String search_key() {
		return this.search_key;
	}
}
