package pipelining.util.injection.autowire;

import java.lang.reflect.InvocationTargetException;

public class Singleton {
	
	protected Singleton() {
		if (!calledFromCreate()) {
			throw new AutowiringException("Singletons cannot be constructed by calling a constructor");
		}
	}

	private boolean calledFromCreate() {
		for (StackTraceElement ste : Thread.currentThread().getStackTrace()) {
			if (ste.getClassName().equals(Singleton.class.getName()) && ste.getMethodName().equals("createSingleton")) {
				return true;
			}
		}
		return false;
	}

	static <S> S createSingleton(Class<S> singletonClass) {
		try {
			return singletonClass.getDeclaredConstructor().newInstance();
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
			throw new AutowiringException("Could not instantiate singleton "+singletonClass, e);
		}
	}

}
