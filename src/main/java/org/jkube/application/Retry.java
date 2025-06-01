package org.jkube.application;

import org.jkube.logging.exception.ThrowingRunnable;
import org.jkube.logging.exception.ThrowingSupplier;

import java.util.Optional;
import java.util.function.Supplier;

import static org.jkube.logging.Log.*;

public class Retry {

	private static final double INCREASE_WAIT_FACTOR = Math.sqrt(2);
	private static final long INITIAL_WAIT = 150; // mili seconds
	private static final int MAX_NUM_RETRIES = 20; // (2^(20/2) * 2 - 1) * 0.15s: retry for approximately 5 minutes, then give up
	private static final Retry DEFAULT_RETRY = new Retry(MAX_NUM_RETRIES, INITIAL_WAIT, INCREASE_WAIT_FACTOR);

	private final int maxNumRetries;
	private final long initialWaitMillis;
	private final double increaseWaitFactor;

	public static <R, RR extends RetryableResult<R>> Optional<R> retry(Supplier<RR> supplier) {
		return DEFAULT_RETRY.withRetryableSupplier(supplier);
	}

	public static <R> Optional<R> retryOptional(ThrowingSupplier<Optional<R>> supplier) {
		return DEFAULT_RETRY.withOptionalSupplier(supplier);
	}

	public static <R> Optional<R> retrySupplier(ThrowingSupplier<R> supplier) {
		return DEFAULT_RETRY.withSupplier(supplier);
	}

	public static boolean retryBoolean(ThrowingSupplier<Boolean> supplier) {
		return DEFAULT_RETRY.withBooleanSupplier(supplier);
	}

	public static boolean retryRunnable(ThrowingRunnable runnable) {
		return DEFAULT_RETRY.withRunnable(runnable);
	}

	public Retry(int maxNumRetries, long initialWaitMilis, double increaseWaitFactor) {
		this.maxNumRetries = maxNumRetries;
		this.initialWaitMillis = initialWaitMilis;
		this.increaseWaitFactor = increaseWaitFactor;
	}

	public <R, RR extends RetryableResult<R>> Optional<R> withRetryableSupplier(Supplier<RR> supplier) {
		long wait = initialWaitMillis;
		int trial = 0;
		RetryableResult<R> retryable;
		Optional<R> result = Optional.empty();
		boolean done = false;
		while(!done) {
			trial++;
			debug("Doing trial {}", trial);
			retryable = supplier.get();
			if (retryable.hasSucceeded()) {
				result = Optional.of(retryable.getResult());
				done = true;
			} else {
				if (retryable.getException() != null) {
					exception(retryable.getException(),"Exception occurred in trial {}", trial);
				} else {
					String reason = retryable.getFailureDescription();
					if (reason == null) {
						warn("Unspecified failure occurred in trial #{}", trial);
					} else {
						warn("Failure occurred in trial #{}: {}", trial, reason);
					}
				}
				if (retryable.shouldRetry()) {
					if (trial <= maxNumRetries) {
						long thisWait = wait;
						debug("Sleeping for {}ms.", thisWait);
						interruptable(() -> Thread.sleep(thisWait));
						wait = Math.round(wait * increaseWaitFactor);
					} else {
						error("Still no result after {} trials, giving up.", trial);
						done = true;
					}
				} else {
					error("Non-recoverable failure, giving up.");
					done = true;
				}
			}
		}
		return result;
	}

	public <R> Optional<R> withOptionalSupplier(ThrowingSupplier<Optional<R>> supplier) {
		long wait = initialWaitMillis;
		int trial = 0;
		Optional<R> result = Optional.empty();
		boolean done = false;
		while(!done) {
			trial++;
			debug("Doing trial {}", trial);
			result = onException(supplier).warn("Exception in trial {}", trial).fallback(Optional.empty());
			done = result.isPresent();
			if (!done) {
				if (trial <= maxNumRetries) {
					warn("Failure in trial #{}, sleeping for {}ms.", trial, wait);
					long thisWait = wait;
					interruptable(() -> Thread.sleep(thisWait));
					wait = Math.round(wait * increaseWaitFactor);
				} else {
					error("Got no result in trial #{}, giving up.", trial);
					done = true;
				}
			}
		}
		return result;
	}

	public <R> Optional<R> withSupplier(ThrowingSupplier<R> supplier) {
		return withOptionalSupplier(() -> Optional.ofNullable(supplier.get()));
	}

	public boolean withBooleanSupplier(ThrowingSupplier<Boolean> supplier) {
		Optional<Boolean> result = withOptionalSupplier(() -> supplier.get() ? Optional.of(true) : Optional.empty());
		return result.isPresent();
	}

	public boolean withRunnable(ThrowingRunnable runnable) {
		Optional<Boolean> result = withOptionalSupplier(() -> {
			runnable.run();
			return Optional.of(true);
		});
		return result.isPresent();
	}

}
