package com.kneissler.util.injection.autowire;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation that indicates that a field shall be automatically set a value during constructor call.
 * This annotation is valid only for fields of class Singleton or Configurable<Singleton>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Autowire {	
}
