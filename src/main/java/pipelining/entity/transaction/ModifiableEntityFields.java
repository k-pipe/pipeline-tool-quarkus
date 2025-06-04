package pipelining.entity.transaction;

import pipelining.entity.EntityFields;

public interface ModifiableEntityFields extends EntityFields {

	@Override
	<V> ModifiableEntityField<V> getField(String key, Class<V> valueClass);

	@Override
	ModifiableEntityFields getSubFields(String key);
}
