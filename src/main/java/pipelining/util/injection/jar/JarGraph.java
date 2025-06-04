package pipelining.util.injection.jar;

import java.util.*;

public class JarGraph {

	private final Map<String, JarNode> nodes = new HashMap<>();

	public JarNode newNode(String name, String versionSpecification, JarNode... parentNodes) {
		return createNode(name, versionSpecification, new HashSet<>(Arrays.asList(parentNodes)));
	}

	public JarNode newNode(String name, String versionSpecification, Set<JarNode> parentNodes) {
		return createNode(name, versionSpecification, parentNodes);
	}

	public JarNode newNode(String name, String versionSpecification) {
		return createNode(name, versionSpecification, new HashSet<JarNode>());
	}

	public JarNode newNode(String name, JarNode... parentNodes) {
		return createNode(name, null, new HashSet<>(Arrays.asList(parentNodes)));
	}

	private JarNode createNode(String name, String versionSpecification, Set<JarNode> parentNodes) {
		JarNode res = new JarNode(this, name, versionSpecification, parentNodes);
		nodes.put(name, res);
		return res;
	}

	public Collection<JarNode> getNodes() {
		return nodes.values();
	}

	public Set<JarNode> getAffectedNodes(Set<JarNode> seedNodes) {
		Set<JarNode> res = new HashSet<JarNode>();
		seedNodes.forEach(node -> addAffected(res, node));
		return res;
	}

	private void addAffected(Set<JarNode> res, JarNode node) {
		if (res.add(node)) {
			node.getChildrenNodes().forEach(child -> addAffected(res, child));
			node.getInjectedNodes().forEach(injected -> addAffected(res, injected));
		}
	}

	public JarNode getNode(String name) {
		JarNode res = nodes.get(name);
		if (res == null) {
			throw new RuntimeException("No such jar node defined: "+name);
		}
		return res;
	}

}
