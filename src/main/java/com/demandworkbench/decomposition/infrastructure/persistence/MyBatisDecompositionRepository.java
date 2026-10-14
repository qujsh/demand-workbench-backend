package com.demandworkbench.decomposition.infrastructure.persistence;

import com.demandworkbench.decomposition.application.DecompositionRepository;
import com.demandworkbench.decomposition.application.DecompositionErrorCode;
import com.demandworkbench.decomposition.application.DecompositionException;
import com.demandworkbench.decomposition.application.WorkItemData;
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
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.UUID;
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
    public void create(String id, long ownerUserId, String content, String model, String promptVersion, LocalDateTime createdAt) {
        var record = new DecompositionRecord();
        record.setDecompositionNo(id);
        record.setOwnerUserId(ownerUserId);
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
        var decomposition = decompositionMapper.findByDecompositionNo(id);
        if (decomposition == null) return;
        workItemMapper.deleteByDecompositionId(decomposition.getId());
        var order = new AtomicInteger();
        for (var module : result.tree().modules()) persist(id, decomposition.getId(), null, module, order);
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
        var record = decompositionMapper.findByDecompositionNo(id);
        if (record == null) return null;
        var errorCode = record.getErrorCode() == null
                ? null
                : DecompositionErrorCode.fromCode(record.getErrorCode());
        return new TaskSnapshot(record.getDecompositionNo(), record.getOwnerUserId(), TaskStatus.fromCode(record.getStatus()),
                TaskStage.fromCode(record.getStage()), errorCode,
                record.getErrorMessage(),
                record.getCreatedAt(), record.getCompletedAt());
    }

    @Override
    public WorkItemTree findTree(String id) {
        var decomposition = decompositionMapper.findByDecompositionNo(id);
        if (decomposition == null) return new WorkItemTree(new ArrayList<>());
        var records = workItemMapper.findByDecompositionId(decomposition.getId());
        Map<Long, WorkItemNode> nodes = new LinkedHashMap<>();
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

    @Override
    @Transactional
    public int deleteWorkItems(long ownerUserId, List<String> workItemNos) {
        if (workItemNos.stream().anyMatch(no -> !decompositionMapper.isWorkItemOwnedBy(no, ownerUserId))) return 0;
        var ids = workItemMapper.findIdsIncludingDescendants(workItemNos);
        return ids.isEmpty() ? 0 : workItemMapper.softDeleteByIds(ids);
    }

    @Override
    @Transactional
    public WorkItemData saveWorkItem(long ownerUserId, WorkItemData data) {
        var decomposition = decompositionMapper.findByDecompositionNo(data.decompositionNo());
        if (decomposition == null || decomposition.getOwnerUserId() != ownerUserId) throw new DecompositionException(DecompositionErrorCode.TASK_NOT_FOUND);

        if (data.workItemNo() != null && !data.workItemNo().isBlank()) {
            var existing = workItemMapper.findByWorkItemNo(data.workItemNo());
            if (existing == null || !existing.getDecompositionId().equals(decomposition.getId())) {
                throw new DecompositionException(DecompositionErrorCode.WORK_ITEM_NOT_FOUND);
            }
            apply(existing, data);
            workItemMapper.update(existing);
            return toData(existing, data.decompositionNo(), data.parentWorkItemNo());
        }

        WorkItemRecord parent = null;
        if (data.parentWorkItemNo() != null && !data.parentWorkItemNo().isBlank()) {
            parent = workItemMapper.findByWorkItemNo(data.parentWorkItemNo());
            if (parent == null || !parent.getDecompositionId().equals(decomposition.getId())
                    || WorkItemType.fromCode(data.type()).getValue()
                    != WorkItemType.fromCode(parent.getType()).getValue() + 1) {
                throw new DecompositionException(DecompositionErrorCode.INVALID_WORK_ITEM_PARENT);
            }
        }
        var record = new WorkItemRecord();
        record.setWorkItemNo(data.decompositionNo() + "-MANUAL-" + UUID.randomUUID().toString().replace("-", ""));
        record.setDecompositionId(decomposition.getId());
        record.setParentId(parent == null ? null : parent.getId());
        record.setType(data.type());
        record.setSortOrder(workItemMapper.findMaxSortOrder(decomposition.getId()) + 1);
        var now = LocalDateTime.now();
        record.setCreatedAt(now);
        record.setUpdatedAt(now);
        apply(record, data);
        workItemMapper.insert(record);
        return toData(record, data.decompositionNo(), data.parentWorkItemNo());
    }

    private void apply(WorkItemRecord record, WorkItemData data) {
        record.setName(data.name());
        record.setDescription(data.description());
        record.setStatus(data.status());
        record.setRoleName(data.role());
        record.setPriority(data.priority());
        record.setStartDate(data.startDate());
        record.setEndDate(data.endDate());
        record.setDependencyText(data.dependency());
    }

    private WorkItemData toData(WorkItemRecord record, String decompositionNo, String parentWorkItemNo) {
        return new WorkItemData(record.getWorkItemNo(), decompositionNo, parentWorkItemNo, record.getType(),
                record.getName(), record.getDescription(), record.getStatus(), record.getRoleName(),
                record.getPriority(), record.getStartDate(), record.getEndDate(), record.getDependencyText());
    }

    private void persist(String taskId, long decompositionId, Long parentId,
                         WorkItemNode node, AtomicInteger order) {
        var persistedId = taskId + "-" + node.id();
        var now = LocalDateTime.now();
        var record = new WorkItemRecord();
        record.setWorkItemNo(persistedId);
        record.setDecompositionId(decompositionId);
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
        for (var child : node.children()) persist(taskId, decompositionId, record.getId(), child, order);
    }

    private WorkItemNode toNode(WorkItemRecord record) {
        return new WorkItemNode(record.getWorkItemNo(), WorkItemType.fromCode(record.getType()), record.getName(),
                record.getDescription(), record.getStatus(), record.getRoleName(), record.getPriority(),
                record.getStartDate(), record.getEndDate(), record.getDependencyText(), new ArrayList<>());
    }
}
