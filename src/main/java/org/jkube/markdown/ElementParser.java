package org.jkube.markdown;

import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

import static org.jkube.logging.Log.warn;

public class ElementParser {

	private static final String PLANTUML = "plantuml";

	private static final String YAML = "yaml";
	private static final String BLOCK_MARKER = "```";
	private static final Set<Character> TABLE_SEPARATOR_CHARS = Set.of(':', '-', '|', ' ');

	private final List<String> lines;
	private final int offset;

	public ElementParser(final List<String> lines, int lineOffset) {
		this.lines = lines;
		this.offset = lineOffset;
	}

	public void parse(final Consumer<MarkdownElement> consumer) {
		int prev = -1;
		boolean inBlock = false;
		int pos;
		for (pos = 0; pos < lines.size(); pos++) {
			String line = lines.get(pos);
			if (blockStart(line) != null) {
				inBlock = !inBlock;
			}
			if (line.isBlank() && !inBlock) {
				if (prev+1 < pos) {
					consumer.accept(parseParagraph(prev+1, pos));
				}
				prev = pos;
			}
		}
		if (prev+1 < pos) {
			consumer.accept(parseParagraph(prev+1, pos));
		}
		if (inBlock) {
			fail("End of block not found", lines.size());
		}
	}

	private MarkdownElement parseParagraph(final int from, final int to) {
		ElementType type;
		List<String> elementLines;
		if ((from+1 < to) && isTableSeparator(lines.get(from+1))) {
			type = ElementType.TABLE;
			elementLines = lines.subList(from, to);
		} else {
			String block = blockStart(lines.get(from));
			if (block == null) {
				type = ElementType.PARAGRAPH;
				elementLines = lines.subList(from, to);
 			} else {
				if (!blockEnd(lines.get(to-1))) {
					fail("End of block expected", to-1);
				}
				if (block.equals(PLANTUML)) {
					type = ElementType.PLANTUML;
				} else 	if (block.equals(YAML)) {
					type = ElementType.YAML;
				} else {
					type = ElementType.OTHER;
				}
				elementLines = lines.subList(from+1, to-1);
			}
		}
		return new MarkdownElement(elementLines, type);
	}

	private boolean isTableSeparator(final String line) {
		for (int p = 0; p < line.length(); p++) {
			if (!TABLE_SEPARATOR_CHARS.contains(line.charAt(p))) {
				return false;
			}
		}
		return true;
	}

	private String blockStart(final String line) {
		String trim = line.trim();
		return trim.startsWith(BLOCK_MARKER) ? trim.substring(BLOCK_MARKER.length()) : null;
	}

	private boolean blockEnd(final String line) {
		return line.trim().equals(BLOCK_MARKER);
	}

	private void fail(final String message, final int line) {
		warn("Parsing element failed in line {}, {}: {}", line+offset, message, lines.get(line));
		throw new MarkdownParsingException("Parsing of element failed in line "+line+offset+": "+message);
	}

}
