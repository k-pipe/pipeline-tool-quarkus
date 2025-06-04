package pipelining.job;

import pipelining.job.implementation.JobClientConfigConstants;
import pipelining.logging.Log;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static pipelining.logging.Log.onException;

public class Kubernetes implements Cluster {

	public static Cluster clusterAt(String hostname, boolean useHttps, String jwtToken) {
		return Log.onException(() -> tryConfigureCluster(hostname, useHttps, jwtToken)).fail("Cluster configuration failed");
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
		final String jobHandlerClass = config.get(JobClientConfigConstants.JOB_HANDLER_CLASS);
		return Log.onException(() -> (JobHandler)Class
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
			res.put(JobClientConfigConstants.HOST, hostname);
			urlPrefix = protocol+"://{HOST}/service/{NAMESPACE}/";
			urlInfix = "/";
		}
		if (jwtToken != null) {
			res.put(JobClientConfigConstants.JWT_TOKEN, jwtToken);
		}
		res.put(JobClientConfigConstants.JOB_HANDLER_CLASS, "org.jkube.job.implementation.JobServiceBasedJobHandler");
		res.put(JobClientConfigConstants.UPLOAD_INPUT_URL, urlPrefix+"resource"+urlInfix+"job/external/{ID}/input/{NAME}");
		res.put(JobClientConfigConstants.DOWNLOAD_OUTPUT_URL, urlPrefix+"resource"+urlInfix+"job/external/{ID}/output/{NAME}");
		res.put(JobClientConfigConstants.JOB_STATE_URL, urlPrefix+"resource"+urlInfix+"job/external/{ID}/state");
		res.put(JobClientConfigConstants.JOB_RESULT_URL, urlPrefix+"resource"+urlInfix+"job/external/{ID}/result");
		res.put(JobClientConfigConstants.START_JOB_URL, urlPrefix+"script"+urlInfix+"api/job/external/{ID}/start");
		res.put(JobClientConfigConstants.JOB_CLEANUP_URL, urlPrefix+"script"+urlInfix+"api/job/external/{ID}/cleanup");
		res.put(JobClientConfigConstants.VALUE_TERMINATED, "TERMINATED");
		res.put(JobClientConfigConstants.VALUE_SUCCESS, "SUCCESS");
		res.put(JobClientConfigConstants.VALUE_WARNING, "WARNING");
		return res;
	}

}
