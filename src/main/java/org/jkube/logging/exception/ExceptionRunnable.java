package org.jkube.logging.exception;

import org.jkube.application.Application;
import org.jkube.logging.Log;

import java.util.function.Consumer;

public class ExceptionRunnable {

	private Throwable thrown;

	public ExceptionRunnable(final ThrowingRunnable runnable) {
		try {
			runnable.run();
			thrown = null;
		} catch (Throwable t) {
			thrown = t;
			Log.exception(t);
		}
	}

	public void rethrow() {
		rethrow(null);
	}

	public void rethrow(String message) {
		if (thrown != null) {
			LoggedException.wrap(message, thrown);
		}
	}

	public void fail() {
		fail(null);
	}

	public void fail(String message) {
		if (thrown != null) {
			Application.fail(message);
		}
	}

	public ExceptionRunnable warn(String message, Object... params) {
		if (thrown != null) {
			Log.warn(message, params);
		}
		return this;
	}

	public ExceptionRunnable log(String message, Object... params) {
		if (thrown != null) {
			Log.log(message, params);
		}
		return this;
	}

	public ExceptionRunnable debug(String message, Object... params) {
		if (thrown != null) {
			Log.debug(message, params);
		}
		return this;
	}

	public void handle(final Consumer<Throwable> handler) {
		if (thrown != null) {
			handler.accept(thrown);
		}
	}
}