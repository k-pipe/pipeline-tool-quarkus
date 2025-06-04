package pipelining.util.storage;

import java.util.Optional;

/**
 * Interface that defines the basic storage access patterns, with optimistic locking
 *
 * @param <E> data entity to be stored
 * @param <I> identifier used to find stored entities (e.g. resource path)
 */
public interface Storage<E,I> { 	
	Optional<Storable<E>> get(I identifier);

	boolean create(Storable<E> storable); // fails if already exists
			
	void createOrOverwrite(Storable<E> storable); // overwrites if it exists already, irrespective of version
	
	boolean store(Storable<E> storable); // optimistic locking: fails if there is a version mismatch
	
	void update(Updater<Storable<E>> updater, I identifier); // retrieves, updates, stores, with retries if optimistically locked
}
