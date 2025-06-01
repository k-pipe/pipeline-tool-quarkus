package com.kneissler.job.client;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.kneissler.job.specification.JobDefinition;
import com.kneissler.job.status.*;
import org.jkube.http.Http;
import org.jkube.json.Json;
import org.jkube.logging.Log;
import org.jkube.util.Expect;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

import static org.jkube.logging.Log.*;

public class JobClient {

	private static final String RESOURCE_SERVICE_URL_PREFIX = "http://resource-service.";
	private static final String RESOURCE_SERVICE_URL_INFIX = ":8080";

	private static final String JOB_RUNNER_URL_PREFIX = "http://job-runner-service.";
	private static final String JOB_RUNNER_URL_INFIX = ":8080/wakeup";


	private static final String NEW_JOBS = "/jobs/new/";
	private static final String RESTART_JOBS = "/jobs/restart/";

	public static final String JOB_STATE = "/state";
	public static final String JOB_RESULT = "/result";
	public static final String JOB_FAILURE_REASON = "/failure";
	public static final String JOB_DESCRIPTION = "/job.json";
	private static final String JOB_RESOURCE_LOG = "/resources.log";
	private static final String JOB_RESOURCE_SETTINGS = "/resources.json";
	public static final String OUTPUT_DIR = "/output/";
	private static final String JOB_INFIX = "/job/";
	private static final String OUTPUT_INFIX = "/output/";

	private static final String RESOURCE_SERVICE_NOT_FOUND_STRING = "404 Not Found";

	private static final String METADATA_FOLDER = "metadata/";
	private static final String PUML = "pipeline.puml";
	private static final String STEPS_FOLDER = "steps/";
	private static final String STARTED_FOLDER = "started/";
	private static final String RESTARTED_FOLDER = "restarted/";
	private static final String SUCCEEDED_FOLDER = "succeeded/";
	private static final String FAILED_FOLDER = "failed/";

	private static final String PIPES_FOLDER = "pipes/";

	public static final String TILDE = "~";
	private static final String CONFIG_JSON = "config.json";
	private static final String DIAGRAM_FILE = "pipeline.png";
	private static final String NUM_BATCHES = "numbatches";

	private final String namespace;

	public JobClient(String namespace)  {
		this.namespace = namespace;
	}

	public List<String> listNewJobs() {
		return listFiles(NEW_JOBS);
	}

	public List<String> listToBeRestartedJobs() {
		return listFiles(RESTART_JOBS);
	}

	private static List<String> listResources(String url) throws IOException {
		List<String> result = new ArrayList<>();
		Optional<String> response = Http.get(url);
		if (response.isPresent()) {
			result.addAll(Arrays.asList(response.get().split("\n")));
		} else {
			debug("Could not get directory");
		}
		return result;
	}

	public List<String> listFiles(String relPath) {
		return listFilesFrom(getResourceUrl(namespace)+relPath);
	}

	private static String getResourceUrl(String namespace) {
		return RESOURCE_SERVICE_URL_PREFIX+namespace+RESOURCE_SERVICE_URL_INFIX;
	}

	private static String getJobRunnerUrl(String namespace) {
		return JOB_RUNNER_URL_PREFIX+namespace+JOB_RUNNER_URL_INFIX;
	}

	public List<String> listFilesFrom(String url) {
		// NO RETRY, return empty list if failed

		// TODO clean up
		List<String> lines;
		try {
			lines = listResources(url);
		} catch (IOException e) {
			e.printStackTrace();
			return Collections.emptyList();
		}
		Listing listing;
		String json = String.join("\n", lines);
		if (json.isBlank()) {
			return Collections.emptyList();
		}
		if (json.equals(RESOURCE_SERVICE_NOT_FOUND_STRING)) {
			return Collections.emptyList();
		}
		//try {
			throw new RuntimeException("not implemented");
			//listing = new ObjectMapper().readValue(String.join("\n", lines), Listing.class);
		//} catch (JsonProcessingException e) {
		//	e.printStackTrace();
		//	return Collections.emptyList();
		//}
		//if (listing.get().isEmpty()) {
		//	// do not retry, simply return empty list on failure
		//	warn("Received no files from "+url);
		//	return Collections.emptyList();
		//}
		//return listing.get();
	}

	public static String getNewJobUrl(String namespace, String jobId) {
		return getResourceUrl(namespace) + NEW_JOBS + jobId;
	}

	public Optional<String> readNewJobUrl(String jobId) {
		return Http.get(getNewJobUrl(namespace, jobId));
	}

	public boolean addNewJob(JobDefinition jobDefinition) {
		boolean success = putString(getNewJobUrl(namespace, jobDefinition.getJobId()), getJobUrl(jobDefinition));
		log("Success put new job url: "+success);
		if (success) {
			log("Waking up job-runner");
			if (!putString(getJobRunnerUrl(namespace), "")) {
				log("Could not wakeup job-runner, if it was just scaled up now (see previous logs), that's okay.");
			}
		}
		return success;
	}

