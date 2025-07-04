package pipelining.script.pipeline;

import pipelining.job.specification.LimitRange;
import pipelining.job.specification.ResourceLimits;
import pipelining.pipeline.RunConfig;
import pipelining.pipeline.Schedule;
import pipelining.script.pipeline.pipeline_v2.PipelineMarkdownV2;
import pipelining.script.pipeline.pipeline_v2.PipelineV2;
import pipelining.util.richfile.resolver.VariableResolver;
import pipelining.codegen.ParagraphScanner;
import pipelining.codegen.Table;
import pipelining.logging.Log;
import pipelining.markdown.ElementType;
import pipelining.markdown.MarkdownElement;
import pipelining.markdown.MarkdownParsingException;
import pipelining.markdown.MarkdownSection;
import pipelining.markdown.pipeline.v1.NamingConventionColumns;
import pipelining.pipeline.definition.PipelineStep;
import pipelining.util.Expect;

import java.nio.file.Path;
import java.util.*;

import static pipelining.logging.Log.log;

public class PipelineMarkdownWithSettings extends PipelineMarkdownV2 {

	private static final String FACTOR_PREFIX = "*";
	private static final String INCREMENT_PREFIX = "+";
	private static final String NO_RETENTION = "-";
	private static final String TIMEOUT = "Timeout";
	private static final String RETENTION = "Retention";
	public static final String DEBUG = "Debug";
	public static final String PRODUCTION = "Production";
	public static final String MULTI_BATCH = "MultiBatch";
	public static final String SETTINGS = "Settings";
	public static final String PARAMETERS = "Parameters";
	public static final String METADATA = "Metadata";
	public static final String CONDITIONALS = "Conditionals";
	public static final String SCHEDULES = "Schedules";
	public static final String RESOURCES = "Resources";
	public static final String RUN_CONFIGURATIONS = "RunConfigurations";
	private static final String ENABLED = "X";
	private static final Set<String> EXCLUDED_NAMINGS = Set.of(
			PipelineMarkdownWithSettings.BUNDLED_IMAGE_PATTERN_KEY,
			PipelineMarkdownWithSettings.MANAGE_NAMESPACE_PATTERN_KEY,
			PipelineMarkdownWithSettings.PATTERN_VAR_PROVIDER,
			PipelineMarkdownWithSettings.PATTERN_VAR_IMAGE_NAME
			);
	private static final String DEFAULT_RUN_CONFIG_NAME = "run";

	private Table<ConstantsColumns> constants;
	private Table<ParameterColumns> parameters;

	private List<Table<String>> conditionals;
	private Table<String> schedules;
	private Table<ResourceColumns> resources;

	private List<Schedule> matchingSchedules;

	private Map<String, RunConfig> runConfigurations;

	private Map<String, String> variables;

	public PipelineMarkdownWithSettings(final Path includeRoot, final Path markdownPath,
			final boolean resolve, final Map<String, String> parameters) {
		super(includeRoot, markdownPath, resolve, parameters);
		readTables();
		checkParametersCorrectAndEnhanceWithDefaults(parameters);
		readTablesWithParameterColumns(parameters.keySet());
		variables = determineVariables(parameters);
		runConfigurations = determineRunConfigurations(variables);
		matchingSchedules = determineSchedules(parameters, variables);
	}

	public Map<String, String> getVariables() {
		return variables;
	}


//public Map<String, String> getNamings() {
//		return namingConventions;
//	}

	private void checkParametersCorrectAndEnhanceWithDefaults(Map<String, String> variables) {
		Map<String, String> variablesUnused = new HashMap<>(variables);
		parameters.getRows().forEach(r -> {
			String name = r.get(ParameterColumns.NAME);
			String value = variablesUnused.remove(name);
			if (value == null) {
				value = r.get(ParameterColumns.DEFAULT);
				Expect.isFalse(value.isEmpty()).elseFail("Parameter has no default and is not specified in command line: "+name);
				variables.put(name, value);
			}
			String allowed = r.get(ParameterColumns.VALUES);
			Expect.isTrue(correctValue(value, allowed)).elseFail("Parameter value is not in "+allowed+": "+value);
		});
		if (!variablesUnused.isEmpty()) {
			log("The following common parameters were not used: " + variablesUnused.keySet());
		}
	}

