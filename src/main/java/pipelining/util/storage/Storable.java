package pipelining.util.storage;

public interface Storable<D> {
	
	public String locationPath();
	
	public Class<D> dataClass();
	
}
