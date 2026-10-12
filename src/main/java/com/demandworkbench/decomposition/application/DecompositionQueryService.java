package com.demandworkbench.decomposition.application;

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

    /**
     * 查询拆解任务状态及已生成的工作项树。
     *
     * @param taskId 拆解任务编号
     * @return 任务状态和拆解结果
     * @throws DecompositionException 任务不存在时抛出
     */
    public DecompositionResult query(String taskId) {
        var task = repository.find(taskId);
        if (task == null) throw new DecompositionException(DecompositionErrorCode.TASK_NOT_FOUND);
        var tree = task.status() == TaskStatus.SUCCESS ? repository.findTree(taskId) : null;
        var modules = tree == null
                ? List.<DecompositionResult.WorkItem>of()
                : tree.modules().stream().map(this::toResult).toList();
        return new DecompositionResult(task.id(), task.status().getCode(), task.stage().getCode(), messageFor(task),
                tree == null ? null : summarize(tree.modules()), modules,
                task.errorCode() == null ? null : task.errorCode().getCode(),
                task.createdAt(), task.completedAt());
    }

    private DecompositionResult.WorkItem toResult(WorkItemNode node) {
        return new DecompositionResult.WorkItem(
                node.id(), node.type().getCode(), node.name(), node.description(), node.status(),
                node.role(), node.priority(), node.startDate(), node.endDate(), node.dependency(), false,
                node.children().stream().map(this::toResult).toList());
    }

    private DecompositionResult.Summary summarize(List<WorkItemNode> roots) {
        var counts = new int[WorkItemType.values().length];
        count(roots, counts);
        return new DecompositionResult.Summary(counts[0], counts[1], counts[2], counts[3]);
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
