package pipelining.util.injection.controller;

public interface RequestContext {

	public String getParam(String paramName);

	public String getUserParam(String paramName);

	public String getBody();

	public void respond(Object responseObj);
	
	public void fail(int httpStatus, String cause);

	public String getJWT();
}
