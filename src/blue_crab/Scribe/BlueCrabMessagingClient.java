package blue_crab.Scribe;

/*
 * The starting point of this class is MyScribeClient.java
 * https://svn.mpi-sb.mpg.de/trac/DS/freepastry/browser/trunk/pastry/src/rice/tutorial/scribe/MyScribeClient.java
 * 
 * That tutorial used deprecrated methods though.  So, we brought things up to speed. 
 * 
 * Furthermore, this has been specialized for search.
 * 
 */

import java.io.IOException;
import java.io.NotSerializableException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collection;

import org.apache.lucene.queryParser.ParseException;

import rice.p2p.commonapi.*;
import rice.p2p.scribe.Scribe;
import rice.p2p.scribe.ScribeMultiClient;
import rice.p2p.scribe.ScribeContent;
import rice.p2p.scribe.ScribeImpl;
import rice.p2p.scribe.Topic;
import rice.p2p.util.rawserialization.JavaSerializationException;
import rice.pastry.commonapi.PastryIdFactory;

import blue_crab.Search.BlueCrabSearcher;

/*
 * TODO: NEED TO IMPLEMENT ALL INHERITED METHOD DEFINTIONS
 */
public class BlueCrabMessagingClient implements ScribeMultiClient, Application {	
	private Scribe scribeHandle;
	private Topic searchTopic;
	private Topic fileRequestTopic;
	protected Endpoint endpoint; 
	private BlueCrabSearcher localSearcher;
	
	public BlueCrabMessagingClient(Node node, BlueCrabSearcher localSearcher){
		this.endpoint = node.buildEndpoint(this, "myinstance");
		this.scribeHandle = new ScribeImpl(node, "scribeInstance");
		this.searchTopic = new Topic(new PastryIdFactory(node.getEnvironment()), "Blue Crab Search Network");
		this.fileRequestTopic = new Topic(new PastryIdFactory(node.getEnvironment()), "Blue Crab File Request Network");
		this.endpoint.register();
		this.localSearcher = localSearcher;
	}
	/*
	 * TODO: REMOVE DEPRECATED METHOD USAGE
	 */
	public void subscribe(){
		((ScribeImpl)scribeHandle).subscribe(searchTopic, this);
		((ScribeImpl)scribeHandle).subscribe(fileRequestTopic, this);
	}
	
	//MUTLICAST RECEIVED
	public void deliver(Topic topic, ScribeContent content) {
		if (((BlueCrabScribeContent)content).from == null) {
			new Exception("Stack trace").printStackTrace();
		}
		if (((BlueCrabScribeContent)content).getType() == BlueCrabMessageType.SEARCH) {
			//System.out.println(this.scribeHandle.hashCode()+"RECEIVED SEARCH REQUEST: "+((BlueCrabScribeSearchContent)content).query());
			//a search
			try {
				ArrayList<BlueCrabSearchResult>results = this.localSearcher.localSearch(((BlueCrabScribeSearchContent)content).query());
				this.sendResultResponseQuery(((BlueCrabScribeSearchContent)content).search_key(), results);
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		if (((BlueCrabScribeContent)content).getType() == BlueCrabMessageType.FILE_REQUEST) {
			//file request received
			System.out.println("Result Response incorrectly sent over multicast!");
			//TODO - IMPLEMENT
		} 
		if (((BlueCrabScribeContent)content).getType() == BlueCrabMessageType.RESULT_RESPONSE) {
			//a result response - strange
			System.out.println("Result Response incorrectly sent over multicast!");
		} else {
			new Exception("Non Standard message received");
		}
	}
		
	public void deliver(Id id, Message msg) {
		// TODO DECIDE TO IMPLEMENT OR NOT
	}
	
	//multicasts a search query out to the nodes
	//returns a search key
	public String sendSearchRequest(String query, String key) throws NoSuchAlgorithmException{
		BlueCrabScribeSearchContent msg = new BlueCrabScribeSearchContent(endpoint.getLocalNodeHandle(), query, key);
		scribeHandle.publish(searchTopic, msg);
		return msg.search_key();
	}
	
	public void sendResultResponseQuery(String search_key, ArrayList<BlueCrabSearchResult> results) {
		BlueCrabScribeSearchResultContent msg = new BlueCrabScribeSearchResultContent(endpoint.getLocalNodeHandle(), search_key);
		msg.setResults(results);
		scribeHandle.anycast(searchTopic, msg);
	}
	public void sendFileRequest(Id file_request) {
		BlueCrabFileRequestContent msg = new BlueCrabFileRequestContent(endpoint.getLocalNodeHandle(), file_request);
		scribeHandle.publish(this.fileRequestTopic, msg);
	}
	
	//ANYCAST RECIEVED
	public boolean anycast(Topic topic, ScribeContent content){
		if (content instanceof BlueCrabScribeSearchResultContent) {
			boolean output = this.localSearcher.addSearchResults(((BlueCrabScribeSearchResultContent)content).search_key(), ((BlueCrabScribeSearchResultContent)content).getResults());
			return output;
		}
		return true;
	}
	
	/*
	 * The mysteriously unimplemented methods
	 */
	public void childAdded(Topic topic, NodeHandle child){
		
	}
	public void childRemoved(Topic topic, NodeHandle child){
		
	}
	public void subscribeFailed(Topic topic){
		
	}
	public boolean forward(RouteMessage message){
		return true;
	}
	public void update(NodeHandle handle, boolean joined){
		
	}

	public void subscribeFailed(Collection<Topic> arg0) {
		// TODO Auto-generated method stub
		
	}

	public void subscribeSuccess(Collection<Topic> arg0) {
		// TODO Auto-generated method stub
		
	}
}