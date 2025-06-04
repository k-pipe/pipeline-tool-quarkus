package pipelining.http;

import pipelining.logging.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import static pipelining.logging.Log.debug;

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
		Log.debug(message);
		return message;
	}
}
