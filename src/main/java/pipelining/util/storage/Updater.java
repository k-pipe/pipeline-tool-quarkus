package pipelining.util.storage;

@FunctionalInterface
public interface Updater<T> {
	public void update(T toBeUpdated);
}
