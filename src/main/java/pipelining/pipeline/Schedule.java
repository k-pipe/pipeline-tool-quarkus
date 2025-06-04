package pipelining.pipeline;

import pipelining.script.pipeline.Yaml;
import pipelining.script.pipeline.YamlList;
import pipelining.script.pipeline.YamlMap;

public class Schedule {
    private final String name;
    private final String schedule;
    private final String timezone;
    private final RunConfig runConfig;
    private final String description;

    public Schedule(String name, String schedule, String timezone, String description, RunConfig runConfig) {
        this.name = name;
        this.schedule = schedule;
        this.timezone = timezone;
        this.runConfig = runConfig;
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public String createManifest(Pipeline pipeline) {
        YamlMap res = Yaml.map()
                .add("apiVersion", "pipeline.k-pipe.cloud/v1")
                .add("kind", "PipelineSchedule");
        res.addMap("metadata")
                .add("name", name)
                .add("namespace", pipeline.getNamespace());
        YamlMap specs = res.addMap("spec")
                .add("pipelineName", pipeline.getName());
        YamlList schedules = specs.addList("schedules");
        schedules.add(Yaml.map()
                    .add("cronSpec",schedule)
                    .add("versionPattern", pipeline.getVersion())
                    .add("timeZone", timezone)
        );
        if (runConfig != null) {
            runConfig.addToYaml(specs.addMap("parameters"));
        }
        return String.join("\n", res.getLines());
    }

}
