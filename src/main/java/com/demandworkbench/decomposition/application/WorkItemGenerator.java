package com.demandworkbench.decomposition.application;

import com.demandworkbench.decomposition.domain.model.WorkItemTree;

public interface WorkItemGenerator {
    GenerationResult generate(String requirementText);

    record GenerationResult(WorkItemTree tree, String rawResponse, Integer promptTokens, Integer completionTokens) {}
}
