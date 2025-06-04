package pipelining.job.implementation;

import pipelining.job.ExecutionResult;
import pipelining.logging.Log;

import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.util.Optional;

import static pipelining.logging.Log.debug;

public class JobService {

	private final JobClient jobClient;

	public JobService(final JobClient jobClient) {
		this.jobClient = jobClient;
	}

	public boolean startJob(final JobData job) {
		Log.debug("Uploading inputs for job {}", job);
		JobIO jobIO = job.getJobIO();
		for (Field input : jobIO.getInputs()) {
			Log.debug("Sending input {}", input.getName());
			InputStream in = jobIO.getInStream(input);
			if ((in == null) || !jobClient.uploadInput(job,  jobIO.getInputName(input), in)) {
				Log.warn("Could upload inputs for job {}", job);
				return false;
			}
		}
		if (!jobClient.startJob(job)) {
			Log.warn("Could not start job {}", job);
			return false;
		}
		return true;
	}

	public void cleanup(final JobData job) {
		if (!jobClient.cleanup(job)) {
			Log.warn("Could not cleanup job {}", job);
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
		Log.debug("Downloading outputs  for job {}", job);
		JobIO jobIO = job.getJobIO();
		for (Field output : jobIO.getOutputs()) {
			Log.debug("Downloading output {}", output.getName());
			OutputStream out = jobIO.getOutStream(output);
			if ((out == null) || !jobClient.downloadOutput(job, jobIO.getOutputName(output), out)) {
				return false;
			}
		}
		return true;
	}

}
