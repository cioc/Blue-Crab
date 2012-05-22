package blue_crab;

/*
 * http://groups.google.com/group/comp.lang.java.help/browse_thread/thread/f8b63fc645c1b487/1d94be050cfc249b
 */
public class Pair<V1,V2> {
	private final V1 first;
	private final V2 second;
	
	public Pair(V1 f, V2 s) {
		this.first = f;
		this.second = s;
	}
	
	public V1 getFirst() {
		return this.first;
	}
	
	public V2 getSecond() {
		return this.second;
	}
}
