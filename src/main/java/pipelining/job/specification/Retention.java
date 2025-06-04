package pipelining.job.specification;

public class Retention {

	public static final int FOREVER = -1;

	private int hoursToKeepJob;
	private int hoursToKeepLogs;
	private int hoursToKeepOutput;

	public Retention() {
	}

	public Retention(final int hoursToKeepJob, final int hoursToKeepLogs, final int hoursToKeepOutput) {
		this.hoursToKeepJob = hoursToKeepJob;
		this.hoursToKeepLogs = hoursToKeepLogs;
		this.hoursToKeepOutput = hoursToKeepOutput;
	}

	public int getHoursToKeepJob() {
		return hoursToKeepJob;
	}

	public int getHoursToKeepLogs() {
		return hoursToKeepLogs;
	}

	public int getHoursToKeepOutput() {
		return hoursToKeepOutput;
	}

	public void setHoursToKeepJob(int hoursToKeepJob) {
		this.hoursToKeepJob = hoursToKeepJob;
	}

	public void setHoursToKeepLogs(int hoursToKeepLogs) {
		this.hoursToKeepLogs = hoursToKeepLogs;
	}

	public void setHoursToKeepOutput(int hoursToKeepOutput) {
		this.hoursToKeepOutput = hoursToKeepOutput;
	}
}
