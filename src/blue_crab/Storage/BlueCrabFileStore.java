package blue_crab.Storage;

//CODE ADAPTED FROM FREEPASTRY TUTORIAL

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;

import rice.Continuation;
import rice.p2p.commonapi.Node;

import org.mpisws.p2p.filetransfer.BBReceipt;
import org.mpisws.p2p.filetransfer.FileReceipt;
import org.mpisws.p2p.filetransfer.FileTransfer;
import org.mpisws.p2p.filetransfer.FileTransferCallback;
import org.mpisws.p2p.filetransfer.FileTransferImpl;
import org.mpisws.p2p.filetransfer.FileTransferListener;
import org.mpisws.p2p.filetransfer.Receipt;
import org.mpisws.p2p.transport.commonapi.IdFactory;

import rice.p2p.commonapi.Application;
import rice.p2p.commonapi.Endpoint;
import rice.p2p.commonapi.Id;
import rice.p2p.commonapi.Message;
import rice.p2p.commonapi.NodeHandle;
import rice.p2p.commonapi.RouteMessage;
import rice.p2p.commonapi.appsocket.*;
import rice.p2p.util.rawserialization.SimpleInputBuffer;
import rice.p2p.util.rawserialization.SimpleOutputBuffer;
import rice.pastry.commonapi.PastryIdFactory;

/*
 *  manages file store
 */
/*
 *  SEE THE FILE SENDING PASTRY TUTORIAL
 */
public class BlueCrabFileStore implements Application{
	private static final long serialVersionUID = 45738L;
	
	private final String storage_directory;
	
	protected Endpoint endpoint;
	protected Node node;
	protected FileTransfer fileTransfer;
	protected final BlueCrabIndexingPersistentStorage storage;
	
	public BlueCrabFileStore(final String directory, Node node, final IdFactory factory, final BlueCrabIndexingPersistentStorage storage) {
		this.storage_directory = directory;
		this.endpoint = node.buildEndpoint(this, "fileStoreInstance");
		this.node = node;
		this.storage = storage;
		
		endpoint.accept(new AppSocketReceiver() {
			public void receiveSocket(AppSocket socket) {
				fileTransfer = new FileTransferImpl(socket, new FileTransferCallback(){
					public void messageReceived(ByteBuffer bb) {
						//MESSAGE RECEIVED
						//WILL NOT BE USED
					}
					public void fileReceived(File f, ByteBuffer metadata) {
						try {
							String metastr = new SimpleInputBuffer(metadata).readUTF();
							String[] pieces = metastr.split("|");
							String id_str = "";
							String file_name_str = "";
							if (pieces.length > 0) {
								id_str = pieces[pieces.length - 1];
								for (int i = 0; i < (pieces.length - 1); ++i) {
									file_name_str += pieces[i];
								}
							} else {
								throw new IOException();
							}
							File dest = new File(storage_directory+"/"+id_str);
							f.renameTo(dest);
							Id id = ((PastryIdFactory)factory).buildId(id_str);
							storage.updateIndexByIdForFile(storage_directory+"/"+id_str, file_name_str, id);
						}
						catch (IOException e) {
							System.err.println("IOException in BlueCrabFileSTore.fileReceived");
						}
					}
					public void receiveException(Exception e) {
						//EXCEPTION
					}
				}, BlueCrabFileStore.this.node.getEnvironment());
				fileTransfer.addListener(new MyFileListener());
				endpoint.accept(this);
			}
			public void receiveSelectResult(AppSocket socket, boolean canRead, boolean canWrite) {
				//RUNTIME EXCEPTION?
			}
			public void receiveException(AppSocket socket, Exception e) {
				e.printStackTrace();
			}
		});
		endpoint.register();
	}
	
	//TODO - TRY TO REMOVE
	//WE MAY JUST KEEP THESE HERE FOR THE SAKE OF ERROR PREVENTION
	class MyFileListener implements FileTransferListener {
		public void fileTransferred(FileReceipt receipt, long bytesTransferred, long total, boolean incoming) {	
			//?
		}
		public void msgTransferred(BBReceipt receipt, int bytesTransferred, int total, boolean incoming) {
			//?
		}
		public void transferCancelled(Receipt receipt, boolean incoming) {
			//?
		}
		public void transferFailed(Receipt receipt, boolean incoming) {
			//?
		}
	}
		
	public void sendFileDirect(NodeHandle nh, final String filePath, final Id id) throws FileNotFoundException, IOException {
		endpoint.connect(nh, new AppSocketReceiver(){
			public void receiveSocket(AppSocket socket) throws FileNotFoundException, IOException {
				FileTransfer sender = new FileTransferImpl(socket, null, node.getEnvironment());
				
				sender.addListener(new MyFileListener());
				//BRING IN SOME FILE
				final File f = new File(filePath);
				if (!f.exists()) {
					throw new FileNotFoundException();
				}
				SimpleOutputBuffer sob = new SimpleOutputBuffer();
				sob.writeUTF(f.getName() + "|"+ id.toStringFull());
					
				sender.sendFile(f, sob.getByteBuffer(), (byte)2, new Continuation<FileReceipt, Exception>(){
					public void receiveException(Exception e) {
						///ERROR
					}
					public void receiveResult(FileReceipt result) {
						//SUCCESS
					}
				});
			}

			public void receiveException(AppSocket arg0, Exception arg1) {
				// TODO Auto-generated method stub
				
			}

			public void receiveSelectResult(AppSocket arg0, boolean arg1,
					boolean arg2) throws IOException {
				// TODO Auto-generated method stub
				
			}
		}, 30000);
	}
	
	public Node getNode() {
		return this.node;
	}
	
	public void deliver(Id id, Message message) {
		//MESSAGE RECEIVED
		//WE USE ANOTHER APPLICATION FOR MESSAGING - THIS IS FOR FILE TRANSFER
	}
	public void update(NodeHandle handle, boolean joined) {
		//NOTHING TO DO FOR NOW
	}
	public boolean forward(RouteMessage message) {
		return true;
	}
	public String toString() {
		return "BlueCrabFileStore: "+endpoint.getId();
	}
}
