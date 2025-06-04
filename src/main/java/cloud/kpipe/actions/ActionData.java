package cloud.kpipe.actions;

import cloud.kpipe.pipeline.Pipeline;
import cloud.kpipe.pipeline.PipelineRun;
import cloud.kpipe.pipeline.Schedule;
import cloud.kpipe.util.FileUtil;
import org.jkube.application.Application;
import org.jkube.job.DockerImage;
import org.jkube.util.Expect;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.stream.Collectors;

public class ActionData {

    private final Set<Path> outputsUsed = new HashSet<>();
    private final Map<String, Path> manifests = new LinkedHashMap<>();

    private final List<List<Pipeline>> parsedPipelines = new LinkedList<>();

    private final Map<String, PipelineRun> runs = new LinkedHashMap<>();

    private final Map<Path, List<DockerImage>> dockerImages = new LinkedHashMap<>();

    public void registerManifestForPipeline(String pipeline, Path manifestPath) {
        Expect.isFalse(manifests.containsKey(pipeline)).elseFail("A pipeline with this name was parsed already before: "+pipeline);
        manifests.put(pipeline, manifestPath);
    }

    public void addParsedPipelines(List<Pipeline> pipelines) {
        Expect.isFalse(pipelines.isEmpty()).elseFail("No pipelines were parsed");
        parsedPipelines.add(pipelines);
    }

    public Pipeline getLatestParsedPipeline() {
        Expect.isFalse(parsedPipelines.isEmpty()).elseFail("Pipeline must be parsed before command 'run'");
        List<Pipeline> latest = parsedPipelines.get(parsedPipelines.size()-1);
        Expect.equal(latest.size(), 1).elseFail("Previous 'parse' command yielded multiple pipelines, must specify which one to run!");
        return latest.get(0);
    }

    public Pipeline findParsedPipeline(String pipelineName) {
        for (List<Pipeline> parsed : parsedPipelines) {
            for (Pipeline p : parsed) {
                if (p.getName().equals(pipelineName)) {
                    return p;
                }
            }
        }
        Expect.isTrue(false).elseFail("No pipeline with this name was parsed: "+pipelineName);
        return null;
    }

    public Path getManifest(String pipelineName) {
        return manifests.get(pipelineName);
    }

    public Path getOutputPath(String filename) {
        Path output = Path.of(filename);
        if (output.getParent() == null) {
            output = Path.of(".").resolve(output);
        }
        return output;
    }

    public void appendToManifest(Path path, String yamlText) {
        if (outputsUsed.add(path)) {
            FileUtil.deleteFileIfExists(path);
            FileUtil.createParentFolderIfNotExists(path);
        } else {
            FileUtil.appendYamlSeparator(path);
        }
        try {
            Files.writeString(path, yamlText, StandardOpenOption.APPEND, StandardOpenOption.CREATE);
        } catch (IOException e) {
            Application.fail("Could not write to file "+path+", exception occurred: "+e);
        }
    }

    public void addRun(String pipelineName, PipelineRun run) {
        runs.put(pipelineName, run);
    }

    public List<String> getManifests() {
        return manifests.values().stream().map(Path::toString).collect(Collectors.toList());
    }

    public Map<String, Set<String>> getRunsByNamespace() {
        Map<String, Set<String>> res = new HashMap<>();
        runs.values().forEach(run -> {
            String namespace = run.getNamespace();
            res.putIfAbsent(namespace, new HashSet<>());
            res.get(namespace).add(run.getRunId());
        });
        return res;
    }

    public Map<Path,List<DockerImage>> getDockerImages() {
        return dockerImages;
    }

    public Set<String> getDockerImageNames(boolean onlyBundled) {
        Set<String> res = new TreeSet<>();
        dockerImages.forEach((path,images) -> {
            images.forEach(image -> {
                if (image.isBundled() || !onlyBundled) {
                    res.add(image.getImageWithTag());
                }
            });
        });
        return res;
    }

    public Map<Path, String> getBundledDockerImagePathsAndNames() {
        Map<Path,String> res = new LinkedHashMap<>();
        dockerImages.forEach((pipelinePath,images) -> {
            images.forEach(image -> {
                if (image.isBundled()) {
                    Path path = pipelinePath.getParent().resolve(image.getPath());
                    String name = image.getImageWithTag();
                    String previous = res.put(path, name);
                    Expect.isTrue((previous == null) || previous.equals(name)).elseFail("Same docker image path "+path+" occurs with different images: "+previous+" vs. "+name);
                }
            });
        });
        return res;
    }

    public void addDockerImage(Path path, DockerImage image) {
        dockerImages.putIfAbsent(path, new LinkedList<>());
        dockerImages.get(path).add(image);
    }

    public Map<Pipeline, List<Schedule>> getSchedules() {
        Map<Pipeline, List<Schedule>> res = new LinkedHashMap<>();
        parsedPipelines.forEach(pl -> {
            pl.forEach(p -> {
                List<Schedule> schedules = p.getSchedules();
                if (!schedules.isEmpty()) {
                    res.put(p, schedules);
                }
            });
        });
        return res;
    }
}
