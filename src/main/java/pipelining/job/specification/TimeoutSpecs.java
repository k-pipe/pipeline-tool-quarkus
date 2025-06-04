package pipelining.job.specification;

public class TimeoutSpecs {
	private int maxInactiveInMinutes;
	private int maxTotalRuntimeInMinutes;

	public TimeoutSpecs() {
	}

	public TimeoutSpecs(final int maxInactiveInMinutes, final int maxTotalRuntimeInMinutes) {
		this.maxInactiveInMinutes = maxInactiveInMinutes;
		this.maxTotalRuntimeInMinutes = maxTotalRuntimeInMinutes;
	}

	public int getMaxInactiveInMinutes() {
		return maxInactiveInMinutes;
	}

	public int getMaxTotalRuntimeInMinutes() {
		return maxTotalRuntimeInMinutes;
	}

	public void setMaxInactiveInMinutes(int maxInactiveInMinutes) {
		this.maxInactiveInMinutes = maxInactiveInMinutes;
	}

	public void setMaxTotalRuntimeInMinutes(int maxTotalRuntimeInMinutes) {
		this.maxTotalRuntimeInMinutes = maxTotalRuntimeInMinutes;
	}
}
