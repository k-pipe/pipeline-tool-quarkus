package cloud.kpipe.actions;

import cloud.kpipe.clparser.Command;
import cloud.kpipe.pipeline.Pipeline;
import cloud.kpipe.pipeline.PipelineRun;
import cloud.kpipe.pipeline.RunConfig;
import org.jkube.util.Expect;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

public class RunAction implements Action {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd-hh-mm-ss");

    @Override
    public void doAction(Command command, ActionData ad) {
        Pipeline pipeline = ad.getLatestParsedPipeline();
        String pipelineName = command.getOptionalOptionValue(Constants.PIPELINE).orElseGet(() -> pipeline.getName());
        String configId = command.getOptionalOptionValue(Constants.CONFIG).orElse(getUniqueConfig(pipeline));
        String runId = command.getOptionalOptionValue(Constants.CONFIG).orElse(createRunId(pipeline.getName()));
        if (command.isFlagSet(Constants.TIMESTAMP)) {
            runId += timestampSuffix();
        }
        PipelineRun run = new PipelineRun(runId, pipeline, pipeline.getConfig(configId));
        Path output = command.getOptionalOptionValue(Constants.OUTPUT).map(ad::getOutputPath).orElse(ad.getManifest(pipelineName));
        ad.appendToManifest(output, run.createManifest());
        ad.addRun(pipelineName, run);
    }

    private String getUniqueConfig(Pipeline pipeline) {
        Map<String, RunConfig> configs = pipeline.getRunConfigs();
        if(configs.isEmpty()) {
            return Pipeline.UNSPECIFIED_RUN_CONFIG; // default config not specified in md file
        }
        Expect.equal(configs.size(), 1).elseFail("There are multiple run configs defined for the pipeline '"+pipeline.getName()+"', must specify which one to use");
        return configs.keySet().iterator().next();
    }

    private String createRunId(String name) {
        return name;
    }

    private String timestampSuffix() {
        return "-"+ LocalDateTime.now().format(DATE_FORMAT);
    }

}
