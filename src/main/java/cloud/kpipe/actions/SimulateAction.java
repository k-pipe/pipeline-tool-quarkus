package cloud.kpipe.actions;

import cloud.kpipe.clparser.Command;
import cloud.kpipe.pipeline.Pipeline;
import cloud.kpipe.pipeline.RunConfig;
import cloud.kpipe.simulator.RunSimulator;
import org.jkube.util.Expect;

import java.util.*;

public class SimulateAction implements Action {
    @Override
    public void doAction(Command command, ActionData ad) {
        String pipelineName = command.getOptionalOptionValue(Constants.PIPELINE).orElseGet(() -> ad.getLatestParsedPipeline().getName());
        Pipeline pipeline = ad.findParsedPipeline(pipelineName);
        String workdir = command.getOptionValue(Constants.WORKDIR);
        String simulationdir = command.getOptionValue(Constants.SIMULATIONDIR);
        String startStep = command.getOptionalOptionValue(Constants.BEGIN).orElse(null);
        String endStep = command.getOptionalOptionValue(Constants.END).orElse(null);
        String credentialsMount = command.getOptionValue(Constants.CREDENTIALS);
        RunSimulator simulator = new RunSimulator(workdir, simulationdir, startStep, endStep, credentialsMount);
        simulator.runLocally(pipeline);
    }

    private String getUniqueConfig(Pipeline pipeline) {
        Map<String, RunConfig> configs = pipeline.getRunConfigs();
        if(configs.isEmpty()) {
            return Pipeline.UNSPECIFIED_RUN_CONFIG; // default config not specified in md file
        }
        Expect.equal(configs.size(), 1).elseFail("There are multiple run configs defined for the pipeline '"+pipeline.getName()+"', must specify which one to use");
        return configs.keySet().iterator().next();
    }

}
