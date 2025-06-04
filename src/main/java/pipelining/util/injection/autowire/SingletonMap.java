package pipelining.util.injection.autowire;

import pipelining.util.injection.jar.JarNode;

import java.util.*;
import java.util.stream.Collectors;

public class SingletonMap {

	private final Map<Class<?>, Singleton> instanceMap;  // map from singleton class to singleton object 
	private final Map<JarNode, Map<Class<?>, List<Class<?>>>> injectableMap;  // map from jarNode and type (class/interface) to list of all singletons that may be injected to a field of given type in given jar node 

	public SingletonMap() {
		instanceMap = new HashMap<>();
		injectableMap = new HashMap<>();
	}

	/**
	 *  Must be called when a singleton class is loaded.
	 *  
	 * @param jarNode the jar node for which the class was loaded
	 * @param loadedClass the class that was loaded
	 */
	void update(JarNode jarNode, Class<?> loadedClass) {
		if (Singleton.class.isAssignableFrom(loadedClass)) {
			if (instanceMap.containsKey(loadedClass)) {
				throw new AutowiringException("tried to put same class twice into singleton map");
			}
			instanceMap.put(loadedClass, null); // create a map entry already, but instances are created later after all classes have been loaded
			for (Class<?> ancestorClass : ancestorClasses(loadedClass)) {
				for (JarNode injectedNode : jarNode.getInjectedNodes()) {
					addToInjectableMap(injectedNode, ancestorClass, loadedClass);
				}
			}
		}
	}

	/**
	 *  Must be called after all class have been loaded to create an new instance per loaded class.
	 */
	public void createSingletonInstances() {
		for (Class<?> singletonClass : instanceMap.keySet()) {
			getOrCreate(singletonClass);
		}
	}

	private Singleton getOrCreate(Class<?> singletonClass) {		
		Singleton res = instanceMap.get(singletonClass);
		if (res == null) { 
			res = (Singleton) Singleton.createSingleton(singletonClass);
			instanceMap.put(singletonClass, res);
		}
		return res;
	}

	private void addToInjectableMap(JarNode injectedNode, Class<?> superClass, Class<?> singletonClass) {
		Map<Class<?>, List<Class<?>>> classMap = injectableMap.get(injectedNode);
		if (classMap == null) {
			classMap = new HashMap<>();
			injectableMap.put(injectedNode, classMap);
		}
		List<Class<?>> injectableList = classMap.get(superClass);
		if (injectableList == null) {
			injectableList = new ArrayList<>();
			classMap.put(superClass, injectableList);
		}
		injectableList.add(singletonClass);
	}

	private Set<Class<?>> ancestorClasses(Class<?> loadedClass) {
		Set<Class<?>> res = new HashSet<>();
		addWithAncestorClasses(res, loadedClass);
		return res;
	}

	private void addWithAncestorClasses(Set<Class<?>> res, Class<?> aClass) {
		if ((aClass != null) && res.add(aClass)) {
			addWithAncestorClasses(res, aClass.getSuperclass());
			for (Class<?> interf : aClass.getInterfaces()) {
				addWithAncestorClasses(res, interf);				
			}
		}
	}

	/**
	 *  Get singleton for specified singleton class. 
	 *  
	 * @param singletonClass the singleton class for which the corresponding instance is requested
	 * @return the singleton, null if class is not a singleton class
	 */
	Singleton getInstance(Class<?> singletonClass) {
		return instanceMap.get(singletonClass);
	}

	/**
	 *  Get list of singleton classes that subclass/implement a given class/interface 
	 *  
	 * @param jarNode the JarNode in which injection takes place (only singletons from injecting JarNodes are considered)
	 * @param aClass the class (or interface) for which all assignable singleton instances shall be found
	 * @param targetClass the class (or interface) for which all assignable singleton instances shall be found
	 * @return list of singleton classes that the given class may be assigned from, if none exist, returns empty list
	 */
	List<Class<?>> getSingletonClassCandidates(JarNode injectedNode, Class<?> targetClass) {
		Map<Class<?>, List<Class<?>>> classMap = injectableMap.get(injectedNode);
		List<Class<?>> res = classMap == null ? null : classMap.get(targetClass);
		return res == null ? Collections.emptyList() : res;
	}

	/**
	 *  Get list of singletons that subclass/implement a given class/interface 
	 *  
	 * @param aClass the class (or interface) for which all assignable singleton instances shall be found
	 * @return list of singletons that the given class may be assigned from, if none exist, returns empty list
	 */
	List<Singleton> getSingletonCandidates(JarNode injectedNode, Class<?> targetClass) {
		return getSingletonClassCandidates(injectedNode, targetClass).stream().map(sc -> getOrCreate(sc)).collect(Collectors.toList());
	}
	
	public String toString() {
		return instanceMap.keySet().stream().map(i -> i.getSimpleName()).collect(Collectors.joining(","));
	}

	public void clear() {
		instanceMap.clear();
		injectableMap.clear();
	}

}
