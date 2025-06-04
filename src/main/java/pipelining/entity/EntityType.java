package pipelining.entity;

public interface EntityType {

	String getName();

	<E extends Entity> Class<E> getEntityClass();
	
	<D extends EntityData> Class<D> getDataClass();
	
	LinkType[] getLinkTypes();

	boolean hasData();
	
	boolean hasUUID();
	
	boolean hasFields();

	boolean hasSubClasses();

}
