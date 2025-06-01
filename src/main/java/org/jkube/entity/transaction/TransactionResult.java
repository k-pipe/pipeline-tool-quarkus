package org.jkube.entity.transaction;

public class TransactionResult {
	private boolean executed;
	private int failedStep;
	private TransactionFailure failureReason;
	private String failureDescription;
}
