package pipelining.util;

import pipelining.logging.FallbackLogger;
import pipelining.logging.Log;

import static pipelining.application.Application.fail;

public class ExpectWithValue<T>  {

	private final String message;
	private final Object[] parameters;
	private final T value;

	public ExpectWithValue(T value, String message, Object... parameters) {
		this.value = value;
		this.message = message;
		this.parameters = parameters;
		if (message != null) {
			Log.warn(message, parameters);
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
