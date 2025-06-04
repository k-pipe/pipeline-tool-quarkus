package pipelining.util.injection.autowire;

import pipelining.logging.Log;
import pipelining.util.injection.jar.*;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Map;

public class AutowiringJarClassLoader extends JarClassLoader {

	private final SingletonMap singletonMap;

	public AutowiringJarClassLoader(JarNode jarNode, JarStore jarStore, JarFile jarFile, MultiJarClassLoader parentClassLoader) {
		super(jarNode, jarStore, jarFile, parentClassLoader);
		singletonMap = parentClassLoader.getSingletonMap();
	}

	public SingletonMap getSingletonMap() {
		return singletonMap;
	}

	@Override
	public Class<?> findClass(String name) throws ClassNotFoundException { 
		Class<?> res = super.findClass(name);
		if (Singleton.class.isAssignableFrom(res)) {
			singletonMap.update(getJarNode(), res);
		}
		return res;
	}
	
	public void autowireSingletons() {
		loadedClassesAssignableTo(AutowiredSingleton.class).forEach(this::autowire);
	}

	private void autowire(Class<? extends AutowiredSingleton> cl) {
		((AutowiredSingleton)singletonMap.getInstance(cl)).autowire();
	}

	public void checkAnnotationConsistency() {
		loadedClasses().forEach(AutowiringHandler::checkConsistency);
	}

	public void resolveAllClasses() {
		loadedClasses().forEach(this::resolveClass);
	}

	public void configure(Map<String, Map<String, String>> configuration) {
		configuration.forEach(this::configure);
	}

	private void configure(String className, Map<String, String> conf) {
		Class<?> aClass = getLoadedClass(className);
		if (aClass == null) {
			Log.warn("Could not configure, no such class was loaded: "+className);
			return;
		}
		conf.forEach((fieldName, value) -> setField(aClass, fieldName, value));
	}

	private void setField(Class<?> aClass, String fieldName, String value) {
		Field field;
		try {
			field = aClass.getDeclaredField(fieldName);
		} catch (NoSuchFieldException | SecurityException e) {
			Log.warn("Problem getting field in class "+aClass.getName()+": "+fieldName, e);
			return;
		}
		if (!Modifier.isStatic(field.getModifiers())) {
			Log.warn("Could not configure, field is not static (class "+aClass.getName()+"): "+fieldName);
			return;			
		}
		field.setAccessible(true);
		try {
			field.set(null, value);
		} catch (IllegalArgumentException | IllegalAccessException e) {
			Log.warn("Problem setting field (class "+aClass.getName()+"): "+fieldName, e);
		}
	}

}
