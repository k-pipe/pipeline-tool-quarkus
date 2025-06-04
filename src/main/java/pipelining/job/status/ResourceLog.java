package pipelining.job.status;

import java.util.ArrayList;
import java.util.List;

public class ResourceLog {
	private final List<Float> cpu = new ArrayList<>();
	private final List<Float> ram = new ArrayList<>();
	private final List<Float> disk = new ArrayList<>();

	public List<Float> getCpu() {
		return cpu;
	}

	public List<Float> getRam() {
		return ram;
	}

	public List<Float> getDisk() {
		return disk;
	}
}
