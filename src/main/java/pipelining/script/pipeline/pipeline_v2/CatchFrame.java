package pipelining.script.pipeline.pipeline_v2;

import pipelining.pipeline.definition.PipelineStep;

import java.util.ArrayList;
import java.util.List;

public class CatchFrame {

	private static final String FAILED = "FAILED.pipe";

	private final List<PipelineStep> steps = new ArrayList<>();

	public void addCatch(final PipelineStep step) {
		steps.add(step);
	}

	public void createTransitions(final PipelineStep from, PipelinePlantUmlV2 puml) {
		steps.forEach(to -> PipelinePumlParserV2.addTransition(from, to, FAILED, FAILED, "DUMMY3", puml));
	}

	public List<PipelineStep> getSteps() {
		return steps;
	}

}
