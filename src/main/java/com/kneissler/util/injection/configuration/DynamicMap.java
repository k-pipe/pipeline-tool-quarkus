package com.kneissler.util.injection.configuration;

import com.kneissler.util.injection.autowire.Autowire;
import com.kneissler.util.injection.autowire.AutowiringException;
import com.kneissler.util.injection.autowire.Singleton;
import com.kneissler.util.injection.autowire.isAutowired;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

class DynamicMap {

	private final Map<String, Dynamic<?>> map;  // map from dynamic location (class name + field name) to dynamic object
	
	DynamicMap() {
		map = new ConcurrentHashMap<>();
	}
	
	/**
	 *  Called when a class is loaded (for the first time or re-loaded).
	 *  If the class implements interface {@link isAutowired}, it is scanned for fields of class {@link Autowire} 
	 *  and creates a new Autowire object. The new Autowire object is stored in the field of the class,
	 *  an the corresponding entry in the map is created or updated. 
	 */
	void update(Class<?> aClass) {
		if (aClass.isInstance(isAutowired.class)) {
			for (Field field : aClass.getDeclaredFields()) {
				if (field.getType().isInstance(Dynamic.class)) {
					map.put(createSpecifier(aClass,  field), new Dynamic<>());
				}
			}
		}
	}

	/**
	 *  Called when a object is created. All fields of type {@link Autowire} are set to the corresponding object in the map. 
	 */
	void autowire(isAutowired object) {
		Class<? extends isAutowired> theClass = object.getClass();
		for (Field field : theClass.getDeclaredFields()) {
			if (field.getType().isInstance(Dynamic.class)) {
				Dynamic<?> foundAutowire = map.getOrDefault(createSpecifier(theClass,  field), new Dynamic<Singleton>());
				field.setAccessible(true);
				try {
					field.set(object, foundAutowire);
				} catch (IllegalArgumentException | IllegalAccessException e) {
					throw new AutowiringException("Could not set field value ", e);
				}
			}
		}
	}

	private String createSpecifier(Class<?> aClass, Field field) {
		return aClass.getName()+"."+field.getName();
	}
	
}
