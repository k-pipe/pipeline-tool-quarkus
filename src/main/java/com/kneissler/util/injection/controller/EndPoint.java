package com.kneissler.util.injection.controller;

public class EndPoint {

	public static enum HttpMethod {
		GET, PUT, POST, DELETE
	}

	@FunctionalInterface
	public static interface Invocation {
		/**
		 * 
		 * @param request access to request parameters, request body and method to send response object
		 * @return true if request successfully handled
		 */
		boolean invoke(RequestContext rc);
	}

	private final HttpMethod method;
	private final EndPointPath endpointPath;
	private final Invocation invocation;

	public EndPoint(HttpMethod method, String pathTemplate, Invocation invocation) {
		this.method = method;
		this.endpointPath = new EndPointPath(pathTemplate);
		this.invocation = invocation;
	}

	public boolean invoke(RequestContext params) {
		return invocation.invoke(params);
	}

	public String getPathPattern() {
		return endpointPath.getPattern();
	}

	public String getPathWithColonArgs() {
		return endpointPath.getPatternWithColonArgs();
	}

	public String getPathWithJoker() {
		return endpointPath.getJokerPattern();
	}

	public String getPath(RequestContext params) {
		return endpointPath.getPath(params);		
	}

	public HttpMethod getMethod() {
		return method;		
	}

}
