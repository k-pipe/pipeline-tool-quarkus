package pipelining.entity.collections;

import pipelining.entity.Entity;

import java.util.Optional;

public interface EntityList<E extends Entity> extends LinkedIterable<E> {
	
	Optional<E> getFirst();
	
	Optional<E> getLast();
}
