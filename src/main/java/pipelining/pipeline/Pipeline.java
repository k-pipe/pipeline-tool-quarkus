package pipelining.pipeline;

import pipelining.script.pipeline.PipelineCreator;
import pipelining.script.pipeline.PipelineMarkdownWithSettings;
import pipelining.script.pipeline.pipeline_v2.PipelineV2;
import pipelining.util.richfile.resolver.VariableResolver;
import pipelining.pipeline.definition.PipelineStep;
import pipelining.util.Expect;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class Pipeline {

    public static final String UNSPECIFIED_RUN_CONFIG = "";
    private final PipelineMarkdownWithSettings markdown;
    private final PipelineV2 pipeline;
    private final List<String> pumlLines;
    private final Map<String, String> namings;
    private final Map<String, String> variables;
    private final Map<String, RunConfig> runConfigs = new LinkedHashMap<>();

    public Pipeline(PipelineMarkdownWithSettings markdown, PipelineV2 pipeline, List<String> pumlLines, Map<String, String> namings, Map<String, String> variables) {
        this.markdown = markdown;
        this.pipeline = pipeline;
        this.pumlLines = pumlLines;
        this.namings = namings;
        this.variables = variables;
    }

    public PipelineMarkdownWithSettings getMarkdown() {
        return markdown;
    }

    public PipelineV2 getPipeline() {
        return pipeline;
    }

    public VariableResolver getResolver() {
        return new VariableResolver(markdown.getVariables());
    }

    public List<String> getPumlLines() {
        return pumlLines;
    }

    public Map<String, String> getNamings() {
        return namings;
    }

    public Map<String, RunConfig> getRunConfigs() {
        return runConfigs;
    }

    public RunConfig getConfig(String configId) {
        if (configId.equals(Pipeline.UNSPECIFIED_RUN_CONFIG)) {
            return null;
        }
        Expect.isTrue(runConfigs.containsKey(configId)).elseFail("No such run config was specified: "+configId);
        return runConfigs.get(configId);
    }

    public String createManifest() {
        return new PipelineCreator(pipeline, pumlLines, null,  null, false).create(namings, variables);
    }

    public String getNamespace() {
        return expectNaming("namespace");
    }

    private String expectNaming(String key) {
        Expect.isTrue(namings.containsKey(key)).elseFail("Naming convention is not specified: "+key);
        return namings.get(key);
    }

    public String getVersion() {
        return expectNaming("pipelineVersion");
    }

    public String getName() {
        return expectNaming("pipelineName");
    }

    public List<Schedule> getSchedules() {
        return markdown.getMatchingSchedules();
    }

    public List<PipelineStep> determineSequence(String startStep, String endStep, Map<PipelineStep, Integer> stepNumbers) {
        List<PipelineStep> stepSequence = pipeline.determineSequence();
        List<PipelineStep> res = new ArrayList<>();
        int count = 0;
        boolean foundStart = startStep == null;
        boolean foundEnd = false;
        for (PipelineStep s : stepSequence) {
            count++;
            stepNumbers.put(s, count);
            if (s.getId().equals(startStep)) {
                foundStart = true;
            }
            if (foundStart && !foundEnd) {
                res.add(s);
            }
            if (s.getId().equals(endStep)) {
                Expect.isTrue(foundStart).elseFail("end step '"+endStep+"' occurs before start step '"+startStep+"'");
                foundEnd = true;
            }
        }
        Expect.isTrue(foundStart).elseFail("start step not found: "+startStep);
        Expect.isTrue((endStep == null) || foundEnd).elseFail("end step not found: "+endStep);
        return res;
    }
}
