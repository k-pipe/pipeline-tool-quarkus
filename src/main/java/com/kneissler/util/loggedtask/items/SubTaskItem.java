package com.kneissler.util.loggedtask.items;

import com.kneissler.util.html.HTMLSection;
import com.kneissler.util.loggedtask.LogItem;
import com.kneissler.util.loggedtask.LoggedTask;

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
