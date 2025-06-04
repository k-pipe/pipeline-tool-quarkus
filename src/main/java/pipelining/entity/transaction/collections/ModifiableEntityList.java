package pipelining.entity.transaction.collections;

import pipelining.entity.Entity;
import pipelining.entity.collections.EntityList;

import java.util.Optional;

public interface ModifiableEntityList<E extends Entity> extends EntityList<E> {
	
	void addFirst(E e);

	void addLast(E e);

	Optional<E> removeFirst();

	Optional<E> removeLast();
	
	void removeAll();
	
	@Override
	ListIterator<E> iterator();

	void expectFirst(E first);

	void expectLast(E first);

	void expectEmpty();
}
