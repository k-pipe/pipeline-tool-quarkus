package org.jkube.logging;

import java.util.List;
import java.util.Set;

public class LogLine {
	long time; // time stamp
	String codeLocation; // may be sourceClass:line or other custom way of specifying code location
	Logger.LogLevel loglevel;  // ERROR, WARN, INFO, DETAIL, DEBUG
	List<String> path; // namespace,service/job,pod,container,subservice,...
	List<String> groups; // nested groups
	Set<String> tags;  // e.g. FRONTEND/BACKEND, DEVELOPER/USER/PROVIDER
	String text; // log line, may contain parameters specified by {}, may have multiple lines separated by \n
	List<String> parameters; // length = number of {} in text

	public long getTime() {
		return time;
	}

	public void setTime(final long time) {
		this.time = time;
	}

	public List<String> getGroups() {
		return groups;
	}

	public void setGroups(final List<String> groups) {
		this.groups = groups;
	}

	public String getCodeLocation() {
		return codeLocation;
	}

	public void setCodeLocation(final String codeLocation) {
		this.codeLocation = codeLocation;
	}

	public Logger.LogLevel getLoglevel() {
		return loglevel;
	}

	public void setLoglevel(final Logger.LogLevel loglevel) {
		this.loglevel = loglevel;
	}

	public List<String> getPath() {
		return path;
	}

	public void setPath(final List<String> path) {
		this.path = path;
	}

	public Set<String> getTags() {
		return tags;
	}

	public void setTags(final Set<String> tags) {
		this.tags = tags;
	}

	public String getText() {
		return text;
	}

	public void setText(final String text) {
		this.text = text;
	}

	public List<String> getParameters() {
		return parameters;
	}

	public void setParameters(final List<String> parameters) {
		this.parameters = parameters;
	}
}
