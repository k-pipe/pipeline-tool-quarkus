package com.kneissler.util.html;

public class HTMLParagraph extends HTMLElement {

	private HTMLText text;

	public HTMLParagraph() {
		text = new HTMLText();
	}

	public HTMLParagraph(String content) {
		text = new HTMLText(content);
	}
	
	public HTMLParagraph(String content, String color) {
		text = new HTMLText(content, color);
	}

	public HTMLText getText() {
		return text;		
	}
	
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(HTMLConst.PARAGRAPH_START+"\n");
		sb.append(text);
		sb.append(HTMLConst.PARAGRAPH_END+"\n");
		return sb.toString();
	}
	
}
