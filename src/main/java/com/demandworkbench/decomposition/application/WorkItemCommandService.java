package com.demandworkbench.decomposition.application;

import com.demandworkbench.decomposition.domain.type.Priority;
import com.demandworkbench.decomposition.domain.type.WorkItemType;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class WorkItemCommandService {
    private final DecompositionRepository repository;

    public WorkItemCommandService(DecompositionRepository repository) {
        this.repository = repository;
    }

    /**
     * 按对外工作项编号批量删除工作项。
     */
    public int delete(List<String> workItemNos) {
        return repository.deleteWorkItems(workItemNos.stream().distinct().toList());
    }

    public WorkItemData save(WorkItemData data) {
        WorkItemType.fromCode(data.type());
        var normalized = new WorkItemData(data.workItemNo(), data.decompositionNo(), data.parentWorkItemNo(),
                data.type(), data.name().trim(), data.description(), data.status(), data.role(),
                Priority.normalize(data.priority()), data.startDate(), data.endDate(), data.dependency());
        return repository.saveWorkItem(normalized);
    }
}
