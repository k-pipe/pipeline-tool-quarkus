package pipelining.util.loggedtask.items;

import pipelining.util.html.HTMLColor;
import pipelining.util.html.HTMLSection;
import pipelining.util.loggedtask.LogItem;

import java.util.ArrayList;
import java.util.List;

public class CompositeItem implements LogItem {

	private final String title;
	private final List<LogItem> items;
	private final HTMLColor color;

	public CompositeItem() {
		this.title = "";
		this.items = new ArrayList<>();
		this.color = HTMLColor.GRAY;
	}

	public CompositeItem(String title, List<LogItem> items, HTMLColor color) {
		this.title = title;
		this.items = items;
		this.color = color;
	}

	public CompositeItem(String title) {
		this.title = title;
		this.items = new ArrayList<>();
		this.color = HTMLColor.GRAY;
	}

	public String getTitle() {
		return title;
	}

	public List<LogItem> getItems() {
		return items;
	}

	public HTMLColor getColor() {
		return color;
	}

	@Override
	public void log(String user, HTMLSection section) {
		putItems(user, section.addSubSection(title, color));
	}

	public void message(String text) {
		addItem(new LogMessage(text, null));
	}

	public void warn(String text) {
		addItem(new LogMessage(text, HTMLColor.YELLOW));
	}

	public void error(String text) {
		addItem(new LogMessage(text, HTMLColor.RED));
	}

	public LogConsole addConsole() {
		LogConsole res = new LogConsole();
		addItem(res);
		return res;
	}

	public void addItem(LogItem item) {
		items.add(item);
	}

	private void putItems(String user, HTMLSection section) {
		items.forEach(item -> item.log(user, section));
	}

	public CompositeItem section(String title) {
		CompositeItem res = new CompositeItem(title);
		addItem(res);
		return res;
	}
}
