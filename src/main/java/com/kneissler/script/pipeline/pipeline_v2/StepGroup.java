package com.kneissler.script.pipeline.pipeline_v2;

import org.jkube.pipeline.definition.PipelineStep;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class StepGroup {

    private List<PipelineStep> steps = new ArrayList<>();

    public StepGroup(PipelineStep... stepArray) {
        for (PipelineStep step : stepArray) {
            add(step);
        }
    }

    public void setSteps(List<PipelineStep> steps) {
        this.steps = steps;
    }

    public boolean contains(PipelineStep step) {
        return steps.contains(step);
    }

    public void merge(StepGroup other) {
        steps.addAll(other.steps);
    }

    public void add(PipelineStep step) {
        steps.add(step);
    }

    public Stream<PipelineStep> stepsStream() {
        return steps.stream();
    }

    @Override
    public String toString() {
        return "["+steps.stream().map(PipelineStep::getId).collect(Collectors.joining(", "))+"]";
    }
}
