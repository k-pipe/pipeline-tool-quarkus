package pipelining.entity.transaction.collections;

import pipelining.entity.Entity;
import pipelining.entity.collections.EntitySet;

public interface ModifyableEntitySet<E extends Entity> extends EntitySet<E> {
		
	void removeAll();

	void expectPresent(E element);

	void expectNotPresent(E element);

	void expectEmpty();
}
