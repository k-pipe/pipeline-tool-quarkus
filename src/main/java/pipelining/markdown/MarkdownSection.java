package pipelining.markdown;

import java.util.ArrayList;
import java.util.List;

public class MarkdownSection {

	private final String title;
	private final int level;
	private final List<MarkdownElement> elements;
	private final List<MarkdownSection> subSections;

	public MarkdownSection(final String title, final int level) {
		this.title = title;
		this.level = level;
		this.elements = new ArrayList<>();
		this.subSections = new ArrayList<>();
	}

	public void addElement(final MarkdownElement element) {
		elements.add(element);
	}

	public void addSubsection(final MarkdownSection subSection) {
		subSections.add(subSection);
	}

	public String getTitle() {
		return title;
	}

	public int getLevel() {
		return level;
	}

	public List<MarkdownElement> getElements() {
		return elements;
	}

	public List<MarkdownSection> getSubSections() {
		return subSections;
	}

	public MarkdownSection getSubSection(final String name) {
		MarkdownSection res = null;
		for (MarkdownSection section : subSections) {
			if (section.getTitle().equalsIgnoreCase(name)) {
				if (res != null) {
					throw new MarkdownParsingException("Sub-Section name occurs twice: "+name);
				}
				res = section;
			}
		}
		return res;
	}

	public MarkdownElement expectElement(final ElementType type) {
		MarkdownElement res = null;
		for (MarkdownElement element : getElements()) {
			if (element.getType().equals(type)) {
				if (res != null) {
					throw new MarkdownParsingException(
							"Multiple elements of type " + type + " in section " + getTitle());
				}
				res = element;
			}
		}
		if (res == null) {
			throw new MarkdownParsingException(
					"Expected element not found: " + type + " in section " + getTitle());
		}
		return res;
	}

	public int numElementsOfType(final ElementType type) {
		int num = 0;
		for (MarkdownElement e : elements) {
			if (e.getType().equals(type)) {
				num++;
			}
		}
		return num;
	}
}
