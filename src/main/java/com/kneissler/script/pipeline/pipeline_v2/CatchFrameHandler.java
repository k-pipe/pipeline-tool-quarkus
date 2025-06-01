package com.kneissler.script.pipeline.pipeline_v2;

import org.jkube.markdown.MarkdownParsingException;
import org.jkube.pipeline.definition.PipelineStep;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

public class CatchFrameHandler {

	private final LinkedList<CatchFrame> activeFrames = new LinkedList<>();
	private final Map<PipelineStep, CatchFrame> step2frame = new HashMap<>();
	private final Map<String, CatchFrame> name2frame = new HashMap<>();

	public void assignToFrame(final PipelineStep step, final boolean withPriority) {
		if (!activeFrames.isEmpty()) {
			if (withPriority || !step2frame.containsKey(step)) {
				step2frame.put(step, activeFrames.getLast());
			}
		}
 	}

	public void openFrame(final String frameName) {
		if (name2frame.containsKey(frameName)) {
			throw new MarkdownParsingException("frame with this name exists already: "+frameName);
		}
		CatchFrame frame = new CatchFrame();
		name2frame.put(frameName, frame);
		activeFrames.addLast(frame);
	}

	public void closeFrame() {
		if (activeFrames.isEmpty()) {
			throw new MarkdownParsingException("no frame to close");
		}
		activeFrames.removeLast();
	}

	public CatchFrame setCatch(final String frameName, final PipelineStep step) {
		CatchFrame frame = name2frame.get(frameName);
		if (frame == null) {
			throw new MarkdownParsingException("no frame defined with name "+frameName);
		}
		frame.addCatch(step);
		return frame;
	}

	public void createCatchTransitions(final PipelinePlantUmlV2 puml) {
		step2frame.forEach((step, frame) -> frame.createTransitions(step, puml));
	}

}
