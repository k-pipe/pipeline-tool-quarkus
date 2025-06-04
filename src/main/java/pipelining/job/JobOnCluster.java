package pipelining.job;

import pipelining.job.implementation.JobIO;
import pipelining.job.implementation.JobSettingsConstants;
import pipelining.logging.Log;

import java.lang.reflect.Field;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class JobOnCluster {

	private final Cluster cluster;
	private final JobInDocker job;
	private final Map<String, String> settings;
	private Integer timeOutMinutes;

	JobOnCluster(Cluster cluster, final JobInDocker job) {
		this.cluster = cluster;
		this.job = job;
		this.settings = new LinkedHashMap<>();
		with(JobSettingsConstants.SETTING_NAMESPACE, JobSettingsConstants.DEFAULT_NAMESPACE);
		//with(SETTING_DOCKER_REPO, job.getImage().getRepository());
		//with(SETTING_DOCKER_IMAGE_NAME, job.getImage().getImageName());
		//with(SETTING_DOCKER_IMAGE_TAG, job.getImage().getVersionTag());
		with(JobSettingsConstants.SETTING_INPUTS, getInputsString(job.getJob()));
	}

	private String getInputsString(final Job job) {
		JobIO jobIO = new JobIO(job);
		StringBuilder res = new StringBuilder();
		for (Field input : jobIO.getInputs()) {
			if (res.length() > 0) {
				res.append(",");
			}
			res.append(jobIO.getInputName(input));
		}
		return res.toString();
	}

	public Map<String,String> getSettings() {
		return settings;
	}

	public JobOnCluster with(String settingKey, String settingValue) {
		String before = settings.put(settingKey, settingValue);
		if (before != null) {
			Log.warn("Setting {} changed from {} to {}", settingKey, before, settingValue);
		}
		return this;
	}

	public JobOnCluster inNamespace(String namespace) {
		settings.put(JobSettingsConstants.SETTING_NAMESPACE, namespace);
		return this;
	}

	public JobOnCluster withId(String jobId) {
		return with(JobSettingsConstants.SETTING_JOB_ID, jobId);
	}

	public JobOnCluster withTimeout(int timeOutMinutes) {
		this.timeOutMinutes = timeOutMinutes;
		settings.put(JobSettingsConstants.SETTING_TIMEOUT, Integer.toString(timeOutMinutes));
		return this;
	}

	public ExecutionResult run() {
		return ExecutionResult.catchExceptions(() -> {
			if ((timeOutMinutes != null) && (timeOutMinutes <= 0)) {
				return submit().get(timeOutMinutes, TimeUnit.MINUTES);
			} else {
				return submit().get();
			}
		});
	}

	public CompletableFuture<ExecutionResult> submit() {
		return cluster.submit(this);
	}

	public JobInDocker getDockerJob() {
		return job;
	}

}
