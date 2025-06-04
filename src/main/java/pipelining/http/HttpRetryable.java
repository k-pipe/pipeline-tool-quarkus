package pipelining.http;

import pipelining.application.RetryableResult;
import pipelining.util.Utf8;

import java.io.IOException;
import java.net.ConnectException;
import java.net.http.HttpTimeoutException;
import java.util.List;

import static pipelining.application.Application.fail;

public class HttpRetryable<R> implements RetryableResult<R> {

	private boolean succeeded;
	private boolean shouldRetry;
	private Throwable exception;
	private final int status;
	private String failureMessage;
	private R body;

	public HttpRetryable(final HttpSettings settings, final HttpResult httpResult) {
		succeeded = inRange(httpResult.getStatus(), settings.successStatusRanges);
		shouldRetry = inRange(httpResult.getStatus(), settings.retryStatusRanges);
		status = httpResult.getStatus();
		exception = httpResult.getException();
		if (succeeded) {
			failureMessage = null;
		} else {
			body = null;
			try {
				failureMessage = Utf8.read(httpResult.getBody());
			} catch (IOException e) {
				exception = e;
			}
		}
	}

	public int getStatus() {
		return status;
	}

	public void readingFailed(final IOException e) {
		succeeded = false;
		shouldRetry = true;
		exception = e;
		failureMessage = "Reading response failed: "+e;
	}

	public HttpRetryable(final Throwable t) {
		succeeded = false;
		shouldRetry = hasCause(t, ConnectException.class) || hasCause(t, HttpTimeoutException.class);
		exception = shouldRetry ? null : t;
		body = null;
		status = -1;
		failureMessage = "Exception occurred: "+t;
	}

	private boolean hasCause(final Throwable thrown, final Class<? extends Exception> exceptionClass) {
		Throwable t = thrown;
		do {
			if (t.getClass().equals(exceptionClass))  {
				return true;
			}
			t = t.getCause();
		} while (t != null);
		return false;
	}

	private boolean inRange(final int status, final List<int[]> ranges) {
		for (int[] range : ranges) {
			if (range.length != 2) {
				fail("Illegal length of range: "+range.length);
			}
			if ((status >= range[0]) && (status < range[1])) {
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean hasSucceeded() {
		return succeeded;
	}

	@Override
	public boolean shouldRetry() {
		return shouldRetry;
	}

	@Override
	public Throwable getException() {
		return exception;
	}

	@Override
	public R getResult() {
		return body;
	}

	@Override
	public String getFailureDescription() {
		return failureMessage;
	}

	public void setResult(final R body) {
		this.body = body;
	}

	public void vetoShouldRetry() {
		this.shouldRetry = false;
	}

}
