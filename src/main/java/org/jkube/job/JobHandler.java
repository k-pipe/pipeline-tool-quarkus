package org.jkube.job;

import java.util.concurrent.CompletableFuture;

public interface JobHandler {
	CompletableFuture<ExecutionResult> createJob(JobOnCluster dockerJob);
}
