package pipelining.job;

import pipelining.job.implementation.JobDockerImageRunner;

import java.nio.file.Path;

public class JobInDocker {

	private final Job job;
	private final DockerImage image;

	public JobInDocker(final Job job, final DockerImage dockerImage) {
		this.job = job;
		this.image = dockerImage;
	}

	public JobOnCluster onCluster(Cluster cluster) {
		return new JobOnCluster(cluster, this);
	}

	/**
	 * Docker image
	 *
	 * @return docker image
	 */
	public DockerImage getImage() {
		return image;
	}

	/**
	 * Get the job that is supposed to be running inside the docker image
	 *
	 * @return job representing the docker image
	 */
	public Job getJob() {
		return job;
	}


	public boolean run(Path workdir) {
		return new JobDockerImageRunner(this).run(workdir);
	}

}
