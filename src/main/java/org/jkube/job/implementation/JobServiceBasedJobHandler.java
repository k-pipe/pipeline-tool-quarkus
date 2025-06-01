package org.jkube.job.implementation;

import org.jkube.job.ExecutionResult;
import org.jkube.job.JobHandler;
import org.jkube.job.JobOnCluster;
import org.jkube.logging.Log;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;

import static org.jkube.logging.Log.*;

public class JobServiceBasedJobHandler implements JobHandler {

	private static final long SLEEP_TIME_SECONDS = 10000;

	private final JobService jobService;
	private final Set<JobData> runningJobs;
	private final AtomicReference<Thread> updateThread;

	public JobServiceBasedJobHandler(Map<String, String> config) {
		this.jobService = new JobService(new JobClient(config));
		this.runningJobs = new LinkedHashSet<>();
		this.updateThread = new AtomicReference<>();
	}

	@Override
	public CompletableFuture<ExecutionResult> createJob(final JobOnCluster clusterJob) {
		JobData job = new JobData(clusterJob);
		if (!jobService.startJob(job)) {
			debug("Starting job {} failed, cleaning up resources.", job);
			jobService.cleanup(job);
			job.getFuture().complete(ExecutionResult.FAILED);
			return job.getFuture();
		}
		addJob(job);
		return job.getFuture();
	}

	private void addJob(final JobData job) {
		debug("Adding job {} to list of running jobs.", job);
		boolean wasEmpty;
		synchronized (runningJobs) {
			wasEmpty = runningJobs.isEmpty();
			runningJobs.add(job);
		}
		if (wasEmpty) {
			debug("Starting update thread.");
			// executorService.submit(this::update);
			// NOTE: We do not use executor service here, we want a single thread that stops
			// (to shutdown the VM when done)
			Thread newThread = new Thread(this::update);
			if (updateThread.compareAndSet(null, newThread)) {
				newThread.start();
				debug("Update thread was started");
			} else {
				warn("There is another thread already running.");
			}
		}
	}

	private void update() {
		boolean done;
		do {
			List<JobData> clone;
			synchronized (runningJobs) {
				clone = new ArrayList<>(runningJobs);
			}
			if (!clone.isEmpty()) {
				debug("Checking states of {} running jobs.", clone.size());
			}
			clone.forEach(this::checkState);
			synchronized (runningJobs) {
				done = runningJobs.isEmpty();
			}
			if (!done) {
				long sleep = SLEEP_TIME_SECONDS;
				debug("Sleeping {} mili seconds", sleep);
				interruptable(() -> Thread.sleep(sleep));
			}
		} while(!done);
		debug("There are no more running jobs, update thread is terminating");
		updateThread.set(null);
	}

	private void checkState(final JobData job) {
		final Optional<ExecutionResult> jobresult = jobService.getResult(job);
		if (jobresult.isPresent()) {
			Log.log("Job {} terminated with result: {}", job, jobresult);
			ExecutionResult result = jobresult.get();
			if (ExecutionResult.SUCCESS.equals(result) || ExecutionResult.WARNING.equals(result)) {
				Log.log("Downloading outputs from terminated job {}", job);
				jobService.downloadOutputs(job);
			}
			boolean removed;
			synchronized (runningJobs) {
				removed = runningJobs.remove(job);
			}
			if (removed) {
				Log.log("Terminated job {} was removed from list of running jobs, now cleaning up resources", job);
				jobService.cleanup(job);
				job.getFuture().complete(result);
			} else {
				Log.warn("Terminated job {} was not in list of running jobs", job);
			}
		} else {
			Log.debug("Job is still running: {}", job);
		}
	}

}


