package pipelining.job.status;

public class ResourceSettings {

	private static final String CPU_UNIT = "m";
	private static final String DISK_UNIT = "M";
	private static final String MEMORY_UNIT = "M";
	private static final String GPU_UNIT = "";

	int cpu;
	int memory;
	int disk;
	int gpu;

	public int getCpu() {
		return cpu;
	}

	public int getMemory() {
		return memory;
	}

	public int getDisk() {
		return disk;
	}

	public int getGpu() {
		return gpu;
	}

	public String cpuSpec() {
		return Integer.toString(cpu)+CPU_UNIT;
	}

	public String memorySpec() {
		return Integer.toString(memory)+MEMORY_UNIT;
	}

	public String diskSpec() {
		return Integer.toString(disk)+DISK_UNIT;
	}

	public String gpuSpec() {
		return Integer.toString(gpu)+GPU_UNIT;
	}

	public void setCpu(final int cpu) {
		this.cpu = cpu;
	}

	public void setMemory(final int memory) {
		this.memory = memory;
	}

	public void setDisk(final int disk) {
		this.disk = disk;
	}

	public void setGpu(final int gpu) {
		this.gpu = gpu;
	}

	@Override
	public String toString() {
		return "cpu=" + cpu +
				", memory=" + memory +
				", disk=" + disk +
				", gpu=" + gpu;
	}
}
