package pipelining.job;

import pipelining.application.Application;
import pipelining.job.implementation.JobRunner;

import static pipelining.application.Application.fail;
import static pipelining.logging.Log.log;
import static pipelining.logging.Log.onException;

public class Run {

	public static final String DOCKER_WORKDIR = "/workdir";
	public static final String DEFAULT_INPUT = "input";
	public static final String DEFAULT_OUTPUT = "output";

	private String inDir;
	private String outDir;

	public static Run inDocker() {
		return new Run();
	}

	private Run() {
		setPaths(DOCKER_WORKDIR);
	}

	private void setPaths(String workdir) {
		this.inDir = workdir + "/" + DEFAULT_INPUT;
		this.outDir = workdir + "/" + DEFAULT_OUTPUT;
	}

	public Run orLocally(String inputDir, String outputDir) {
		if (!Application.isRunningInDocker()) {
			this.inDir = inputDir;
			this.outDir = outputDir;
		}
		return this;
	}

	public Run orLocally(String workDir) {
		if (!Application.isRunningInDocker()) {
			setPaths(workDir);
		}
		return this;
	}

	public void job(Class<? extends Job> jobClass) {
		JobRunner runner = new JobRunner(jobClass, inDir, outDir);
		if (executedSuccessfully(runner)) {
			log("Job of class "+jobClass+" was executed successfully");
		} else {
			fail("Job of class "+jobClass+" failed, terminating with nonzero error code");
		}
	}

	public static boolean executedSuccessfully(JobRunner jobRunner) {
		return onException(jobRunner::run)
				.warn("A problem occurred during execution of job")
				.fallback(false);
	}

}