	public Optional<JobDefinition> readJobDefinition(String jobUrl) {
		return getJson(jobUrl + JOB_DESCRIPTION, JobDefinition.class);
	}

	public boolean writeJobDefinition(JobDefinition jobDefinition) {
		return putJson(getJobUrl(jobDefinition) + JOB_DESCRIPTION, jobDefinition);
	}

	public static String getOutputUrl(String namespace, String taskId, String jobId, String resourceName) {
		return constructJobUrl(namespace, taskId, jobId)+OUTPUT_INFIX+resourceName;
	}

	public static String getJobUrl(final JobDefinition jobDefinition) {
		return constructJobUrl(jobDefinition.getNamespace(), jobDefinition.getTaskId(), jobDefinition.getJobId());
	}

	private static String constructJobUrl(String namespace, String taskId, String jobId) {
		return constructTaskUrl(namespace, taskId)+jobId;
	}

	public static String constructTaskUrl(String namespace, String taskId) {
		return getResourceUrl(namespace)+JOB_INFIX+taskId.replaceAll("~", "/")+"/";
	}

	public boolean sendStatus(String jobUrl, JobState state) {
		return putString(jobUrl + JOB_STATE, state.toString());
	}

	public boolean sendStatus(JobDefinition jobDefinition, JobState state) {
		return sendStatus(getJobUrl(jobDefinition), state);
	}


	public List<String> getPuml(JobDefinition job) {
		String url = job.getInputResources().get(CONFIG_JSON);
		url = parentUrl(parentUrl(url)) + "/"+ PUML;
		Log.log("Getting puml from {}", url);
		return Http.get(url).map(s -> Arrays.asList(s.split("\n"))).orElse(null);
	}

	private String parentUrl(String url) {
		int pos = url.lastIndexOf('/');
		Expect.greater(pos, 0).elseFail("/ not found in url "+url);
		return url.substring(0, pos);
	}

	public void sendStepStarted(String taskUrl, String batchStepId) {
		Expect.isTrue(putString(taskUrl + METADATA_FOLDER + STEPS_FOLDER + STARTED_FOLDER + batchStepId, ""));
	}

	public void sendStepRestarted(String taskUrl, String batchStepId) {
		Expect.isTrue(putString(taskUrl + METADATA_FOLDER + STEPS_FOLDER + RESTARTED_FOLDER + batchStepId, ""));
	}

	public void sendStepSucceeded(String taskUrl, String batchStepId) {
		Expect.isTrue(putString(taskUrl + METADATA_FOLDER + STEPS_FOLDER + SUCCEEDED_FOLDER + batchStepId, ""));
	}

	public void sendStepFailed(String taskUrl, String batchStepId) {
		Expect.isTrue(putString(taskUrl + METADATA_FOLDER + STEPS_FOLDER + FAILED_FOLDER + batchStepId, ""));
	}

	public void sendNumBatches(String taskUrl, int numBatches) {
		Expect.isTrue(putString(taskUrl + METADATA_FOLDER + NUM_BATCHES, Integer.toString(numBatches)));
	}

	public int gendNumBatches(String taskUrl) {
		return Http
				.get(taskUrl + METADATA_FOLDER + NUM_BATCHES)
				.map(Integer::parseInt).orElse(1);
	}


	public List<StepItem> getStepsInState(String taskUrl, String stateFolder) {
		return listFilesFrom(taskUrl + METADATA_FOLDER + STEPS_FOLDER + stateFolder)
				.stream()
				.map(this::parseStepItem)
				.collect(Collectors.toList());
	}

	public boolean sendOutputSize(String taskUrl, String batchStepId, String pipeId, long size) {
		return putString(taskUrl + METADATA_FOLDER + PIPES_FOLDER + batchStepId + TILDE + pipeId + TILDE + size, "");
	}

	public List<PipeSizeItem> readOutputSizes(String taskUrl) {
		return listFilesFrom(taskUrl + METADATA_FOLDER + PIPES_FOLDER)
				.stream()
				.map(this::parsePipeSize)
				.collect(Collectors.toList());
	}

	private PipeSizeItem parsePipeSize(String name) {
		return new PipeSizeItem(name.split(TILDE));
	}

	private StepItem parseStepItem(String name) {
		return new StepItem(name.split(TILDE));
	}

	public List<String> getPipeSizes(String taskUrl) {
		return listFilesFrom(taskUrl + METADATA_FOLDER + PIPES_FOLDER);
	}

	public boolean sendResult(String jobUrl, JobState state) {
		return putString(jobUrl + JOB_STATE, state.toString());
	}

	public boolean deleteFromNewList(String jobId) {
		return Http.delete(getNewJobUrl(namespace, jobId));
	}

	public boolean deleteFromRestartList(String jobId) {
		return Http.delete(getRestartListURL(namespace)+jobId);
	}

