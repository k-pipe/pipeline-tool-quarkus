package com.kneissler.job.specification;

import java.util.Map;

public class JobDefinition {

	/**
	 * ID of the job (uniquely defined from job specs excluding resources)
	 */
	String jobId;

	/**
	 * namespace the job will be running in
	 */
	String namespace;

	/**
	 * Job class (different jobs may share same class). Initial ressources
	 * will be assigned according to experiences with previous jobs of same class
	 */
	String jobClass;

	/**
	 * ID of the task which the job belongs to
	 */
	String taskId;

	/**
	 * URL of job resources
	 */
	String jobUrl;

	ImageDefinition companionImage;
	ImageDefinition mainImage;

	RetentionMap retention;

	Map<String,String> inputResources; // key: local file name (relative to main folder of git), value: url where to retrieve resource

	ResourceLimits resources;

	TimeoutSpecs timeout;

	String terminatedUrl; // url to notify when terminated

	String restartUrl; // url to store resource failure log when insufficient resources

	public JobDefinition() {
	}

	public String getJobId() {
		return jobId;
	}

	public String getNamespace() {
		return namespace;
	}

	public String getJobClass() {
		return jobClass;
	}

	public String getTaskId() {
		return taskId;
	}

	public String getJobUrl() {
		return jobUrl;
	}

	public String getRestartUrl() {
		return restartUrl;
	}

	public ImageDefinition getCompanionImage() {
		return companionImage;
	}

	public ImageDefinition getMainImage() {
		return mainImage;
	}

	public RetentionMap getRetention() {
		return retention;
	}

	public Map<String, String> getInputResources() {
		return inputResources;
	}

	public ResourceLimits getResources() {
		return resources;
	}

	public TimeoutSpecs getTimeout() {
		return timeout;
	}

	public String getTerminatedUrl() {
		return terminatedUrl;
	}

	public void setJobId(final String jobId) {
		this.jobId = jobId;
	}

	public void setNamespace(final String namespace) {
		this.namespace = namespace;
	}

	public void setJobClass(final String jobClass) {
		this.jobClass = jobClass;
	}

	public void setTaskId(final String taskId) {
		this.taskId = taskId;
	}

	public void setJobUrl(final String jobUrl) {
		this.jobUrl = jobUrl;
	}

	public void setCompanionImage(final ImageDefinition companionImage) {
		this.companionImage = companionImage;
	}

	public void setMainImage(final ImageDefinition mainImage) {
		this.mainImage = mainImage;
	}

	public void setRetention(final RetentionMap retention) {
		this.retention = retention;
	}

	public void setInputResources(final Map<String, String> inputResources) {
		this.inputResources = inputResources;
	}

	public void setResources(final ResourceLimits resources) {
		this.resources = resources;
	}

	public void setTimeout(final TimeoutSpecs timeout) {
		this.timeout = timeout;
	}

	public void setTerminatedUrl(final String terminatedUrl) {
		this.terminatedUrl = terminatedUrl;
	}

	public void setRestartUrl(final String restartUrl) {
		this.restartUrl = restartUrl;
	}
}
