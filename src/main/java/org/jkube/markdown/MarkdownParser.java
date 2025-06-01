package org.jkube.markdown;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.jkube.logging.Log.onException;
import static org.jkube.logging.Log.warn;

public class MarkdownParser {

	private static final String HEADER_CHAR = "#";
	private final List<String> lines;
	private final Path path;

	public MarkdownParser(final Path path, final List<String> lines) {
		this.path = path;
		this.lines = lines;
	}

	public MarkdownParser(final Path path) {
		this(path, onException(() -> Files.readAllLines(path)).fail("Could not read markdown file "+path));
	}

	public MarkdownSection parse() {
		return parseSectionsRecursively(lines, 1, 1);
	}

	private MarkdownSection parseSectionsRecursively(final List<String> lines, final int lineOffset, final int depth) {
		int first = expectNotBlank(lines, lineOffset);
		String title = expectTitle(lines.get(first), depth, lineOffset+first);
		//log("Parsing {}, depth {}", title, depth);
		MarkdownSection res = new MarkdownSection(title, depth);
		List<Integer> subTitlePos = findTitles(lines, depth+1);
		int elementsFrom = first + 1;
		int elementsTo = subTitlePos.isEmpty() ? lines.size() : subTitlePos.get(0);
		if (elementsTo > elementsFrom) {
			ElementParser elementParser = new ElementParser(lines.subList(elementsFrom, elementsTo),
					lineOffset + elementsFrom);
			elementParser.parse(res::addElement);
		}
		for (int i = 0; i < subTitlePos.size(); i++) {
			int from = subTitlePos.get(i);
			int to = i + 1 < subTitlePos.size() ? subTitlePos.get(i + 1) : lines.size();
			res.addSubsection(parseSectionsRecursively(lines.subList(from, to), lineOffset + from, depth + 1));
		}
		return res;
	}

	private List<Integer> findTitles(final List<String> lines, final int depth) {
		String marker = HEADER_CHAR.repeat(depth)+" ";
		List<Integer> res = new ArrayList<>();
		for (int i = 0; i < lines.size(); i++) {
			if (lines.get(i).trim().startsWith(marker)) {
				res.add(i);
			}
		}
		return res;
	}

	private String expectTitle(final String line, final int depth, final int lineNum) {
		String trim= line.trim();
		String marker = HEADER_CHAR.repeat(depth);
		if (!trim.startsWith(marker+" ")) {
			fail(lineNum, "Expected section title starting with "+marker);
		}
		if (!trim.endsWith(" "+marker)) {
			fail(lineNum, "Expected section title ending with "+marker);
		}
		return trim.substring(depth+1, trim.length()-depth-1).trim();
	}

	private int expectNotBlank(final List<String> lines, final int lineOffset) {
		int i = 0;
		while (i < lines.size()) {
			if (!lines.get(i).isBlank()) {
				return i;
			}
			i++;
		}
		fail(lineOffset, "Unexpected end of file");
		return -1;
	}

	private void fail(final int lineNum, final String s) {
		warn("Parsing {}:{} failed, {}: {}",path, lineNum, s, lines.get(lineNum-1));
		throw new MarkdownParsingException("Parsing of "+path+" failed in line "+lineNum+": "+s);
	}

}