	public Optional<JobState> getStatus(JobDefinition job) {
		log("Get Status: "+job.getJobId());
		Optional<String> state = Http.get(job.getJobUrl() + JOB_STATE);
		log("Got: "+(state.isPresent() ? state : "NotPresent"));
		if (state.isEmpty()) {
			warn("no state returned for job from "+job.getJobUrl());
			return Optional.empty();
		}
		try {
			return Optional.of(JobState.valueOf(state.get()));
		} catch (IllegalArgumentException e) {
			warn("unknown state "+state.get()+" returned for job from "+job.getJobUrl());
			return Optional.empty();
		}
	}

	public Optional<JobResult> getResult(JobDefinition job) {
		Optional<String> result = Http.get(job.getJobUrl() + JOB_RESULT);
		if (result.isEmpty()) {
			warn("no result returned for job from "+job.getJobUrl());
			return Optional.empty();
		}
		try {
			return Optional.of(JobResult.valueOf(result.get()));
		} catch (IllegalArgumentException e) {
			warn("unknown result "+result.get()+" returned for job from "+job.getJobUrl());
			return Optional.empty();
		}
	}

	public Optional<ResourceFailureLog> loadFailureLog(final String jobId) {
		return getJson(getRestartListURL(namespace)+jobId, ResourceFailureLog.class);
	}

	public static String getRestartListURL(String namespace) {
		return getResourceUrl(namespace)+RESTART_JOBS;
	}

	public boolean putIntoRestartList(final String restartUrl, final ResourceFailureLog log) {
		return putJson(restartUrl+log.getJobId(), log);
	}

	public boolean sendResult(final JobDefinition job, final JobResult result) {
		return putString(job.getJobUrl() + JOB_RESULT, result.toString());
	}

	public boolean sendFailureReason(final JobDefinition job, final FailureReason failureReason) {
		return putString(job.getJobUrl() + JOB_FAILURE_REASON, failureReason.toString());
	}

	public boolean sendResourceSettings(final JobDefinition job, final ResourceSettings settings) {
		return putJson(job.getJobUrl()+JOB_RESOURCE_SETTINGS, settings);
	}

	public Optional<ResourceSettings> readResourceSettings(JobDefinition job) {
		return getJson(job.getJobUrl() + JOB_RESOURCE_SETTINGS, ResourceSettings.class);
	}

	public boolean sendResourceLogs(final JobDefinition job, final ResourceLog log) {
		return putJson(job.getJobUrl()+JOB_RESOURCE_LOG, log);
	}

	public boolean sendTerminationSignal(final String terminatedUrl, final boolean success) {
		return putString(terminatedUrl, Boolean.toString(success));
	}

	public boolean downloadInput(String inputDir, JobDefinition job) {
		log("Getting Input Data");
		for (Map.Entry<String, String> e : job.getInputResources().entrySet()) {
			//store(e.getKey(), client.loadData(e.getValue()).get());
			if (!download(inputDir, e.getKey(), e.getValue())) {
				// if any input fails, stop
				return false;
			}
		}
		return true;
	}

	private boolean download(String inputDir, String filename, String dataUrl) {
		File file = new File(inputDir, filename);
		File dir = file.getParentFile();
		if ((dir != null) && !dir.exists()) {
			if (!dir.mkdirs()) {
				Log.error("Could not create directories "+dir);
				return false;
			}
		}
		Log.log("Downloading {} from {}", file, dataUrl);
		boolean res = Http.download(dataUrl, file.toPath());
		if (res) {
			Log.log("Successfully downloaded {} from {}", file, dataUrl);
		} else {
			warn("Downloading of {} from {} failed.", file, dataUrl);
		}
		return res;
	}

	public boolean uploadOutput(String dir, JobDefinition job) {
		log("Uploading output files");
		for (String file : new File(dir).list()) {
			log("Uploading "+file);
			String url = job.getJobUrl() + OUTPUT_DIR + file;
			if (!Http.upload(url, Path.of(dir+file))) {
				warn("Uploading of {} to {} failed.", dir+file, url);
				// if any input fails, stop
				return false;
			} else {
				log("Successfully uploaded {} to {}", dir+file, url);
			}
		}
		return true;
	}

	public void sendImage(String taskUrl, byte[] image) {
		String url = taskUrl+METADATA_FOLDER+DIAGRAM_FILE;
		Expect.isTrue(Http.streamUp(url, new ByteArrayInputStream(image))).elseFail("Could not upload image to "+url);
	}


	private boolean putJson(final String url, final Object json) {
		return putString(url, Json.toString(json));
	}

	private boolean putString(final String url, final String string) {
		return Http.put(url, string).isPresent();
	}

	private <T> Optional<T> getJson(String url, Class<T> jsonClass) {
		return Http.get(url).map(s -> Json.fromString(s, jsonClass));
	}

}
