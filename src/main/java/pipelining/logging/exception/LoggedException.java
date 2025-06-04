package pipelining.logging.exception;

public class LoggedException extends RuntimeException {

	public static void wrap(String message, final Throwable e) {
		if (e instanceof LoggedException) {
			// do not wrap twice!
			throw (LoggedException)e;
		} else {
			throw new LoggedException(message == null ? e.getMessage() : message, e);
		}
	}

	private LoggedException(final String message, final Throwable e) {
		super(message, e);
	}
}
