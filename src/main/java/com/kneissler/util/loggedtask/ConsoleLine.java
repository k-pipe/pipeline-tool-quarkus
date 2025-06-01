package com.kneissler.util.loggedtask;

import com.kneissler.util.html.HTMLColor;

public class ConsoleLine {

	private final String text;
	private final HTMLColor color;

	private final boolean adminOnly;

	public ConsoleLine(String text, HTMLColor color, boolean adminOnly) {
		this.text = text;
		this.color = color;
		this.adminOnly = adminOnly;
	}
	
	public String getText() {
		return text;
	}

	public HTMLColor getColor() {
		return color;
	}

	public boolean adminOnly() {
		return adminOnly;
	}


}
