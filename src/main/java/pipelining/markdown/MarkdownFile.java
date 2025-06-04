package pipelining.markdown;

import pipelining.codegen.ParagraphScanner;
import pipelining.codegen.Table;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class MarkdownFile {

	private final MarkdownSection mainSection;
	private final Path path;

	public MarkdownFile(Path path, List<String> lines) {
		this.path = path;
		this.mainSection = new MarkdownParser(path, lines).parse();
	}

	public MarkdownFile(Path path) {
		this.path = path;
		this.mainSection = new MarkdownParser(path).parse();
	}

	public MarkdownSection getMainSection() {
		return mainSection;
	}

	public <C extends Enum<C>> Table<C> expectTable(final C[] values, final boolean allowMissing, final String... sectionPath) {
		MarkdownSection section = expectSection(sectionPath);
		MarkdownElement element = section.expectElement(ElementType.TABLE);
		List<C> valueList = new ArrayList<>(List.of(values));
		return new Table<>(new ParagraphScanner(element.getLines()), valueList, allowMissing);
	}

	public <C extends Enum<C>> Table<C> optionalTable(final C[] values, final boolean allowMissing, final String... sectionPath) {
		MarkdownSection section = tryGetSection(sectionPath);
		List<String> lines = section == null ? List.of() : section.expectElement(ElementType.TABLE).getLines();
		List<C> valueList = new ArrayList<>(List.of(values));
		return new Table<>(new ParagraphScanner(lines), valueList, allowMissing);
	}

	public Table<String> expectStringTable(final List<String> columns, final boolean allowMissing, final String... sectionPath) {
		MarkdownSection section = expectSection(sectionPath);
		MarkdownElement element = section.expectElement(ElementType.TABLE);
		return new Table<>(new ParagraphScanner(element.getLines()), columns, allowMissing);
	}


	public MarkdownSection expectSection(final String... sectionPath) {
		MarkdownSection section = getMainSection();
		for (String expected : sectionPath) {
			section = section.getSubSection(expected);
			if (section == null) {
				throw new MarkdownParsingException("Expected subsection not found: "+expected);
			}
		}
		return section;
	}

	public MarkdownSection tryGetSection(final String... sectionPath) {
		MarkdownSection section = getMainSection();
		for (String expected : sectionPath) {
			section = section.getSubSection(expected);
			if (section == null) {
				return null;
			}
		}
		return section;
	}


	public byte[] loadLinkedData(final MarkdownLink link) {
		Path refPath = null;
		try {
			refPath = path.resolveSibling(link.getReference());
			return Files.readAllBytes(refPath);
		} catch (IOException e) {
			throw new MarkdownParsingException("Could not load linked file from "+refPath);
		}
	}

}
