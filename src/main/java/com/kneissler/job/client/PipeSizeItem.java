package com.kneissler.job.client;

import org.jkube.util.Expect;

public class PipeSizeItem {

    private final String stepId;

    private final int batchNum;

    private final String pipeId;

    private final long size;

    public PipeSizeItem(String[] split) {
        Expect.equal(split.length, 4).elseFail("Expected 4 pipe size elements, got "+split.length);
        this.stepId = split[0];
        this.batchNum = Integer.parseInt(split[1]);
        this.pipeId = split[2];
        this.size = Integer.parseInt(split[3]);
    }

    public String getStepId() {
        return stepId;
    }

    public int getBatchNum() {
        return batchNum;
    }

    public String getPipeId() {
        return pipeId;
    }

    public long getSize() {
        return size;
    }
}
