package pipelining.entity;

import pipelining.entity.collections.LinkedIterable;
import pipelining.entity.transaction.TransactionBlock;

import java.util.UUID;

public interface EntityEngine {

	<E extends Entity> E getByUUID(EntityType type, UUID uuid);

	<E extends Entity> LinkedIterable<E> getAll(EntityType type);

	TransactionBlock transactionBlock();

}
