package pipelining.util.loggedtask.items;

import pipelining.util.html.HTMLSection;
import pipelining.util.loggedtask.LogItem;
import pipelining.util.loggedtask.LoggedTask;

public class SubTaskItem implements LogItem {

	private final LoggedTask subTask;

	public SubTaskItem(LoggedTask subTask) {
		this.subTask = subTask;
	}

	@Override
	public void log(String user, HTMLSection section) {
		subTask.putItems(user, section.addSubSection(subTask.getTitle(), subTask.getColor()));
	}

}
