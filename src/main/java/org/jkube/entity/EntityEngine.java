package org.jkube.entity;

import org.jkube.entity.collections.LinkedIterable;
import org.jkube.entity.transaction.TransactionBlock;

import java.util.UUID;

public interface EntityEngine {

	<E extends Entity> E getByUUID(EntityType type, UUID uuid);

	<E extends Entity> LinkedIterable<E> getAll(EntityType type);

	TransactionBlock transactionBlock();

}
