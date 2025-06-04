package pipelining.util;

import pipelining.logging.Log;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
import java.util.regex.Pattern;

import static pipelining.logging.Log.debug;
import static pipelining.logging.Log.log;

public class ExternalProcess {

	// key of variable that stores anv settings MAP (env settings will be saved as value in comma separated form: k1=v1,k2=v2,..
	public static final String ENV_MAP_KEY = "ENV_VARIABLES_MAP";

	private static final long INITIAL_TIMEOUT = 15*60L; // 15 minutes
	private static final long DESTROY_SLEEP = 1000;
	private static final long RUNTIME_WARN = 10;

	private final Map<String, String> envVariables;
	private final List<String> output = new ArrayList<>();
	private final List<String> warnings = new ArrayList<>();
	private final List<String> errors = new ArrayList<>();
	private final List<Pattern> acceptedErrorLines = new ArrayList<>();
	private final List<Pattern> warningLines = new ArrayList<>();
	private final List<Pattern> errorOutputLines = new ArrayList<>();
	private Pattern successMarker;
	private boolean allowSuccessMarkerMissing;
	private boolean successMarkerFound;
	private ProcessBuilder pb;
	private Predicate<String> lineFilter;
	private String command;
	private String loggedCommand;
	private boolean nonZeroExitValueAllowed;
	private long timeout = INITIAL_TIMEOUT;
	private boolean hasTimedOut = false;


	public ExternalProcess(Map<String, String> variables) {
		this.envVariables = new LinkedHashMap<>();
		String envVarsEncoded = variables == null ? null : variables.get(ENV_MAP_KEY);
		if (envVarsEncoded != null) {
			for (String kvs : envVarsEncoded.split(",")) {
				String[] kva = kvs.split("=");
				if (kva.length != 2) {
					error("Value of variable "+ENV_MAP_KEY+" is not a comma separated list of strings of form k=v: "+kvs);
				}
				envVariables.put(kva[0], kva[1]);
			}
		}
	}

	public ExternalProcess command(List<String> command) {
		return command(command.toArray(new String[0]));
	}

	public ExternalProcess command(String command, List<String> arguments) {
		List<String> commandline = new ArrayList<>();
		commandline.add(command);
		commandline.addAll(arguments);
		return command(commandline);
	}

	public ExternalProcess command(String... command) {
		this.pb = new ProcessBuilder(command);
		this.pb.environment().putAll(envVariables);
		this.successMarker = null;
		this.command = joinWithSpaces(command);
		this.loggedCommand = this.command;
		return this;
	}

	public ExternalProcess loggedCommand(String... loggedCommand) {
		this.loggedCommand = joinWithSpaces(loggedCommand);
		return this;
	}

	@Override
	public String toString() {
		return command;
	}

	private static String joinWithSpaces(String[] strings) {
		return String.join(" ", List.of(strings));
	}

	public ExternalProcess dir(String directory) {
		this.pb.directory(new File(directory));
		return this;
	}

	public ExternalProcess dir(Path path) {
		this.pb.directory(path.toFile());
		return this;
	}

	public ExternalProcess lineFilter(Predicate<String> lineFilter) {
		this.lineFilter = lineFilter;
		return this;
	}

	public ExternalProcess execute() {
		successMarkerFound = false;
		try {
			tryExecute();
		} catch (IOException e) {
			error("IOException occurred: "+e);
		} catch (InterruptedException e) {
			warn("Process was interrupted");
			throw new RuntimeException("interrupted", e);
		}
		if (!warnings.isEmpty()) {
			warn("There were "+warnings.size()+" warning lines");
		}
		if (!errors.isEmpty()) {
			warn("There were "+errors.size()+" error lines");
		}
		if ((successMarker != null) && !successMarkerFound && !allowSuccessMarkerMissing) {
			error("Success marker '"+successMarker+"' was not found in output.");				
		}
		return this;
	}

