package pipelining.job.implementation;

import pipelining.application.Application;
import pipelining.http.Http;
import pipelining.http.HttpSettings;
import pipelining.job.ExecutionResult;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import static pipelining.application.Retry.retryBoolean;
import static pipelining.job.implementation.JobClientConfigConstants.*;
import static pipelining.job.implementation.JobSettingsConstants.SETTING_NAMESPACE;
import static pipelining.logging.Log.*;

public class JobClient {
	private final Map<String, String> configuration;
	private final Map<String, String> previousJobStates = new LinkedHashMap<>();
	private final HttpSettings httpSettings;

	public JobClient(final Map<String, String> configuration) {
		this.configuration = configuration;
		this.httpSettings = new HttpSettings();
		if (configuration.containsKey(JWT_TOKEN)) {
			httpSettings.headers.put(JWT_TOKEN, configValue(JWT_TOKEN));
		}
	}

	public boolean uploadInput(final JobData job, final String inputName, final InputStream in) {
		URL url = getUrl(UPLOAD_INPUT_URL, job, inputName);
		debug("Uploading to {}", url);
		return retryBoolean(() -> Http.streamUp(httpSettings, url.toString(), in));
	}

	public boolean startJob(final JobData job) {
		URL url = getUrl(START_JOB_URL, job, null);
		String jobSettings = settingsString(job.getJobOnCluster().getSettings());
		debug("Starting job {}: put settings {} to {}", job, jobSettings, url);
		return retryBoolean(() -> Http.put(httpSettings, url.toString(), jobSettings).isPresent());
	}

	private String settingsString(final Map<String, String> settings) {
		StringBuilder sb = new StringBuilder();
		settings.forEach((k,v) -> {
			// job id setting is not required, is part of url already
			if (!JobSettingsConstants.SETTING_JOB_ID.equals(k)) {
				sb.append(jobSettingLine(k,v));
			}
		});
		return sb.toString();
	}

	private String jobSettingLine(final String k, final String v) {
		return k.replaceAll(":", "_")+":"+v+"\n";
	}

	public boolean hasTerminated(final JobData job) {
		URL url = getUrl(JOB_STATE_URL, job, null);
		debug("Reading job state from {}", url);
		Optional<String> res = Http.get(httpSettings, url.toString()); // do not retry, if file does not exist, assume not terminated
		if (res.isPresent()) {
			String state = res.get();
			debug("Got job state {}", res.get());
			String previous = previousJobStates.get(job.getId());
			if (previous == null) {
				previous = "not-existent";
			}
			if (!previous.equals(state)) {
				previousJobStates.put(job.getId(), state);
				log("Job state changed from {} to {}", previous, state);
			}
			return configValue(VALUE_TERMINATED).equals(res.get());
		} else {
			debug("Could not get job state.");
			// we don't know the job state, all we can do is assume it's still running
			return false;
		}
	}

	public Optional<ExecutionResult> getResult(final JobData job) {
		URL url = getUrl(JOB_RESULT_URL, job, null);
		debug("Reading job result from {}", url);
		Optional<String> res = Http.get(httpSettings, url.toString()); // do not retry, if file does not exit return empty
		if (res.isPresent()) {
			debug("Got job result {}", res.get());
			if (configValue(VALUE_SUCCESS).equals(res.get())) {
				return Optional.of(ExecutionResult.SUCCESS);
			}
			if (configValue(VALUE_WARNING).equals(res.get())) {
				return Optional.of(ExecutionResult.WARNING);
			}
			// all other are mapped to failed
			return Optional.of(ExecutionResult.FAILED);
		} else {
			debug("Could not get job result.");
		}
		return Optional.empty();
	}

	public boolean downloadOutput(final JobData job, final String outputName, final OutputStream out) {
		URL url = getUrl(DOWNLOAD_OUTPUT_URL, job, outputName);
		debug("Downloading from {}", url);
		return retryBoolean(() -> Http.streamDown(httpSettings, url.toString(), out));
	}

	public boolean cleanup(final JobData job) {
		URL url = getUrl(JOB_CLEANUP_URL, job, null);
		debug("Cleaning up resources {} calling {}", job, url);
		return retryBoolean(() -> Http.put(httpSettings, url.toString(), "").isPresent());
	}

	private URL getUrl(final String templateKey, JobData job, String resourceName) {
		String url = configValue(templateKey);
		url = replace(url, JOB_ID_PARAM, job.getId());
		url = replace(url, HOST_PARAM, configValue(HOST));
		if (resourceName != null) {
			url = replace(url, NAME_PARAM, resourceName);
		}
		final String namespace = job.getJobOnCluster().getSettings().get(SETTING_NAMESPACE);
		String finalUrl = replace(url, NAMESPACE_PARAM, namespace);
		return onException(() -> new URL(finalUrl)).fail("Invalid url received for job service");
	}

	private String replace(final String url, final String key, final String value) {
		return url.replaceAll("\\{"+key+"}", value);
	}

	private String configValue(final String key) {
		String res = configuration.get(key);
		if (res == null) {
			Application.fail("Configuration key is not set: "+key);
		}
		return res;
	}

}
