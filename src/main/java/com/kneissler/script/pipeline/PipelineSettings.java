package com.kneissler.script.pipeline;

public class PipelineSettings {
	private boolean debug;
	private boolean production;
	private boolean multiBatch;

	public void setDebug(final boolean value) {
		this.debug = value;
	}

	public void setProduction(final boolean value) {
		this.production = value;
	}

	public void setMultiBatch(final boolean value) {
		this.multiBatch = value;
	}

	public boolean isDebug() {
		return debug;
	}

	public boolean isProduction() {
		return production;
	}

	public boolean isMultiBatch() {
		return multiBatch;
	}
}
