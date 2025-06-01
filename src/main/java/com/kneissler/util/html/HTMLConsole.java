package com.kneissler.util.html;

public class HTMLConsole extends HTMLElement {

	private static final String INDENT = "&nbsp;&nbsp;";
	private final HTMLText text;

	public HTMLConsole() {
		text = new HTMLText();
	}

	public HTMLConsole(String content) {
		text = new HTMLText(content);
	}
	
	public HTMLText getText() {
		return text;		
	}
	
	public String toString() {
		if (text.isEmpty()) {
			return "";
		}
		return HTMLConst.CONSOLE_START + "\n" +
				text +
				HTMLConst.CONSOLE_END + "\n";
	}

	public void addLine(String string) {
		text.appendText(simulateIndent(string)+"<br>\n");
	}
	
	private String simulateIndent(String string) {
		StringBuilder res = new StringBuilder();
		while (string.startsWith("   ")) {
			string = string.substring(3);
			res.append(INDENT);
		}
		res.append(string);
		return res.toString();
	}

	public void addLine(String string, HTMLColor color) {
		text.appendText("<span style=\"color:"+getTextColor(color)+"\">"+simulateIndent(string)+"</span><br>\n");
	}

	private String getTextColor(HTMLColor color) {
		if (color == null) {
			return "#ffffff";
		}
		switch (color) {
		case RED: {
			return "#ff8080";
		}
		case GREEN: {
			return "#00b000";
		}
		default:
			return color.toString().toLowerCase();
		}
	}

}
