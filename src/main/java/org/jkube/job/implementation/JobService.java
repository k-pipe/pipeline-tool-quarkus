package org.jkube.job.implementation;

import org.jkube.job.ExecutionResult;

import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.util.Optional;

import static org.jkube.logging.Log.debug;
import static org.jkube.logging.Log.warn;

public class JobService {

	private final JobClient jobClient;

	public JobService(final JobClient jobClient) {
		this.jobClient = jobClient;
	}

	public boolean startJob(final JobData job) {
		debug("Uploading inputs for job {}", job);
		JobIO jobIO = job.getJobIO();
		for (Field input : jobIO.getInputs()) {
			debug("Sending input {}", input.getName());
			InputStream in = jobIO.getInStream(input);
			if ((in == null) || !jobClient.uploadInput(job,  jobIO.getInputName(input), in)) {
				warn("Could upload inputs for job {}", job);
				return false;
			}
		}
		if (!jobClient.startJob(job)) {
			warn("Could not start job {}", job);
			return false;
		}
		return true;
	}

	public void cleanup(final JobData job) {
		if (!jobClient.cleanup(job)) {
			warn("Could not cleanup job {}", job);
		}
	}

	public Optional<ExecutionResult> getResult(final JobData job) {
		if (jobClient.hasTerminated(job)) {
			return jobClient.getResult(job);
		} else {
			return Optional.empty();
		}
	}

	public boolean downloadOutputs(final JobData job) {
		debug("Downloading outputs  for job {}", job);
		JobIO jobIO = job.getJobIO();
		for (Field output : jobIO.getOutputs()) {
			debug("Downloading output {}", output.getName());
			OutputStream out = jobIO.getOutStream(output);
			if ((out == null) || !jobClient.downloadOutput(job, jobIO.getOutputName(output), out)) {
				return false;
			}
		}
		return true;
	}

}
