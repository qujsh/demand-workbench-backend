package com.demandworkbench.decomposition.infrastructure.ai;

import java.util.List;

record AiWorkItemTree(List<AiNode> modules) {
    record AiNode(String name, String description, String priority, String dependency, List<AiNode> children) {}
}
