package org.jkube.entity.transaction.collections;

import org.jkube.entity.Entity;
import org.jkube.entity.collections.EntitySet;

public interface ModifyableEntitySet<E extends Entity> extends EntitySet<E> {
		
	void removeAll();

	void expectPresent(E element);

	void expectNotPresent(E element);

	void expectEmpty();
}