	private Map<String, String> determineVariables(Map<String, String> parameters) {
		Map<String, String> variables = new HashMap<>(parameters);
		constants.getRows().forEach(r -> {
			String name = r.get(ConstantsColumns.NAME);
			String value = r.get(ConstantsColumns.VALUE);
			addResolved(name, value, variables);
		});
		Set<String> found = new HashSet<>();
		Set<String> missing = new HashSet<>();
		conditionals.forEach(cond -> {
			cond.getRows().forEach(r -> {
				String name = r.get(ConstantsColumns.NAME.name());
				String value = r.get(ConstantsColumns.VALUE.name());
				if (!found.contains(name)) {
					if (valuesMatch(r, parameters))	{
						addResolved(name, value, variables);
						found.add(name);
					}
				}
				missing.add(name);
			});
		});
		missing.removeAll(found);
		Expect.equal(missing.size(), 0).elseFail("Some conditionals did not get a value: "+missing);
		/*log("Variables after resolution:");
		variables.forEach((k,v) -> {
			log("   "+k+"="+v);
		});*/
		return variables;
	}

	private boolean valuesMatch(Map<String, String> conditions, Map<String, String> parameters) {
		for (Map.Entry<String, String> e : parameters.entrySet()) {
			String key = e.getKey();
			String value = e.getValue();
			String expected = conditions.get(key);
			if (!((expected == null) || expected.equals(WILDCARD) || expected.equals(value))) {
				return false;
			}
		}
		return true;
	}

	private void addResolved(String name, String value, Map<String, String> variables) {
		Expect.isFalse(variables.containsKey(name)).elseFail("Variable defined twice: "+name);
		VariableResolver resolver = new VariableResolver(variables);
		variables.put(name, resolver.substituteVariables(value));
	}

	private boolean correctValue(String value, String allowedValues) {
		return allowedValues.equals(WILDCARD) || List.of(allowedValues.split(",")).contains(value);
	}

	private void readTables() {
		parameters = expectTable(ParameterColumns.values(), false, GENERAL_SETUP, PARAMETERS);
		constants = expectTable(ConstantsColumns.values(), false, METADATA);
		//namingConventions = expectTable(NamingConventionColumns.values(), false, GENERAL_SETUP, NAMING_CONVENTIONS);
		resources = expectTable(ResourceColumns.values(), true, RESOURCES);
	}

	private void readTablesWithParameterColumns(Set<String> parameterKeys) {
		List<String> conditionalColumns = extendedColumns(ConstantsColumns.values(), parameterKeys);
		conditionals = readTablesInSubsections(conditionalColumns, true, SETTINGS);
		List<String> scheduledColumns = extendedColumns(SchedulesColumns.values(), parameterKeys);
		schedules = expectStringTable(scheduledColumns, true, SCHEDULES);
	}

	private <E extends Enum<E>> List<String> extendedColumns(E[] values, Set<String> parameterKeys) {
		List<String> res = new LinkedList<>();
		Arrays.stream(values).forEach(col -> res.add(col.toString()));
		res.addAll(parameterKeys);
		return res;
	}

	private List<Table<String>> readTablesInSubsections(List<String> columns, boolean allowMissing, String... sectionPath) {
		MarkdownSection section = tryGetSection(sectionPath);
		List<Table<String>> res = new LinkedList<>();
		if (section != null) {
			for (MarkdownSection sub : section.getSubSections()) {
				MarkdownElement element = sub.expectElement(ElementType.TABLE);
				res.add(new Table<>(new ParagraphScanner(element.getLines()), columns, allowMissing));
			}
		}
		return res;
	}

	public PipelineV2 createPipeline(Map<String, String> variables) {
		setImages(variables);
		final LinkedHashMap<PipelineStep, PipelineStepSettings> stepSettings = new LinkedHashMap<>();
		getSteps().values().forEach(step -> stepSettings.put(step, new PipelineStepSettings()));
		setResources(stepSettings);
		return getPlantUML().createPipeline(
				getMainSection().getTitle(),
				getMainSection().expectElement(ElementType.PARAGRAPH).getLines(),
				stepSettings
		);
	}

	private Map<String, RunConfig> determineRunConfigurations(Map<String, String> variables) {
		Map<String, RunConfig> res = new LinkedHashMap<>();
		MarkdownSection section = tryGetSection(RUN_CONFIGURATIONS);
		if (section != null) {
			for (MarkdownSection sub : section.getSubSections()) {
				MarkdownElement element = sub.expectElement(ElementType.YAML);
				res.put(sub.getTitle(), parseRunConfig(sub.getTitle(), element.getLines(), variables));
			}
		}
		return res;
	}

	private RunConfig parseRunConfig(String name, List<String> lines, Map<String, String> variables) {
		VariableResolver resolver = new VariableResolver(variables);
		RunConfig res = new RunConfig(name);
		lines.forEach(line -> {
			String[] split = line.split(":");
			res.put(split[0].trim(), resolver.substituteVariables(split[1].trim()));
		});
		return res;
	}

