package com.demandworkbench.decomposition.application;

import com.demandworkbench.decomposition.domain.model.WorkItemTree;
import com.demandworkbench.decomposition.domain.type.TaskStage;
import com.demandworkbench.decomposition.domain.type.TaskStatus;
import java.time.LocalDateTime;
import java.util.List;

public interface DecompositionRepository {
    void create(String id, String content, String model, String promptVersion, LocalDateTime createdAt);
    void updateStage(String id, TaskStatus status, TaskStage stage);
    void complete(String id, WorkItemGenerator.GenerationResult result);
    void fail(String id, DecompositionErrorCode errorCode, String errorMessage);
    TaskSnapshot find(String id);
    WorkItemTree findTree(String id);
    int failInterruptedTasks();
    int deleteWorkItems(List<String> workItemNos);

    record TaskSnapshot(String id, TaskStatus status, TaskStage stage, DecompositionErrorCode errorCode,
                        String errorMessage, LocalDateTime createdAt, LocalDateTime completedAt) {}
}
