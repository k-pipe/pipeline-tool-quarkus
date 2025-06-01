package org.jkube.http;

import java.io.InputStream;

import static org.jkube.logging.Log.debug;

public class HttpResult {

	private final int status;
	private final InputStream body;
	private final Throwable exception;

	public HttpResult(final int status, final InputStream body) {
		debug("Http status code: "+status);
		this.status = status;
		this.body = body;
		this.exception = null;
	}

	public HttpResult(final Throwable t) {
		this.status = -1;
		this.body = null;
		this.exception = t;
	}

	public int getStatus() {
		return status;
	}

	public InputStream getBody() {
		return body;
	}

	public Throwable getException() {
		return exception;
	}
}
