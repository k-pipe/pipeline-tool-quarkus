package pipelining.entity.transaction.collections;

import pipelining.entity.Entity;
import pipelining.entity.collections.EntityMap;

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
