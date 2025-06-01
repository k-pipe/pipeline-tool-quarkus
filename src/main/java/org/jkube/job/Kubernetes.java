package org.jkube.job;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static org.jkube.job.implementation.JobClientConfigConstants.*;
import static org.jkube.logging.Log.onException;

public class Kubernetes implements Cluster {

	public static Cluster clusterAt(String hostname, boolean useHttps, String jwtToken) {
		return onException(() -> tryConfigureCluster(hostname, useHttps, jwtToken)).fail("Cluster configuration failed");
	}

	public static Cluster clusterAt(String hostname, boolean useHttps) {
		return clusterAt(hostname, useHttps, null);
	}

	public static Cluster clusterAt(String hostname) {
		return clusterAt(hostname, false);
	}

	public static Cluster localCluster() {
		return clusterAt(null);
	}

	private static Cluster tryConfigureCluster(String hostname, boolean useHttps, String jwtToken) {
		return new Kubernetes(lookupConfiguration(hostname, useHttps, jwtToken));
	}

	private final JobHandler jobHandler;

	private Kubernetes(Map<String, String> config) {
		this.jobHandler = createJobHandler(config);
	}

	@Override
	public CompletableFuture<ExecutionResult> submit(final JobOnCluster clusterJob) {
		return jobHandler.createJob(clusterJob);
	}

	private static JobHandler createJobHandler(Map<String, String> config) {
		final String jobHandlerClass = config.get(JOB_HANDLER_CLASS);
		return onException(() -> (JobHandler)Class
				.forName(jobHandlerClass)
				.getConstructor(Map.class).newInstance(config)
		).fail("Job handler could not be created");
	}

	private static Map<String, String> lookupConfiguration(final String hostname, boolean useHttps, String jwtToken) {
		// TODO lookup job engine configuration from a generic cluster info endpoint
		// until this is implemented, just set the configuration map statically
		Map<String,String> res = new LinkedHashMap<>();
		String protocol = useHttps ? "https" : "http";
		String urlPrefix;
		String urlInfix;
		if (hostname == null) {
			urlPrefix = protocol+"://";
			urlInfix = "-service.{NAMESPACE}:8080/";
		} else {
			res.put(HOST, hostname);
			urlPrefix = protocol+"://{HOST}/service/{NAMESPACE}/";
			urlInfix = "/";
		}
		if (jwtToken != null) {
			res.put(JWT_TOKEN, jwtToken);
		}
		res.put(JOB_HANDLER_CLASS, "org.jkube.job.implementation.JobServiceBasedJobHandler");
		res.put(UPLOAD_INPUT_URL, urlPrefix+"resource"+urlInfix+"job/external/{ID}/input/{NAME}");
		res.put(DOWNLOAD_OUTPUT_URL, urlPrefix+"resource"+urlInfix+"job/external/{ID}/output/{NAME}");
		res.put(JOB_STATE_URL, urlPrefix+"resource"+urlInfix+"job/external/{ID}/state");
		res.put(JOB_RESULT_URL, urlPrefix+"resource"+urlInfix+"job/external/{ID}/result");
		res.put(START_JOB_URL, urlPrefix+"script"+urlInfix+"api/job/external/{ID}/start");
		res.put(JOB_CLEANUP_URL, urlPrefix+"script"+urlInfix+"api/job/external/{ID}/cleanup");
		res.put(VALUE_TERMINATED, "TERMINATED");
		res.put(VALUE_SUCCESS, "SUCCESS");
		res.put(VALUE_WARNING, "WARNING");
		return res;
	}

}
