package pipelining.job;

public interface Job extends Runnable {
	default JobInDocker inImage(DockerImage dockerImage) {
		return new JobInDocker(this, dockerImage);
	}
}
