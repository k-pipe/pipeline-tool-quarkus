package com.kneissler.job.client;

import com.kneissler.ui.domain.StepState;
import org.jkube.pipeline.definition.PipelineStep;

import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

public class StepStatistics {

    private final PipelineStep step;

    private final Map<Integer, BatchRunStatistics> batchRuns;

    public StepStatistics(PipelineStep step) {
        this.step = step;
        this.batchRuns = new TreeMap<>();
    }

    public PipelineStep getStep() {
        return step;
    }

    public void incCount(int batchNum, String pipeId, long size) {
        batchRuns.putIfAbsent(batchNum, new BatchRunStatistics());
        batchRuns.get(batchNum).incCount(pipeId, size);
    }

    public void setState(int batchNum, StepState state) {
        batchRuns.putIfAbsent(batchNum, new BatchRunStatistics());
        batchRuns.get(batchNum).setState(state);
    }

    public StepState getAggregatedState(int numBatches, boolean isBatched) {
        StepState state = null;
        int numSuccess = 0;
        for (BatchRunStatistics br : batchRuns.values()) {
            state = aggregate(state, br.getState());
            if (br.getState().equals(StepState.SUCCESS)) {
                numSuccess++;
            }
        }
        if (StepState.SUCCESS.equals(state) && (numSuccess != numBatches) && isBatched) {
            // all successful, but some batch runs not startet yet
            state = StepState.RUNNING;
        }
        return state;
    }

    private StepState aggregate(StepState s1, StepState s2) {
        if (s1 == null) {
            return s2;
        }
        if (s2 == null) {
            return s1;
        }
        return s1.compareTo(s2) > 0 ? s1 : s2;
    }

    public long getAggregatedCount(String pipename) {
        return batchRuns.values().stream().collect(Collectors.summingLong(brs -> brs.getCount(pipename)));
    }

}
