package pipelining.markdown.pipeline.v1;

import pipelining.pipeline.definition.PipelineStep;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class PipelinePlantUML {

    private final List<String> lines;
    private final LinkedHashMap<String, Object> lines2parsedObjects;

    private final Map<String, PipelineStep> steps;
    private PipelineStep input;
    private PipelineStep output;

    public PipelinePlantUML(List<String> lines) {
        this.lines = lines;
        this.lines2parsedObjects = new LinkedHashMap<>();
        this.steps = new LinkedHashMap<>();
        this.input = null;
        this.output = null;
    }

    public List<String> getLines() {
        return lines;
    }

    public LinkedHashMap<String, Object> getLines2parsedObjects() {
        return lines2parsedObjects;
    }


    public Map<String, PipelineStep> getSteps() {
        return steps;
    }

    public void setInput(PipelineStep input) {
        this.input = input;
    }

    public void setOutput(PipelineStep output) {
        this.output = output;
    }

    public PipelineStep getInput() {
        return input;
    }

    public PipelineStep getOutput() {
        return output;
    }

}
