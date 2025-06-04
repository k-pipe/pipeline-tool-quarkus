package pipelining.pipeline.definition;

import pipelining.markdown.MarkdownParsingException;
import pipelining.logging.Log;

import java.util.*;
import java.util.stream.Collectors;

public class Pipeline {

	private final String name;
	private final List<String> description;
	private final List<PipelineStep> steps;
	private PipelineStep first;

	public Pipeline(final String name, final List<String> description, final Collection<PipelineStep> steps, PipelineStep input, PipelineStep output) {
		this.name = name;
		this.description = description;
		this.steps = new ArrayList<>(steps);
		this.first = determineFirstStep(input);
		determineSequence();
	}

	private PipelineStep determineFirstStep(final PipelineStep startStep) {
		if ((startStep != null) && !startStep.getOutputs().isEmpty()) {
			if (startStep.getOutputs().size() > 1) {
				throw new RuntimeException("Pipeline start has multiple outgoing connectors");
			}
			return startStep.getOutputs().iterator().next().getTarget();
		}
		first = null;
		for (PipelineStep step : steps) {
			if (step.getInputs().isEmpty()) {
				if (first != null) {
					throw new MarkdownParsingException("Multiple start candidates in pipeline graph");
				}
				first = step;
			}
		}
		if (first == null) {
			throw new MarkdownParsingException("No start node in pipeline graph");
		}
		return first;
	}

	public List<PipelineStep> determineSequence() {
		Set<PipelineStep> visited = new LinkedHashSet<>();
		visited.add(first);
		while(visitNext(visited));
		if (visited.size() != getSteps().size()) {
			Set<PipelineStep> missing = new LinkedHashSet<>(getSteps());
			missing.removeAll(visited);
			Log.error("Some steps were not visited: "+missing.stream().map(PipelineStep::getId).collect(Collectors.joining(", ")));
		}
		System.out.println("Step Sequence determined: "+visited.stream().map(PipelineStep::getId).collect(Collectors.joining(" -> ")));
		return new ArrayList<>(visited);
	}

	private boolean visitNext(final Set<PipelineStep> visited) {
		Optional<PipelineStep> next = steps.stream().filter(candidate -> canBeVisitedNext(candidate, visited)).findFirst();
		next.ifPresent(visited::add);
		return next.isPresent();
	}

	private boolean canBeVisitedNext(final PipelineStep candidate, final Set<PipelineStep> visited) {
		if (visited.contains(candidate)) {
			return false;
		}
		return candidate.getInputs().values().stream().filter(input -> !visited.contains(input.getSource())).findFirst().isEmpty();
	}

	public List<PipelineStep> getSteps() {
		return steps;
	}

	public String getName() {
		return name;
	}

	public List<String> getDescription() {
		return description;
	}

	public PipelineStep getStartStep() {
		return first;
	}
}
