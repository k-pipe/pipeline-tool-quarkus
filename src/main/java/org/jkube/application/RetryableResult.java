package org.jkube.application;

public interface RetryableResult<R> {

	/**
	 * Indicates that an operation has succeeded, i.e. a result can be expected
	 *
	 * @return true if an exception was thrown or the operation should be retried or the operation has failed irrecoverably
	 */
	boolean hasSucceeded();

	/**
	 * Indicates that an operation has failed due to a possibly temporary problem, i.e. it might succeed when retried
	 *
	 * @return true to suggest a retry
	 */
	boolean shouldRetry();

	/**
	 * Access to the exception thrown during execution of an operation (if any)
	 *
	 * @return exception thrown while executing the operation, or null if no exception occurred
	 */
	Throwable getException();

	/**
	 * Provide result of the operation, this method should be called only if hasSucceeded() returns true.
	 * It should not be used to determine if the operation succeeded (use hasSucceeded() instead).
	 * Note: This method might return a null objected for successful operations.
	 *
	 * @return result of the operation (might be null for some operations, even if successfully executed)
	 */
	R getResult();

	/**
	 * Provide a description of the reason of the failure.
	 *
	 * @return textual description of the failure reason, or null if no succeeded or not available
	 */
	String getFailureDescription();

}
