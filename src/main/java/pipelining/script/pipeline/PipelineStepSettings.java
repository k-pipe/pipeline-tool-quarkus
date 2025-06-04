package pipelining.script.pipeline;

import pipelining.job.specification.ResourceLimits;
import pipelining.job.specification.Retention;
import pipelining.job.specification.TimeoutSpecs;
import pipelining.job.status.JobResult;

import java.util.LinkedHashMap;
import java.util.Map;

public class PipelineStepSettings {

	private final Map<JobResult, Retention> retentionMap;
	private String jobClass;
	private TimeoutSpecs timeoutSpecs;
	private ResourceLimits resourceLimits;

	public PipelineStepSettings() {
		this.retentionMap = new LinkedHashMap<>();
	}

	public String getJobClass() {
		return jobClass;
	}

	public TimeoutSpecs getTimeoutSpecs() {
		return timeoutSpecs;
	}

	public ResourceLimits getResourceLimits() {
		return resourceLimits;
	}

	public Map<String, Retention> getRetentionMap() {
		Map<String, Retention> res = new LinkedHashMap<>();
		retentionMap.forEach((result, retention) -> res.put(result.toString(), retention));
		return res;
	}

	public void setJobClass(final String jobClass) {
		this.jobClass = jobClass;
	}

	public void setTimeoutSpecs(final TimeoutSpecs timeoutSpecs) {
		this.timeoutSpecs = timeoutSpecs;
	}

	public void setRetention(final JobResult result, Retention retention) {
		this.retentionMap.put(result, retention);
	}

	public void setResourceLimits(final ResourceLimits resourceLimits) {
		this.resourceLimits = resourceLimits;
	}

}
