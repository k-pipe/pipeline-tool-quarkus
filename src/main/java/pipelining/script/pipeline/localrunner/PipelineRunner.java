package pipelining.script.pipeline.localrunner;

import pipelining.script.pipeline.PipelineCreator;
import pipelining.script.pipeline.PipelineMarkdownWithSettings;
import pipelining.script.pipeline.pipeline_v2.PipelineV2;
import pipelining.ui.UIHandler;
import pipelining.http.Http;
import pipelining.job.DockerImage;
import pipelining.job.implementation.DockerImageRunner;
import pipelining.pipeline.definition.PipelineConnector;
import pipelining.pipeline.definition.PipelineStep;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.*;

import static pipelining.application.Application.fail;
import static pipelining.logging.Log.*;

public class PipelineRunner {

	public static final String WORKDIR =  "/workdir";

	private static final String INPUT_DIR = "input";
	private static final String OUTPUT_DIR = "output";

	private static final String CREDENTIALS = "docker.credentials";
	private static final String JWT_TOKEN = "token.jwt";
	private static final String IMAGE_FILE = "status.png";

	protected final RunnerCLOptions options;
	private final String workdirInHost; // cannot be a Path, since it might be on different OS!
	private final Path workdirInDocker;
	private final PipelineV2 pipeline;
	private final Map<PipelineStep, Integer> stepNumber;
	private final UIHandler uiHandler;
	private final List<String> pumlLines;
	private Boolean loggedIn;

	public PipelineRunner(RunnerCLOptions options) {
		log("Executing com.kneissler.pipeline.PipelineRunner");
		this.options = options;
		this.workdirInHost = options.getWorkdir();
		log("Pipeline will be executed on local machine in working directory {}", workdirInHost);
		this.workdirInDocker = Path.of(WORKDIR);
		PipelineMarkdownWithSettings markdown = parseMarkdown(resolveInDocker(options.getMarkdown()));
		this.pipeline = markdown.createPipeline(null);
		this.pumlLines = markdown.getPipelineUMLLines();
		this.loggedIn = null;
		this.stepNumber = new LinkedHashMap<>();
		int count = 0;
		for (PipelineStep step : pipeline.determineSequence()) {
			this.stepNumber.put(step, ++count);
		}
		String namespace = options.getNamespace();
		if (namespace == null) {
			namespace = "Unspecified";
		}
		this.uiHandler = new UIHandler(
				options.isImageEnabled() ? resolveInDocker(IMAGE_FILE) : null,
				options.getPipelineViewerUrl(),
				markdown.getUmlWithObjects(),
				namespace,
				pipeline.getName());
	}

	private PipelineMarkdownWithSettings parseMarkdown(final Path markdownPath) {
		return new PipelineMarkdownWithSettings(workdirInDocker, workdirInDocker.resolve(markdownPath),
				options.getExtendedSyntax(),
				options.getVariables());
	}


	private String uploadPipeline(PipelineV2 pipeline, List<String> pumlLines, String jwtToken) {
		String urlPrefix = "http://"+options.getCluster()+"/service/"+options.getNamespace()+"/";
		return new PipelineCreator(pipeline, pumlLines, urlPrefix, jwtToken == null ? "" : "?jwt="+jwtToken, options.getGroupCombined()).create(null);
	}

