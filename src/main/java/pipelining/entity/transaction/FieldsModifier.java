package pipelining.entity.transaction;

@FunctionalInterface
public interface FieldsModifier<F extends ModifiableEntityFields> {
	void modify(F fields);
}
