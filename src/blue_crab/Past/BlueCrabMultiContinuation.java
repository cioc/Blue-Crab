package blue_crab.Past;

import rice.Continuation;
import rice.Continuation.MultiContinuation;
import rice.p2p.commonapi.NodeHandleSet;

public class BlueCrabMultiContinuation extends MultiContinuation {
	protected NodeHandleSet replicas;
	
	public BlueCrabMultiContinuation(Continuation arg0, int arg1) {
		super(arg0, arg1);
		// TODO Auto-generated constructor stub
	}
	public void setNodeHandleSet(NodeHandleSet r) {
		this.replicas = r;
	}
	public NodeHandleSet getNodeHandleSet() {
		return this.replicas;
	}
}
