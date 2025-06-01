package org.jkube.job;

import java.util.concurrent.CompletableFuture;

public interface Cluster {

	CompletableFuture<ExecutionResult> submit(JobOnCluster clusterJob);

}
