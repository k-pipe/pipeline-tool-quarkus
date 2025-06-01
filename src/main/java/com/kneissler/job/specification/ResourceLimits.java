package com.kneissler.job.specification;

public class ResourceLimits {
	LimitRange cpu;
	LimitRange memory;
	LimitRange disk;
	LimitRange gpu;

	public ResourceLimits() {
	}

	public ResourceLimits(final LimitRange cpu, final LimitRange memory, final LimitRange disk, final LimitRange gpu) {
		this.cpu = cpu;
		this.memory = memory;
		this.disk = disk;
		this.gpu = gpu;
	}

	public LimitRange getCpu() {
		return cpu;
	}

	public LimitRange getMemory() {
		return memory;
	}

	public LimitRange getDisk() {
		return disk;
	}

	public LimitRange getGpu() {
		return gpu;
	}

	public void setCpu(LimitRange cpu) {
		this.cpu = cpu;
	}

	public void setMemory(LimitRange memory) {
		this.memory = memory;
	}

	public void setDisk(LimitRange disk) {
		this.disk = disk;
	}

	public void setGpu(LimitRange gpu) {
		this.gpu = gpu;
	}
}
