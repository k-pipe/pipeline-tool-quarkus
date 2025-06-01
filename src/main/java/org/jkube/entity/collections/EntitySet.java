package org.jkube.entity.collections;

import org.jkube.entity.Entity;

public interface EntitySet<E extends Entity> extends LinkedIterable<E> {
	
	boolean contains(E entity);
	
}
