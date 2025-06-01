package com.kneissler.util.loggedtask.items;

import com.kneissler.util.html.HTMLColor;
import com.kneissler.util.html.HTMLSection;
import com.kneissler.util.loggedtask.LogItem;

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
