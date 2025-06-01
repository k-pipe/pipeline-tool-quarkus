package com.kneissler.util.injection.jar;

import com.kneissler.util.injection.autowire.AutowiringJarClassLoader;
import com.kneissler.util.injection.autowire.SingletonMap;
import org.jkube.logging.Log;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class MultiJarClassLoader extends ClassLoader {
	
	private Map<JarNode, AutowiringJarClassLoader> nodeLoaders;
	private List<AutowiringJarClassLoader> loadSequence;
	private final SingletonMap singletonMap;
	private StaticResourceProvider resourceProvider;
	
	public MultiJarClassLoader(Map<JarNode, AutowiringJarClassLoader> nodeLoaders) {
		this.nodeLoaders = nodeLoaders;
		this.loadSequence = new ArrayList<>();
		this.singletonMap = new SingletonMap();
	}

	public AutowiringJarClassLoader getLoader(JarNode node) {
		return nodeLoaders.get(node);
	}
	
	/**
	 * Loads all classes, then creates all singleton instances (involving autowiring)
	 */
	public void initialize(JarStore jarStore) {
		determineLoadSequence();
		Log.log("Loading classes");
		loadSequence.forEach(JarClassLoader::loadAllClasses);
		Log.log("Resolving classes");
		loadSequence.forEach(AutowiringJarClassLoader::resolveAllClasses);
		Log.log("Creating singleton instance");
		singletonMap.createSingletonInstances();
		Log.log("Checking annotation consistency");
		loadSequence.forEach(AutowiringJarClassLoader::checkAnnotationConsistency);
		Log.log("Autowiring singletons");
		loadSequence.forEach(AutowiringJarClassLoader::autowireSingletons);
	}

	private void determineLoadSequence() {
		Set<JarNode> added = new HashSet<>();
		addWithParents(nodeLoaders.keySet(), added);
		System.out.println("LoadSequence: "+loadSequence.stream().map(cl -> cl.getJarNode().getName()).collect(Collectors.joining(",")));
	}

	private void addWithParents(Collection<JarNode> nodes, Set<JarNode> added) {
		nodes.forEach(node -> {
			if (added.add(node)) {
				addWithParents(node.getParentNodes(), added);
				loadSequence.add(nodeLoaders.get(node));
			}
		});
	}

	@Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
		for (JarClassLoader loader : loadSequence) {
			Class<?> res = loader.getLoadedClass(name);
			if (res != null) {
				return res;
			}
		}
		throw new ClassNotFoundException("Class was not found in Jar Files of dynamic JarSet");
	}

	public SingletonMap getSingletonMap() {
		return singletonMap;
	}
	
	public void forEachLoadedClass(Consumer<? super Class<?>> action) {
		loadSequence.forEach(jcl -> {
			System.out.println(jcl.getJarNode().getName()+": "+jcl.numLoadedClasses()+" classes");
			jcl.loadedClasses().forEach(action);
		});
	}

	public StaticResourceProvider getResourceProvider() {
		if (resourceProvider == null) {
			resourceProvider = new StaticResourceProvider(nodeLoaders.values());
		}
		return resourceProvider;
	}

	public int numNodes() {
		return nodeLoaders.size();
	}

}
