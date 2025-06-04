package pipelining.script.pipeline.pipeline_v2;

import pipelining.util.richfile.RichFile;
import pipelining.markdown.pipeline.v1.PipelineMarkdown;

import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static pipelining.logging.Log.onException;

public class PipelineMarkdownV2 extends PipelineMarkdown {

	@Override
	public PipelinePlantUmlV2 getPlantUML() {
		return (PipelinePlantUmlV2) super.getPlantUML();
	}

	@Override
	protected PipelinePumlParserV2 createParser() {
		return new PipelinePumlParserV2();
	}

	public PipelineMarkdownV2(final Path includeRoot, final Path markdownPath, boolean resolve, Map<String, String> variables) {
		super(markdownPath, loadMarkdown(includeRoot, markdownPath, resolve, variables));
		getPlantUML().init();
	}

	public List<String> getPipelineUMLLines() {
		return getPlantUML().getLines();
	}

	public Map<String, Object> getUmlWithObjects() {
		return getPlantUML().getLines2parsedObjects();
	}

	private static List<String> loadMarkdown(final Path includeRoot, final Path markdownPath,
			final boolean resolve, final Map<String, String> variables) {
		return resolve
				? resolveRichMarkdown(includeRoot, markdownPath, variables)
				: onException(() -> Files.readAllLines(markdownPath)).fail("Could not read markdown file "+markdownPath);
	}

	private static List<String> resolveRichMarkdown(Path includeRoot, Path markdownPath, Map<String, String> variables) {
		List<String> resolved = new RichFile(includeRoot, markdownPath).resolve(variables);
		try (PrintStream out = new PrintStream(markdownPath.resolveSibling(Path.of("resolved-pipeline.mb")).toFile())) {
			resolved.forEach(out::println);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		return resolved;
	}

}
