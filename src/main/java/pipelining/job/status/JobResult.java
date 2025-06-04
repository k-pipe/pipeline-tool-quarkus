package pipelining.job.status;

public enum JobResult {
	/** Job business logic and wrappers terminated successfully, no warnings were issued  */
	SUCCESS,
	/** Job business logic has returned zero exit code but some warnings were issued */
	WARNING,
	/** Job has been stopped due to timeout or pod shutdown */
	STOPPED,
	/** Job business logic has returned a non-zero exit code or some other problem occurred */
	FAILED
}
