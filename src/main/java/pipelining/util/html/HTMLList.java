package pipelining.util.html;

import java.util.ArrayList;
import java.util.List;


public class HTMLList extends HTMLElement {

	private final boolean numbered;
	private final List<HTMLElement> items = new ArrayList<>();
	
	public HTMLList(boolean numbered) {
		this.numbered = numbered;
	}
	
	public HTMLText addTextItem() {
		HTMLText res = new HTMLText();
		items.add(res);
		return res;
	}

	public void addTextItem(HTMLText text) {
		items.add(text);
	}

	public HTMLList addSubListItem(HTMLText itemText) {
		HTMLMultiElement multi = new HTMLMultiElement();
		HTMLList res = new HTMLList(false);
		multi.add(itemText);
		multi.add(res);
		items.add(multi);
		return res;
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(numbered ? "<ol>" : "<ul>");
		sb.append("\n");
		for (HTMLElement i : items) {
			sb.append("<li>");				
			sb.append("\n");				
			sb.append(i);
			sb.append("\n");				
			sb.append("</li>");				
			sb.append("\n");				
		}
		sb.append(numbered ? "</ol>" : "</ul>");
		sb.append("\n");
		return sb.toString();
	}

}
