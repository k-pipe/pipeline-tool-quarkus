package org.jkube.entity.collections;

public interface Entry<K, V> {
	K getKey();
	V getValue();
}
