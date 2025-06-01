package org.jkube.logging.exception;

@FunctionalInterface
public interface Interruptable {
	void run() throws InterruptedException;
}
