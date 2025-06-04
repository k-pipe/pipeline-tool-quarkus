package pipelining.job;

import java.util.concurrent.CompletableFuture;

public interface Cluster {

	CompletableFuture<ExecutionResult> submit(JobOnCluster clusterJob);

}
