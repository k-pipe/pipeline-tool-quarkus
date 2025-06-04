package pipelining.util.injection.jar;

import java.time.format.DateTimeFormatter;

public class JarName {

	public static final String JAR_EXTENSION = ".jar";
	public static final String SEPARATOR1 = "_"; // separates service name / branch+version / timestamp
	public static final String SEPARATOR2 = "-"; // separates multiple branch elements / version
	public static final String SEPARATOR3 = "."; // separates numbers in semantic version
	public static final DateTimeFormatter SNAPSHOT_TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm-ss");

}
