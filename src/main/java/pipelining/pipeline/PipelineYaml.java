package pipelining.pipeline;

import pipelining.script.pipeline.Yaml;
import pipelining.script.pipeline.YamlList;
import pipelining.script.pipeline.YamlMap;
import pipelining.util.richfile.resolver.VariableResolver;
import pipelining.job.DockerImage;
import pipelining.pipeline.definition.PipelineConnector;
import pipelining.pipeline.definition.PipelineStep;
import pipelining.util.Expect;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class PipelineYaml {

    private final Map<String, String> variables;
    private String description;
    private String name;
    private List<PipelineStep> steps;

    private PipelineConnector batchingConnector;

    public PipelineYaml(Map<String, String> variables) {
        this.variables = variables;
    }

    private String naming(String key) {
        String res = variables.get(key);
        Expect.notNull(res).elseFail("No such naming convention was set: "+key);
        return res;
    }

    public String getDescription() {
        return description;
    }

    public String getName() {
        return name;
    }

    public List<PipelineStep> getSteps() {
        return steps;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setSteps(List<PipelineStep> steps) {
        this.steps = steps;
    }
    public void setBatchingSubPipline(PipelineConnector batchingConnector) {
        this.batchingConnector = batchingConnector;
    }

    public String getString() {
        YamlMap res = Yaml.map()
                .add("apiVersion", "pipeline.k-pipe.cloud/v1")
                .add("kind", "PipelineDefinition");
        res.addMap("metadata")
                .add("name", naming("pipelineDefinition"))
                .add("namespace", naming("namespace"));
        res.addMap("spec")
                .add("pipelineName", naming("pipelineName"))
                .add("version", naming("pipelineVersion"))
                .add("description", description)
                .add("pipelineStructure", createPipelineStructure())
                .add("jobSpecs", createJobSpecs())
                .add("stepFinalizers", createStepFinalizers());
        return String.join("\n", res.getLines());
    }

    private YamlMap createPipelineStructure() {
        return Yaml.map()
            .add("jobSteps", createJobSteps())
            .add("subPipelines", createSubPipelines())
            .add("pipes", createPipes());
    }

    private YamlList createJobSteps() {
        YamlList res = Yaml.list();
        steps.forEach(s -> res.add(createJobStep(s)));
        return res;
    }

    private YamlMap createJobStep(PipelineStep step) {
        Object confdata = step.getConfigInputs().get("config.json");
        String confString = confdata == null ? "" : new String((byte[])confdata, StandardCharsets.UTF_8);
        List<String> config = Arrays.asList(confString.split("\n"));
        VariableResolver vr = new VariableResolver(variables);
        List<String> configResolved = config.stream().map(vr::substituteVariables).collect(Collectors.toList());
        return Yaml.map()
            .add("id", step.getId())
            .add("image", createImage(step.getDockerImage()))
            //.add("args", createArgs(step.getConfigInputs(), step.getInputs(), step.getOutputs()))
            .add("config", Yaml.subYaml(configResolved));
    }


    private YamlList createSubPipelines() {
        YamlList res = Yaml.list();
        if (batchingConnector != null) {
            res.add(createSubPipeline());
        }
        return res;
    }

    private YamlMap createSubPipeline() {
        return Yaml.map()
                .add("id", "batch")
                .add("namespace", "{team}")
                .add("pipelineName", "{pipeline_name}-batch")
                .add("versionPattern", "{tag}")
                .add("batched", true);
    }

    private YamlList createPipes() {
        YamlList res = Yaml.list();
        steps.forEach(s -> {
            s.getOutputs().forEach(o -> res.add(createPipe(o)));
        });
        return res;
        
    }

    private YamlMap createPipe(PipelineConnector pc) {
        if (pc.equals(batchingConnector)) {
            throw new RuntimeException("not implemented");
        }
        return Yaml.map()
            .add("from", Yaml.map()
                .add("stepId", pc.getSource().getId())
                .add("name", pc.getNameAtSource())
            )
            .add("to", Yaml.map()
                    .add("stepId", pc.getTarget().getId())
                    .add("name", pc.getNameAtTarget())
            )
            .add("filename", pc.getFilename());
    }

    private YamlList createJobSpecs() {
        return Yaml.list().add(Yaml.map()
            .add("stepPattern", "*")
            .add("jobSpec", Yaml.map()
                .add("serviceAccountName", naming("k8s-serviceAccount"))
                .add("backoffLimit", 0)
                .add("imagePullPolicy", "Always")
            )
        );
    }

    private YamlList createStepFinalizers() {
        return Yaml.list().add(Yaml.map()
                .add("workloadSpec", Yaml.map()
                        .add("id", "archive")
                        .add("image", createImage("google/cloud-sdk:slim"))
                        .add("command", "sh", "-c", variables.get("stepFinalizerCommand"))
                )
                .add("jobSpec", Yaml.map()
                        .add("serviceAccountName", variables.get("k8s-serviceAccount"))
                        .add("backoffLimit", 1)
                )
       );
    }

    private YamlMap createImage(String name) {
        return Yaml.map()
            .add("name", name);
    }

    private YamlMap createImage(DockerImage image) {
        YamlMap res = Yaml.map().add("name", image.getImage());
        if (image.isManaged()) {
            res.add("provider", image.getProvider());
            if (image.getGeneration() != null) {
                res.add("generation", image.getGeneration());
            }
        } else  if (image.getTag() != null) {
            res.add("tag", image.getTag());
        }
        return res;
    }

}
