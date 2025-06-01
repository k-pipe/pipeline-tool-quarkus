package org.jkube.application;

public interface ConextDependentFlag<C> {
	boolean isSet(C context);
	void set(C context, boolean newValue);
}
