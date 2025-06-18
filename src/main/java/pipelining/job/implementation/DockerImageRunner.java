package pipelining.job.implementation;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import pipelining.application.Application;
import pipelining.job.DockerImage;
import pipelining.logging.Log;
import pipelining.pipeline.Pipeline;
import pipelining.script.pipeline.localrunner.PipelineRunner;
import pipelining.util.Expect;
import pipelining.util.ExternalProcess;
import pipelining.util.ExternalTable;
import pipelining.util.FileCache;

import java.io.IOException;
import java.util.*;
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

	public DockerImageRunner(DockerImage dockerImage, Pipeline pipeline) {
		this(dockerImage, Collections.emptyList(), false, false, null);
	}

	public DockerImageRunner(DockerImage dockerImage, List<String> commandlineArgs, boolean mountDockerSock, boolean interactive, String credentialsMount) {
		this(dockerImage, commandlineArgs, mountDockerSock, interactive, null, credentialsMount);
	}

	public DockerImageRunner(DockerImage dockerImage, List<String> commandlineArgs, boolean mountDockerSock, boolean interactive, String gpus, String credentialsMount) {
		this(dockerImage, commandlineArgs, mountDockerSock, interactive, gpus, null, credentialsMount);
	}

	public DockerImageRunner(DockerImage dockerImage, List<String> commandlineArgs, boolean mountDockerSock, boolean interactive, String gpus, String platform, String credentialsMount) {
		this.dockerImageSpec = getImage(dockerImage);
		this.commandlineArgs = commandlineArgs;
		this.mountDockerSock = mountDockerSock;
		this.interactive = interactive;
		this.gpus = gpus;
		this.platform = platform;
		this.credentialsMount = credentialsMount;
	}

	private String getImage(DockerImage dockerImage) {
		if (dockerImage.isManaged()) {
			String provider = dockerImage.getProvider();
			Integer generation = dockerImage.getGeneration();
			String managedImageName = dockerImage.getImage();
			return lookupImageWithTag(provider, managedImageName, generation);
		} else {
			return dockerImage.getImageWithTag();
		}
	}

	private String lookupImageWithTag(String provider, String managedImageName, Integer generation) {
		String name = "pmi-"+provider+"-"+managedImageName;
		if (generation != null)  {
			name += "-gen"+generation;
		}
		name += ".yaml";
		List<String> lines;
		if (FileCache.exists(name)) {
			lines = FileCache.read(name);
		} else {
			Log.log("Looking up image version for " + name);
			ExternalProcess proc = new ExternalProcess(Map.of()).command("kubectl", List.of("get", "pmi", managedImageName, "-n", provider, "-o", "jsonpath={.spec.generations}"));
			proc.execute();
			Expect.isTrue(proc.hasSucceeded()).elseFail("Could not get managed pipeline docker image version");
			lines = proc.getOutput();
			FileCache.write(name, lines);
		}
		Expect.size(lines, 1).elseFail("Expected exactly one line, got: "+lines.size());
		Map<Integer,String> generations = parseImageGenerations(lines.get(0));
		if (generation == null) {
			OptionalInt maxGen = generations.keySet().stream().mapToInt(g -> g).max();
			Expect.isTrue(maxGen.isPresent()).elseFail("no generations specified");
			generation = maxGen.getAsInt();
		}
		Expect.isTrue(generations.containsKey(generation)).elseFail("No such generation defined: "+generation);
		return generations.get(generation);
	}

	private Map<Integer, String> parseImageGenerations(String json) {
		Map<Integer, String> res = new HashMap<>();
		ObjectMapper mapper = new ObjectMapper();
		try {
			JsonNode node = mapper.readTree(json);
			Expect.isTrue(node.isArray()).elseFail("Expected an array, got: "+node);
			Iterator<JsonNode> i = node.elements();
			while (i.hasNext()) {
				JsonNode e = i.next();
				Integer generation = e.get("generation").asInt();
				StringBuilder sb = new StringBuilder();
				add(sb, e, "repository");
				add(sb, e, "path");
				add(sb, e, "name");
				sb.append(":");
				sb.append(e.get("tag").asText());
				res.put(generation, sb.toString());
			}
		} catch (JsonProcessingException e) {
			Application.fail("Could not parse json: "+e);
		}
		return res;
	}

	private void add(StringBuilder sb, JsonNode n, String key) {
		if (n.has(key)) {
			if (sb.length() != 0) {
				sb.append("/");
			}
			sb.append(n.get(key).asText());
		}
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
		res.add(hostDirAbsolute + ":" + PipelineRunner.WORKDIR);
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
