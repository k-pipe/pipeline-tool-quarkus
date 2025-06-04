package pipelining.logging;

public interface Logger {
	/**
	 *
	 * @param level the logging level
	 * @param e exception to be logged (null for normal logs)
	 * @param message the message to be logged (can be null)
	 * @param parameters list of parameters to be substituted in message
	 */
	void log(LogLevel level, Throwable e, String message, Object[] parameters);

	enum LogLevel {
		ERROR, WARN, LOG, DEBUG, TRACE
	}
}
