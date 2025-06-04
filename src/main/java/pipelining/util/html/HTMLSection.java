package pipelining.util.html;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class HTMLSection extends HTMLElement {

	public final HTMLDocument document;
	public final String header;
	public HTMLColor color;
	public final int level;
	public final int sectionIndex;
	public final List<HTMLElement> elements = new ArrayList<>();
	
	public HTMLSection(HTMLDocument document, String header, HTMLColor color, int level, int sectionIndex) {
		this.document = document;
		this.header = header;
		this.color = color;
		this.level = level;
		this.sectionIndex = sectionIndex;
	}
	
	public HTMLSection addSubSection(String header, HTMLColor color) {
		HTMLSection res = document.createSection(header, level+1, color);
		elements.add(res);
		return res;
	}

	public List<HTMLSection> getSubSections() {
		return elements.stream().filter(ss -> ss instanceof HTMLSection).map(ss -> (HTMLSection)ss).collect(Collectors.toList());
	}
	
	public HTMLParagraph addParagraph(String text) {
		return addElement(new HTMLParagraph(text));
	}
	
	public HTMLParagraph addParagraph(String text, HTMLColor color) {
		return addElement(new HTMLParagraph(text, getTextColor(color)));
	}

	private String getTextColor(HTMLColor color) {
		if (color == null) {
			return null;
		}
		switch (color) {
		case RED: 
			return "#c00000";
		case YELLOW: 
			return "#e09000";
		default:
			return color.toString().toLowerCase();
		}
	}

	public HTMLConsole addConsole() {
		return addElement(new HTMLConsole());
	}

	public HTMLLine addLine() {
		return addElement(new HTMLLine());
	}

	public HTMLTable addTable(List<String> colHeaders, boolean firstRowIsHeader) {
		return addElement(new HTMLTable(colHeaders, firstRowIsHeader));
	}

	public HTMLList addList(boolean numbered) {
		return addElement(new HTMLList(numbered));
	}

	private <T extends HTMLElement> T addElement(T element) {
		elements.add(element);
		return element;
	}

	public int getLevel() {
		return level;
	}

	public String getTitle() {
		return header;
	}

	public String getName() {
		return "sect"+sectionIndex;
	}

	public String getLink() {
		return "#"+getName();
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(HTMLConst.SECTION_HEADER_START);
		sb.append(" col"+color.toString().toLowerCase());
		sb.append(" level"+level);
		sb.append(HTMLConst.SECTION_HEADER_MIDDLE);
		sb.append(header);
//		sb.append("<a name=\"");
//		sb.append(getName());
//		sb.append("\"></a>\n");		
		sb.append(HTMLConst.SECTION_HEADER_END+"\n");
		sb.append(HTMLConst.SECTION_CONTENT_START+"\n");
		for (HTMLElement e : elements) {
			sb.append(e);				
			//sb.append("\n");				
		}
		sb.append(HTMLConst.SECTION_CONTENT_END+"\n");
		return sb.toString();
	}

	public List<HTMLElement> getSubElements() {
		return elements;
	}

	public void setColor(HTMLColor color) {
		this.color = color;
	}
}
