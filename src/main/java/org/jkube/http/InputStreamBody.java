package org.jkube.http;

import java.io.InputStream;
import java.util.function.Supplier;

public class InputStreamBody implements Supplier<InputStream> {

	private final InputStream in;

	public InputStreamBody(final InputStream in) {
		this.in = in;
	}

	@Override
	public InputStream get() {
		return in;
	}
}
