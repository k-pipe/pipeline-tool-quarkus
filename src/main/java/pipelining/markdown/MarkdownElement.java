package pipelining.markdown;

import pipelining.logging.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MarkdownElement {

	private static final String GROUP = "([^\\]\\)]+)";
	private static final Pattern LINK_PATTERN = Pattern.compile(".*\\["+GROUP+"\\]\\("+GROUP+"\\).*");

	private final ElementType type;
	private final List<String> lines;
	private final List<MarkdownLink> links;

	public MarkdownElement(final List<String> lines, final ElementType type) {
		this.lines = lines;
		this.type = type;
		this.links = new ArrayList<>();
		if (ElementType.PARAGRAPH.equals(type)) {
			lines.forEach(this::extractLinks);
		}
	}

	private void extractLinks(final String line) {
		Matcher matcher = LINK_PATTERN.matcher(line);
		if (matcher.matches()) {
			String name = matcher.group(1);
			String ref = matcher.group(2);
			Log.debug("Link detected: "+name+" --> "+ref);
			links.add(new MarkdownLink(name, ref));
		}
	}

	public List<String> getLines() {
		return lines;
	}

	public ElementType getType() {
		return type;
	}

	public List<MarkdownLink> getLinks() {
		return links;
	}
}
