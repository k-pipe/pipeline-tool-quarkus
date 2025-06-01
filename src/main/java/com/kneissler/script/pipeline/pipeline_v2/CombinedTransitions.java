package com.kneissler.script.pipeline.pipeline_v2;

import org.jkube.pipeline.definition.PipelineConnector;
import org.jkube.pipeline.definition.PipelineStep;

import java.util.*;
import java.util.stream.Collectors;

import static org.jkube.application.Application.fail;
import static org.jkube.logging.Log.log;

public class CombinedTransitions {

    private final Set<PipelineConnector> combiningConnectors = new HashSet<>();
    private final List<StepGroup> groups = new ArrayList<>();

    public void add(PipelineConnector connector) {
        combiningConnectors.add(connector);
    }

    public void determineGroups() {
        combiningConnectors.forEach(this::updateGroup);
        checkConsistency();
        logFoundGroups();
    }

    public List<StepGroup> getGroups() {
        return groups;
    }

    private void updateGroup(PipelineConnector pipelineConnector) {
        PipelineStep from = pipelineConnector.getSource();
        PipelineStep to = pipelineConnector.getTarget();
        Optional<StepGroup> fromGroup = findGroup(from);
        Optional<StepGroup> toGroup = findGroup(to);
        if (fromGroup.isPresent() && toGroup.isPresent()) {
            fromGroup.get().merge(toGroup.get());
            groups.remove(toGroup.get());
        } else if (fromGroup.isPresent()) {
            fromGroup.get().add(to);
        } else if (toGroup.isPresent()) {
            toGroup.get().add(from);
        } else {
            groups.add(new StepGroup(from, to));
        }
    }

    private Optional<StepGroup> findGroup(PipelineStep step) {
        return groups.stream().filter(g -> g.contains(step)).findFirst();
    }

    private void checkConsistency() {
        groups.forEach(this::checkConsistency);
    }

    private void checkConsistency(StepGroup group) {
        group.stepsStream().forEach(step -> step.getOutputs().stream()
                .filter(c -> group.contains(c.getTarget()))
                .forEach(this::checkIsCombining));
    }

    private void checkIsCombining(PipelineConnector pipelineConnector) {
        if (!combiningConnectors.contains(pipelineConnector)) {
            fail("Transition must be marked combining: "+pipelineConnector.getSource().getId()+" -> "+pipelineConnector.getTarget().getId());
        }
    }

    public void logFoundGroups() {
        log("Found {} groups of combined steps:", groups.size());
        groups.forEach(g -> log("{}", g.stepsStream().map(PipelineStep::getId).collect(Collectors.joining(", "))));
    }

}
