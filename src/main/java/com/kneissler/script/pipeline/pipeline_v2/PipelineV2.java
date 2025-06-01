package com.kneissler.script.pipeline.pipeline_v2;

import com.kneissler.script.pipeline.PipelineStepSettings;
import org.jkube.pipeline.definition.Pipeline;
import org.jkube.pipeline.definition.PipelineConnector;
import org.jkube.pipeline.definition.PipelineStep;

import java.util.*;

import static org.jkube.logging.Log.log;

public class PipelineV2 extends Pipeline {

    private final PipelineStep input;
    private final PipelineStep output;
    private final Map<PipelineStep, PipelineStepSettings> stepSettings;
    private final PipelineConnector batchedConnector;
    private final CombinedTransitions combinedTransitions;

    public PipelineV2(final String name, final List<String> description, final Collection<PipelineStep> steps, PipelineStep input, PipelineStep output,
                      Map<PipelineStep, PipelineStepSettings> stepSettings, PipelineConnector batchedConnector,
                      CombinedTransitions combinedTransitions) {
        super(name, description, steps, input, output);
        this.input = input;
        this.output = output;
        this.stepSettings = stepSettings;
        this.batchedConnector = batchedConnector;
        this.combinedTransitions = combinedTransitions;
    }

    @Override
    public List<PipelineStep> determineSequence() {
        Collection<StepGroup> groups = determineSequence(false, false);
        List<PipelineStep> res = new ArrayList<>();
        groups.forEach(g -> g.stepsStream().forEach(res::add));
        return res;
    }

    public Collection<StepGroup> determineSequence(boolean batched, boolean groupCombined) {
        List<StepGroup> groups = new ArrayList<>();
        if (groupCombined) {
            groups.addAll(combinedTransitions.getGroups());
        }
        addSingletonGroupsForMissing(groups, getSteps());
        log("After adding single steps, there are {} groups", groups.size());
        StepGroupVisitor visitor = new StepGroupVisitor(groups);
        if (batched) {
            visitor.visitAllBatched(getStartStep(), batchedConnector.getSource());
        } else {
            visitor.visitAll(getStartStep());
        }
        visitor.logGroupSequenceAndCheckConsistency(getSteps());
        return visitor.getGroupSequence();
    }

    private void addSingletonGroupsForMissing(List<StepGroup> groups, List<PipelineStep> steps) {
        Set<PipelineStep> inGroups = new HashSet<>();
        groups.forEach(g -> g.stepsStream().forEach(inGroups::add));
        steps.forEach(step -> {
            if (!inGroups.contains(step)) {
                groups.add(new StepGroup(step));
            }
        });
    }

    private boolean canBeVisitedNext(final PipelineStep candidate, final Set<PipelineStep> visited, boolean allowBatchedStep) {
        if (candidate.equals(batchedConnector.getSource()) && !allowBatchedStep) {
            return false;
        }
        if (visited.contains(candidate)) {
            return false;
        }
        return candidate.getInputs().values().stream().filter(input -> !visited.contains(input.getSource())).findFirst().isEmpty();
    }

    public PipelineStep getInput() {
        return input;
    }

    public PipelineStep getOutput() {
        return output;
    }

    public Map<PipelineStep, PipelineStepSettings> getStepSettings() {
        return stepSettings;
    }

    public PipelineStepSettings getStepSettings(PipelineStep step) {
        return stepSettings.get(step);
    }

    public PipelineConnector getBatchingConnector() {
        return batchedConnector;
    }

    public List<PipelineStep> determineUnbatchedSteps(List<PipelineStep> steps) {
        List<PipelineStep> res = new ArrayList<>();
        for (PipelineStep c : steps) {
            res.add(c);
            if (c.equals(batchedConnector.getSource())) {
                return res;
            }
        }
        throw new RuntimeException("Could not find batchedConnector source");
    }

    public List<PipelineStep> determineBatchedSteps(List<PipelineStep> steps) {
        List<PipelineStep> res = new ArrayList<>();
        boolean found = false;
        for (PipelineStep s : steps) {
            if (s == batchedConnector.getTarget()) {
                found = true;
            }
            if (found) {
                res.add(s);
            }
        }
        if (res.isEmpty()) {
            throw new RuntimeException("Could not find batchedConnector target");
        }
        return res;
    }

    public boolean isMultiBatch() {
        return batchedConnector != null;
    }
}
