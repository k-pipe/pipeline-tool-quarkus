package pipelining.actions;

public class Constants {

    public static final String WILDCARD = "*";
    public static final String WILDCARD_FIND_REGEX = "\\*";
    public static final String WILDCARD_REPLACE_REGEX = ".*";

    // Commands
    public static final String PARSE = "parse";
    public static final String RUN = "run";
    public static final String SCHEDULE = "schedule";
    public static final String LOGIN = "login";
    public static final String CLEAN = "clean";
    public static final String BUILD = "build";
    public static final String PUSH = "push";
    public static final String PULL = "pull";

    public static final String APPLY = "apply";
    public static final String SIMULATE = "simulate";
    public static final String FOLLOW = "follow";

    public static final String I = "i";
    public static final String INPUT = "input";
    public static final String INPUT_DEFAULT = "*.md";

    public static final String O = "o";
    public static final String OUTPUT = "output";
    public static final String OUTPUT_DEFAULT = "*.yml";

    public static final String C = "c";
    public static final String CONFIG = "config";

    public static final String NAME = "name";

    public static final String N = "n";

    public static final String NAMESPACE = "namespace";
    public static final String PIPELINE = "pipeline";
    public static final String R = "r";

    public static final String REGISTRY = "registry";

    public static final String P = "p";

    public static final String K = "k";
    public static final String KEEP_EXISTING = "keep-existing";

    public static final String T = "t";
    public static final String TIMEOUT = "timeout";
    public static final String TIMESTAMP = "timestamp";

    public static final String W = "w";
    public static final String WORKDIR = "workdir";
    public static final String SIMULATIONDIR = "simulationdir";
    public static final String DEFAULT_SIMULATION_DIR = "simulation";
    public static final String CREDENTIALS = "credentials";
    public static final String DEFAULT_CREDENTIALS = "/root/.config/gcloud:/root/.config/gcloud";
    public static final String S = "s";
    public static final String B = "b";
    public static final String BEGIN = "begin";

    public static final String E = "e";
    public static final String END = "end";

}
