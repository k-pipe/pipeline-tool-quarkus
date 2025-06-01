package org.jkube.entity.transaction.collections;

import org.jkube.entity.Entity;
import org.jkube.entity.collections.EntityMap;

import java.util.Optional;

public interface ModifiableEntityMap<K extends Entity, V extends Entity> extends EntityMap<K, V> {

	Optional<V> put(K key, V value);

	Optional<V> remove(K key);
	
	void removeAll();

	void expectKeyPresent(K key);

	void expectKeyNotPresent(K key);

	void expectValue(K key, V value);

	void expectEmpty();
}
