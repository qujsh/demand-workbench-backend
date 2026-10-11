package com.demandworkbench.decomposition.application;

import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.demandworkbench.decomposition.domain.model.WorkItemNode;
import com.demandworkbench.decomposition.domain.model.WorkItemTree;
import com.demandworkbench.decomposition.domain.type.TaskStage;
import com.demandworkbench.decomposition.domain.type.TaskStatus;
import com.demandworkbench.decomposition.domain.type.WorkItemType;
import java.util.List;
import org.junit.jupiter.api.Test;

class DecompositionJobRunnerTest {
    private final WorkItemGenerator generator = mock(WorkItemGenerator.class);
    private final DecompositionRepository repository = mock(DecompositionRepository.class);
    private final DecompositionJobRunner runner = new DecompositionJobRunner(generator, repository);

    @Test
    void shouldAdvanceStagesAndPersistGeneratedTree() {
        var module = new WorkItemNode("MODULE-001", WorkItemType.MODULE, "用户模块", "", "未开始",
                "未分配", "", null, null, "", List.of());
        var result = new WorkItemGenerator.GenerationResult(new WorkItemTree(List.of(module)), null, null, null);
        when(generator.generate("需求内容")).thenReturn(result);

        runner.run("DEC-1", "需求内容");

        var order = inOrder(repository, generator);
        order.verify(repository).updateStage("DEC-1", TaskStatus.PROCESSING, TaskStage.AI_GENERATING);
        order.verify(generator).generate("需求内容");
        order.verify(repository).updateStage("DEC-1", TaskStatus.PROCESSING, TaskStage.VALIDATING);
        order.verify(repository).updateStage("DEC-1", TaskStatus.PROCESSING, TaskStage.SAVING);
        order.verify(repository).complete("DEC-1", result);
    }

    @Test
    void shouldMarkTaskFailedWhenGeneratorFails() {
        when(generator.generate("需求内容"))
                .thenThrow(new DecompositionException("AI_REQUEST_FAILED", "AI 服务不可用"));

        runner.run("DEC-1", "需求内容");

        verify(repository).fail("DEC-1", "AI_REQUEST_FAILED", "AI 服务不可用");
    }
}
