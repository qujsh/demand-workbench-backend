package com.demandworkbench.requirement.application;

import static org.assertj.core.api.Assertions.assertThat;
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
        when(requirementRepository.findByRequirementNo(1L, "REQ-1")).thenReturn(requirement());
        when(taskRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        service.save(1L, "REQ-1", task("draft", "", ""));

        verify(taskRepository).save(any());
    }

    @Test
    void shouldRejectPublishedTaskWithoutDecomposition() {
        when(requirementRepository.findByRequirementNo(1L, "REQ-1")).thenReturn(requirement(null));

        assertThatThrownBy(() -> service.save(1L, "REQ-1", task("published", "任务", "简介")))
                .isInstanceOf(RequirementException.class)
                .extracting("errorCode")
                .isEqualTo(RequirementErrorCode.INVALID_PUBLISH_TASK);
    }

    @Test
    void shouldUpdateRequirementStageAfterPublishing() {
        when(requirementRepository.findByRequirementNo(1L, "REQ-1")).thenReturn(requirement("DEC-1"));
        when(taskRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        service.save(1L, "REQ-1", task("published", "任务", "简介"));

        verify(requirementRepository).updateStage(1L, "REQ-1", "published");
    }

    @Test
    void shouldCancelPublishedTaskAndRequirementTogether() {
        var published = task("published", "任务", "简介");
        var cancelled = task("cancelled", "任务", "简介");
        when(taskRepository.findByRequirementNo("REQ-1")).thenReturn(published);
        when(taskRepository.cancel("REQ-1")).thenReturn(cancelled);
        when(requirementRepository.findByRequirementNo(1L, "REQ-1")).thenReturn(requirement("DEC-1"));

        service.cancel(1L, "REQ-1");

        verify(taskRepository).cancel("REQ-1");
        verify(requirementRepository).updateStage(1L, "REQ-1", "cancelled");
    }

    @Test
    void shouldRejectCancellingDraftTask() {
        when(taskRepository.findByRequirementNo("REQ-1")).thenReturn(task("draft", "任务", "简介"));
        when(requirementRepository.findByRequirementNo(1L, "REQ-1")).thenReturn(requirement("DEC-1"));

        assertThatThrownBy(() -> service.cancel(1L, "REQ-1"))
                .isInstanceOf(RequirementException.class)
                .extracting("errorCode")
                .isEqualTo(RequirementErrorCode.PUBLISH_TASK_CANCEL_FAILED);
    }

    @Test
    void shouldKeepCancelledStatusWhileEditingRepublishDraft() {
        when(requirementRepository.findByRequirementNo(1L, "REQ-1")).thenReturn(requirement("DEC-1"));
        when(taskRepository.findByRequirementNo("REQ-1"))
                .thenReturn(task("cancelled", "旧任务", "旧简介"));
        when(taskRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        var saved = service.save(1L, "REQ-1", task("draft", "新任务", "新简介"));

        assertThat(saved.status()).isEqualTo("cancelled");
    }

    @Test
    void shouldRepublishCancelledTaskAfterConfirmation() {
        when(requirementRepository.findByRequirementNo(1L, "REQ-1")).thenReturn(requirement("DEC-1"));
        when(taskRepository.findByRequirementNo("REQ-1"))
                .thenReturn(task("cancelled", "旧任务", "旧简介"));
        when(taskRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        var saved = service.save(1L, "REQ-1", task("published", "新任务", "新简介"));

        assertThat(saved.status()).isEqualTo("published");
        verify(requirementRepository).updateStage(1L, "REQ-1", "published");
    }

    private PublishTaskData task(String status, String title, String summary) {
        return new PublishTaskData(null, null, "REQ-1", null, status, title, "分类", summary, "", "不限",
                1, "固定价格", "", null, null, "待定", "报名审核", "所有人可见", null);
    }

    private RequirementData requirement() {
        return requirement("DEC-1");
    }

    private RequirementData requirement(String decompositionNo) {
        return new RequirementData(1L, "REQ-1", "REQ", "需求", "", "", "draft",
                decompositionNo, null, null, 0, 0, List.of());
    }
}
