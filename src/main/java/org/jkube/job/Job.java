package org.jkube.job;

public interface Job extends Runnable {
	default JobInDocker inImage(DockerImage dockerImage) {
		return new JobInDocker(this, dockerImage);
	}
}
