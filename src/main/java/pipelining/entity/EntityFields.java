package pipelining.entity;

public interface EntityFields {
	long getVersion();

	/**
	 * Return a field without resolving 
	 * 
	 * @param <V> value type
	 * @param key a field key that corresponds to a leaf node in the field tree
	 * @param valueClass the class of the field
	 * @return field
	 */
	<V> EntityField<V> getField(String key, Class<V> valueClass);
	
	/**
	 * Get sub-fields without resolving
	 * 
	 * @param key a field key that corresponds to a branch node in the field tree
	 * @return sub fields
	 */
	EntityFields getSubFields(String key);	

}
