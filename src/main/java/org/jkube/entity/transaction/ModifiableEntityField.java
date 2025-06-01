package org.jkube.entity.transaction;

import org.jkube.entity.ResolvedEntityField;

public interface ModifiableEntityField<V> extends ResolvedEntityField<V> {
	void setValue(V value);
}
