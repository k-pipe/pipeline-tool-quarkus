package pipelining.util.injection.jar;

import java.io.FileNotFoundException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public class JarClassLoader extends URLClassLoader {

	private final JarNode jarNode;
	private final JarFile jarFile;
	private final URL jarURL;
	private final Map<String, Class<?>> loadedClasses;

	private JarClassLoader(JarNode jarNode, JarFile jarFile, URL jarUrl, ClassLoader parentClassLoader) {
		super(new URL[] { jarUrl }, parentClassLoader);
		this.jarNode = jarNode;
		this.jarFile = jarFile;
		this.jarURL = jarUrl;
		this.loadedClasses = new HashMap<>();
	}

	public JarClassLoader(JarNode jarNode, JarStore jarStore, JarFile jarFile, ClassLoader parentClassLoader) {
		this(jarNode, jarFile, jarStore.getUrl(jarFile), parentClassLoader);
	}

	public JarNode getJarNode() {
		return jarNode;
	}

	public JarFile getJarFile() {
		return jarFile;
	}

	// NOTE: we turn protected into public method here, to give multi-jar-class loader access to it!
	@Override
	public Class<?> findClass(String name) throws ClassNotFoundException {
		System.out.println("FindClass("+name+")");
		Class<?> res = super.findClass(name);
		if (res != null) {
			loadedClasses.put(name, res);
		}
		return res;
	}

	public void loadAllClasses() {
		System.out.println("Loading classes for node "+jarNode.getName()+" from "+jarURL+" (jar name: "+jarFile.getJarName()+")");
		List<String> names;
		try {
			names = new JarContent(jarURL).getNames();
		} catch (FileNotFoundException e1) {
			throw new JarLoadingException("Could not open jar from URL: "+jarURL, e1);
		}
		//Log.detail("Jar store has {} entries", names.size());
		for (String n : names) {
			if (n.endsWith(".class")) {
				n = n.substring(0, n.length() - 6).replaceAll("/", ".");
				if (!loadedClasses.containsKey(n)) {
					try {
						loadClass(n);
						if (!loadedClasses.containsKey(n)) {
							throw new JarLoadingException("Already known class "+n+" was re-defined in jar from "+jarURL);
						}
					} catch (final ClassNotFoundException e) {
						throw new JarLoadingException("class is in jar file but was not found by class loader", e);
					}
				}
			}
		}
	}

	public Class<?> getLoadedClass(String name) {
		return loadedClasses.get(name);
	}

	public boolean hasLoadedClass(String name) {
		return loadedClasses.get(name) != null;
	}

	public Stream<Class<?>> loadedClasses() {
		return loadedClasses.values().stream();
	}

	public int numLoadedClasses() {
		return loadedClasses.size();
	}

	@SuppressWarnings("unchecked")
	public <C> Stream<Class<C>> loadedClassesAssignableTo(Class<C> assignableTo) {
		return loadedClasses().filter(cl -> assignableTo.isAssignableFrom(cl)).map(cl -> (Class<C>)cl);
	}

}
