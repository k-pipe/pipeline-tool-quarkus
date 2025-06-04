package pipelining.entity.collections;

import pipelining.entity.Entity;

import java.util.Optional;

public interface EntityArray<E extends Entity> extends LinkedIterable<E> {
	Optional<E> get(int index);
}