	private void tryExecute() throws IOException, InterruptedException {
		debug("COMMAND> "+loggedCommand);
		long timestamp = System.currentTimeMillis();
		Process proc = pb.start();
		OutputReader outReader = createReader(proc.getInputStream(), false);
		OutputReader errReader = createReader(proc.getErrorStream(), true);
		log("Process started");
		proc.waitFor(timeout, TimeUnit.SECONDS);
		log("Process ended");
		long seconds = (System.currentTimeMillis() - timestamp + 500)/1000;
		String runtimeMessage = "(was running for "+seconds+" seconds)";
		while (proc.isAlive()) {
			hasTimedOut = true;
			warn("Process not finished after maximal timeout. Destroying it");
			proc.destroyForcibly();
			Thread.sleep(DESTROY_SLEEP);
		}
		outReader.stop();
		errReader.stop();
		if (proc.exitValue() != 0) {
			if (nonZeroExitValueAllowed) {
				warn("Exit value: "+proc.exitValue());
			} else {
				error("Exit value: "+proc.exitValue());
			}
		}					
	}

	private OutputReader createReader(InputStream inputStream, boolean error) {
		return new OutputReader(inputStream) {
			@Override
			public void processLine(String line) {
				processOutputLine(line, error);
			}
		};
	}

	protected void processOutputLine(String line, boolean error) {
		if ((lineFilter != null) && !lineFilter.test(line)) {
			debug("IGNORE> "+line);
		} else if ((successMarker != null) && matches(line, successMarker)) {
			successMarkerFound = true;
			output.add(line);
			log("SUCCESS> "+line);
		} else if (error && matches(line, acceptedErrorLines)) {
			output.add(line);
			log("ACCEPT> "+line);
		} else if (matches(line, warningLines)) {
			warnings.add(line);
			warn("WARN> "+line);
		} else if (error) {
			errors.add(line);
			error("ERROR> "+line);
		} else if (matches(line, errorOutputLines)) {
			errors.add(line);
			error("ERROR> "+line);
		} else {
			output.add(line);
			log("OUTPUT> "+line);
		}
	}

	private void error(String string) {
		Log.error(string);
	}

	private void warn(String string) {
		log(string);
	}

	private boolean matches(String line, List<Pattern> patterns) {
		if (line.isBlank()) {
			return true;
		}
		for (Pattern pattern : patterns) {
			if (matches(line, pattern)) {
				return true;
			}
		}
		return false;
	}

	private boolean matches(String line, Pattern pattern) {
		return pattern.matcher(line).matches();
	}

	public ExternalProcess successMarker(String pattern) {
		this.successMarker = createMatcher(pattern);
		return noError(pattern);
	}

	public ExternalProcess allowSuccessMarkerMissing() {
		this.allowSuccessMarkerMissing = true;
		return this;
	}

	public boolean successMarkerIsOptional() {
		return allowSuccessMarkerMissing;
	}

	public ExternalProcess errorMarker(String pattern) {
		errorOutputLines.add(createMatcher(pattern));
		return this;
	}

	public ExternalProcess warning(String pattern) {
		warningLines.add(createMatcher(pattern));
		return this;
	}

	public ExternalProcess noError(String pattern) {
		acceptedErrorLines.add(createMatcher(pattern));
		return this;
	}

	private Pattern createMatcher(String pattern) {
		if (!pattern.contains(".*")) {
			pattern += ".*";
		}
		return Pattern.compile(pattern);
	}

	public ExternalProcess warning(List<String> patterns) {
		patterns.forEach(this::warning);
		return this;
	}

	public ExternalProcess noError(List<String> patterns) {
		patterns.forEach(this::noError);
		return this;
	}

	public List<String> getErrors() {
		return errors;
	}

	public List<String> getWarnings() {
		return warnings;
	}

	public List<String> getOutput() {
		return output;
	}

	public boolean hasSucceeded() {
		return errors.isEmpty();
	}

	public boolean hasFailed() {
		return !hasSucceeded();
	}

	public boolean successMarkerFound() {
		return successMarkerFound;
	}

	public void nonZeroExitValueAllowed() {
		nonZeroExitValueAllowed = true;
	}

	public ExternalProcess timeoutSeconds(int numSecs) {
		timeout = numSecs;
		return this;
	}

	public boolean hasTimedOut() {
		return hasTimedOut;
	}

	public boolean hasCommand() {
		return command != null;
	}
}
