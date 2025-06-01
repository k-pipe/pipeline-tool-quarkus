package org.jkube.entity.transaction;

@FunctionalInterface
public interface DataModifier<D extends ModifiableEntityData> {
	void modify(D data);
}
