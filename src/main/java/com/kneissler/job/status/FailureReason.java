package com.kneissler.job.status;

public enum FailureReason {
	/** Could not read job description json */
	JOB_DESCRIPTION_NOT_FOUND,
	/** Inputs could not be downloaded */
	DOWNLOAD_FAILED,
	/** Job business logic has returned a non-zero exit code but some warnings were issued */
	BUSINESS_LOGIC_FAILED,
	/** Init container had an error */
	INIT_ERROR,
	/** Log wrapper caused an error */
	WRAPPER_ERROR,
	/** Monitor sidecar caused an error */
	MONITOR_ERROR,
	/** Job Iexecution was stopped because no new log chars were received for specified time */
	INACTIVE_TMEOUT,
	/** Job execution was stopped because overall job time limit was exceeded  */
	OVERALL_TIMEOUT,
	/** Job result INSUFFICIENT_RESOURCES due to out of memory  */
	OUT_OF_MEMORY,
	/** Job result INSUFFICIENT_RESOURCES due to disk full  */
	DISK_FULL,
	/** Outputs could not be uploaded */
	UPLOAD_FAILED,
	/** Update job data failed */
	UPDATE_JOB_DATA_FAILED,
	/** Job stopped because pod was shutdown  */
	POD_SHUTDOWN,
}
