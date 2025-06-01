package cloud.kpipe.pipeline;

import com.kneissler.script.pipeline.Yaml;
import com.kneissler.script.pipeline.YamlMap;

public class PipelineRun {

    private final String runId;
    private final String namespace;
    private final String pipelineName;
    private final String pipelineVersion;
    private final RunConfig config;

    public PipelineRun(String runId, Pipeline pipeline, RunConfig config) {
        this.runId = runId;
        this.namespace = pipeline.getNamespace();
        this.pipelineName = pipeline.getName();
        this.pipelineVersion = pipeline.getVersion();
        this.config = config;
    }

    public String createManifest() {
        YamlMap res = Yaml.map()
                .add("apiVersion", "pipeline.k-pipe.cloud/v1")
                .add("kind", "PipelineRun");
        res.addMap("metadata")
                .add("name", runId)
                .add("namespace", namespace);
        YamlMap specs = res.addMap("spec")
                .add("pipelineName", pipelineName)
                .add("versionPattern", pipelineVersion);
        if (config != null) {
            config.addToYaml(specs.addMap("parameters"));
        }
        return String.join("\n", res.getLines());
    }

    public String getRunId() {
        return runId;
    }

    public String getNamespace() {
        return namespace;
    }
}
