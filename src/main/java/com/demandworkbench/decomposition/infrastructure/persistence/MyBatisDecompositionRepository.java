package com.demandworkbench.decomposition.infrastructure.persistence;

import com.demandworkbench.decomposition.application.DecompositionRepository;
import com.demandworkbench.decomposition.application.DecompositionErrorCode;
import com.demandworkbench.decomposition.application.WorkItemGenerator;
import com.demandworkbench.decomposition.domain.model.WorkItemNode;
import com.demandworkbench.decomposition.domain.model.WorkItemTree;
import com.demandworkbench.decomposition.domain.type.TaskStage;
import com.demandworkbench.decomposition.domain.type.TaskStatus;
import com.demandworkbench.decomposition.domain.type.WorkItemType;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class MyBatisDecompositionRepository implements DecompositionRepository {
    private final DecompositionMapper decompositionMapper;
    private final WorkItemMapper workItemMapper;

    public MyBatisDecompositionRepository(DecompositionMapper decompositionMapper, WorkItemMapper workItemMapper) {
        this.decompositionMapper = decompositionMapper;
        this.workItemMapper = workItemMapper;
    }

    @Override
    public void create(String id, String content, String model, String promptVersion, LocalDateTime createdAt) {
        var record = new DecompositionRecord();
        record.setId(id);
        record.setSourceContent(content);
        record.setStatus(TaskStatus.QUEUED.getCode());
        record.setStage(TaskStage.QUEUED.getCode());
        record.setModel(model);
        record.setPromptVersion(promptVersion);
        record.setCreatedAt(createdAt);
        decompositionMapper.insert(record);
    }

    @Override
    public void updateStage(String id, TaskStatus status, TaskStage stage) {
        decompositionMapper.updateStage(id, status.getCode(), stage.getCode());
    }

    @Override
    @Transactional
    public void complete(String id, WorkItemGenerator.GenerationResult result) {
        workItemMapper.deleteByDecompositionId(id);
        var order = new AtomicInteger();
        for (var module : result.tree().modules()) persist(id, null, module, order);
        decompositionMapper.markSuccess(id, TaskStatus.SUCCESS.getCode(), TaskStage.COMPLETED.getCode(),
                result.rawResponse(), result.promptTokens(), result.completionTokens());
    }

    @Override
    public void fail(String id, DecompositionErrorCode errorCode, String errorMessage) {
        decompositionMapper.markFailed(id, TaskStatus.FAILED.getCode(), TaskStage.FAILED.getCode(),
                errorCode.getCode(), errorMessage);
    }

    @Override
    public TaskSnapshot find(String id) {
        var record = decompositionMapper.findById(id);
        if (record == null) return null;
        var errorCode = record.getErrorCode() == null
                ? null
                : DecompositionErrorCode.fromCode(record.getErrorCode());
        return new TaskSnapshot(record.getId(), TaskStatus.fromCode(record.getStatus()),
                TaskStage.fromCode(record.getStage()), errorCode,
                record.getErrorMessage(),
                record.getCreatedAt(), record.getCompletedAt());
    }

    @Override
    public WorkItemTree findTree(String id) {
        var records = workItemMapper.findByDecompositionId(id);
        Map<String, WorkItemNode> nodes = new LinkedHashMap<>();
        for (var record : records) nodes.put(record.getId(), toNode(record));
        var roots = new ArrayList<WorkItemNode>();
        for (var record : records) {
            var node = nodes.get(record.getId());
            if (record.getParentId() == null) roots.add(node);
            else {
                var parent = nodes.get(record.getParentId());
                if (parent != null) parent.children().add(node);
            }
        }
        return new WorkItemTree(roots);
    }

    @Override
    public int failInterruptedTasks() {
        var errorCode = DecompositionErrorCode.SERVER_RESTARTED;
        return decompositionMapper.failInterruptedTasks(TaskStatus.FAILED.getCode(), TaskStage.FAILED.getCode(),
                errorCode.getCode(), errorCode.getMsg(), TaskStatus.QUEUED.getCode(), TaskStatus.PROCESSING.getCode());
    }

    private void persist(String taskId, String parentId, WorkItemNode node, AtomicInteger order) {
        var persistedId = taskId + "-" + node.id();
        var now = LocalDateTime.now();
        var record = new WorkItemRecord();
        record.setId(persistedId);
        record.setDecompositionId(taskId);
        record.setParentId(parentId);
        record.setType(node.type().getCode());
        record.setName(node.name());
        record.setDescription(node.description());
        record.setStatus(node.status());
        record.setRoleName(node.role());
        record.setPriority(node.priority());
        record.setStartDate(node.startDate());
        record.setEndDate(node.endDate());
        record.setDependencyText(node.dependency());
        record.setSortOrder(order.incrementAndGet());
        record.setCreatedAt(now);
        record.setUpdatedAt(now);
        workItemMapper.insert(record);
        for (var child : node.children()) persist(taskId, persistedId, child, order);
    }

    private WorkItemNode toNode(WorkItemRecord record) {
        return new WorkItemNode(record.getId(), WorkItemType.fromCode(record.getType()), record.getName(),
                record.getDescription(), record.getStatus(), record.getRoleName(), record.getPriority(),
                record.getStartDate(), record.getEndDate(), record.getDependencyText(), new ArrayList<>());
    }
}
