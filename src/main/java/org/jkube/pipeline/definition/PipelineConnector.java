package org.jkube.pipeline.definition;

public class PipelineConnector {
	private final PipelineStep source;
	private final PipelineStep target;
	private String nameAtSource;
	private String nameAtTarget;

	private String filename;

	public PipelineConnector(final PipelineStep source, final PipelineStep target, final String nameAtSource, final String nameAtTarget, final String filename) {
		this.source = source;
		this.target = target;
		this.nameAtSource = nameAtSource;
		this.nameAtTarget = nameAtTarget;
		this.filename = filename;
	}

	public PipelineStep getSource() {
		return source;
	}

	public PipelineStep getTarget() {
		return target;
	}

	public String getNameAtSource() {
		return nameAtSource;
	}

	public String getNameAtTarget() {
		return nameAtTarget;
	}

	public void setNameAtSource(final String nameAtSource) {
		this.nameAtSource = nameAtSource;
	}

	public void setNameAtTarget(final String nameAtTarget) {
		this.nameAtTarget = nameAtTarget;
	}

	public String getFilename() {
		return filename;
	}
}
