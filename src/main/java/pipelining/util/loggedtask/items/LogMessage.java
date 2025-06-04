package pipelining.util.loggedtask.items;

import pipelining.util.html.HTMLColor;
import pipelining.util.html.HTMLSection;
import pipelining.util.loggedtask.LogItem;

public class LogMessage implements LogItem {

	private final HTMLColor color;
	private final String text;

	public LogMessage() {
		this.text = "";
		this.color = HTMLColor.GRAY;
	}

	public LogMessage(String text, HTMLColor color) {
		this.text = text;
		this.color = color;
	}

	public HTMLColor getColor() {
		return color;
	}

	public String getText() {
		return text;
	}

	@Override
	public void log(String user, HTMLSection section) {
		section.addParagraph(text, color);
	}

}
