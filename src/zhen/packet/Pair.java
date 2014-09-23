package zhen.packet;

public class Pair<T,K> {
	public Pair(T first, K second){
		this.first= first; this.second = second;
	}
	
	public T first;
	public K second;
}
