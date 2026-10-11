package com.demandworkbench.decomposition.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.demandworkbench.decomposition.domain.model.WorkItemNode;
import com.demandworkbench.decomposition.domain.model.WorkItemTree;
import com.demandworkbench.decomposition.domain.type.TaskStage;
import com.demandworkbench.decomposition.domain.type.TaskStatus;
import com.demandworkbench.decomposition.domain.type.WorkItemType;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;

class DecompositionQueryServiceTest {
    private final DecompositionRepository repository = mock(DecompositionRepository.class);
    private final DecompositionQueryService service = new DecompositionQueryService(repository);

    @Test
    void shouldReturnTreeAndSummaryForCompletedTask() {
        var child = new WorkItemNode("SUBTASK-001", WorkItemType.SUBTASK, "编写接口", "", "未开始",
                "后端开发", "中", null, null, "", List.of());
        var work = new WorkItemNode("WORK-001", WorkItemType.WORK, "登录接口", "", "未开始",
                "后端开发", "高", null, null, "", List.of(child));
        var feature = new WorkItemNode("FEATURE-001", WorkItemType.FEATURE, "登录", "", "未开始",
                "未分配", "", null, null, "", List.of(work));
        var module = new WorkItemNode("MODULE-001", WorkItemType.MODULE, "用户", "", "未开始",
                "未分配", "", null, null, "", List.of(feature));
        var createdAt = LocalDateTime.of(2026, 10, 11, 10, 0);
        when(repository.find("DEC-1")).thenReturn(new DecompositionRepository.TaskSnapshot(
                "DEC-1", TaskStatus.SUCCESS, TaskStage.COMPLETED, null, null, createdAt, createdAt.plusMinutes(1)));
        when(repository.findTree("DEC-1")).thenReturn(new WorkItemTree(List.of(module)));

        var response = service.query("DEC-1");

        assertThat(response.status()).isEqualTo("SUCCESS");
        assertThat(response.summary()).isEqualTo(new com.demandworkbench.decomposition.controller.response.TaskResponse.Summary(1, 1, 1, 1));
        assertThat(response.modules()).hasSize(1);
        assertThat(response.modules().getFirst().children().getFirst().children().getFirst().name()).isEqualTo("登录接口");
    }
}
