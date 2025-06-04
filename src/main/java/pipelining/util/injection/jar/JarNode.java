package pipelining.util.injection.jar;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class JarNode {

	public static final JarNode PREDEFINED_CLASSES_NODE = new JarNode(null, "<<PREDEFINED>>", null, Collections.emptySet()).selfInjecting(); 

	private final JarGraph jarGraph;
	private final String name;
	private final String versionSpecification;
	private final Set<JarNode> parentNodes;
	private final Set<JarNode> childrenNodes;
	private final Set<JarNode> injectedNodes;
	private final Set<JarNode> injectingNodes;

	JarNode(JarGraph jarGraph, String name, String versionSpecification, Set<JarNode> parentNodes) {
		this.jarGraph = jarGraph;
		this.name = name;
		this.versionSpecification = versionSpecification;
		parentNodes.forEach(pn -> expectSameGraph(pn));
		this.parentNodes = new HashSet<>();
		this.childrenNodes = new HashSet<>();
		this.injectedNodes = new HashSet<>();
		this.injectingNodes = new HashSet<>();
		for (JarNode parentNode : parentNodes) {
			parentNode.setChildNode(this);
		}
	}

	public JarNode setChildNode(JarNode child) {
		childrenNodes.add(child);
		child.parentNodes.add(this);
		return this;
	}
	
	public JarNode selfInjecting() {
		injectsInto(this);
		return this;
	}

	public JarNode injectsInto(JarNode injectedNode) {
		expectSameGraph(injectedNode);
		injectedNodes.add(injectedNode);
		injectedNode.injectingNodes.add(this);
		return this;
	}

	public JarNode injectsInto(JarNode... injectedNodes) {
		for (JarNode node : injectedNodes) {
			injectsInto(node);
		}
		return this;
	}

	public JarNode injectsInto(Collection<JarNode> injectedNodes) {
		for (JarNode node : injectedNodes) {
			injectsInto(node);
		}
		return this;
	}

	public JarNode injectsIntoPredefinedClasses() {
		return injectsInto(PREDEFINED_CLASSES_NODE);		
	}

	private void expectSameGraph(JarNode node) {
		if ((node.jarGraph != null) && (jarGraph != null) && !node.jarGraph.equals(jarGraph)) {
			throw new IllegalArgumentException("jar node does not belong to same graph");
		}
	}

	public Set<JarNode> getParentNodes() {
		return parentNodes;
	}

	public Set<JarNode> getChildrenNodes() {
		return childrenNodes;
	}

	public Set<JarNode> getInjectedNodes() {
		return injectedNodes;
	}

	public String getName() {
		return name;
	}

	public String getVersionPattern() {
		return versionSpecification;
	}

}
