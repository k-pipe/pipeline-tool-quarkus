package org.jkube.http;

import org.jkube.util.Utf8;

import java.io.IOException;
import java.io.InputStream;

import static org.jkube.logging.Log.debug;

public class ResponseDebug implements StreamReader<String> {

	@Override
	public String read(final InputStream body) throws IOException {
		String msg = "Operation responded: "+Utf8.read(body);
		debug(msg);
		return msg;
	}
}
