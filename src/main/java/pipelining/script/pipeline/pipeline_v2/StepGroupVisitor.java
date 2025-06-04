package pipelining.script.pipeline.pipeline_v2;

import pipelining.pipeline.definition.PipelineConnector;
import pipelining.pipeline.definition.PipelineStep;
import pipelining.util.Expect;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static pipelining.application.Application.fail;
import static pipelining.logging.Log.error;
import static pipelining.logging.Log.log;

public class StepGroupVisitor {

    private final List<StepGroup> allGroups;
    private final LinkedHashSet<StepGroup> groupSequence = new LinkedHashSet<>();
    private final Set<PipelineStep> visited = new HashSet<>();

    public StepGroupVisitor(List<StepGroup> allGroups) {
        this.allGroups = allGroups;
    }

    public Collection<StepGroup> getGroupSequence() {
        return groupSequence;
    }

    public void logGroupSequenceAndCheckConsistency(List<PipelineStep> steps) {
        log("Step Group Sequence determined: " + groupSequence.stream().map(StepGroup::toString).collect(Collectors.joining(" -> ")));
        if (visited.size() != steps.size()) {
            Set<PipelineStep> missing = new LinkedHashSet<>(steps);
            missing.removeAll(visited);
            error("Some steps were not visited: "+missing.stream().map(PipelineStep::getId).collect(Collectors.joining(", ")));
        }
    }

    public void visitAll(PipelineStep startStep) {
        visitGroup(findGroup(startStep));
        visitGroupsExcept(null);
    }

    public void visitAllBatched(PipelineStep startStep, PipelineStep batchingStep) {
        StepGroup startGroup = findGroup(startStep);
        StepGroup batchingGroup = findGroup(batchingStep);
        // visit group with start step
        visitGroup(startGroup);
        if (!batchingGroup.equals(startGroup)) {
            // find all groups that can be visited before the one that contains the batching step
            visitGroupsExcept(batchingGroup);
            // visit the group that contains the batching step
            visitGroup(batchingGroup);
        }
        // verify that there is no connection from visited steps (except the batching step) to unvisited steps
        checkStepSplits(batchingStep);
        // visit all remaining groups
        visitGroupsExcept(null);
    }

    private StepGroup findGroup(PipelineStep step) {
        return Expect.present(allGroups.stream().filter(g -> g.contains(step)).findFirst())
                .elseFail("no group found containing step "+step.getId());
    }

    private void visitGroupsExcept(StepGroup excluded) {
        Optional<StepGroup> found;
        do {
            found = findNextVisitableGroup(excluded);
            found.ifPresent(this::visitGroup);
        } while(found.isPresent());
    }

    public void visitGroup(StepGroup group) {
        groupSequence.add(group);
        List<PipelineStep> visitingSequence = new ArrayList<>();
        Optional<PipelineStep> found;
        do {
            found = findNextVisitableStep(group.stepsStream());
            found.ifPresent(step -> {
                visitingSequence.add(step);
                visited.add(step);
            });
        } while(found.isPresent());
        group.setSteps(visitingSequence);
    }

    private Optional<StepGroup> findNextVisitableGroup(StepGroup excluded) {
        return allGroups.stream().filter(g -> !g.equals(excluded) && canVisitGroup(g)).findFirst();
    }

    private Optional<PipelineStep> findNextVisitableStep(Stream<PipelineStep> steps) {
        return steps.filter(this::canVisitStep).findFirst();
    }

    private boolean canVisitGroup(StepGroup group) {
        return !groupSequence.contains(group) &&
                group.stepsStream()
                        .filter(step -> hasMissingInput(step, group))
                        .findFirst()
                        .isEmpty();
    }

    private boolean canVisitStep(PipelineStep step) {
        return !visited.contains(step) &&
                step.getInputs().values().stream()
                        .filter(this::isInputMissing)
                        .findFirst()
                        .isEmpty();
    }

    private boolean hasMissingInput(PipelineStep step, StepGroup group) {
        return step.getInputs().values().stream()
                        .anyMatch(pc -> isInputOutSideGroupMissing(pc.getSource(), group));
    }

    private boolean isInputMissing(PipelineConnector pipelineConnector) {
        return !visited.contains(pipelineConnector.getSource());
    }

    private boolean isInputOutSideGroupMissing(PipelineStep from, StepGroup group) {
        return !group.contains(from) && !visited.contains(from);

    }

    private void checkStepSplits(PipelineStep batchingStep) {
        groupSequence.forEach(g -> g.stepsStream()
                .filter(visited::contains)
                .filter(step -> !step.equals(batchingStep))
                .forEach(this::checkNoConnectionToNotVisited));
   }

    private void checkNoConnectionToNotVisited(PipelineStep step) {
        if (step.getOutputs().stream().anyMatch(pc -> !visited.contains(pc.getTarget()))) {
            fail("There is a connection by-passing the batched connector");
        }
    }

}
