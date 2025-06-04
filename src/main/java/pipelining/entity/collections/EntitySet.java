package pipelining.entity.collections;

import pipelining.entity.Entity;

public interface EntitySet<E extends Entity> extends LinkedIterable<E> {
	
	boolean contains(E entity);
	
}
