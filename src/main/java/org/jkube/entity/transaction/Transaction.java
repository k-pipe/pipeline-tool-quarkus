package org.jkube.entity.transaction;

import org.jkube.entity.EntityType;
import org.jkube.entity.transaction.collections.ModifiableEntityList;

import java.util.UUID;

public interface Transaction {

	ModifiableEntity getRoot();

	<E extends ModifiableEntity> E getByUUID(EntityType type, UUID uuid);

	<E extends ModifiableEntity> ModifiableEntityList<E> getAll(EntityType type);

	<E extends ModifiableEntity> E create(EntityType type, UUID uuid);

	<E extends ModifiableEntity> E create(EntityType type);

	void fail();

}
