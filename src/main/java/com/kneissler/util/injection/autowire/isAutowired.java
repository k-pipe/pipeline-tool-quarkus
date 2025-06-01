package com.kneissler.util.injection.autowire;

/**
 *  Interface that classes implement in order to become automatically wired. In most cases, however, the autowired class should 
 *  be derived from the base class {@link Autowired} instead of adding this interface to the list of implemented interfaces.
 *   
 *  If for whatever reason, you are forced and implement this interface instead of extending {@link Autowired} , do not forget to add a call 
 *  to {@code super.autowire()} in every constructor.  
 */
public interface isAutowired {

	default void autowire() {
		AutowiringHandler.autowire(this);
	}
	
}
