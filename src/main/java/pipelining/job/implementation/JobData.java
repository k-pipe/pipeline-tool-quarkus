package pipelining.job.implementation;

import pipelining.job.ExecutionResult;
import pipelining.job.JobOnCluster;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Random;
import java.util.concurrent.CompletableFuture;

import static pipelining.job.implementation.JobSettingsConstants.SETTING_JOB_ID;

public class JobData {

	private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd-hh-mm-ss.SSS");
	private static final Random RANDOM = new Random();

	private final JobOnCluster job;
	private final JobIO jobIO;
	private final CompletableFuture<ExecutionResult> future;
	private final String jobId;

	public JobData(final JobOnCluster clusterJob) {
		this.job = clusterJob;
		this.jobIO = new JobIO(clusterJob.getDockerJob().getJob());
		this.future = new CompletableFuture<>();
		this.jobId = determineJobId(clusterJob);
	}

	private String determineJobId(final JobOnCluster jonOnCluster) {
		String res = jonOnCluster.getSettings().get(SETTING_JOB_ID);
		if ((res != null) && !res.isBlank()) {
			return res;
		}
		return jonOnCluster.getDockerJob().getImage().getImage()+"-"+ LocalDateTime.now().format(DATE_FORMAT)+"-"+RANDOM.nextInt(1000000);
	}

	public CompletableFuture<ExecutionResult> getFuture() {
		return future;
	}

	public String getId() {
		return jobId;
	}

	public JobOnCluster getJobOnCluster() {
		return job;
	}

	public JobIO getJobIO() {
		return jobIO;
	}

	@Override
	public String toString() {
		return jobId;
	}
}
