package pipelining.util.html;

import java.util.ArrayList;
import java.util.List;

public class HTMLText extends HTMLElement {

	private static class Span {
		String color;
		String text;
		public Span(String text, String color) {
			this.text = text;
			this.color = color;
		}
	}
	
	private final List<Span> spans = new ArrayList<>();
	
	public HTMLText() {
	}

	public HTMLText(String text, String color) {
		spans.add(new Span(text, color));
	}
	
	public HTMLText(String text) {
		this(text, null);
	}
	
	public void appendText(String text) {
		spans.add(new Span(text, null));
	}

	public void appendLink(String text, String url) {
		appendText("<a href=\""+url+"\">"+text+"</a>");		
	}
	
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for (Span span : spans) {
			if (span.color != null) {
				sb.append("<span style=\"color:").append(span.color).append("\">");
			}
			sb.append(span.text);
			if (span.color != null) {
				sb.append("</span>");	
			}
			sb.append("\n");				
		}
		return sb.toString();
	}

	public boolean isEmpty() {
		return spans.isEmpty();
	}
		
}
