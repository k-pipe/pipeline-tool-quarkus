package org.jkube.application;

@FunctionalInterface
public interface FailureHandler {
	void fail(String message, int failureCode);
}
