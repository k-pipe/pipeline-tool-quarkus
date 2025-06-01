package org.jkube.entity.transaction.collections;

import org.jkube.entity.Entity;
import org.jkube.entity.collections.EntityArray;

import java.util.Optional;

public interface ModifiableEntityArray<E extends Entity> extends EntityArray<E> {

	void set(int index, E e);

	void addLast(E e);

	Optional<E> removeLast();
	
	void removeAll();

	void expectElementAt(int index, Entity element);

	void expectEmpty();
}
