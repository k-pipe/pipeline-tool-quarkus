package org.jkube.http;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import static org.jkube.logging.Log.debug;

public class StreamDown implements StreamReader<String> {

	private final OutputStream out;

	public StreamDown(final OutputStream out) {
		this.out = out;
	}

	@Override
	public String read(final InputStream body) throws IOException {
		long bytes = body.transferTo(out);
		out.close();
		String message = "streamed down "+bytes+" bytes";
		debug(message);
		return message;
	}
}
