package com.kneissler.job.client;

import com.kneissler.ui.domain.StepState;
import org.jkube.logging.Log;

import java.util.LinkedHashMap;
import java.util.Map;

public class BatchRunStatistics {
    private StepState state;
    private Map<String, Long> pipeSizes = new LinkedHashMap<>();

    public void incCount(String pipeId, long size) {
        pipeSizes.put(pipeId, pipeSizes.getOrDefault(pipeId, 0l)+size);
    }

    public void setState(StepState state) {
        this.state = state;
    }

    public StepState getState() {
        return state;
    }

    public long getCount(String pipename) {
        Log.log("Count for {}: {}", pipename, pipeSizes.get(pipename));
        return pipeSizes.getOrDefault(pipename, 0L);
    }
}
