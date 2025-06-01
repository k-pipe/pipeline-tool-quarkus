package org.jkube.logging;

import java.io.PrintStream;

public class FallbackLogger implements Logger {

	@Override
	public void log(final LogLevel level, final Throwable e, final String message, final Object[] parameters) {
		PrintStream logto = level.compareTo(LogLevel.LOG) < 0 ? System.err : System.out;
		if (message != null) {
			logto.println(substitute(message, parameters));
		}
		if (e != null) {
			e.printStackTrace();
		}
 	}

	public static String substitute(final String text, final Object[] parameters) {
		if ((parameters == null) || (parameters.length == 0)) {
			return text;
		}
		StringBuilder sb = new StringBuilder();
		int parNum = 0;
		int pos = 0;
		while (pos < text.length()) {
			char c = text.charAt(pos);
			if ((pos < text.length()-1) && (c == '{')  && (text.charAt(pos+1) == '}')) {
				pos++;
				if (parNum < parameters.length) {
					sb.append(parameters[parNum]);
				} else {
					sb.append("?");
				}
				parNum++;
			} else {
				sb.append(c);
			}
			pos++;
		}
		if (parNum != parameters.length) {
			Log.warn("Mismatch in log parameter: message has {} slots, {}  parameters provided", parNum, parameters.length);
		}
		return sb.toString();
	}

}
