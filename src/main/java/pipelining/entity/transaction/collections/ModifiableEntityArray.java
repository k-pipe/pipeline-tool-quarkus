package pipelining.entity.transaction.collections;

import pipelining.entity.Entity;
import pipelining.entity.collections.EntityArray;

import java.util.Optional;

public interface ModifiableEntityArray<E extends Entity> extends EntityArray<E> {

	void set(int index, E e);

	void addLast(E e);

	Optional<E> removeLast();
	
	void removeAll();

	void expectElementAt(int index, Entity element);

	void expectEmpty();
}
