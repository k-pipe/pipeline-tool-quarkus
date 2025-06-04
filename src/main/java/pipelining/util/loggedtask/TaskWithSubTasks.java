package pipelining.util.loggedtask;

import pipelining.util.loggedtask.items.SubTaskItem;

import java.util.ArrayList;
import java.util.List;

public class TaskWithSubTasks extends LoggedTask {

	private final List<LoggedTask> subTasks; 
	private boolean stopOnFailure;

	public TaskWithSubTasks(String title) {
		super(title);
		this.subTasks = new ArrayList<>();
		this.stopOnFailure = false;
	}

	public void stopOnFailure() {
		stopOnFailure = true;
	}
	
	public List<LoggedTask> getSubTasks() {
		return subTasks;
	}

	public void addSubtask(LoggedTask subTask) {
		subTasks.add(subTask);
		addRelevantFor(subTask);
		subTask.setLevel(getLevel()+1);
	}

	public void executeSubtask(LoggedTask subTask) {
		addSubtask(subTask);
		addItem(new SubTaskItem(subTask));
		subTask.run();
		if (subTask.hasFailed()) {
			setFailed();
		}
		if (subTask.didSomething()) {
			setDidSomething();
		}
	}

	@Override
	public List<String> getExecutedLeaves(String user) {
		List<String> res = new ArrayList<>();
		if (getRelevantFor().contains(user)) {
			subTasks.forEach(subTask ->
				subTask.getExecutedLeaves(user).forEach(s -> res.add(getTitle()+" -> "+s))
			);
			if (res.isEmpty() && didSomething()) {
				res.add(getTitle());
			}
		}
		return res;
	}

	@Override
	public List<String> getFailedLeaves(String user) {
		List<String> res = new ArrayList<>();
		if (getRelevantFor().contains(user)) {
			subTasks.forEach(subTask ->
				subTask.getFailedLeaves(user).forEach(s -> res.add(getTitle()+" -> "+s))
			);
			if (res.isEmpty() && hasFailed()) {
				res.add(getTitle());
			}
		}
		return res;
	}

	@Override
	protected void execute() {
		runSubtasks();
		executionSummary();
	}

	private void runSubtasks() {
		boolean stopped = false;
		for (LoggedTask subTask : subTasks) {
			if (!stopped && !subTask.wasExecuted()) {
				addItem(new SubTaskItem(subTask));
				subTask.run();
				if (subTask.hasFailed()) {
					setFailed();
					if (stopOnFailure) {
						stopped = true;
					}
				}
				if (subTask.didSomething()) {
					setDidSomething();
				}
			}
		}
	}

	private void executionSummary() {
		int count = 0;
		int failed = 0;
		int didNothing = 0;
		for (LoggedTask t : subTasks) {
			if (t.wasExecuted()) {
				count++;
				if (t.hasFailed()) {
					failed++;
				}
				if (!t.didSomething()) {
					didNothing++;
				}
			}
		}
		if (failed > 0) {
			warn(failed+" of "+count+" subtasks have failed.");
			setFailed();
		} else if (didNothing > 0) {
			message((count-didNothing)+" tasks were executed successfully, "+didNothing+" tasks did nothing.");
		} else {
			message(count+" tasks were executed successfully.");
		}
	}

	public TaskWithSubTasks createSubTask(String title) {
		TaskWithSubTasks subTask = new TaskWithSubTasks(title);
		subTasks.add(subTask);
		return subTask;
	}

	public void addSubtask(String title, Runnable runnable) {
		LoggedTask subTask = new LoggedTask(title) {			
			@Override
			protected void execute() {
				runnable.run();
			}
		};
		subTasks.add(subTask);
	}

	@Override
	public boolean didSomething() {
		if (super.didSomething()) {
			return true;
		}
		for (LoggedTask subTask : subTasks) {
			if (subTask.didSomething()) {
				return true;
			}
		}
		return false;
	}

}
