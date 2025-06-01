package org.jkube.entity.transaction.collections;

import org.jkube.entity.Entity;

import java.util.Iterator;

public interface ListIterator<E extends Entity> extends Iterator<E> {

	@Override
	void remove();
	
	void insertBefore(E inserted);

	void insertAfter(E inserted);
}
