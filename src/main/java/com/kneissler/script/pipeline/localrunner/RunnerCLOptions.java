package com.kneissler.script.pipeline.localrunner;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.jkube.application.Application.fail;
import static org.jkube.logging.Log.log;

public abstract class RunnerCLOptions {

	public static final char SLASH = '/';
	public static final char BACKSLASH = '\\';

	public static final String W = "w";
	public static final String WORKDIR = "workdir";
	public static final String F = "f";
	public static final String FROM = "from";
	public static final String T = "t";
	public static final String TO = "to";
	public static final String P = "p";
	public static final String STEP = "step";
	public static final String S = "s";
	public static final String PULL = "pull";
	public static final String I = "i";
	public static final String INTERACTIVE = "interactive";
	public static final String H = "h";
	public static final String HELP = "help";
	public static final String D = "d";
	public static final String DOCKERIMAGE = "dockerimage";
	public static final String N = "n";
	public static final String NAMESPACE = "namespace";
	public static final String C = "c";
	public static final String CLUSTER = "cluster";
	public static final String M = "m";
	public static final String MARKDOWN = "markdown";

	public static final String O = "o";
	public static final String OPERATION = "operation";

	public static final String V = "v";
	public static final String VARIABLES = "variables";
	public static final String R = "r";
	public static final String RUN = "run";
	public static final String U = "u";
	public static final String USER_INTERFACE = "user-interface";
	public static final String G = "g";
	public static final String GROUP = "group-combined";
	public static final String E = "e";
	public static final String EXTENDED = "extended-syntax";

	public static final String ARCHITECTURE = "architecture";

	public static final String A = "a";

	private static final String J = "j";
	private static final String JWT = "jwt";

	public static final String PULLSECRET = "pullsecret";

	public static final String PS = "P";

	public static final String CALL = "docker run -v /var/run/docker.sock:/var/run/docker.sock jkube/pipeline";
	public static final String DEFAULT_MARKDOWN = "pipeline.md";

	protected String workdir;
	protected String from;
	protected String to;
	protected String cluster;
	protected String namespace;
	protected String markdown;
	protected Map<String,String> variables;
	protected boolean isRunSpecified;
	protected String taskId;
	protected boolean pull;
	protected boolean imageEnabled;
	protected String pipelineViewerUrl;
	protected boolean extendedSyntax;
	protected boolean groupCombined;
	protected boolean interactive;

	protected OperationMode operation;

	protected String platform;

	protected String pullSecret;
	protected String jwt;

	public void init(final String[] arguments) {
		if (arguments.length == 1) {
			// automatically generate the help statement
			showHelpAndExit();
		} else {
			parseCommandLine(arguments);
		}
		workdir = addSeparatorCharAtEndIfNotPresent(workdir);
	}

	protected abstract void showHelp();

	protected void showHelpAndExit() {
		showHelp();
		System.exit(0);
	}

	protected abstract boolean hasOption(final String key);

	protected abstract String getOptionValue(final String key);

	protected abstract String getOptionValue(final String key, final String defaultValue);

	protected abstract void addOption(String shortKey, String longKey, boolean hasArg, String description);

	protected abstract void addOptionWithOptionalArg(String shortKey, String longKey, String description);

	protected void parseCommandLine(final String[] arguments) {
		log("Comandline args: {}", Arrays.asList(arguments));
		if (hasOption(HELP)) {
			showHelpAndExit();
		}
		workdir = getOptionValue(W);
		if (workdir == null) {
			fail("No workdir command line argument specified");
		}
		if (hasOption(STEP)) {
			if (hasOption(FROM) || hasOption(TO)) {
				fail("Cannot use option "+STEP+" in combination with "+FROM+" or "+TO);
			}
			from = to = getOptionValue(STEP);
		} else {
			from = getOptionValue(FROM);
			to = getOptionValue(TO);
		}
		cluster =  getOptionValue(CLUSTER);
		namespace =  getOptionValue(NAMESPACE);
		markdown =  getOptionValue(MARKDOWN, DEFAULT_MARKDOWN);
		pipelineViewerUrl = getOptionValue(USER_INTERFACE);
		extendedSyntax =  hasOption(EXTENDED);
		groupCombined =  hasOption(GROUP);
		isRunSpecified = hasOption(RUN);
		taskId = getOptionValue(RUN);
		if (isRunSpecified && (cluster == null)) {
			fail("option --run requires --cluster to be set to cluster hostname/ip address");
		}
		imageEnabled = hasOption(USER_INTERFACE);
		pipelineViewerUrl = getOptionValue(USER_INTERFACE);

		pull = hasOption(PULL);
		interactive = hasOption(INTERACTIVE);
		pullSecret = getOptionValue(PULLSECRET);
		platform = getOptionValue(ARCHITECTURE);
		jwt = getOptionValue(JWT);
		operation = hasOption(OPERATION) ? OperationMode.valueOf(getOptionValue(OPERATION).toUpperCase()) : null;
		variables = new LinkedHashMap<>();
		if (hasOption(VARIABLES)) {
			for (String kv : getOptionValue(VARIABLES).split(";")) {
				String[] split = kv.split("=");
				if (split.length != 2) {
					fail("expected entry of form key=value, got "+kv);
				}
				variables.put(split[0].trim(), split[1].trim());
			}
		}
		log("Parameters: from={}, to={}, hostDir={}", from, to, workdir);
	}