	private List<Schedule> determineSchedules(Map<String, String> parameters, Map<String, String> variables) {
		List<Schedule> res = new ArrayList<>();
		Set<String> found = new HashSet<>();
		schedules.getRows().forEach(r -> {
			String name = r.get(SchedulesColumns.NAME.name());
			String schedule = r.get(SchedulesColumns.SCHEDULE.name());
			String timezone = r.get(SchedulesColumns.TIMEZONE.name());
			String runConfigName = r.get(SchedulesColumns.RUNCONFIG.name());
			RunConfig runConfig;
			if (runConfigName.isBlank()) {
				runConfig = new RunConfig(DEFAULT_RUN_CONFIG_NAME);
			} else {
				runConfig = runConfigurations.get(runConfigName);
				Expect.notNull(runConfig).elseFail("No run configuration defined with this name: " + runConfigName);
			}
			String description = r.get(SchedulesColumns.DESCRIPTION.name());
			if (valuesMatch(r, parameters)) {
				Expect.isFalse(found.contains(name)).elseFail("Multiple matching schedule rows with same name: " + name);
				found.add(name);
				res.add(new Schedule(name, schedule, timezone, description, runConfig));
			}
		});
		return res;
	}

	private void setResources(final LinkedHashMap<PipelineStep, PipelineStepSettings> stepSettings) {
		for (PipelineStep step : getSteps().values()) {
			Map<ResourceColumns, String> row = determineRow(step, resources, ResourceColumns.STEP);
			if (row == null) {
				throw new MarkdownParsingException("Step "+step.getId()+" not found and no wildcard entry in table");
			}
			final ResourceLimits resourceLimits = new ResourceLimits();
			resourceLimits.setCpu(parseLimits(row.get(ResourceColumns.CPU)));
			resourceLimits.setMemory(parseLimits(row.get(ResourceColumns.MEMORY)));
			resourceLimits.setDisk(parseLimits(row.get(ResourceColumns.DISK)));
			resourceLimits.setGpu(parseLimits(row.get(ResourceColumns.GPU)));
			PipelineStepSettings settings = stepSettings.get(step);
			settings.setResourceLimits(resourceLimits);
			settings.setJobClass(row.get(ResourceColumns.JOBCLASS));
		}
	}

	private LimitRange parseLimits(final String s) {
		// format X[-X] [*X] [+X]
		String[] split = s.split(" ");
		LimitRange res = new LimitRange();
		parseRange(res, split[0]);
		if (split.length > 3) {
			throw new MarkdownParsingException("expected at most 3 sections, found "+split.length+": "+s);
		}
		for (int i = 1; i < split.length; i++) {
			parseChange(res, split[i]);
		}
		return res;
	}

	private void parseRange(final LimitRange res, final String s) {
		String[] split = s.split("-");
		if (split.length > 2) {
			throw new MarkdownParsingException("expected at most 2 numbers in range sections, found "+split.length+": "+s);
		}
		int min = parseInt(split[0]);
		res.setMinimum(min);
		int max = split.length == 2 ? parseInt(split[1]) : min;
		res.setMaximum(max);
	}

	private int parseInt(final String s) {
		try {
			return Integer.parseInt(s);
		} catch (NumberFormatException e) {
			throw new MarkdownParsingException("Illegal integer value: "+s);
		}
	}

	private void parseChange(final LimitRange res, final String s) {
		if (s.startsWith(FACTOR_PREFIX)) {
			res.setFactor(parseFloat(s.substring(FACTOR_PREFIX.length())));
		} else if (s.startsWith(INCREMENT_PREFIX)) {
			res.setFactor(parseInt(s.substring(INCREMENT_PREFIX.length())));
		} else {
			throw new MarkdownParsingException("must set increment/factor with "+FACTOR_PREFIX+" or "+INCREMENT_PREFIX);
		}
	}

	private float parseFloat(final String s) {
		try {
			return Float.parseFloat(s);
		} catch (NumberFormatException e) {
			throw new MarkdownParsingException("Illegal float value: "+s);
		}
	}

	public void extendNamingConventions() {
		VariableResolver resolver = new VariableResolver(variables);
		EXCLUDED_NAMINGS.forEach(ex -> variables.put(ex, "${"+ex+"}"));

		namingConventions.getRows().forEach(row -> {
			String key = row.get(NamingConventionColumns.NAME);
			String value = row.get(NamingConventionColumns.VALUE);
			//if (!EXCLUDED_NAMINGS.contains(key)) {
				String resolved = resolver.substituteVariables(value);
				variables.put(key, resolved);
			//}
		});
	}

	public List<Schedule> getMatchingSchedules() {
		return matchingSchedules;
	}
}
