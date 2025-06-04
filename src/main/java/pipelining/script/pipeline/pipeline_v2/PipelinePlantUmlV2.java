package pipelining.script.pipeline.pipeline_v2;

import pipelining.script.pipeline.PipelineStepSettings;
import pipelining.markdown.MarkdownParsingException;
import pipelining.markdown.pipeline.v1.PipelinePlantUML;
import pipelining.pipeline.definition.PipelineConnector;
import pipelining.pipeline.definition.PipelineStep;

import java.util.*;

import static pipelining.logging.Log.log;

public class PipelinePlantUmlV2 extends PipelinePlantUML {

    private static final String SOURCE_PIPENAME_SEPARATOR = "-";

    private final List<PipelineConnector> createdConnectors = new ArrayList<>();
    private final CombinedTransitions combinedTransitions = new CombinedTransitions();

    private final CatchFrameHandler frameHandler = new CatchFrameHandler();

    private PipelineConnector batchedConnector;

    public PipelinePlantUmlV2(List<String> lines) {
        super(lines);
    }

    public void init() {
        getFrameHandler().createCatchTransitions(this);
        makeInputNamesUnique();
        connectors().forEach(this::registerConnector);
        getCombinedTransitions().determineGroups();

    }

    public List<PipelineConnector> connectors() {
        return createdConnectors;
    }

    public PipelineConnector getBatchedConnector() {
        return batchedConnector;
    }

    public void setBatchedConnector(PipelineConnector batchedConnector) {
        this.batchedConnector = batchedConnector;
    }

    public CombinedTransitions getCombinedTransitions() {
        return combinedTransitions;
    }

    public CatchFrameHandler getFrameHandler() {
        return frameHandler;
    }

    private void makeInputNamesUnique() {
        Map<PipelineStep, List<PipelineConnector>> targetSources = new LinkedHashMap<>();
        connectors().forEach(c -> {
            targetSources.putIfAbsent(c.getTarget(), new ArrayList<>());
            targetSources.get(c.getTarget()).add(c);
        });
        targetSources.forEach(this::makeInputNamesUnique);
    }

    private void makeInputNamesUnique(final PipelineStep step, final List<PipelineConnector> inputs) {
        if (namesNotUnique(inputs)) {
            log("Adding source prefix for in step {}",step.getId());
            inputs.forEach(this::addInputToNameAtTarget);
        }
    }

    private boolean namesNotUnique(final List<PipelineConnector> inputs) {
        Set<String> names = new HashSet<>();
        for (PipelineConnector c : inputs) {
            if (!names.add(c.getNameAtTarget())) {
                return true;
            }
        }
        return false;
    }

    private void addInputToNameAtTarget(final PipelineConnector connector) {
        String newName = connector.getSource().getId()+ SOURCE_PIPENAME_SEPARATOR +connector.getNameAtTarget();
        log(connector.getNameAtTarget()+" --> "+newName);
        connector.setNameAtTarget(newName);
    }

    private void registerConnector(PipelineConnector c) {
        if (c.getTarget().getInputs().containsKey(c.getNameAtTarget())) {
            throw new MarkdownParsingException("Incoming link with name " + c.getNameAtTarget()+ " occurs twice in step " + c.getTarget().getId());
        }
        c.getTarget().getInputs().put(c.getNameAtTarget(), c);
        c.getSource().getOutputs().add(c);
    }

    public PipelineV2 createPipeline(String title, List<String> description, Map<PipelineStep, PipelineStepSettings> stepSettings) {
        return new PipelineV2(
                title,
                description,
                getSteps().values(),
                getInput(),
                getOutput(),
                stepSettings,
                getBatchedConnector(),
                getCombinedTransitions());
    }

}
