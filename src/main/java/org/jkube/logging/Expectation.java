package org.jkube.logging;

public class Expectation {

	private final Object value;
	private final boolean check;

	public Expectation(final Object value, final boolean doCheck) {
		this.value = value;
		this.check = doCheck;
	}

}
