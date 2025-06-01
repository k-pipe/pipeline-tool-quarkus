package org.jkube.job.implementation;

import org.jkube.job.JobInDocker;

import java.io.File;
import java.lang.reflect.Field;
import java.nio.file.Path;

import static org.jkube.logging.Log.onException;
import static org.jkube.logging.Log.warn;

public class JobDockerImageRunner extends DockerImageRunner {

	private final JobIO jobIO;

	public JobDockerImageRunner(final JobInDocker job) {
		super(job.getImage());
		this.jobIO = new JobIO(job.getJob());
	}

	public boolean run(Path workdirPath) {
		boolean result = onException(() -> tryRun(workdirPath)).fallback(false);
		cleanupRecursively(workdirPath.toFile());
		return result;
	}

	public boolean tryRun(Path workdirPath) {
		Workdir workdir = new Workdir(workdirPath);
		for (final Field input : jobIO.getInputs()) {
			if (!workdir.writeInputFile(jobIO.getInputName(input), jobIO.getInStream(input))) {
				return false;
			}
		}
		if (!run(workdirPath.toAbsolutePath().toString())) {
			return false;
		}
		for (final Field output : jobIO.getOutputs()) {
			if (!workdir.readOutputFile(jobIO.getOutputName(output), jobIO.getOutStream(output))) {
				return false;
			}
		}
		return true;
	}

	private void cleanupRecursively(final File folder) {
		File[] files = folder.listFiles();
		if (files != null) {
			for (File file : files) {
				if (file.isDirectory()) {
					cleanupRecursively(file);
				}
				if (!file.delete()) {
					warn("Could not delete file " + file);
				}
			}
		}
	}

}
