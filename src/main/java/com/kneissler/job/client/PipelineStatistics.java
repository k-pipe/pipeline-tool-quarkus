package com.kneissler.job.client;

import com.kneissler.ui.PipelineVisualizer;
import com.kneissler.ui.domain.StepState;
import org.jkube.logging.Log;
import org.jkube.pipeline.definition.PipelineConnector;
import org.jkube.pipeline.definition.PipelineStep;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class PipelineStatistics {

    public static final String PIPE_EXTENSION = ".pipe";

    private static final String FAILED = "FAILED";
    private static final String TEXT_EXTENSION = ".txt";
    private Map<String, StepStatistics> stepStatisticsMap = new LinkedHashMap<>();

    public PipelineStatistics(Map<String, PipelineStep> steps) {
        steps.forEach((id, step) -> stepStatisticsMap.put(cleanup(id), new StepStatistics(step)));
    }

    private String cleanup(String stepId) {
        return stepId.toLowerCase().replaceAll("[^0-9a-z]", "");
    }

    public Map<String, StepStatistics> getStepStatisticsMap() {
        return stepStatisticsMap;
    }

    public void setSizes(List<PipeSizeItem> pipeSizes) {
        pipeSizes.forEach(ps -> stepStatisticsMap.get(ps.getStepId()).incCount(ps.getBatchNum(), ps.getPipeId(), ps.getSize()));
    }

    public void setStates(List<StepItem> items, StepState state) {
        items.forEach(i -> stepStatisticsMap.get(i.getStepId()).setState(i.getBatchNum(), state));
    }

    public void updateVisualizer(PipelineVisualizer pv, int numBatches, Set<PipelineStep> batchedSteps) {
        stepStatisticsMap.values().forEach(ss -> updateStep(ss, pv, numBatches, batchedSteps));
    }

    private void updateStep(StepStatistics ss, PipelineVisualizer pv, int numBatches, Set<PipelineStep> batchedSteps) {
        pv.setState(ss.getStep(), ss.getAggregatedState(numBatches, batchedSteps.contains(ss)));
        pv.setNumFailed(ss.getStep(), ss.getAggregatedCount(FAILED));
        Log.log("Num outputs: "+   ss.getStep().getOutputs().size());
        ss.getStep().getOutputs().forEach(c -> pv.setNumItems(c, getCountString(ss, c)));
    }

    private String getCountString(StepStatistics ss, PipelineConnector c) {
        String name = c.getNameAtSource();
        long num = ss.getAggregatedCount(name);
        return isPipe(name) ? Long.toString(num) :
                isText(name) ? Long.toString(num)+" lines" : byteString(num);
    }

    public static boolean isPipe(String file) {
        return file.endsWith(PIPE_EXTENSION);
    }

    public static boolean isText(String file) {
        return file.endsWith(TEXT_EXTENSION);
    }

    private String byteString(long num) {
        if (num < 1024) {
            return num +" bytes";
        }
        if (num < 1024*1024) {
            return num / 1024 +"kb";
        }
        if (num < 1024*1024*1024) {
            return num / 1024 / 1024 +"Mb";
        }
        return num / 1024 / 1024 / 1024 +"Gb";
    }
}
