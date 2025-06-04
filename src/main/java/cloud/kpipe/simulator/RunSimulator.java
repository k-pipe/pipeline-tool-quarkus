package cloud.kpipe.simulator;

import cloud.kpipe.pipeline.Pipeline;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.kneissler.ui.UIHandler;
import com.kneissler.util.richfile.resolver.VariableResolver;
import org.jkube.job.implementation.DockerImageRunner;
import org.jkube.pipeline.definition.PipelineConnector;
import org.jkube.pipeline.definition.PipelineStep;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static cloud.kpipe.simulator.RunSimulator.InceptionLevel.*;
import static org.jkube.application.Application.fail;
import static org.jkube.job.Run.DOCKER_WORKDIR;
import static org.jkube.logging.Log.*;

public class RunSimulator {

    private final String INPUT_FOLDER = "input";
    private final String OUTPUT_FOLDER = "output";

    private final String workdir;
    private final String simulationdir;
    private final String startStep;
    private final String endStep;
    private final String credentialsMount;
    private UIHandler uiHandler;
    private final Map<PipelineStep, Integer> stepNumbers = new LinkedHashMap<>();


    public RunSimulator(String workdir, String simulationdir, String startStep, String endStep, String credentialsMount) {
        this.workdir = workdir;
        this.simulationdir = simulationdir;
        this.startStep = startStep;
        this.endStep = endStep;
        this.credentialsMount = credentialsMount;
    }

    public void setUiHandler(UIHandler uiHandler) {
        this.uiHandler = uiHandler;
    }

    public void runLocally(Pipeline pipeline) {
        log("Pipeline name: {}", pipeline.getName());
        long allstart = System.currentTimeMillis();
        List<PipelineStep> stepSequence = pipeline.determineSequence(startStep, endStep, stepNumbers);
        log("Simulating {} pipeline steps from [{}] to [{}]", stepSequence.size(), stepSequence.get(0).getId(), stepSequence.get(stepSequence.size()-1).getId());
        if (uiHandler != null) {
            uiHandler.beforeRun(stepNumbers);
        }
        VariableResolver resolver = pipeline.getResolver();
        String last = null;
        try {
            for (PipelineStep step : stepSequence) {
                last = step.getId();
                long stepstart = System.currentTimeMillis();
                log("---------------------------------------------------------------------------");
                log("Running step {}: {}", stepNumbers.get(step), step.getId());
                if (uiHandler != null) {
                    uiHandler.beforeStep(step);
                }
                runStep(step, resolver);
                if (uiHandler != null) {
                    uiHandler.afterStep(outputDir(step, TOOL_CONTAINER), step);
                }
                log("Terminated step {} after {} seconds", stepNumbers.get(step), getSeconds(stepstart));
            }
            if (uiHandler != null) {
                uiHandler.afterRun(true);
            }
            log("---------------------------------------------------------------------------");
            log("Terminated running {} pipeline steps of {} in {} seconds.", stepSequence.size(), pipeline.getName(), getSeconds(allstart));
        } catch (RuntimeException e) {
            if (uiHandler != null) {
                uiHandler.afterRun(false);
            }
            exception(e);
            log("---------------------------------------------------------------------------");
            log("Failed executing step {} of {}, after {} seconds.", last, pipeline.getName(), getSeconds(allstart));
        }
    }

    // there are three nested levels where file locations can be specified:
    // HOST: the absolute path in the host system
    // TOOL_CONTAINER: the path inside the container that runs this tool
    // STEP_CONTAINER: the path inside the container that executes a simulated pipeline step
    static enum InceptionLevel {
        HOST, TOOL_CONTAINER, STEP_CONTAINER
    }

    private Path stepPath(PipelineStep step, InceptionLevel level) {
        String stepDir = dirName(step);
        switch (level) {
            case HOST:
                return Path.of(workdir).resolve(simulationdir).resolve(stepDir);
            case TOOL_CONTAINER:
                return Path.of(DOCKER_WORKDIR).resolve(simulationdir).resolve(stepDir);
            case STEP_CONTAINER:
                return Path.of(DOCKER_WORKDIR);
        }
        return null;
    }

    private Path inputDir(PipelineStep step, InceptionLevel level) {
        return stepPath(step, level).resolve(INPUT_FOLDER);
    }

    private Path outputDir(PipelineStep step, InceptionLevel level) {
        return stepPath(step, level).resolve(OUTPUT_FOLDER);
    }

