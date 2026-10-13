package com.demandworkbench.requirement.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.demandworkbench.decomposition.application.DecompositionRepository;
import com.demandworkbench.decomposition.domain.type.TaskStage;
import com.demandworkbench.decomposition.domain.type.TaskStatus;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;

class RequirementServiceTest {
    private final RequirementRepository repository = mock(RequirementRepository.class);
    private final DecompositionRepository decompositionRepository = mock(DecompositionRepository.class);
    private final RequirementService service = new RequirementService(repository, decompositionRepository);

    @Test
    void shouldCreateRequirementWithGeneratedTitleCode() {
        var command = command("开发一套预约服务系统", null);
        when(repository.create(any())).thenAnswer(invocation -> invocation.getArgument(0));

        var result = service.save(null, command);

        assertThat(result.titleCode()).isEqualTo("KFYTYY");
        verify(repository).create(any());
    }

    @Test
    void shouldKeepExistingNumberAndTitleCodeWhenTitleChanges() {
        var existing = new RequirementData(25L, "REQ-KFYTYY-000025", "KFYTYY", "旧标题", "", "",
                "analyzing", null, LocalDateTime.now(), LocalDateTime.now(), 0, 0, List.of());
        when(repository.findByRequirementNo(existing.requirementNo())).thenReturn(existing);
        when(repository.update(any())).thenAnswer(invocation -> invocation.getArgument(0));

        var result = service.save(existing.requirementNo(), command("完全不同的标题", null));

        assertThat(result.requirementNo()).isEqualTo(existing.requirementNo());
        assertThat(result.titleCode()).isEqualTo("KFYTYY");
    }

    @Test
    void shouldKeepPublishedStageWhenRequirementIsSavedAgain() {
        var existing = new RequirementData(25L, "REQ-KFYTYY-000025", "KFYTYY", "旧标题", "", "",
                "published", null, LocalDateTime.now(), LocalDateTime.now(), 0, 0, List.of());
        when(repository.findByRequirementNo(existing.requirementNo())).thenReturn(existing);
        when(repository.update(any())).thenAnswer(invocation -> invocation.getArgument(0));

        var result = service.save(existing.requirementNo(), command("修改后的标题", null));

        assertThat(result.stage()).isEqualTo("published");
    }

    @Test
    void shouldAllowManualStageChangeBetweenDraftAndReadyToPublish() {
        var existing = new RequirementData(25L, "REQ-KFYTYY-000025", "KFYTYY", "标题", "", "",
                "ready_to_publish", null, LocalDateTime.now(), LocalDateTime.now(), 0, 0, List.of());
        when(repository.findByRequirementNo(existing.requirementNo()))
                .thenReturn(existing)
                .thenReturn(new RequirementData(existing.id(), existing.requirementNo(), existing.titleCode(),
                        existing.title(), existing.userDescription(), existing.aiDescription(), "draft", null,
                        existing.createdAt(), existing.updatedAt(), 0, 0, List.of()));

        var result = service.updateStage(existing.requirementNo(), "draft");

        verify(repository).updateStage(existing.requirementNo(), "draft");
        assertThat(result.stage()).isEqualTo("draft");
    }

    @Test
    void shouldRejectManualChangeForPublishedRequirement() {
        var existing = new RequirementData(25L, "REQ-KFYTYY-000025", "KFYTYY", "标题", "", "",
                "published", null, LocalDateTime.now(), LocalDateTime.now(), 0, 0, List.of());
        when(repository.findByRequirementNo(existing.requirementNo())).thenReturn(existing);

        assertThatThrownBy(() -> service.updateStage(existing.requirementNo(), "draft"))
                .isInstanceOf(RequirementException.class)
                .extracting("errorCode")
                .isEqualTo(RequirementErrorCode.INVALID_REQUIREMENT_STAGE);
    }

    @Test
    void shouldRejectUncompletedDecomposition() {
        when(decompositionRepository.find("DEC-1")).thenReturn(new DecompositionRepository.TaskSnapshot(
                "DEC-1", TaskStatus.PROCESSING, TaskStage.AI_GENERATING, null, null,
                LocalDateTime.now(), null));

        assertThatThrownBy(() -> service.save(null, command("需求", "DEC-1")))
                .isInstanceOf(RequirementException.class)
                .extracting("errorCode")
                .isEqualTo(RequirementErrorCode.DECOMPOSITION_NOT_COMPLETED);
    }

    @Test
    void shouldSoftDeleteRequirementByBusinessNumber() {
        when(repository.softDelete("REQ-KFYTYY-000025")).thenReturn(true);

        service.delete("REQ-KFYTYY-000025");

        verify(repository).softDelete("REQ-KFYTYY-000025");
    }

    @Test
    void shouldRejectDeletingMissingRequirement() {
        when(repository.softDelete("REQ-MISSING-000001")).thenReturn(false);

        assertThatThrownBy(() -> service.delete("REQ-MISSING-000001"))
                .isInstanceOf(RequirementException.class)
                .extracting("errorCode")
                .isEqualTo(RequirementErrorCode.REQUIREMENT_NOT_FOUND);
    }

    private RequirementService.SaveCommand command(String title, String decompositionId) {
        return new RequirementService.SaveCommand(title, "用户描述", "AI 描述", "analyzing",
                decompositionId, List.of());
    }
}
