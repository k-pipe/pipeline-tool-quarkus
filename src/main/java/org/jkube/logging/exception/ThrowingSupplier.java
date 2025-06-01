package org.jkube.logging.exception;

@FunctionalInterface
public interface ThrowingSupplier<R> {
	R get() throws Throwable;
}
