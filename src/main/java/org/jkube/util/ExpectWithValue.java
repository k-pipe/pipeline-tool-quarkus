package org.jkube.util;

import org.jkube.logging.FallbackLogger;

import static org.jkube.application.Application.fail;
import static org.jkube.logging.Log.warn;

public class ExpectWithValue<T>  {

	private final String message;
	private final Object[] parameters;
	private final T value;

	public ExpectWithValue(T value, String message, Object... parameters) {
		this.value = value;
		this.message = message;
		this.parameters = parameters;
		if (message != null) {
			warn(message, parameters);
		}
	}

	public T elseFail() {
		if (message != null) {
			return fail(FallbackLogger.substitute(message, parameters));
		}
		return value;
	}

	public T elseFail(String failureMessage) {
		if (message != null) {
			fail(failureMessage);
		}
		return value;
	}

}
