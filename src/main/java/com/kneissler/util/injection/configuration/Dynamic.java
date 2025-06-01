package com.kneissler.util.injection.configuration;

import java.util.concurrent.atomic.AtomicReference;

/**
 * Generic class to be used for fields that shall be changed dynamically during runtime (due to changes in configuration). instead of read from 
 * configuration at creation time of an object.
 * An AtomicReference object will be created for each field of this class. Due to configuration changes, the value that the reference is set to may change
 * at unspecified times. The current value can be retrieved by calling the method {@link Dynamic.get()}. 
 * Two subsequent calls of get() may return different value objects. If blocks of code expect that the value does not change, you should replace multiple 
 * calls of get() by just one in the beginning and store the return value in a local variable.
 * 
 * @param <V> value type (class of the value to be dynamically configured)
 */
public final class Dynamic<V> {
	
	private final AtomicReference<V> reference; 
	
	Dynamic() {
		reference = new AtomicReference<>();
	}
	
	public V get() {
		return reference.get();
	}
	
	void set(V value) {
		reference.set(value);
	}
	
}
