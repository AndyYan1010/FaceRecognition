package com.botian.recognition.sdksupport;

import com.tencent.cloud.ai.fr.pipeline.AbsStep;

import java.util.ArrayList;
import java.util.List;

public class SyncJobBuilder {

    private final List<AbsStep<StuffBox>> mPipeline = new ArrayList<>();

    public SyncJobBuilder addStep(AbsStep<StuffBox> step) {
        mPipeline.add(step);
        return this;
    }

    public List<AbsStep<StuffBox>> build() {
        return mPipeline;
    }

}