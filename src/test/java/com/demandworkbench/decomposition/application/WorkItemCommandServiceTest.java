package com.demandworkbench.decomposition.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
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
}
