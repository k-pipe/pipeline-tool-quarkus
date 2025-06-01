package org.jkube.pipeline.definition;

import org.jkube.job.DockerImage;
import org.jkube.markdown.MarkdownParsingException;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class PipelineStep {
	private final String id;
	private final Map<String, PipelineConnector> inputs;
	private final List<PipelineConnector> outputs;
	private final Map<String, byte[]> configInputs;
	private DockerImage image;

	public PipelineStep(final String id) {
		this.id = id;
		this.inputs = new LinkedHashMap<>();
		this.outputs = new ArrayList<>();
		this.configInputs = new LinkedHashMap<>();
	}

	public String getId() {
		return id;
	}

	public Map<String, PipelineConnector> getInputs() {
		return inputs;
	}

	public List<PipelineConnector> getOutputs() {
		return outputs;
	}

	public Map<String, byte[]> getConfigInputs() {
		return configInputs;
	}

	public DockerImage getDockerImage() {
		return image;
	}

	public void setImage(final DockerImage image) {
		this.image = image;
	}

	public static PipelineConnector addTransition(final PipelineStep from, final PipelineStep to, final String nameFrom, String nameTo, String filename) {
		if (to.inputs.containsKey(nameTo)) {
			throw new MarkdownParsingException("Incoming link with name "+nameTo+" occurs twice in step "+to.id);
		}
		PipelineConnector connector = new PipelineConnector(from, to, nameFrom, nameTo, filename);
		to.inputs.put(nameTo, connector);
		from.outputs.add(connector);
		return connector;
	}

	public void setConfigInput(final String inputName, final byte[] data) {
		if (configInputs.containsKey(inputName)) {
			throw new MarkdownParsingException("Config Input with name "+inputName+" occurs twice in step "+id);
		}
		configInputs.put(inputName, data);
	}

}
