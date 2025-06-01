package com.kneissler.util.loggedtask;

import com.kneissler.util.loggedtask.items.SubTaskItem;

import java.util.LinkedList;
import java.util.function.Supplier;

public class RetryTask extends TaskWithSubTasks {
	
	private final Supplier<LoggedTask> taskSupplier;	
	private final LinkedList<LoggedTask> trials;
	private final int maxNumTrials;
	private final long delayAfterFailureMilis;	
	private RetryMode mode;	

	private RetryTask(String title, int maxNumRetries, long delayAfterFailureMilis, Supplier<LoggedTask> taskSupplier, LoggedTask firstTrial) {
		super(title);
		addRelevantFor(firstTrial);
		this.taskSupplier = taskSupplier;
		this.trials = new LinkedList<>();
		this.trials.add(firstTrial);
		this.maxNumTrials = maxNumRetries;
		this.delayAfterFailureMilis = delayAfterFailureMilis;
		this.mode = RetryMode.RETRY_IF_FAILED;
	}
	
	public RetryTask(int maxNumTrials, long delayAfterFailureMilis, Supplier<LoggedTask> taskSupplier, LoggedTask firstTrial) {
		this(firstTrial.getTitle(), maxNumTrials, delayAfterFailureMilis, taskSupplier, firstTrial);
	}

	public RetryTask(String title, int maxNumTrials, long delayAfterFailureMilis, Supplier<LoggedTask> taskSupplier) {
		this(title, maxNumTrials, delayAfterFailureMilis, taskSupplier, taskSupplier.get());
	}

	public RetryTask(int maxNumTrials, long delayAfterFailureMilis, Supplier<LoggedTask> taskSupplier) {
		this(maxNumTrials, delayAfterFailureMilis, taskSupplier, taskSupplier.get());
	}
	
	public RetryTask retryIfDidNothing() {
		mode = RetryMode.RETRY_IF_DID_NOTHING;
		return this;
	}

	@Override
	protected void execute() {
		LoggedTask task = trials.get(0);
		int trial = 1;
		boolean done = false;
		do {
			System.out.println("Running task (trial "+trial+")");
			task.run();
			if (didRetry(task, trial)) {
				trial++;
				task = trials.getLast();
			} else {
				done = true;
			}
 		} while(!done);
		if (trial == 1) {
			copyFrom(task);
		} else {
			if (task.hasFailed()) {
				message("Setting failed 2.");
				setFailed();
			}
			trials.forEach(t -> addItem(new SubTaskItem(t)));
		}
	}

	private boolean didRetry(LoggedTask task, int trial) {
		boolean retry = mode.equals(RetryMode.RETRY_IF_FAILED) ? task.hasFailed() : !(task.didSomething() || task.hasFailed());
		//message("Trial "+trial+" failed="+task.hasFailed()+", didSomething="+task.didSomething()+" ==> retry="+retry+" mode="+mode);
		if (retry) {
			if (trial < maxNumTrials) {
				task.message("Sleeping "+delayAfterFailureMilis+" ms.");
				System.out.println("Sleeping");
				try {
					Thread.sleep(delayAfterFailureMilis);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				trials.add(taskSupplier.get());
				return true;
			} else {
				message("Giving up after "+trial+" trials.");
				setFailed();
			}
		} else {
			if (task.didSomething()) {
				setDidSomething();
			}
			if (task.hasFailed()) {
				message("Setting failed.");
				setFailed();
			}
		}
		return false;
	}

}
