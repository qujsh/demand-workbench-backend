package com.demandworkbench.decomposition.application;

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
}
