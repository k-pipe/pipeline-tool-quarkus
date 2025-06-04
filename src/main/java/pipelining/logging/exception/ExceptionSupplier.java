package pipelining.logging.exception;

import pipelining.application.Application;
import pipelining.logging.Log;

import java.util.Optional;

public class ExceptionSupplier<T> {

	private Throwable thrown;
	private T result;

	public ExceptionSupplier(final ThrowingSupplier<T> throwingSupplier) {
		try {
			result = throwingSupplier.get();
			thrown = null;
		} catch (Throwable t) {
			result = null;
			thrown = t;
			Log.exception(t);
		}
	}

	public T rethrow() {
		return rethrow(null);
	}

	public T rethrow(String message) {
		if (thrown != null) {
			LoggedException.wrap(message, thrown);
		}
		return result;
	}

	public T fail() {
		return fail(null);
	}

	public T fail(String message) {
		if (thrown != null) {
			Application.fail(message);
		}
		return result;
	}

	public ExceptionSupplier<T> warn(String message, Object... params) {
		if (thrown != null) {
			Log.warn(message, params);
		}
		return this;
	}

	public ExceptionSupplier<T> log(String message, Object... params) {
		if (thrown != null) {
			Log.log(message, params);
		}
		return this;
	}

	public ExceptionSupplier<T> debug(String message, Object... params) {
		if (thrown != null) {
			Log.debug(message, params);
		}
		return this;
	}

	public Optional<T> optional() {
		return Optional.ofNullable(result);
	}

	public T fallback(T fallbackValue) {
		return thrown == null ? result : fallbackValue;
	}

	public T fallbackNull() {
		return fallback(null);
	}
}
