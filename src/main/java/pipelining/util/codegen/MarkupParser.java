package pipelining.util.codegen;

import java.util.ArrayList;
import java.util.List;

public class MarkupParser {

	private String path;
	private MarkupFile doc;
	private List<SectionGroup<?>> sectionGroups;

	public MarkupParser(String path) {
		this.path = path;
		this.sectionGroups = new ArrayList<>();
		this.doc = new MarkupFile(path);
	}

	public <E extends Enum<E>> void expectSection(E[] enumValues, boolean allowMissing, @SuppressWarnings("unchecked") Section<E>... sections) {
		for (Section<E> s : sections) {
			sectionGroups.add(SectionGroup.singleSection(s, enumValues, allowMissing));
		}
	}

	private static enum DUMMY {};

	public <E extends Enum<E>> Section<E> expectSection(int level, String title, E[] enumValues, boolean allowMissing) {
		Section<E> section = new Section<E>(level, title);
		sectionGroups.add(SectionGroup.singleSection(section, enumValues, allowMissing));
		return section;
	}

	public <E extends Enum<E>> SectionGroup<E> expectSubSections(int superLevel, String superHeader, E[] enumValues, boolean allowMissing) {
		SectionGroup<E> group = SectionGroup.multiSection(superLevel, superHeader, enumValues, allowMissing);
		sectionGroups.add(group);
		return group;
	}

	public Section<DUMMY> expectIrrelevantSection(int level, String text, boolean allowMissing) {
		return addSingleSection(new Section<DUMMY>(level, text), null, allowMissing);
	}

	private <E extends Enum<E>> Section<E> addSingleSection(Section<E> section, E[] values, boolean allowMissing) {
		sectionGroups.add(SectionGroup.singleSection(section, values, allowMissing));
		return section;
	}

	public void parse() {
		for (SectionGroup<?> sg : sectionGroups) {
			parseSectionGroup(sg);
		}
	}

	private <E extends Enum<E>> void parseSectionGroup(SectionGroup<E> sg) {
		if (sg.isMulti()) {
			parseSubSections(sg);
		} else {
			parseSection(sg.getSections().get(0), sg.getColumns(), sg.allowMissing());
		}
	}

	private <E extends Enum<E>> void parseSubSections(SectionGroup<E> sg) {
		int pos = findHeaderParagraphIndex(sg.getHeaderLevel(), sg.getHeaderText());
		Section<E> subsection = null;
		pos++;
		boolean done = pos >= doc.getParagraphs().size();
		while (!done) {
			List<String> paragraph = doc.getParagraphs().get(pos);
			int level = headerLevel(paragraph);
			//System.out.println("Paragraph: level "+level+":"+paragraph.get(0));
			if (level == sg.getHeaderLevel()+1) {
				subsection = new Section<E>(level, getHeaderTitle(paragraph));
			} else if (level == 0) {
				if (isTable(paragraph)) {
					if (subsection != null) {
						if (subsection.getTable() != null) {
							throw new RuntimeException("multiple tables in section: "+sg.getHeaderText());
						} 
						subsection.setTable(parseTable(paragraph, sg.getColumns(), sg.allowMissing()));
						sg.getSections().add(subsection);
					}
				} else {
					if (subsection != null) {
						subsection.addDescription(paragraph);
					}
				}
			} else if (level <= sg.getHeaderLevel()) {
				done = true;
			}
			pos++;
			if (pos >= doc.getParagraphs().size()) {
				done = true;
			}
		}
	}

	private <E extends Enum<E>> void parseSection(Section<E> section, E[] columns, boolean allowMissing) {
		int pos = findHeaderParagraphIndex(section.headerLevel(), section.headerText());
		pos++;
		boolean done = pos < doc.getParagraphs().size();
		while (!done) {
			List<String> paragraph = doc.getParagraphs().get(pos);
			if (isTable(paragraph)) {
				section.setTable(parseTable(paragraph, columns, allowMissing));
				done = true;
			} else {
				section.addDescription(paragraph);
			}
			pos++;
			if (pos >= doc.getParagraphs().size()) {
				throw new RuntimeException("no table found for section "+section.headerText());
			}
		}
	}

	private int findHeaderParagraphIndex(int headerLevel, String headerText) {
		int found = -1;
		for (int i = 0; i < doc.getParagraphs().size(); i++) {
			List<String> paragraph = doc.getParagraphs().get(i);
			//System.out.println("Paragraph: level "+headerLevel(paragraph)+":"+paragraph.get(0));
			if ((headerLevel(paragraph) == headerLevel) && getHeaderTitle(paragraph).equalsIgnoreCase(headerText)) {
				if (found != -1) {
					throw new RuntimeException("Multiple headers at level "+headerLevel+" with title "+headerText);
				}
				found = i;
			}
		}
		if (found == -1) {
			throw new RuntimeException("No headers at level "+headerLevel+" with title "+headerText);
		}
		return found;
	}

	private boolean isTable(List<String> paragraph) {
		return (paragraph.size() >= 2) && isTableSeparator(paragraph.get(1));
	}

	private boolean isTableSeparator(String string) {
		return !string.chars().anyMatch(c -> !separatorChar(c));
	}

	private boolean separatorChar(int c) {
		return (c == '-') || (c == '|') || (c == ' ') || (c == ':');
	}

	private String getHeaderTitle(List<String> paragraph) {
		int level = headerLevel(paragraph);
		String first = paragraph.get(0).trim();
		if (level == 0) {
			throw new RuntimeException("Expected header, received "+first);
		}
		return first.substring(level+1).trim();
	}

	private int headerLevel(List<String> paragraph) {
		String first = paragraph.get(0).trim();
		int i = 0;
		while ((i < first.length()) && (first.charAt(i) == '#')) {
			i++;
		}
		return i;
	}

	private <E extends Enum<E>> Table<E> parseTable(List<String> paragraph, E[] columns, boolean allowMissing) {
		List<E> columnList = new ArrayList<>();
		for (E col : columns) {
			columnList.add(col);
		}
		return new Table<E>(new ParagraphScanner(paragraph), columnList, allowMissing);
	}

}
