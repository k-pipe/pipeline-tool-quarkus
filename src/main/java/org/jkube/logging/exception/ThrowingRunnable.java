package org.jkube.logging.exception;

@FunctionalInterface
public interface ThrowingRunnable {
	void run() throws Throwable;
}
