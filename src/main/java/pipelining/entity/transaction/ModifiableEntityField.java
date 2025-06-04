package pipelining.entity.transaction;

import pipelining.entity.ResolvedEntityField;

public interface ModifiableEntityField<V> extends ResolvedEntityField<V> {
	void setValue(V value);
}
