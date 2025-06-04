package pipelining.script.pipeline;

import pipelining.pipeline.PipelineYaml;
import pipelining.script.pipeline.pipeline_v2.PipelineV2;
import pipelining.script.pipeline.pipeline_v2.StepGroup;
import pipelining.http.Http;
import pipelining.markdown.pipeline.v1.PipelineMarkdown;
import pipelining.pipeline.definition.PipelineConnector;
import pipelining.pipeline.definition.PipelineStep;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class PipelineCreator {

	private static final String CONFIG_TYPE = "config";
	private static final String PIPE_TYPE = "pipe";
	public static final String BATCHER = "batcher";
	public static final String PROCESS = "process";
	public static final String IO_PIPELINE = "io";
	public static final String PIPELINE = "pipeline";
	private static final String CONFIG = "config";
	private static final String CONNECTOR = "connector";

	private static final String URL_PREFIX = "http://";
	private static final String URL_INFIX = "-service:8080/";
	private static final String CONFIG_URL_BASE = URL_PREFIX+"resource"+URL_INFIX+"/pipeline/";

	private static final String CONFIG_PIPELNE_RESOURCE_INFIX = "resource/pipeline/";
	private static final String SCRIPT_ENDPOINT_INFIX = "script/api/script/";

	private static final String STRING_TYPE = "String";

	// variable that obtains the resource service url of the batches list
	private static final String URL_BASE_VAR = "urlbase";

	// variable to construct the resource service url for the processing scripts
	private static final String SCRIPT_URL_VAR = "scriptUrl";

	// loop variable holding the name of the next batch to be processed
	private static final String NEXT_BATCH_VAR = "nextBatch";

	// variable to construct the script service url for starting the processing scripts
	private static final String START_URL_VAR = "startUrl";

	// variable to pass batch as argument to processBatch script
	private static final String BATCH_PARAM_VAR = "batchParam";

	// constant: infix for constructing script start url
	private static final String SCRIPT_URL_PREFIX_VAR = "scriptUrlPrefix";
	private static final String SCRIPT_URL_PREFIX = "http://script-service:8080/api/script/";

	// constant: infix for constructing script start url
	private static final String SCRIPT_URL_INFIX_VAR = "scripturlInfix";
	private static final String SCRIPT_URL_PARAMETER_INFIX = "&documents=";

	// constant: filename of the list of batches in start step ouptut
	private static final String BATCHES_LIST_FILENAME_VAR = "batchListFilename";
	private static final String BATCHES_LIST_FILENAME = "batches.txt";

	private final PipelineV2 pipeline;
	private final Map<PipelineConnector, String> connectorVariables;
	private final String urlPrefix;
	private final String jwtSuffix;
	private final boolean groupCombined;
	private final List<String> pumlLines;

	public PipelineCreator(final PipelineMarkdownWithSettings markdown,
			final String urlPrefix, final String jwtSuffix) {
		this(markdown.createPipeline(null), markdown.getPipelineUMLLines(), urlPrefix, jwtSuffix, false);
	}

	public PipelineCreator(final PipelineV2 pipeline, final List<String> pumlLines,
						   final String urlPrefix, final String jwtSuffix, boolean groupCombined) {
		this.pipeline = pipeline;
		this.urlPrefix = urlPrefix;
		this.jwtSuffix = jwtSuffix;
		this.connectorVariables = new LinkedHashMap<>();
		this.groupCombined = groupCombined;
		this.pumlLines = pumlLines;
		pipeline.getSteps().forEach(this::createConnectorVariables);
	}

	private void createConnectorVariables(final PipelineStep step) {
		step.getOutputs().forEach(connector -> connectorVariables.put(connector, connectorVariable(step, connector.getNameAtSource())));
		PipelineConnector startConnector = pipeline.getBatchingConnector();
		if (startConnector != null) {
			connectorVariables.put(startConnector, BATCH_PARAM_VAR);
		}
	}

	public String create(Map<String, String> namings, Map<String, String> variables) {
		//uploadPUML();
		uploadInputs();
		if (pipeline.isMultiBatch()) {
			final Collection<StepGroup> groups = pipeline.determineSequence(true, groupCombined);
			List<PipelineStep> steps = getSteps(groups);
			uploadScript(createProcessBatchScript(steps));
			return uploadScript(createBatcherScript(steps, namings, variables));
		}
		if (isIOPipeline()) {
			return uploadScript(createIOPipelineScript());
		}
		return createFullPipelineYaml(namings, variables);
	}

	private void uploadPUML() {
		String pumlUrl = pipelinePumlUrl();
		System.out.println("-------------------------------------------------");
		System.out.println("Uploading "+pumlLines.size()+" puml lines to "+pumlUrl);
		Http.put(pumlUrl, String.join("\n", pumlLines));
	}

	public static List<PipelineStep> getSteps(Collection<StepGroup> groups) {
		List<PipelineStep> res = new ArrayList<>();
		groups.forEach(g -> g.stepsStream().forEach(res::add));
		return res;
	}

	private boolean isIOPipeline() {
		if ((pipeline.getInput() == null) != (pipeline.getOutput() == null)) {
			throw new RuntimeException("IN/OUT must be set together");
		}
		return pipeline.getInput() != null;
	}

	private String createBatcherScript(final List<PipelineStep> allSteps, Map<String, String> naming, Map<String, String> variables) {
		PipelineYaml res = new PipelineYaml(naming, variables);
		res.setName(PIPELINE);
		res.setDescription(String.join(" ", pipeline.getDescription()));
		List<PipelineStep> steps = pipeline.determineUnbatchedSteps(allSteps);
		res.setSteps(steps);
		res.setBatchingSubPipline(pipeline.getBatchingConnector());
		return res.getString();

		/*
		Script res = createScript(BATCHER);
		res.initialize();
		res.setDescription(String.join(" ", pipeline.getDescription()));
		// only start step
		List<PipelineStep> steps = pipeline.determineUnbatchedSteps(allSteps);
		System.out.println("Un-Batched Steps: "+steps.stream().map(PipelineStep::getId).collect(Collectors.joining(", ")));
		res.setVariables(determineVariables(steps, true, false));
		res.getSteps().add(setConstantsStep(steps, true));
		steps.forEach(s -> res.getSteps().add(createJobStep(s)));
		// loop calling batch processing
		String batchesListVar = connectorVariables.get(pipeline.getBatchingConnector());
		res.getSteps().add(predefined("GetUrl", List.of(batchesListVar), List.of(URL_BASE_VAR), null, null));
		res.getSteps().add(predefined("SplitFirst", List.of(batchesListVar), List.of(NEXT_BATCH_VAR, batchesListVar), "+1", "+2"));
		res.getSteps().add(succeededStep());
		res.getSteps().add(predefined("ReplaceAll", List.of(URL_BASE_VAR, BATCHES_LIST_FILENAME_VAR, NEXT_BATCH_VAR), List.of(SCRIPT_URL_VAR), null, null));
		res.getSteps().add(predefined("Concatenate", List.of(SCRIPT_URL_PREFIX_VAR, NEXT_BATCH_VAR, SCRIPT_URL_INFIX_VAR, SCRIPT_URL_VAR), List.of(START_URL_VAR), null, null));
		//res.getSteps().add(predefined("Put", List.of(START_URL_VAR), List.of(), null, "-4"));
		res.getSteps().add(predefined("CallScript", List.of(START_URL_VAR), List.of(), null, "-4"));
		return res;
		 */
		//return "";
	}

	private String createIOPipelineScript() {
		/*
		Script res = createScript(IO_PIPELINE);
		res.initialize();
		res.setDescription("IO pipeline job for pipeline "+pipeline.getName());
		res.setArguments(List.of(BATCH_PARAM_VAR));
		Collection<StepGroup> groups = pipeline.determineSequence(false, groupCombined);
		List<PipelineStep> steps = getSteps(groups);
		res.setVariables(determineVariables(steps, false, true));
		res.getSteps().add(setConstantsStep(steps, false));
		steps.forEach(s -> res.getSteps().add(createJobStep(s)));
		res.getSteps().add(succeededStep());
		return res;
		 */
		return "";
	}

	private String createProcessBatchScript(final List<PipelineStep> allSteps) {
		/*
		Script res = createScript(PROCESS);
		res.initialize();
		res.setDescription("Batch processing job for pipeline "+pipeline.getName());
		res.setArguments(List.of(BATCH_PARAM_VAR));
		// all steps excluding start step
		List<PipelineStep> steps = pipeline.determineBatchedSteps(allSteps);
		System.out.println("Batched Steps: "+steps.stream().map(PipelineStep::getId).collect(Collectors.joining(", ")));
		res.setVariables(determineVariables(steps, false, true));
		res.getSteps().add(setConstantsStep(steps, false));
		steps.forEach(s -> res.getSteps().add(createJobStep(s)));
		res.getSteps().add(succeededStep());
		return res;
		 */
		return "";
	}

	private String createFullPipelineYaml(Map<String, String> naming, Map<String, String> variables) {
		PipelineYaml res = new PipelineYaml(naming, variables);
		res.setName(PIPELINE);
		res.setDescription(String.join(" ", pipeline.getDescription()));
		// all steps
		Collection<StepGroup> groups = pipeline.determineSequence(false, groupCombined);
		List<PipelineStep> steps = getSteps(groups);
		res.setSteps(steps);
		return res.getString();
	}

/*	private String createScript(final String scriptname) {
		return new Script(pipeline.getName().replaceAll("-", "").replaceAll("\\.","")+"~"+scriptname);
	}
*/
	private void uploadInputs() {
		pipeline.getSteps().forEach(this::uploadInputs);
	}

	private void uploadInputs(final PipelineStep pipelineStep) {
		pipelineStep.getConfigInputs().forEach((name, data) -> {
			if (!name.equals(PipelineMarkdown.CONFIG)) {
				uploadInput(pipelineStep, name, data);
			}
		});
	}

	private void uploadInput(final PipelineStep step, final String name, final byte[] data) {
		uploadData(configUrl_EXT(step, name), data);
	}

	private Map<String, String> determineVariables(Collection<PipelineStep> steps, boolean batcherScript, boolean processScript) {
		Map<String, String> res = new LinkedHashMap<>();
		steps.forEach(step -> addVariables(res, step));
		if (batcherScript) {
			res.put(SCRIPT_URL_PREFIX_VAR, STRING_TYPE);
			res.put(SCRIPT_URL_INFIX_VAR, STRING_TYPE);
			res.put(NEXT_BATCH_VAR, STRING_TYPE);
			res.put(URL_BASE_VAR, STRING_TYPE);
			res.put(SCRIPT_URL_VAR, STRING_TYPE);
			res.put(START_URL_VAR, STRING_TYPE);
			res.put(BATCH_PARAM_VAR, STRING_TYPE);
		}
		if (processScript) {
			res.put(BATCH_PARAM_VAR, STRING_TYPE);
		}
		return res;
	}

	private void addVariables(final Map<String, String> res, final PipelineStep step) {
		step.getConfigInputs().keySet().forEach(config -> res.put(configVariable(step, config), CONFIG_TYPE));
		step.getOutputs().forEach(connector -> res.put(connectorVariables.get(connector), PIPE_TYPE));
	}

/*	private ScriptStep setConstantsStep(List<PipelineStep> steps, final boolean batcherScript) {
		ScriptStep res = new ScriptStep();
		res.setType(StepType.INPUT);
		res.setName("setConstants");
		Input inputSpec = new Input();
		inputSpec.setTitle("Setting pipeline constants");
		inputSpec.setResources(new LinkedHashMap<>());
		steps.forEach(step -> addInputConstants(step, inputSpec));
		res.setInput(inputSpec);
		if (batcherScript) {
			inputSpec.setConstants(getBatcherScriptConstants());
		}
		return res;
	}
*/

	private Map<String, String> getBatcherScriptConstants() {
		Map<String, String> res = new LinkedHashMap<>();
		res.put(SCRIPT_URL_PREFIX_VAR, SCRIPT_URL_PREFIX+pipeline.getName()+"~"+PROCESS+"/start?task=");
		res.put(SCRIPT_URL_INFIX_VAR, SCRIPT_URL_PARAMETER_INFIX);
		res.put(BATCHES_LIST_FILENAME_VAR, BATCHES_LIST_FILENAME);
		return res;
	}

/*	private void addInputConstants(final PipelineStep step, final Input inputSpec) {
		step.getConfigInputs().keySet().forEach(name -> inputSpec.getResources().put(configVariable(step, name), configUrl(step, name)));
	}

	private ScriptStep createJobStep(final PipelineStep step) {
		ScriptStep res = new ScriptStep();
		res.setType(StepType.JOB);
		res.setName(step.getId());
		res.setJob(createJob(step));
		return res;
	}

	private JobInvocation createJob(final PipelineStep step) {
		JobInvocation res = new JobInvocation();
		res.setJobName(step.getId());
		PipelineStepSettings settings = pipeline.getStepSettings(step);
		res.setJobClass(settings.getJobClass());
		res.setCompanionImage(null);
		res.setRetention(settings.getRetentionMap());
		res.setTimeout(settings.getTimeoutSpecs());
		res.setResources(settings.getResourceLimits());
		DockerImage di = step.getDockerImage();
		ImageDefinition id = new ImageDefinition();
		id.setRepository(di.getRepository());
		id.setImage(di.getImageName());
		id.setTag((di.getVersionTag()));
		res.setMainImage(id);
		res.setInput(new LinkedHashMap<>());
		res.setOutput(new LinkedHashMap<>());
		addToMapConfig(step, res.getInput());
		addToMap(step.getInputs(), res.getInput());
		addToMap(step.getOutputs(), res.getOutput());
		res.setRequiresApproval(false);
		return res;
	}
*/
	private void addToMapConfig(final PipelineStep step, final Map<String, String> input) {
		step.getConfigInputs().keySet().forEach(name -> input.put(name, configVariable(step, name)));
	}

	private void addToMap(final Map<String, PipelineConnector> connectors, final Map<String, String> ioMap) {
		connectors.forEach((name, connector) -> ioMap.put(name, connectorVariables.get(connector)));
	}

	private void addToMap(final List<PipelineConnector> connectors, final Map<String, String> ioMap) {
		connectors.forEach(connector -> ioMap.put(connector.getNameAtSource(), connectorVariables.get(connector)));
	}

	private String configVariable(final PipelineStep step, final String name) {
		return CONFIG+"-"+step.getId()+"-"+name;
	}

	private String connectorVariable(final PipelineStep step, final String name) {
		return CONNECTOR+"-"+step.getId()+"-"+name;
	}

	private String configUrl_EXT(final PipelineStep step, final String configName) {
		return urlPrefix+CONFIG_PIPELNE_RESOURCE_INFIX+pipeline.getName()+"/config/"+step.getId()+"/"+configName+jwtSuffix;
	}

	private String pipelinePumlUrl() {
		return urlPrefix+CONFIG_PIPELNE_RESOURCE_INFIX+pipeline.getName()+"/config/pipeline.puml"+jwtSuffix;
	}

	private String configUrl(final PipelineStep step, final String configName) {
		return CONFIG_URL_BASE+pipeline.getName()+"/config/"+step.getId()+"/"+configName;
	}

	private String uploadScript(String script) {
		System.out.println("-------------------------------------------------");
		String scriptUrl = urlPrefix+ SCRIPT_ENDPOINT_INFIX;
		try {
			Files.writeString(Path.of("pipeline.yaml"), script);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		/*
		System.out.println("Sending script "+script.getScriptID()+" to "+scriptUrl);
		Http.put(scriptUrl+jwtSuffix, Json.toString(script));
		System.out.println("Done.");
		return script.getScriptID();
		 */
		return null;
	}

	private void uploadData(final String configUrl, final byte[] data) {
		System.out.println("-------------------------------------------------");
		System.out.println("Uploading "+data.length+" bytes to "+configUrl);
		Http.streamUp(configUrl, new ByteArrayInputStream(data));
	}

}
