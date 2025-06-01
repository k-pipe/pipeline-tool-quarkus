package com.kneissler.job.client;

import org.jkube.util.Expect;

public class StepItem {

    private final String stepId;

    private final int batchNum;

    public StepItem(String[] split) {
        Expect.equal(split.length, 2).elseFail("Expected 2 step elements, got "+split.length);
        this.stepId = split[0];
        this.batchNum = Integer.parseInt(split[1]);
    }

    public String getStepId() {
        return stepId;
    }

    public int getBatchNum() {
        return batchNum;
    }
}