    private Path inputSubDir(PipelineConnector connector, InceptionLevel level) {
        return inputDir(connector.getTarget(), level).resolve(connector.getSource().getId());
    }

    private void runStep(PipelineStep step, VariableResolver resolver) {
        cleanDirectories(step);
        step.getConfigInputs().forEach((name, data) -> writeData(step, name, yaml2json(data, resolver)));
        step.getInputs().forEach((name, connector) -> copyInput(step, name, connector));
        if (!runJob(step)) {
            fail("Execution of step "+step.getId()+" failed.");
        }
    }

    private byte[] yaml2json(byte[] data, VariableResolver resolver) {
        String input = new String(data);
        String resolved = List.of(input.split(("\n"))).stream().map(resolver::substituteVariables).collect(Collectors.joining("\n"));
        if (resolved.isBlank()) {
            return "{}".getBytes();
        }
        ObjectMapper yamlReader = new ObjectMapper(new YAMLFactory());
        ObjectMapper jsonWriter = new ObjectMapper();
        try {
            Map<String, Object> object = yamlReader.readValue(resolved, Map.class);
            String jsonStr = jsonWriter.writeValueAsString(object);
            return jsonStr.getBytes();
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    private void copyInput(final PipelineStep step, final String nameInTarget, final PipelineConnector connector) {
        String filename = connector.getFilename();
        final Path source = outputDir(connector.getSource(), TOOL_CONTAINER).resolve(filename);
        final Path target = inputSubDir(connector, TOOL_CONTAINER).resolve(filename);
        log("Copy {} bytes from {} to {}", source.toFile().length(), source, target);
        makePath(target);
        onException(() -> Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING))
                .fail("Could not copy file "+source+" to "+target);
    }

    private String dirName(final PipelineStep step) {
        int strlen = Integer.toString(stepNumbers.size()).length();
        StringBuilder numStr = new StringBuilder(Integer.toString(stepNumbers.get(step)));
        while (numStr.length() < strlen) {
            numStr.insert(0, "0");
        }
        return numStr+"-"+step.getId();
    }

    private boolean runJob(final PipelineStep step) {
        DockerImageRunner runner = getRunner(step);
        return runner.run(stepPath(step, HOST).toAbsolutePath().toString());
    }

    private void cleanDirectories(final PipelineStep step) {
        removeRecursively(inputDir(step, TOOL_CONTAINER).toFile());
        removeRecursively(outputDir(step, TOOL_CONTAINER).toFile());
    }

    protected DockerImageRunner getRunner(PipelineStep step) {
        return new DockerImageRunner(step.getDockerImage(), createArgs(step), false, false, null, credentialsMount);
    }

    private List<String> createArgs(PipelineStep step) {
        List<String> res = new ArrayList<>();
        res.add("--config");
        res.add(inputDir(step, STEP_CONTAINER).resolve("config.json").toString());
        step.getInputs().values().forEach(connector -> {
            res.add("--"+connector.getNameAtTarget());
            res.add(inputSubDir(connector, STEP_CONTAINER).resolve(connector.getFilename()).toString());
        });
        Map<String, Set<String>> outputs = new LinkedHashMap<>();
        step.getOutputs().forEach(connector -> {
            String arg = connector.getNameAtSource();
            String filename = outputDir(step, STEP_CONTAINER).resolve(connector.getFilename()).toString();
            outputs.putIfAbsent(arg, new LinkedHashSet<>());
            outputs.get(arg).add(filename);
        });
        outputs.forEach((arg, paths) ->{
            res.add("--"+arg);
            res.add(paths.stream().collect(Collectors.joining(",")));
        });
        return res;
    }


    private void writeData(final PipelineStep step, final String name, final byte[] data) {
        final Path target = inputDir(step, TOOL_CONTAINER).resolve(name);
        log("Writing {} bytes to {}", data.length, target);
        makePath(target);
        onException(() -> Files.write(target, data)).fail("Could not write file "+ target);
    }

    private void makePath(final Path path) {
        File dir= path.getParent().toFile();
        if (!dir.exists()) {
            log("Creating directory "+dir);
            if (!dir.mkdirs()) {
                fail("Could not create directory "+dir);
            }
        }
    }

    private long getSeconds(final long start) {
        return Math.round((System.currentTimeMillis()-start)*0.001);
    }

    private void removeRecursively(final File dir) {
        log("Removing files in {}", dir);
        final File[] files = dir.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    removeRecursively(file);
                } else {
                    onException(() -> Files.delete(file.toPath())).fail("Could not delete file " + file);
                }
            }
        }
    }

}
