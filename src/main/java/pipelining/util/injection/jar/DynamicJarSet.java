package pipelining.util.injection.jar;

import pipelining.util.injection.autowire.AutowiringJarClassLoader;
import pipelining.util.injection.autowire.SingletonMap;
import pipelining.util.versions.JarVersion;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Consumer;

/**
 * Given a graph of jar files with dependencies, creates a class loader that can deal with multiple jar files,
 * managed by a given jar store. Which jar file version is used must be specified at creation of the
 * DynamicJarSet. A given DynamicJarSet can be used to create an updated DynamicJarSet by 
 * specifying different versions for some of the nodes in the jar graph (the nodes for which an update version is specified
 * and all dependent nodes will be reloaded in the updated DynamicJarSet, all not affected nodes will not be reloaded).
 *
 */
public class DynamicJarSet {

	private final JarGraph jarGraph;
	private final JarStore jarStore;
	private final MultiJarClassLoader multiClassLoader;
	private final Map<JarNode, JarVersion> nodeVersions;

	private DynamicJarSet(JarGraph jarGraph, JarStore jarStore, MultiJarClassLoader multiClassLoader, Map<JarNode, JarVersion> nodeVersions) {
		this.jarGraph = jarGraph;
		this.jarStore = jarStore;
		this.multiClassLoader = multiClassLoader;
		this.nodeVersions = nodeVersions;
		this.multiClassLoader.initialize(jarStore);
	}

	public static DynamicJarSet loadJarSet(JarGraph jarGraph, Map<JarNode, JarVersion> nodeVersions, JarStore jarStore) {
		Map<JarNode, AutowiringJarClassLoader> nodeLoaders = new HashMap<>();
		MultiJarClassLoader mjcl = new MultiJarClassLoader(nodeLoaders);
		for (Entry<JarNode, JarVersion> e : nodeVersions.entrySet()) {
			System.out.println("Using version "+e.getValue().getSuffix()+" of "+e.getKey().getName()+"...");
			nodeLoaders.put(e.getKey(), createLoader(mjcl, jarStore, e.getKey(), e.getValue()));
		}
		return new DynamicJarSet(jarGraph, jarStore, mjcl, nodeVersions);
	}

	public DynamicJarSet update(Map<JarNode, JarVersion> updatedNodes) {	
		return updateWithMessage(updatedNodes, null);
	}

	public DynamicJarSet updateWithMessage(Map<JarNode, JarVersion> updatedNodes, StringBuilder message) {		
		Map<JarNode, AutowiringJarClassLoader> nodeLoaders = new HashMap<>();
		MultiJarClassLoader mjcl = new MultiJarClassLoader(nodeLoaders);
		Set<JarNode> requireUpdate = new HashSet<>();
		Map<JarNode, JarVersion> newVersions = new HashMap<>();
		updatedNodes.forEach((node, version) -> {
			JarVersion oldVersion = nodeVersions.get(node);
			if (version.isOlderOrSameDate(oldVersion)) {
				newVersions.put(node, oldVersion);				
			} else {
				requireUpdate.add(node);
				newVersions.put(node, version);
			}
		});
		if (message != null) {
			message.append("Updating nodes:\n");
		}
		for (JarNode node : jarGraph.getAffectedNodes(requireUpdate)) {
			JarVersion version = updatedNodes.get(node);
			if (message != null) {
				message.append("  "+node.getName()+(version != null ? " --> "+version.getSuffix() : "")+"\n");
			}
			nodeLoaders.put(node, (version == null) ? cloneLoader(mjcl, node) : createLoader(mjcl, jarStore, node, version));
		}
		return new DynamicJarSet(jarGraph, jarStore, mjcl, newVersions);
	}

	public SingletonMap getSingletonMap() {
		return multiClassLoader.getSingletonMap();
	}

	private AutowiringJarClassLoader cloneLoader(MultiJarClassLoader mjcl, JarNode node) {
		return new AutowiringJarClassLoader(node, jarStore, multiClassLoader.getLoader(node).getJarFile(), mjcl);
	}

	private static AutowiringJarClassLoader createLoader(MultiJarClassLoader parent, JarStore jarStore, JarNode node, JarVersion version) {
		return new AutowiringJarClassLoader(node, jarStore, jarStore.getJarFile(node, version), parent);
	}

	public void forEachLoadedClass(Consumer<? super Class<?>> action) {
		multiClassLoader.forEachLoadedClass(action);
	}
	
	public StaticResourceProvider getResourceProvider() {
		return multiClassLoader.getResourceProvider();
	}

	public void configure(Map<JarNode, Map<String, Map<String, String>>> jarConfiguration) {
		jarConfiguration.forEach((node, conf) -> multiClassLoader.getLoader(node).configure(conf));
	}

	public Class<?> classForName(String className) throws ClassNotFoundException {
		return multiClassLoader.findClass(className);
	}

	public int numJarFiles() {
		return multiClassLoader.numNodes();
	}
}
