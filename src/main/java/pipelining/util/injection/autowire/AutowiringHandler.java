package pipelining.util.injection.autowire;

import pipelining.util.injection.jar.DynamicJarSet;
import pipelining.util.injection.jar.JarNode;
import pipelining.logging.Log;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class AutowiringHandler {

	private static final SingletonMap predefinedSingletonMap = new SingletonMap(); 
	private static final AtomicReference<SingletonMap> currentSingletonMap = new AtomicReference<>(new SingletonMap()); 

	static void autowire(isAutowired object) {
		ClassLoader classLoader = object.getClass().getClassLoader();
		if (classLoader instanceof AutowiringJarClassLoader) {
			try (AutowiringJarClassLoader awcl = (AutowiringJarClassLoader)classLoader) {
				// use singleton map class loader, fall back to predefined singletons
				autowire(awcl.getJarNode(), object, awcl.getSingletonMap(), predefinedSingletonMap);
			} catch (IOException e) {
				throw new AutowiringException("could not close class loader", e);
			}
		} else {
			// not loaded by a JarClassLoader, use only predefined singletons
			autowire(JarNode.PREDEFINED_CLASSES_NODE, object, currentSingletonMap.get(), predefinedSingletonMap);
		}
	}

	static void checkConsistency(Class<?> theClass) {
		boolean hasAutowiredAnnotations = false;
		Field[] fields;
		try {
			fields = theClass.getDeclaredFields();
		} catch (NoClassDefFoundError e) {
			Log.warn("Could not load some of the field classes for "+theClass+": "+e);
			fields = new Field[0];
		}
		for (Field field : fields) {
			if (field.isAnnotationPresent(Autowire.class)) {
				hasAutowiredAnnotations = true;
				if (Modifier.isFinal(field.getModifiers())) {
					throw new AutowiringException("Field "+field.getName()+" of singleton class "+theClass.getName()+" is not final.");
				}
			} else {
				if (Singleton.class.isAssignableFrom(field.getType())) {
					throw new AutowiringException("Field "+field.getName()+" of singleton class "+theClass.getName()+" is missing the autowired annotation.");
				}
			}
		}
		if (hasAutowiredAnnotations && !isAutowired.class.isAssignableFrom(theClass)) {
			if (Singleton.class.isAssignableFrom(theClass)) {
				throw new AutowiringException("Singleton Class c"+theClass.getName()+" has fields with @Autowire annotation, but is not Autowirable, you should let this class extend AutowiredSingleton instead!");							
			} else {
				throw new AutowiringException("Class "+theClass.getName()+" has fields with @Autowire annotation, but is not Autowirable, you should let this class extend Autowired instead!");							
			}
		}
	}

	private static void autowire(JarNode jarNode, isAutowired object, SingletonMap... singletonMapSequence) {
		Class<? extends isAutowired> theClass = object.getClass();
		for (Field field : theClass.getDeclaredFields()) {
			if (field.isAnnotationPresent(Autowire.class)) {
				Singleton singleton = expectUniqueSingleton(jarNode, field.getType(), singletonMapSequence);
				field.setAccessible(true);
				try {
					field.set(object, singleton);
				} catch (IllegalArgumentException | IllegalAccessException e) {
					throw new AutowiringException("Could not set field value ", e);
				}
			}
		}
	}

	@SafeVarargs
	public static void initializePredefinedSingletons(Class<? extends Singleton>... singletonClasses) {
		predefinedSingletonMap.clear();
		for (Class<? extends Singleton> cl : singletonClasses) {
			predefinedSingletonMap.update(JarNode.PREDEFINED_CLASSES_NODE, cl);
		}
	}

	public static List<Singleton> getAssinableSingletons(JarNode jarNode, Class<?> targetClass, SingletonMap... singletonMapSequence) {
		List<Singleton> candidates = new ArrayList<>();
		for (SingletonMap singletonMap : singletonMapSequence) {
			candidates.addAll(singletonMap.getSingletonCandidates(jarNode, targetClass));
		}
		return candidates;
	}

	public static Singleton expectUniqueSingleton(JarNode jarNode, Class<?> targetClass, SingletonMap... singletonMapSequence) {
		List<Singleton> candidates = getAssinableSingletons(jarNode, targetClass, singletonMapSequence);
		if (candidates.isEmpty()) 
			throw new AutowiringException("No singleton candidate for type "+targetClass);
		if (candidates.size() > 1) 
			throw new AutowiringException("Multiple singleton candidates for type "+targetClass+": "+candidates);
		return candidates.get(0);
	}

	public static Singleton expectUniquePredefinedSingleton(JarNode jarNode, Class<?> targetClass) {
		return expectUniqueSingleton(jarNode, targetClass, currentSingletonMap.get(), predefinedSingletonMap);
	}

	public static void setActiveJarSet(DynamicJarSet jarSet) {		
		currentSingletonMap.set(jarSet.getSingletonMap());
	}

	@SafeVarargs
	public static void prefinedSingletons(JarNode jarNode, Class<? extends Singleton>... singletonClasses) {		
		for (Class<?> singletonClass : singletonClasses) {
			predefinedSingletonMap.update(jarNode, singletonClass);
		}
		predefinedSingletonMap.createSingletonInstances();
	}	

}
