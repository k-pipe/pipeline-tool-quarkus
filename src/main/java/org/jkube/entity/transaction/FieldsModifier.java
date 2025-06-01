package org.jkube.entity.transaction;

@FunctionalInterface
public interface FieldsModifier<F extends ModifiableEntityFields> {
	void modify(F fields);
}