	public void runLocally() {
		log("Pipeline name: {}", pipeline.getName());
		log("Pipeline description: {}", pipeline.getDescription());
		long allstart = System.currentTimeMillis();
		List<PipelineStep> stepSequence = pipeline.determineSequence();
		int firstStep = options.hasFrom() ? findStep(options.getFrom()) : 1;
		int lastStep = options.hasTo() ? findStep(options.getTo()) : stepNumber.size();
		log("Running pipeline steps {} to {}", firstStep, lastStep);
		uiHandler.beforeRun(stepNumber);
		try {
			for (int stepNum = firstStep; stepNum <= lastStep; stepNum++) {
				PipelineStep step = stepSequence.get(stepNum - 1);
				long stepstart = System.currentTimeMillis();
				log("---------------------------------------------------------------------------");
				log("Running step {}: {}", stepNumber.get(step), step.getId());
				uiHandler.beforeStep(step);
				runStep(step);
				uiHandler.afterStep(resolveInDocker(dirName(step), OUTPUT_DIR), step);
				log("Terminated step {} after {} seconds", stepNumber.get(step), getSeconds(stepstart));
			}
			uiHandler.afterRun(true);
			log("---------------------------------------------------------------------------");
			log("Terminated running {} pipeline steps of {} in {} seconds.", stepNumber.size(), pipeline.getDescription(), getSeconds(allstart));
		} catch (RuntimeException e) {
			uiHandler.afterRun(false);
			exception(e);
			log("---------------------------------------------------------------------------");
			log("Failed after {} pipeline steps of {} in {} seconds.", stepNumber.size(), pipeline.getDescription(), getSeconds(allstart));
		}
	}

	private int findStep(final String nameOrNumber) {
		for (Map.Entry<PipelineStep, Integer> e : stepNumber.entrySet()) {
			if (e.getKey().getId().equals(nameOrNumber)) {
				return e.getValue();
			}
		}
		return Integer.parseInt(nameOrNumber);
	}

	private long getSeconds(final long start) {
		return Math.round((System.currentTimeMillis()-start)*0.001);
	}

	private void runStep(final PipelineStep step) {
		cleanDirectories(step);
		step.getConfigInputs().forEach((name, data) -> writeData(step, name, data));
		step.getInputs().forEach((name, connector) -> copyInput(step, name, connector));
		if (!runJob(step)) {
			fail("Execution of step "+step.getId()+" failed.");
		}
	}

	private void cleanDirectories(final PipelineStep step) {
		//removeRecursively(resolveInDocker(dirName(step), INPUT_DIR).toFile());
		removeRecursively(resolveInDocker(dirName(step), OUTPUT_DIR).toFile());
	}

	private void removeRecursively(final File dir) {
		log("Removing files in {}", dir);
		final File[] files = dir.listFiles();
		if (files != null) {
			for (File file : files) {
				if (file.isDirectory()) {
					removeRecursively(file);
				} else {
					onException(() -> Files.delete(file.toPath())).fail("Could not delete file " + file);
				}
			}
		}
	}

	private boolean runJob(final PipelineStep step) {
		DockerImageRunner runner = getRunner(step.getDockerImage());
		if (options.getPull() & (loggedIn == null)) {
			Path cred = resolveInDocker(CREDENTIALS);
			loggedIn = cred.toFile().exists() && login(runner, cred);
		}
		if (options.getPull() && !runner.pull()) {
			fail("Could not pull image, stopping pipeline execution");
		}
		return runner.run(resolveOnHost(dirName(step)));
	}

	protected DockerImageRunner getRunner(DockerImage dockerImage) {
		return new DockerImageRunner(dockerImage, Collections.emptyList(), true, options.getInteractive(), null, options.getPlatform(), null);
	}

	private boolean login(final DockerImageRunner runner, final Path credentials) {
		List<String> lines = onException(() -> Files.readAllLines(credentials)).fail("Could not load credentials from "+credentials);
		if (lines.size() != 2) {
			warn("Unexpected number of lines in credential file, expected 2, found "+lines.size());
			return false;
		}
		if (!runner.login(lines.get(0), lines.get(1))) {
			warn("Could not login to docker repository");
			return false;
		}
		return true;
	}

	private void writeData(final PipelineStep step, final String name, final byte[] data) {
		final Path target = resolveInDocker(dirName(step), INPUT_DIR, name);
		log("Writing {} bytes to {}", data.length, target);
		makePath(target);
		onException(() -> Files.write(target, data)).fail("Could not write file "+ target);
	}

