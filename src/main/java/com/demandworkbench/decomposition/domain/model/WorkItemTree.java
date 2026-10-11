package com.demandworkbench.decomposition.domain.model;

import java.util.List;

public record WorkItemTree(List<WorkItemNode> modules) {
    public WorkItemTree {
        modules = modules == null ? List.of() : List.copyOf(modules);
    }
}
