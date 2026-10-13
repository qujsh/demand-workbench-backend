package com.demandworkbench.requirement.application;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.jupiter.api.Test;

class PublishTaskServiceTest {
    private final PublishTaskRepository taskRepository = mock(PublishTaskRepository.class);
    private final RequirementRepository requirementRepository = mock(RequirementRepository.class);
    private final PublishTaskService service = new PublishTaskService(taskRepository, requirementRepository);

    @Test
    void shouldAllowIncompleteDraft() {
        when(requirementRepository.findByRequirementNo("REQ-1")).thenReturn(requirement());
        when(taskRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        service.save("REQ-1", task("draft", "", "", List.of()));

        verify(taskRepository).save(any());
    }

    @Test
    void shouldRejectPublishedTaskWithoutWorkItems() {
        when(requirementRepository.findByRequirementNo("REQ-1")).thenReturn(requirement());

        assertThatThrownBy(() -> service.save("REQ-1", task("published", "任务", "简介", List.of())))
                .isInstanceOf(RequirementException.class)
                .extracting("errorCode")
                .isEqualTo(RequirementErrorCode.INVALID_PUBLISH_TASK);
    }

    @Test
    void shouldUpdateRequirementStageAfterPublishing() {
        when(requirementRepository.findByRequirementNo("REQ-1")).thenReturn(requirement());
        when(taskRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        service.save("REQ-1", task("published", "任务", "简介", List.of("WORK-1")));

        verify(requirementRepository).updateStage("REQ-1", "published");
    }

    private PublishTaskData task(String status, String title, String summary, List<String> workItems) {
        return new PublishTaskData(null, null, "REQ-1", status, title, "分类", summary, "", "不限",
                1, "固定价格", "", null, null, "待定", "报名审核", "所有人可见", workItems, null);
    }

    private RequirementData requirement() {
        return new RequirementData(1L, "REQ-1", "REQ", "需求", "", "", "ready_to_publish", null,
                null, null, 0, 0, List.of());
    }
}
