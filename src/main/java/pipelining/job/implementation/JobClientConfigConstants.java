package pipelining.job.implementation;

public class JobClientConfigConstants {


	// configuration key for host name and jwt token
	public static final String HOST = "host";
	public static final String JWT_TOKEN = "jwt";

	// configuration key for java class to be used as job handler
	public static final String JOB_HANDLER_CLASS = "handler";

	// configuration keys for url templates
	public static final String UPLOAD_INPUT_URL = "input";
	public static final String START_JOB_URL = "start";
	public static final String DOWNLOAD_OUTPUT_URL = "output";
	public static final String JOB_STATE_URL = "state";
	public static final String JOB_RESULT_URL = "result";
	public static final String JOB_CLEANUP_URL = "cleanup";

	// configuration key for for special values returned by job service
	public static final String VALUE_TERMINATED = "terminated";
	public static final String VALUE_SUCCESS = "success";
	public static final String VALUE_WARNING = "warning";

	// parameters used in url templates
	public static final String JOB_ID_PARAM = "ID";
	public static final String HOST_PARAM = "HOST";
	public static final String NAMESPACE_PARAM = "NAMESPACE";
	public static final String NAME_PARAM = "NAME";

}
