package com.demandworkbench.decomposition.application;

import com.demandworkbench.decomposition.domain.model.WorkItemTree;
import com.demandworkbench.decomposition.domain.type.TaskStage;
import com.demandworkbench.decomposition.domain.type.TaskStatus;
import java.time.LocalDateTime;
import java.util.List;

public interface DecompositionRepository {
    default void create(String id, String content, String model, String promptVersion, LocalDateTime createdAt) {
        create(id, 1L, content, model, promptVersion, createdAt);
    }
    void create(String id, long ownerUserId, String content, String model, String promptVersion, LocalDateTime createdAt);
    void updateStage(String id, TaskStatus status, TaskStage stage);
    void complete(String id, WorkItemGenerator.GenerationResult result);
    void fail(String id, DecompositionErrorCode errorCode, String errorMessage);
    TaskSnapshot find(String id);
    WorkItemTree findTree(String id);
    int failInterruptedTasks();
    int deleteWorkItems(long ownerUserId, List<String> workItemNos);
    WorkItemData saveWorkItem(long ownerUserId, WorkItemData data);

    default int deleteWorkItems(List<String> workItemNos) { return deleteWorkItems(1L, workItemNos); }
    default WorkItemData saveWorkItem(WorkItemData data) { return saveWorkItem(1L, data); }

    record TaskSnapshot(String id, long ownerUserId, TaskStatus status, TaskStage stage, DecompositionErrorCode errorCode,
                        String errorMessage, LocalDateTime createdAt, LocalDateTime completedAt) {
        public TaskSnapshot(String id, TaskStatus status, TaskStage stage, DecompositionErrorCode errorCode,
                            String errorMessage, LocalDateTime createdAt, LocalDateTime completedAt) {
            this(id, 1L, status, stage, errorCode, errorMessage, createdAt, completedAt);
        }
    }
}
