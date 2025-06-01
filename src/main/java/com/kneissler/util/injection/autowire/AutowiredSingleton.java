package com.kneissler.util.injection.autowire;

/**
 * Base classes from which singletons with automatic wiring functionality should be derived.
 */
public class AutowiredSingleton extends Singleton implements isAutowired {

	protected AutowiredSingleton() {
		// AutowiredSingletons cannot be autowired in constructor (cyclic dependencies)
	}
	
}