	private void copyInput(final PipelineStep step, final String nameInTarget, final PipelineConnector connector) {
		String nameInSource = connector.getNameAtSource();
		final Path source = resolveInDocker(dirName(connector.getSource()), OUTPUT_DIR, nameInSource);
		final Path target = resolveInDocker(dirName(step), INPUT_DIR, nameInTarget);
		log("Copy {} bytes from {} to {}", source.toFile().length(), source, target);
		makePath(target);
		onException(() -> Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING))
				.fail("Could not copy file "+source+" to "+target);
	}

	private String dirName(final PipelineStep step) {
		int strlen = Integer.toString(pipeline.getSteps().size()).length();
		StringBuilder numStr = new StringBuilder(Integer.toString(stepNumber.get(step)));
		while (numStr.length() < strlen) {
			numStr.insert(0, "0");
		}
		return numStr+"-"+step.getId();
	}

	private String resolveOnHost(final String... names) {
		char sep = workdirInHost.charAt(workdirInHost.length()-1);
		StringBuilder sb = new StringBuilder();
		sb.append(workdirInHost);
		for (String name : names) {
			if (sb.charAt(sb.length()-1) != sep) {
				sb.append(sep);
			}
			sb.append(name);
		}
		return sb.toString();
	}

	private Path resolveInDocker(final String... names) {
		Path res = workdirInDocker;
		for (String name : names) {
			res = res.resolve(name);
		}
		return res;
	}

	private void makePath(final Path path) {
		File dir= path.getParent().toFile();
		if (!dir.exists()) {
			log("Creating directory "+dir);
			if (!dir.mkdirs()) {
				fail("Could not create directory "+dir);
			}
		}
	}

	public void runOrUpload() {
		OperationMode mode = options.getOperationMode();
		if (mode == null) {
			mode = guessOperationMode(options.getCluster() != null, options.getNamespace() != null, options.isRunSpecified);
			log("Variable operation was not set, assuming desired opeeration mode: "+mode);
		}
		log("Operating pipeline tool in mode: "+mode);
		if (mode.equals(OperationMode.RUN_LOCALLY)) {
			runLocally();
		} else {
			String jwtToken = options.getJwt();
			Path tokenFile = resolveInDocker(JWT_TOKEN);
			if ((jwtToken == null) && tokenFile.toFile().exists()) {
				jwtToken = onException(() -> Files.readString(tokenFile)).fail("Could not load jwt token from "+JWT_TOKEN);
				log("Read JWT token from file "+tokenFile);
			}
			String scriptId = uploadPipeline(pipeline, pumlLines, jwtToken);
			if (mode.equals(OperationMode.PUSH_AND_RUN)) {
				triggerRun(jwtToken, scriptId);
			} else if (mode.equals(OperationMode.PUSH_AND_SCHEDULE)) {
				scheduleExecution();
			}
		}
	}

	private void triggerRun(String jwtToken, String scriptId) {
		String taskId = options.getTaskId();
		log("Running pipeline "+ scriptId
				+" on cluster "+options.getCluster()
				+" in namespace "+options.getNamespace()
				+(taskId == null ? "" : " with taskId "+taskId));
		String url = "http://"+options.getCluster()
				+"/service/"
				+options.getNamespace()
				+"/script/api/script/"
				+ scriptId
				+"/start?namespace="+options.getNamespace()
				+(taskId == null ? "" : "&task="+taskId)
				+(jwtToken == null ? "" : "&jwt="+ jwtToken);
		log("Calling url "+url);
		final Optional<String> response = Http.put(url, null);
		if (response.isEmpty()) {
			fail("Could not start script");
		}
		else {
			log("Response from script service: "+response.get());
		}
	}

	private OperationMode guessOperationMode(boolean clusterSet, boolean nameSpaceSet, boolean isRunSpecified) {
		return isRunSpecified ? OperationMode.PUSH_AND_RUN
				              : nameSpaceSet ? OperationMode.PUSH
										     : OperationMode.RUN_LOCALLY;
	}

	private void scheduleExecution() {
		log("Scheduling of regular execution on cluster not implemented, yet)");
	}

}
