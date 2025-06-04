package pipelining.util.loggedtask;

import pipelining.util.html.HTMLDocument;

public class TestRetryTask {

	public static void main(String... args) {
		int count[] = new int[1];
		RetryTask task = new RetryTask(5, 1000, () -> new LoggedTask("Retried Task") {			
			@Override
			protected void execute() {
				count[0]++;
				message("This is the wrapped task in trail "+count[0]);
				if (count[0] < 3) {
					setFailed();
					return;
				} 
				if (count[0] == 3) {
					throw new RuntimeException("Test exception");
				}
				setDidSomething();
			}
		});
		task.run();
		HTMLDocument html = task.createRelevantLog("user1", "This is a test of html logging", "Another line");
		html.saveAs("C:\\tmp\\log2.html");
	}

}
