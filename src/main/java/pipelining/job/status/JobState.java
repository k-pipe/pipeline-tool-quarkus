package pipelining.job.status;

public enum JobState {
	/** Job description stored, but kubernetes job not created, yet */
	CREATED,
	/** Kubernetes job exists */
	SCHEDULED,
	/** Job's init container started  */
	PREPARING,
	/** Job's init container finished  */
	INITIALIZED,
	/** Job's monitor container started  */
	MONITORED,
	/** Job's main container started  */
	STARTED,
	/** Business logics in main container is running  */
	RUNNING,
	/** Job has terminated (successfully or with non-resource-related failure
	 *  or it was stopped due to timeout) */
	TERMINATED,
	/** Job was marked to be restarted, will go back to state SCHEDULED
	 * Note: Jobs that failed due to insufficient resources will get this state
	 * automatically (instead of TERMINATED). Jobs may also be manually assigned
	 * the status RESTART. This can be done to indicate that the job will be
	 * restarted (note: the mere marking of the JobState as RESTART does not trigger the restart
	 * by itself, the job has to be put into the Job-Start-Folder to get eventually restarted).
	 */
	RESTART
}
