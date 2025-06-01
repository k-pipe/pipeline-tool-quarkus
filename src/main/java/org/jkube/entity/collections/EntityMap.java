package org.jkube.entity.collections;

import org.jkube.entity.Entity;

import java.util.Optional;
import java.util.function.BiConsumer;

public interface EntityMap<K extends Entity, V extends Entity> extends LinkedIterable<Entry<K, V>> {

	boolean containsKey(K key);

	Optional<V> get(K key);
	
	EntitySet<K> keys();
	
	LinkedIterable<V> values();
	
	void forEach(BiConsumer<K, V> consumer);
}
