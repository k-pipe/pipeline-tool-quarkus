package com.kneissler.job.status;

public class ResourceFailureLog {
	private String jobId;
	private String jobUrl;
	private ResourceSettings settings;
	private FailureReason failureReason;
	private ResourceLog resourceLog;

	public ResourceFailureLog(final String jobId, final String jobUrl, final ResourceSettings settings,
			final FailureReason failureReason, final ResourceLog resourceLog) {
		this.jobId = jobId;
		this.jobUrl = jobUrl;
		this.settings = settings;
		this.failureReason = failureReason;
		this.resourceLog = resourceLog;
	}

	public String getJobId() {
		return jobId;
	}

	public String getJobUrl() {
		return jobUrl;
	}

	public ResourceSettings getSettings() {
		return settings;
	}

	public FailureReason getFailureReason() {
		return failureReason;
	}

	public ResourceLog getResourceLog() {
		return resourceLog;
	}

}
