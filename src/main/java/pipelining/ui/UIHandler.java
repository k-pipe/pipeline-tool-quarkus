package pipelining.ui;

import pipelining.ui.domain.JobState;
import pipelining.ui.domain.StepState;
import pipelining.pipeline.definition.PipelineConnector;
import pipelining.pipeline.definition.PipelineStep;
import pipelining.util.Utf8;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.*;

import static pipelining.logging.Log.log;
import static pipelining.logging.Log.onException;

public class UIHandler {

    private static final String FAILED = "FAILED.pipe";
    private final PipelineViewerClient pipelineViewer;
    private final PipelineVisualizer visualizer;
    private final boolean viewerActive;
    private final boolean updateImage;
    private final Path imageFile;
    private final String namespace;
    private final String run;

    public UIHandler(Path imageFile, String url, Map<String, Object> umlWithObjects,
                     String namespace, String runName) {
        this.imageFile = imageFile;
        this.updateImage = imageFile != null;
        this.namespace = namespace;
        this.run = runName;
        this.visualizer = new PipelineVisualizer(umlWithObjects);
        this.viewerActive = url != null;
        pipelineViewer = viewerActive ? new PipelineViewerClient(url) : null;
        if (viewerActive) {
            log("Writing data to pipeline viewer");
        }
    }

    public void beforeRun(Map<PipelineStep, Integer> stepNumbers) {
        if (!viewerActive) {
            return;
        }
        JobState state = JobState.RUNNING;
        log("Writing state "+state+" to pipeline viewer ("+namespace+" "+run+")");
        pipelineViewer.setStepNumbers(stepNumbers);
        pipelineViewer.setState(namespace, run, state);
    }

    public void beforeStep(PipelineStep step) {
        if (updateImage) {
            visualizer.setState(step, StepState.RUNNING);
            updateImage();
        }
        if (!viewerActive) {
            return;
        }
        log("Writing config for step "+step.getId()+" to pipeline viewer ("+namespace+" "+run+")");
        String config = getInputAsString(step, "config.json");
        //System.out.println(config);
        pipelineViewer.setStepConfig(namespace, run, step, config);
        log("Sending image before step {}", step.getId());
        pipelineViewer.setImage(namespace, run, visualizer.getImage(), visualizer.getImageType());
        pipelineViewer.setStepState(namespace, run, step, StepState.RUNNING);
        pipelineViewer.deleteAllPipelineData(namespace, run, step.getId());
    }

    public void afterStep(Path outputDir, PipelineStep step) {
        if (!updateImage) {
            return;
        }
        groupByFile(step.getOutputs()).forEach((filename, connectors) -> {
                InputStream in = toStream(outputDir, filename);
                long num = viewerActive
                        ? pipelineViewer.sumbitPipelineData(in, namespace, run, connectors, determineUrl(step, filename))
                        : countLines(in);
                visualizer.setNumItems(Long.toString(num), connectors);
                if (filename.equals(FAILED)) {
                    visualizer.setNumFailed(step, num);
                }
            }
        );
        String error = readAllLines(outputDir, "error").trim();
        String state = readAllLines(outputDir, "../state").trim();
        String log = readAllLines(outputDir, "log").trim();
        log("State: {}, Error: {}", state, error);
        if (error.isBlank() && !state.equals("SUCCESS")) {
            error = "FAILED, BUT NO ERROR LOGS CREATED!";
        }
        boolean success = error.isBlank();
        StepState stepstate = success ? StepState.SUCCESS : StepState.ERROR;
        visualizer.setState(step, stepstate);
        updateImage();
        log("Sending image after step {}", step.getId());
        if (!viewerActive) {
            return;
        }
        pipelineViewer.setImage(namespace, run, visualizer.getImage(), visualizer.getImageType());
        log("Writing step results to pipeline viewer ("+namespace+" "+run+")");
        pipelineViewer.setStepInfo(namespace, run, step,
                stepstate,
                log,
                success ? null : error);
    }

    private String determineUrl(PipelineStep step, String filename) {
        return "dummyurl/"+step.getId()+"/output/"+filename;
    }

    private String readAllLines(Path dir, String fileName) {
        File file = dir.resolve(fileName).toFile();
        return file.exists()
                ? onException(() -> Utf8.read(new FileInputStream(file))).fail("could not read file "+file+" in folder "+dir)
                : "";
    }

    private Map<String, List<PipelineConnector>> groupByFile(List<PipelineConnector> outputs) {
        Map<String, List<PipelineConnector>> res = new HashMap<>();
        outputs.forEach(pc -> {
            res.putIfAbsent(pc.getNameAtSource(), new ArrayList<>());
            res.get(pc.getNameAtSource()).add(pc);
        });
        return res;
    }

    private InputStream toStream(Path outputDir, String filename) {
        return onException(() -> new FileInputStream(outputDir.resolve(filename).toFile()))
                .fail("could not open file "+filename+" in folder "+outputDir);
    }

    private String getInputAsString(PipelineStep step, String key) {
        byte[] data = step.getConfigInputs().get(key);
        return data == null ? null : new String(data, StandardCharsets.UTF_8);
    }

    public void afterRun(boolean succeeded) {
        if (updateImage) {
            updateImage();
        }
        if (!viewerActive) {
            return;
        }
        JobState state = succeeded ? JobState.SUCCEEDED : JobState.FAILED;
        log("Writing state "+state+" to pipeline viewer ("+namespace+" "+run+")");
        pipelineViewer.setState(namespace, run, state);
        log("Sending final status image");
        pipelineViewer.setImage(namespace, run, visualizer.getImage(), visualizer.getImageType());
    }

    private void updateImage() {
        visualizer.storeImage(imageFile);
    }

    private long countLines(InputStream in) {
        long num = 0;
        Iterator<String> i = Utf8.lineIterator(in);
        while (i.hasNext()) {
            i.next();
            num++;
        }
        return num;
    }

}
