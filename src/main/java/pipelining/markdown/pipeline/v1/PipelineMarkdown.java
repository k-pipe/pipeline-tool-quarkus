package pipelining.markdown.pipeline.v1;

import pipelining.util.richfile.resolver.VariableResolver;
import pipelining.codegen.Table;
import pipelining.job.DockerImage;
import pipelining.pipeline.definition.Pipeline;
import pipelining.pipeline.definition.PipelineStep;
import pipelining.util.Expect;
import pipelining.logging.Log;
import pipelining.markdown.*;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PipelineMarkdown extends MarkdownFile {

	public static final String DOCKER_IMAGES = "Docker Images";

	public static final String BUNDLED = "Bundled";
	public static final String MANAGED = "Managed";
	public static final String GENERIC = "Generic";
	public static final String PIPELINE = "Pipeline Structure";
	public static final String CONFIGURATION = "Configuration";
	public static final String CONFIG = "config.json";
	public static final String WILDCARD = "*";

	public static final String GENERAL_SETUP = "General Setup";

	public static final String NAMING_CONVENTIONS = "Naming Conventions";
	public static final String PATTERN_VAR_IMAGE_NAME = "imagePath";
	private static final String PATTERN_VAR_GENERATION = "generation";
	public static final String PATTERN_VAR_PROVIDER = "provider";
	public static final String BUNDLED_IMAGE_PATTERN_KEY = "bundledImageName";
	public static final String MANAGE_NAMESPACE_PATTERN_KEY = "managedImageNamespace";

	private Table<BundledImageColumns> bundledImages;
	private Table<ManagedImageColumns> managedImages;
	private Table<GenericImageColumns> genericImages;
	protected Table<NamingConventionColumns> namingConventions;
	private PipelinePlantUML plantUML;

	public PipelineMarkdown(final Path path, List<String> lines) {
		super(path, lines);
		getSectionInfos();
	}

	public PipelineMarkdown(final Path path) {
		super(path);
		getSectionInfos();
	}

	public Map<String, PipelineStep> getSteps() {
		return plantUML.getSteps();
	}

	public PipelineStep getInput() {
		return plantUML.getInput();
	}

	public PipelineStep getOutput() {
		return plantUML.getOutput();
	}

	public PipelinePlantUML getPlantUML() {
		return plantUML;
	}

	private void getSectionInfos() {
		parseLines();
		parseParamJsons();
		bundledImages = optionalTable(BundledImageColumns.values(), false, DOCKER_IMAGES, BUNDLED);
		managedImages = optionalTable(ManagedImageColumns.values(), false, DOCKER_IMAGES, MANAGED);
		genericImages = optionalTable(GenericImageColumns.values(), false, DOCKER_IMAGES, GENERIC);
		namingConventions = expectTable(NamingConventionColumns.values(), false, GENERAL_SETUP, NAMING_CONVENTIONS);
	}

	protected PipelinePumlParser createParser() {
		return new PipelinePumlParser();
	}

	protected void parseLines() {
		MarkdownElement element = expectSection(PIPELINE).expectElement(ElementType.PLANTUML);
		this.plantUML = createParser().parse(element.getLines());
	}

	protected void parseParamJsons() {
		MarkdownSection section = expectSection(CONFIGURATION);
		for (MarkdownSection subsection : section.getSubSections()) {
			// single block in section is considered config
			if (subsection.numElementsOfType(ElementType.OTHER) == 1) {
				addInput(subsection.getTitle(), CONFIG, subsection.expectElement(ElementType.OTHER).getLines());
			}
			// add single blocks in subsections
			for (MarkdownSection subsubSection : subsection.getSubSections()) {
				if (subsubSection.numElementsOfType(ElementType.OTHER) == 1) {
					addInput(subsection.getTitle(), subsubSection.getTitle(), subsubSection.expectElement(ElementType.OTHER).getLines());
				}
			}
			for (MarkdownElement element : subsection.getElements()) {
				if (element.getType().equals(ElementType.PARAGRAPH)) {
					for (MarkdownLink link : element.getLinks()) {
						addInput(subsection.getTitle(), link.getName(), loadLinkedData(link));
					}
				}
			}
		}
	}

	private void addInput(final String stepId, final String inputName, byte[] data) {
		Log.log("Read {} bytes for input {} of step {}", data.length, inputName, stepId);
		expectStep(stepId).setConfigInput(inputName, data);
	}

	private void addInput(final String stepId, final String inputName, List<String> lines) {
		byte[] data = String.join("\n", lines).getBytes(StandardCharsets.UTF_8);
		Log.debug("Read {} lines ({} bytes) for input {} of step {}", lines.size(), data.length, inputName, stepId);
		expectStep(stepId).setConfigInput(inputName, data);
	}

	private PipelineStep expectStep(final String stepId) {
		PipelineStep res = plantUML.getSteps().get(stepId);
		if (res == null) {
			throw new MarkdownParsingException("No such step in pipeline: "+stepId);
		}
		return res;
	}

	private void show() {
		System.out.println("Parsed Markdown:");
	}

	public Pipeline createPipeline(Map<String, String> variables) {
		setImages(variables);
		return new Pipeline(getMainSection().getTitle(),
				getMainSection().expectElement(ElementType.PARAGRAPH).getLines(),
				plantUML.getSteps().values(),
				plantUML.getInput(),
				plantUML.getOutput());
	}

	protected void setImages(Map<String, String> variables) {
		for (PipelineStep step : plantUML.getSteps().values()) {
			int count = 0;
			if (bundledImage(step, variables)) {
				count++;
			}
			if (managedImage(step, variables)) {
				count++;
			}
			if (genericImage(step)) {
				count++;
			}
			if (count == 0) {
				throw new MarkdownParsingException("No docker image specified for step "+step.getId());
			}
			if (count > 1) {
				throw new MarkdownParsingException("Multiple docker image specified for step "+step.getId());
			}
		}
	}

	private boolean bundledImage(PipelineStep step, Map<String, String> variables) {
		Map<BundledImageColumns, String> row = determineRow(step, bundledImages, BundledImageColumns.STEP);
		if (row == null) {
			return false;
		}
		String imageLink = row.get(BundledImageColumns.IMAGE);
		String[] imageAndPath = parseLink(imageLink);
		String image = determineBundledImage(imageAndPath[0], variables);
		step.setImage(DockerImage.bundled(image, imageAndPath[1]));
		return true;
	}

	private String determineBundledImage(String imageName, Map<String, String> variables) {
		Map<String, String> extendedVars = new HashMap<>(variables);
		extendedVars.put(PATTERN_VAR_IMAGE_NAME, imageName);
		return resolveNamingConvention(BUNDLED_IMAGE_PATTERN_KEY, extendedVars);
	}

	private boolean managedImage(PipelineStep step, Map<String, String> variables) {
		Map<ManagedImageColumns, String> row = determineRow(step, managedImages, ManagedImageColumns.STEP);
		if (row == null) {
			return false;
		}
		String imageLink = row.get(ManagedImageColumns.IMAGE);
		String[] imageAndPath = parseLink(imageLink);
		String image = imageAndPath[0];
		String provider = determineManagedNamespace(row.get(ManagedImageColumns.PROVIDER), variables);
		String generation = row.get(ManagedImageColumns.GENERATION);
		Integer igen;
		if ((generation == null) || generation.isBlank()) {
			igen = null;
		} else {
			igen = Integer.parseInt(generation);
		}
		step.setImage(DockerImage.managed(provider, image, imageAndPath[1], igen));
		return true;
	}

	public String determineManagedNamespace(String provider, Map<String, String> variables) {
		Map<String, String> extendedVars = new HashMap<>(variables);
		extendedVars.put(PATTERN_VAR_PROVIDER, provider);
		return resolveNamingConvention(MANAGE_NAMESPACE_PATTERN_KEY, extendedVars);
	}

	private String resolveNamingConvention(String name, Map<String, String> variables) {
		Map<NamingConventionColumns, String> row = namingConventions.findRow(NamingConventionColumns.NAME, name);
		Expect.notNull(row).elseFail("Naming convention definition not found: "+name);
		String value = row.get(NamingConventionColumns.VALUE);
		VariableResolver resolver = new VariableResolver(variables);
		return resolver.substituteVariables(value);
	}

	private boolean genericImage(PipelineStep step) {
		Map<GenericImageColumns, String> row = determineRow(step, genericImages, GenericImageColumns.STEP);
		if (row == null) {
			return false;
		}
		String image = row.get(GenericImageColumns.IMAGE);
		String tag = row.get(GenericImageColumns.TAG);
		step.setImage(DockerImage.generic( image, tag));
		return true;
	}


	private String getPath(String image, String path) {
		String prefix = "https://gitlab.breuni.de/";
		if (!path.endsWith("/"+image)) {
			throw new RuntimeException("The image path must end with image name (the link text)");
		}
		if (!path.startsWith(prefix)) {
			throw new RuntimeException("The image path must start with: "+prefix);
		}
		return path.substring(prefix.length(), path.length() - image.length()-1);
	}

	private String[] parseLink(String imageLink) {
		Matcher matcher = Pattern.compile("\\[(.*)\\]\\((.*)\\)").matcher(imageLink);
		if (!matcher.matches()) {
			throw new RuntimeException("The link does not match the expected pattern: "+imageLink);
		}
		return new String[] { matcher.group(1), matcher.group(2) };
	}

	private void checkConsistency(boolean managed, String team, String version) {
		if (managed) {
			if (!version.isBlank()) {
				throw new RuntimeException("version column must be empty for managed docker images");
			}
			if (team.isBlank()) {
				throw new RuntimeException("team column must be set for managed docker images");
			}
		} else {
			if (version.isBlank()) {
				throw new RuntimeException("version column must be set for not managed docker images");
			}
		}
	}

	private String determineRepository(String providingTeam) {
		if (providingTeam.isBlank()) {
			return "europe-west3-docker.pkg.dev/breuni-team-admin-{team}/docker-{environment}";
		}
		return "europe-west3-docker.pkg.dev/breuni-team-admin-"+providingTeam+"/docker-prod";
	}

	protected <C> Map<C, String> determineRow(final PipelineStep step, final Table<C> table, final C stepColumn) {
		List<Map<C, String>> rows = table.findRows(stepColumn, step.getId());
		if (rows.size() > 1) {
			throw new MarkdownParsingException("Step "+step.getId()+" found twice in table");
		}
		if (rows.size() == 1) {
			return rows.get(0);
		}
		rows = table.findRows(stepColumn, WILDCARD);
		if (rows.size() > 1) {
			throw new MarkdownParsingException("Wildcard entry found twice in table");
		}
		if (rows.isEmpty()) {
			return null;
		}
		return rows.get(0);
	}

}
