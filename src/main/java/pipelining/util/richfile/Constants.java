package pipelining.util.richfile;

import java.util.regex.Pattern;

public class Constants {
    public static final String MARKER = "#";
    public static final String SETTINGS = MARKER+"SETTINGS ";
    public static final String SET = MARKER+"SET ";
    public static final String INCLUDE = MARKER+"INCLUDE ";
    public static final String END = MARKER+"END";
    public static final String ELSE = MARKER+"ELSE";
    public static final String IF = MARKER+"IF ";
    public static final String MACRO = MARKER+"MACRO ";
    public static final Pattern PROPERTY_REGEX = Pattern.compile(".*\\$\\{([a-zA-Z0-9_-]+)\\}.*");
    public static final String COMMENT = "//";
}
