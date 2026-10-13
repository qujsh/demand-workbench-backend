package com.demandworkbench.decomposition.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;

class WorkItemCommandServiceTest {
    private final DecompositionRepository repository = mock(DecompositionRepository.class);
    private final WorkItemCommandService service = new WorkItemCommandService(repository);

    @Test
    void shouldDeduplicateAndDeleteWorkItems() {
        var workItemNos = List.of("WORK-1", "SUB-1");
        when(repository.deleteWorkItems(workItemNos)).thenReturn(2);

        var deletedCount = service.delete(List.of("WORK-1", "SUB-1", "WORK-1"));

        assertThat(deletedCount).isEqualTo(2);
        verify(repository).deleteWorkItems(workItemNos);
    }

    @Test
    void shouldNormalizeAndSaveWorkItem() {
        var input = new WorkItemData(null, "DEC-1", "WORK-1", "subtask", "  新子项  ", "描述",
                "未开始", "前端开发", "高", LocalDate.parse("2026-10-13"), null, null);
        var normalized = new WorkItemData(null, "DEC-1", "WORK-1", "subtask", "新子项", "描述",
                "未开始", "前端开发", "high", LocalDate.parse("2026-10-13"), null, null);
        var saved = new WorkItemData("DEC-1-MANUAL-1", "DEC-1", "WORK-1", "subtask", "新子项", "描述",
                "未开始", "前端开发", "high", LocalDate.parse("2026-10-13"), null, null);
        when(repository.saveWorkItem(normalized)).thenReturn(saved);

        assertThat(service.save(input)).isEqualTo(saved);
        verify(repository).saveWorkItem(normalized);
    }
}
