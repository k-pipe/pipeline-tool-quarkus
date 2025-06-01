package com.kneissler.util.loggedtask;

public class LoggedTaskLog {

	private static final int LEVEL_INSET = 5;
	public static boolean LOG_VERBOSE = true;

	public static void logHeading(String title, int level) {
		if (level <= 0) {
			return;
		}
		if (LOG_VERBOSE) {
			log(level, "");
			if (level == 1) {
				log(level, line(title,"="));
				log(level, title);
				log(level, line(title,"="));
			} else if (level == 2) {
				log(level, line(title,"-"));
				log(level, title);
				log(level, line(title,"-"));
			} else if (level == 3) {
				log(level, title);
				log(level, line(title,"="));
			} else {
				log(level, title);
				log(level, line(title,"-"));			
			}
			log(level, "");
		} else {
			log(level, title);
		}
	}

	private static String line(String title, String lineChar) {
		return repeat(lineChar, title.length());
	}

	public static void log(int level, String string) {
		System.out.println(repeat(" ",(level-1)*LEVEL_INSET)+string);
	}

	private static String repeat(String string, int num) {
		return String.valueOf(string).repeat(Math.max(0, num));
	}

}
