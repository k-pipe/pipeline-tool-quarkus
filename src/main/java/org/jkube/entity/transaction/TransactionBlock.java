package org.jkube.entity.transaction;

import java.util.function.Consumer;

public interface TransactionBlock {

	Transaction add(Transaction transaction);

	void submit();

	void submit(Consumer<TransactionResult> resultHandler);
}
