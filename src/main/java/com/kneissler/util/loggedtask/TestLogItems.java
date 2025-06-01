package com.kneissler.util.loggedtask;

import com.kneissler.util.html.HTMLDocument;
import com.kneissler.util.loggedtask.items.LogConsole;

public class TestLogItems {

	public static void main(String... args) {
		TaskWithSubTasks mainTask = new TaskWithSubTasks("Main Task");
		mainTask.message("This task has 5 subtasks");
		for (int i = 0; i < 5; i++) {
			int taskId = i;
			LoggedTask subTask = new LoggedTask("SubTask "+i) {
				@Override
				protected void execute() {
					if (taskId == 0) {
						message("This is a message");
						warn("This is a warning");
						error("This is an error");
					} else if (taskId == 3) {
						error("this task fails");
						setFailed();
					} else if (taskId == 4) {
						message("this task throws an exception");
						throw new RuntimeException("Testing");
					} else if (taskId == 2) {
						message("this task does something");
						setDidSomething();
					} else if (taskId == 1) {
						message("This task has a log console");
						LogConsole console = addConsole();
						console.command("Command line");
						console.add("Normal line");
						console.ignore("Ignore");
						console.warn("Warn");
						console.error("Error");
						console.success("Success");
					}
				}
			};
			subTask.addRelevantFor(mainTask);
			mainTask.addSubtask(subTask);
		}
		mainTask.addRelevantFor("user1");
		mainTask.message("This is a message that was added before running");
		mainTask.run();
		mainTask.message("This is a message that was added after running");
		HTMLDocument html = mainTask.createRelevantLog("user1", "This is a test of html logging", "Another line");
		html.saveAs("C:\\tmp\\log1.html");
	}
}
