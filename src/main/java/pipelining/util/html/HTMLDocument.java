package pipelining.util.html;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

public class HTMLDocument {
	
	private final List<HTMLElement> elements = new ArrayList<>();
	private final String title;
	private final int tableOfContentLevel;
	private int sectionCount;
	
	public HTMLDocument(String title, int tableOfContentLevel) {
		this.title = title;
		this.tableOfContentLevel = tableOfContentLevel;
		this.sectionCount = 0;
	}
	
	public void addParagraph(String text) {
	   elements.add(new HTMLParagraph(text));
	}

	public HTMLSection addSection(String header, HTMLColor color) {
		HTMLSection res = createSection(header, 1, color);
		elements.add(res);
		return res;
	}
	
	HTMLSection createSection(String header, int level, HTMLColor color) {
		return new HTMLSection(this, header, color, level, ++sectionCount);
	}

	public void saveAs(String filename) {
		try (PrintStream out = new PrintStream(new File(filename))) {
			out.print(toString());
		} catch (FileNotFoundException e) {
			throw new RuntimeException("Problem writing file "+filename, e);
		}
	}
	
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(HTMLConst.DOC_START);
		sb.append(title+"\n");
		sb.append(HTMLConst.HTML_START);
		sb.append(HTMLConst.HEADER_START+title+HTMLConst.HEADER_END+"\n");
		if (tableOfContentLevel > 0) {
			sb.append(createTOC());
		}
		for (HTMLElement element : elements) {
			sb.append(element);
			sb.append("\n");
		}
		sb.append(HTMLConst.HTML_END);
		return sb.toString();
	}

	private HTMLSection createTOC() {
		HTMLSection toc = new HTMLSection(this, "Table of Contents", HTMLColor.GRAY, 1, 0);
		HTMLList list = toc.addList(false);
		addRecursively(1, list, elements);
		return toc;
	}

	private void addRecursively(int level, HTMLList list, List<HTMLElement> elements) {
		if (level <= tableOfContentLevel) {
			elements.forEach(el -> {
				if (el instanceof HTMLSection) {
					HTMLSection s = (HTMLSection)el;
					HTMLText link = new HTMLText();
					link.appendLink(s.getTitle(), s.getLink());
					if ((level == tableOfContentLevel) || s.getSubSections().isEmpty()) {
						list.addTextItem(link);
					} else {
						HTMLList subList = list.addSubListItem(link);
						addRecursively(level+1, subList, s.getSubElements());
					}
				}
			});
		}
	}

	public void addLine() {
		elements.add(new HTMLLine());
	}
	
}
