package org.jkube.job;

import org.jkube.logging.exception.ThrowingSupplier;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import static org.jkube.logging.Log.*;

public enum ExecutionResult {

	SUCCESS, WARNING, FAILED, INTERRUPTED, TIMEOUT, EXCEPTION;

	public static ExecutionResult catchExceptions(final ThrowingSupplier<ExecutionResult> executor) {
		try {
			return executor.get();
		} catch (InterruptedException ie) {
			warn("interrupted waiting for execution result");
			return INTERRUPTED;
		} catch (ExecutionException ee) {
			warn("exception in execution occurred");
			return EXCEPTION;
		}  catch (TimeoutException ea) {
			log("timeout in job execution occurred");
			return TIMEOUT;
		} catch (Throwable e) {
			exception(e, "Unexpected exception during job execution");
			return FAILED;
		}
	}

}