	protected void populateOptions() {
		addOption(W, WORKDIR, true, "working directory (must be absolute)");
		addOption(F, FROM, true, "run from step");
		addOption(T, TO, true, "run to step");
		addOption(S, STEP, true, "run single step");
		addOption(P, PULL, false, "pull docker images of steps");
		addOption(I, INTERACTIVE, false, "run docker steps with interactive flag");
		addOption(D, DOCKERIMAGE, true, "specify pipeline runner docker image");
		addOption(H, HELP, false, "show commandline usage");
		addOption(C, CLUSTER, true, "ip address of K8s cluster to upload/run to (run locally if not specified)");
		addOption(N, NAMESPACE, true, "namespace in K8s cluster (only valid in combination with run)");
		addOption(M, MARKDOWN, true, "path of markdown file holding pipeline definition (relative to base dir)");
		addOption(V, VARIABLES, true, "variable definitions (format key1=value1;key2=value2...)");
		addOption(E, EXTENDED, false, "use extended syntax (include, variables, macros)");
		addOption(G, GROUP, false, "run groups of combined steps together in one pod");
		addOption(A, ARCHITECTURE, true, "specifies the target platform for the docker images (e.g. linux/amd64)");
		addOption(PS, PULLSECRET, true, "pull secret for the run-pipeline docker image (not used for pulling step images)");
		addOption(J, JWT, true, "jwt token (for accessing kubernetes cluster with authentication)");
		addOption(O, OPERATION, true, "mode of operation, one of: RUN_LOCALLY (execute on local machine), PUSH (push to cluster), PUSH_AND_RUN (execute once on cluster), PUSH_AND_SCHEDULE (install for regular execution on cluster)");
		addOptionWithOptionalArg(U, USER_INTERFACE, "enable status image generation, use optional argument to provide url of pipeline viewer");
		addOptionWithOptionalArg(R, RUN, "run on cluster, argument specifies the task id, requires --cluster ");
	}

	public String getWorkdir() {
		return workdir;
	}

	private String addSeparatorCharAtEndIfNotPresent(final String workdir) {
		int posSlash = workdir.indexOf(SLASH);
		int posBackslash = workdir.indexOf(BACKSLASH);
		boolean hasSlash = posSlash != -1;
		boolean hasBackslash = posBackslash != -1;
		if (!hasSlash && !hasBackslash) {
			fail("not a valid absolute path: "+workdir);
		}
		char sep = hasSlash ? SLASH : BACKSLASH;
		return (workdir.charAt(workdir.length()-1) != sep) ? workdir + sep : workdir;
	}

	public boolean hasFrom() {
		return from != null;
	}

	public String getFrom() {
		return from;
	}

	public boolean hasTo() {
		return to != null;
	}

	public String getTo() {
		return to;
	}

	public String getCluster() {
		return cluster;
	}

	public String getNamespace() {
		return namespace;
	}

	public String getMarkdown() {
		return markdown;
	}

	public Map<String, String> getVariables() {
		return variables;
	}

	public boolean isRunSpecified() {
		return isRunSpecified;
	}

	public String getTaskId() {
		return taskId;
	}

	public boolean getPull() { return pull; }

	public boolean getExtendedSyntax() { return extendedSyntax; }

	public boolean getGroupCombined() { return groupCombined; }

	public boolean isImageEnabled() { return imageEnabled; }

	public String getPipelineViewerUrl() { return pipelineViewerUrl; }

	public boolean getInteractive() { return interactive; }

	public String getPullsecret() { return pullSecret; }

	public String getPlatform() { return platform; }

	public String getJwt() { return jwt; }

	public OperationMode getOperationMode() {
		return operation;
	}
}
