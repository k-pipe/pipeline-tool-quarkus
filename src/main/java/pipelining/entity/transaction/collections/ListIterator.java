package pipelining.entity.transaction.collections;

import pipelining.entity.Entity;

import java.util.Iterator;

public interface ListIterator<E extends Entity> extends Iterator<E> {

	@Override
	void remove();
	
	void insertBefore(E inserted);

	void insertAfter(E inserted);
}
