package pipelining.util.loggedtask;

import pipelining.util.loggedtask.items.LogConsole;
import pipelining.util.loggedtask.items.LogMessage;
import pipelining.util.html.*;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;

public abstract class LoggedTask {

	public static final String ADMIN = "admin@kneissler.com";

	private String title;
	private int level;
	private final Set<String> relevantFor;
	private final List<LogItem> items; 	
	private boolean wasExecuted;
	private boolean failed;
	private boolean didSomething;
	private Exception exception;
	private final long startTime;

	protected LoggedTask(String title) {
		this.title = title;
		this.relevantFor = new LinkedHashSet<>();
		this.items = new ArrayList<>();
		this.failed = false;
		this.didSomething = false;
		this.exception = null;
		this.startTime = System.currentTimeMillis();
		this.wasExecuted = false;
	}

	public static LoggedTask create(String title, Runnable runnable) {
		return new LoggedTask(title) {			
			@Override
			protected void execute() {
				runnable.run();
			}
		};
	}

	public static LoggedTask create(String title, Predicate<LoggedTask> predicate) {
		return new LoggedTask(title) {			
			@Override
			protected void execute() {
				if (!predicate.test(this)) {
					setFailed();
				}
			}
		};
	}
	
	public static LoggedTask create(String title, Consumer<LoggedTask> consumer) {
		return new LoggedTask(title) {			
			@Override
			protected void execute() {
				consumer.accept(this);
			}
		};
	}

	protected abstract void execute(); 

	public void setTitle(String title) { 	
		this.title = title;
	}

	public void addRelevantFor(String email) { 	
		this.relevantFor.add(email);
	}

	public void addRelevantFor(LoggedTask other) { 	
		this.relevantFor.addAll(other.relevantFor);
	}

	public void setLevel(int level) { 	
		this.level = level;
	}

	public int getLevel() { 	
		return level;
	}

	public LoggedTask run() {
		try {
			LoggedTaskLog.logHeading(getTitle(), getLevel());
			execute();
		} catch (Exception e) {
			setFailed();
			System.err.println("Exception in task "+getTitle()+": "+e.getMessage());
			e.printStackTrace();
			exception = e;
		} finally {			
			wasExecuted = true;
		}
		return this;
	}

	public void runInThread() {
		new Thread(this::run).start();
	}


	public String getTitle() {
		return title;
	}

	public boolean wasExecuted() {
		return wasExecuted;
	}

	public Set<String> getRelevantFor() {
		return relevantFor;
	}

	public void setSuccess(boolean success) {
		failed = !success;
	}

	public void setFailed() {
		failed = true;
	}

	public void setDidSomething() {
		didSomething = true;
	}

	public void addItem(LogItem item) {
		items.add(item);
	}

	public void message(String text) {
		addItem(new LogMessage(text, null));
		LoggedTaskLog.log(level, text);
	}

	public void warn(String text) {
		addItem(new LogMessage(text, HTMLColor.YELLOW));
		LoggedTaskLog.log(level, "WARNING: "+text);
	}

	public void error(String text) {
		addItem(new LogMessage(text, HTMLColor.RED));
		LoggedTaskLog.log(level, "ERROR: "+text);
		setFailed();
	}

	public LogConsole addConsole() {
		LogConsole res = new LogConsole();
		addItem(res);
		return res;
	}

	public List<String> getExecutedLeaves(String user) {
		List<String> res = new ArrayList<>();
		if (relevantFor.contains(user) && didSomething()) {
			res.add(title);
		}
		return res;
	}

	public List<String> getFailedLeaves(String user) {
		List<String> res = new ArrayList<>();
		if (relevantFor.contains(user) && failed) {
			res.add(title);
		}
		return res;
	}

	public HTMLColor getColor() {
		return failed ? HTMLColor.RED : didSomething() ? HTMLColor.GREEN : HTMLColor.GRAY;
	}

	public void putItems(String user, HTMLSection section) {
		items.forEach(item -> item.log(user, section));
		if (exception != null) {
			addExceptionTrace(section, exception);	
		}		
	}

	public HTMLDocument createRelevantLog(String user, String... rootParagraphs) {
		HTMLDocument res = HTML.createDocument(title);
		for (String text : rootParagraphs) {
			res.addParagraph(text);
		}
		putItems(user, res.addSection(title, getColor()));
		return res;
	}

	private void addExceptionTrace(HTMLSection section, Exception e) {
		section.addParagraph("An exception occurred: "+e);
		HTMLConsole console = section.addConsole();
		Throwable cause = e;
		while (cause != null) {
			console.addLine(cause.toString(), HTMLColor.RED);
			for (StackTraceElement st : cause.getStackTrace()) {
				console.addLine(st.toString());
			}
			cause = cause.getCause();
		}
	}

	public boolean hasFailed() {
		return failed;
	}

	public boolean didSomething() {
		return didSomething;
	}

	protected void copyFrom(LoggedTask other) {
		this.title = other.title;
		this.relevantFor.addAll(other.relevantFor); 	
		this.items.addAll(other.items); 	
		this.didSomething = other.didSomething;
		this.failed = other.failed;
		this.exception = other.exception;
	}

	public String getRunTime() {
		long time = System.currentTimeMillis() - startTime;
		if (time < 2000) {
			return time+"ms";
		} 
		time = Math.round(time / 1000.0);
		if (time < 120) {
			return time+"s";
		}
		time = Math.round(time / 60.0);
		if (time < 120) {
			return time+"min";
		}
		time = Math.round(time / 60.0);
		return time+"h";
	}

	public static LoggedTask fail(String title, String message) {
		return LoggedTask.create(title, task -> {
			task.error(message);
			return false;
		});
	}

}
