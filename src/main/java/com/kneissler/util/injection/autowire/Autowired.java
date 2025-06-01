package com.kneissler.util.injection.autowire;

/**
 * Base classes from which classes with automatic wiring functionality should be derived.
 */
public class Autowired implements isAutowired {

	protected Autowired() {
		autowire();
	}
	
}
