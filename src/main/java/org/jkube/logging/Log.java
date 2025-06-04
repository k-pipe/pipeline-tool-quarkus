package org.jkube.logging;

import org.jkube.application.Application;
import org.jkube.logging.exception.*;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

public class Log {

	private static final AtomicReference<Logger> LOGGER = new AtomicReference<>(new FallbackLogger());

	public static void setLogger(Logger logger) {
		Logger previous = LOGGER.getAndSet(logger);
		if (!(previous instanceof FallbackLogger)) {
			warn("Logger was set twice: "+previous.getClass()+"(first) vs. "+logger.getClass()+"(second)");
		}
	}

	public static void log(String message, Object... parameters) {
		LOGGER.get().log(Logger.LogLevel.LOG, null, message, parameters);
	}

	public static void debug(String message, Object... parameters) {
		if (Application.isDebugEnabled()) {
			LOGGER.get().log(Logger.LogLevel.DEBUG, null, message, parameters);
		}
	}

	public static void debug(String message, Supplier<List<Object>> parameters) {
		if (Application.isDebugEnabled()) {
			LOGGER.get().log(Logger.LogLevel.DEBUG, null, message, parameters.get().toArray());
		}
	}

	public static void trace(String message, Object... parameters) {
		if (Application.isTraceEnabled()) {
			LOGGER.get().log(Logger.LogLevel.TRACE, null, message, parameters);
		}
	}

	public static void trace(String message, Supplier<List<Object>> parameters) {
		if (Application.isTraceEnabled()) {
			LOGGER.get().log(Logger.LogLevel.TRACE, null, message, parameters.get().toArray());
		}
	}

	public static void warn(final String message, Object... parameters) {
		LOGGER.get().log(Logger.LogLevel.WARN, null, message, parameters);
	}

	public static void error(final String message, Object... parameters) {
		LOGGER.get().log(Logger.LogLevel.ERROR, null, message, parameters);
		System.exit(1);
	}

	public static void exception(final Throwable e, final String message, Object... parameters) {
		LOGGER.get().log(Logger.LogLevel.ERROR, e, message, parameters);
	}

	public static void exception(final Throwable e) {
		exception(e, null);
	}

	public static ExceptionRunnable onException(ThrowingRunnable throwingRunnable) {
		return new ExceptionRunnable(throwingRunnable);
	}

	public static <T> ExceptionSupplier<T> onException(ThrowingSupplier<T> throwingSupplier) {
		return new ExceptionSupplier<>(throwingSupplier);
	}

	public static void interruptable(Interruptable interruptable) {
		try {
			interruptable.run();
		} catch (InterruptedException e) {
			Log.debug("Thread was interrupted, propagating interruption.");
			Thread.currentThread().interrupt();
		}
	}

	public static Expectation ensure(Object value) {
		return new Expectation(value, true);
	}

	public static Expectation assume(Object value) {
		return new Expectation(value, Application.isDebugEnabled() || !Application.isInProduction());
	}

}
