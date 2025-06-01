package org.jkube.entity;

import org.jkube.entity.collections.LinkedIterable;

import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;

public interface Entity {	
	
	// read operations
	
	EntityType getType();
	
	UUID getUUID();	

	<D extends EntityData> D getData();

	<E extends Entity> E getLinked(int linkIndex);

	<E extends Entity> Optional<E> getOptionalLinked(int linkIndex);

	<R> LinkedIterable<R> getLinkedCollection(int linkIndex);

	EntityFields getFields();

	<R extends ResolvedEntity> void resolve(Consumer<R> resolvedEntityConsumer);

	<E extends Enum<E>> E getSubClass(Class<E> subclassesEnumClass);

}
