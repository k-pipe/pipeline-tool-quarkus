package org.jkube.http;

import java.io.IOException;
import java.io.InputStream;

public interface StreamReader<R> {
	R read(final InputStream body) throws IOException;
}
