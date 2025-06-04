package pipelining.job.specification;

public class RetentionMap {
	Retention success;
	Retention warning;
	Retention stopped;
	Retention failed;

	public RetentionMap() {
	}

	public RetentionMap(final Retention success, final Retention warning,
			final Retention stopped, final Retention failed) {
		this.success = success;
		this.warning = warning;
		this.stopped = stopped;
		this.failed = failed;
	}

	public Retention getSuccess() {
		return success;
	}

	public Retention getWarning() {
		return warning;
	}

	public Retention getStopped() {
		return stopped;
	}

	public Retention getFailed() {
		return failed;
	}

}
