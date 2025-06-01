package org.jkube.util;

public class ExpectationFailedException extends RuntimeException {
	public ExpectationFailedException(String message) {
		super(message);
	}
}
