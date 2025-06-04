package pipelining.entity;

public interface ResolvedEntityFields extends EntityFields {

	@Override
	<T> ResolvedEntityField<T> getField(String key, Class<T> valueClass);

	@Override
	ResolvedEntityFields getSubFields(String key);
}
