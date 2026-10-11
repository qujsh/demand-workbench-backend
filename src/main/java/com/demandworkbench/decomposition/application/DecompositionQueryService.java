package com.demandworkbench.decomposition.application;

import com.demandworkbench.decomposition.controller.response.TaskResponse;
import com.demandworkbench.decomposition.controller.response.WorkItemResponse;
import com.demandworkbench.decomposition.domain.model.WorkItemNode;
import com.demandworkbench.decomposition.domain.type.TaskStatus;
import com.demandworkbench.decomposition.domain.type.WorkItemType;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class DecompositionQueryService {
    private final DecompositionRepository repository;

    public DecompositionQueryService(DecompositionRepository repository) {
        this.repository = repository;
    }

    public TaskResponse query(String taskId) {
        var task = repository.find(taskId);
        if (task == null) throw new DecompositionException("TASK_NOT_FOUND", "拆解任务不存在");
        var tree = task.status() == TaskStatus.SUCCESS ? repository.findTree(taskId) : null;
        var modules = tree == null ? List.<WorkItemResponse>of() : tree.modules().stream().map(this::toResponse).toList();
        return new TaskResponse(task.id(), task.status().name(), task.stage().name(), messageFor(task),
                tree == null ? null : summarize(tree.modules()), modules, task.errorCode(),
                task.createdAt(), task.completedAt());
    }

    private WorkItemResponse toResponse(WorkItemNode node) {
        return new WorkItemResponse(node.id(), node.type().value(), node.name(), node.description(), node.status(),
                node.role(), node.priority(), node.startDate(), node.endDate(), node.dependency(), false,
                node.children().stream().map(this::toResponse).toList());
    }

    private TaskResponse.Summary summarize(List<WorkItemNode> roots) {
        var counts = new int[WorkItemType.values().length];
        count(roots, counts);
        return new TaskResponse.Summary(counts[0], counts[1], counts[2], counts[3]);
    }

    private void count(List<WorkItemNode> nodes, int[] counts) {
        for (var node : nodes) {
            counts[node.type().ordinal()]++;
            count(node.children(), counts);
        }
    }

    private String messageFor(DecompositionRepository.TaskSnapshot task) {
        if (task.status() == TaskStatus.FAILED) return task.errorMessage();
        return switch (task.stage()) {
            case QUEUED -> "拆解任务已提交，正在等待处理";
            case AI_GENERATING -> "AI正在识别模块、功能和工作项";
            case VALIDATING -> "正在校验工作项结构";
            case SAVING -> "正在保存拆解结果";
            case COMPLETED -> "工作项拆解完成";
            case FAILED -> "工作项拆解失败";
        };
    }
}
