package pipelining.job.status;

public enum WrapperState {
	RUNNING, SUCCESS, ERROR, LOG_TIMEOUT, OVERALL_TIMEOUT, POD_SHUTTING_DOWN;
}
