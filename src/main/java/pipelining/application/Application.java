package pipelining.application;

import pipelining.logging.Log;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static pipelining.logging.Log.debug;

public class Application {

	private static final Application APPLICATION = new Application();
	private static final int DEFAULT_FAILURE_CODE = -1;
	private static final boolean RUNS_IN_K8s = System.getenv("KUBERNETES_SERVICE_HOST") != null;
	private static final boolean RUNS_IN_DOCKER = RUNS_IN_K8s || determineRunningInsideDocker();

	// debug is set globally for the application
	private boolean debug = false;

	// trace may be set depending on some context, but by default it is not depending on context
	private ConextDependentFlag trace = new NoContextFlag();

	// use context supplier to determine current context, default is dummy
	private Supplier contextSupplier = () -> null;

	private boolean production = false;
	private FailureHandler failureHandler = null;

	public static void setDebug(boolean debugEnabled) {
		APPLICATION.debug = debugEnabled;
	}

	public static boolean isDebugEnabled() {
		return APPLICATION.debug;
	}

	public static <C> void setContextDependentTrace(ConextDependentFlag<C> traceFlag, Supplier<C> contextProvider) {
		APPLICATION.trace = traceFlag;
		APPLICATION.contextSupplier = contextProvider;
	}

	public static void setTrace(boolean traceEnabled) {
		APPLICATION.trace.set(APPLICATION.contextSupplier.get(), traceEnabled);
	}

	public static boolean isTraceEnabled() {
		return APPLICATION.trace.isSet(APPLICATION.contextSupplier.get());
	}

	public static void setProduction(boolean inProduction) {
		APPLICATION.production = inProduction;
	}

	public static void setFailureHandler(FailureHandler failureHandler) {
		APPLICATION.failureHandler = failureHandler;
	}

	public static boolean isInProduction() {
		return APPLICATION.production;
	}

	public static boolean isRunningInKubernetes() {
		return RUNS_IN_K8s;
	}

	public static boolean isRunningInDocker() {
		return RUNS_IN_DOCKER;
	}

	public static <T> T fail(final String message) {
		fail(message, DEFAULT_FAILURE_CODE);
		return null;
	}

	public static void fail(final String message, final int failureCode) {
		APPLICATION.failWithCode(message, failureCode);
	}

	public void failWithCode(String message, int failureCode) {
		if (failureHandler == null) {
			//Log.error("Critical failure: {}, no failure handler installed --> terminating VM.", message);
			//System.exit(failureCode);
			Log.error("Critical failure: "+message);
			throw new RuntimeException(message);
		} else {
			failureHandler.fail(message, failureCode);
		}
	}

	private static boolean determineRunningInsideDocker() {
		boolean res = checkDocker();
		Log.debug("Running inside docker container: {}", res);
		return res;
	}

	private static boolean checkDocker() {
		return checkDocker1() || checkDocker2();
	}

	private static boolean checkDocker1() {
		try (Stream<String> stream = Files.lines(Path.of("/proc/1/cgroup"))) {
			return stream.anyMatch(line -> line.contains("/docker") || line.contains("/ecs"));
		} catch (IOException e) {
			return false;
		}
	}

	private static boolean checkDocker2() {
		// see https://stackoverflow.com/questions/23513045/how-to-check-if-a-process-is-running-inside-docker-container/25518538#25518538
		return new File("/.dockerenv").exists();
	}

}
