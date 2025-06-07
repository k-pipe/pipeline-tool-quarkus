package pipelining.job.implementation;

import pipelining.job.DockerImage;
import pipelining.job.Run;
import pipelining.logging.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static pipelining.logging.Log.*;

public class DockerImageRunner {

	protected final String dockerImageSpec;
	protected final boolean mountDockerSock;
	protected final boolean interactive;
	protected final String credentialsMount;
	protected final String gpus;

	protected final String platform;
	protected final List<String> commandlineArgs;

	public DockerImageRunner(DockerImage dockerImage) {
		this(dockerImage, Collections.emptyList(), false, false, null);
	}

	public DockerImageRunner(DockerImage dockerImage, List<String> commandlineArgs, boolean mountDockerSock, boolean interactive, String credentialsMount) {
		this(dockerImage, commandlineArgs, mountDockerSock, interactive, null, credentialsMount);
	}

	public DockerImageRunner(DockerImage dockerImage, List<String> commandlineArgs, boolean mountDockerSock, boolean interactive, String gpus, String credentialsMount) {
		this(dockerImage, commandlineArgs, mountDockerSock, interactive, gpus, null, credentialsMount);
	}

	public DockerImageRunner(DockerImage dockerImage, List<String> commandlineArgs, boolean mountDockerSock, boolean interactive, String gpus, String platform, String credentialsMount) {
		this.dockerImageSpec = dockerImage.getImageWithTag();
		this.commandlineArgs = commandlineArgs;
		this.mountDockerSock = mountDockerSock;
		this.interactive = interactive;
		this.gpus = gpus;
		this.platform = platform;
		this.credentialsMount = credentialsMount;
	}

	public boolean login(String username, String password) {
		log("Logging in to docker repo {}", username);
		return onException(() -> tryRun(buildLoginCommandLine(username, password)))
				.warn("Problem logging in to docker")
				.fallback(false);
	}

	public boolean pull() {
		log("Pulling docker image {}", dockerImageSpec);
		return onException(() -> tryRun(buildPullCommandLine()))
				.warn("Problem occurred pulling Docker image")
				.fallback(false);
	}

	public boolean run(final String hostDirAbsolute) {
		log("Executing docker image {} with workdir {} (in host) with args {}", dockerImageSpec, hostDirAbsolute, commandlineArgs);
		return onException(() -> tryRun(buildRunCommandLine(hostDirAbsolute)))
				.warn("Problem occurred running Docker process")
				.fallback(false);
	}

	public boolean tryRun(List<String> commandline) throws IOException {
		Process docker = new ProcessBuilder()
				.inheritIO()
				.command(commandline)
				.start();
		try {
			docker.waitFor();
		} catch (InterruptedException e) {
			exception(e);
			return false;
		}
		if (docker.exitValue() == 0) {
			log("Docker process has terminated normally");
		} else {
			warn("Docker process has exited with error code: " + docker.exitValue());
		}
		return docker.exitValue() == 0;
	}

	private List<String> buildRunCommandLine(final String hostDirAbsolute) {
		List<String> res = new ArrayList<>();
		res.add("docker");
		res.add("run");
		if (interactive) {
			res.add("-it");
		}
		if (gpus != null) {
			res.add("--gpus");
			res.add(gpus);
		}
		if (platform != null) {
			res.add("--platform");
			res.add(platform);
		}
		if ((credentialsMount != null) && (credentialsMount.length() > 0)) {
			res.add("-v");
			res.add(credentialsMount);
		}
		res.add("-v");
		res.add(hostDirAbsolute + ":" + Run.DOCKER_WORKDIR);
		if (mountDockerSock) {
			res.add("-v");
			res.add("/var/run/docker.sock:/var/run/docker.sock");
		}
		res.add(dockerImageSpec);
		res.addAll(commandlineArgs);
		Log.log("Command: "+res.stream().collect(Collectors.joining(" ")));
		return res;
	}

	private List<String> buildPullCommandLine() {
		List<String> res = new ArrayList<>();
		res.add("docker");
		res.add("pull");
		res.add(dockerImageSpec);
		return res;
	}

	private List<String> buildLoginCommandLine(String username, String password) {
		List<String> res = new ArrayList<>();
		res.add("docker");
		res.add("login");
		res.add("--username");
		res.add(username);
		res.add("--password");
		res.add(password);
		return res;
	}

}
