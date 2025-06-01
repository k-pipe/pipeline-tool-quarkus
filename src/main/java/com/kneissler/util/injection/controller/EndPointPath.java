package com.kneissler.util.injection.controller;

import org.jkube.util.Expect;

public class EndPointPath {

	private String path; // params in {}	
	private String[] pathSegments; // at even positions fixed, at odd positions: param name	

	public EndPointPath(String pathTemplate) {
		this.path = pathTemplate;
		pathSegments = pathTemplate.split("[{}]");
		checkConsistency(pathTemplate);
	}

	private void checkConsistency(String pathTemplate) {
		int pos = 0;
		for (int i = 0; i < pathSegments.length; i++) {
			if (i % 2 == 1) {
				expectChar(pathTemplate, pos, '{');
				pos++;
			}
			pos += pathSegments[i].length();
			if (i % 2 == 1) {
				expectChar(pathTemplate, pos, '}');
				pos++;

			}
		}
		Expect.equal(pos, pathTemplate.length()).elseFail("Could not parse pathTemplate: "+pathTemplate);
	}

	private void expectChar(String string, int pos, char expected) {
		Expect.equal(expected, string.charAt(pos)).elseFail("Expected "+expected+" at position "+pos+" in pathTemplate: "+string);
	}

	public String getPattern() {
		return path;
	}

	public String getJokerPattern() {
		return pathSegments.length == 1 ? pathSegments[0] : pathSegments[0]+"*";
	}

	public String getPath(RequestContext params) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < pathSegments.length; i++) {
			if (i % 2 == 0) {
				sb.append(pathSegments[i]);
			} else {
				sb.append(params.getParam(pathSegments[i]));
			}
		}
		return sb.toString();
	}

	public String getPatternWithColonArgs() {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < pathSegments.length; i++) {
			if (i % 2 == 1) {
				sb.append(":");
			}
			sb.append(pathSegments[i]);
		}		
		return sb.toString();
	}

}
