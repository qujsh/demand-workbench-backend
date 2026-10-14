package com.demandworkbench.requirement.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;

class PublishSummaryServiceTest {
    private final RequirementRepository repository = mock(RequirementRepository.class);
    private final PublishSummaryGenerator generator = mock(PublishSummaryGenerator.class);
    private final PublishSummaryService service = new PublishSummaryService(repository, generator);

    @Test
    void shouldStreamAndPersistGeneratedSummary() {
        when(repository.findPublishSummary(1L, "REQ-1")).thenReturn(
                new PublishSummaryData("REQ-1", "pending", null, null, null));
        when(repository.findByRequirementNo(1L, "REQ-1")).thenReturn(requirement(
                "## 必需（P0）\n已确认范围\n## 可选（P1）\n待确认范围\n# 3. 技术风险点\n缓存方案",
                "原始需求"));
        when(generator.generate("原始需求", "## 必需（P0）\n已确认范围"))
                .thenReturn(Flux.just("精简", "任务简介"));

        var chunks = service.generate(1L, "REQ-1").collectList().block();

        assertThat(chunks).containsExactly("精简", "任务简介");
        verify(repository).updatePublishSummaryStatus(1L, "REQ-1", "generating", "publish-summary-v2");
        verify(repository).savePublishSummary(1L, "REQ-1", "精简任务简介", "publish-summary-v2");
    }

    @Test
    void shouldReturnSavedSummaryWithoutCallingGenerator() {
        when(repository.findPublishSummary(1L, "REQ-1")).thenReturn(
                new PublishSummaryData("REQ-1", "success", "已有简介", "publish-summary-v1",
                        LocalDateTime.now()));

        var chunks = service.generate(1L, "REQ-1").collectList().block();

        assertThat(chunks).containsExactly("已有简介");
        verify(generator, never()).generate(
                org.mockito.ArgumentMatchers.anyString(), org.mockito.ArgumentMatchers.anyString());
    }

    @Test
    void shouldUseUserDescriptionDirectlyWhenAiDescriptionIsEmpty() {
        when(repository.findPublishSummary(1L, "REQ-1")).thenReturn(
                new PublishSummaryData("REQ-1", "pending", null, null, null));
        when(repository.findByRequirementNo(1L, "REQ-1")).thenReturn(requirement("", "  原始需求  "));

        var chunks = service.generate(1L, "REQ-1").collectList().block();

        assertThat(chunks).containsExactly("原始需求");
        verify(repository).savePublishSummary(1L, "REQ-1", "原始需求", "user-description-v1");
        verify(generator, never()).generate(
                org.mockito.ArgumentMatchers.anyString(), org.mockito.ArgumentMatchers.anyString());
    }

    @Test
    void shouldMarkFailedWhenGeneratorFails() {
        when(repository.findPublishSummary(1L, "REQ-1")).thenReturn(
                new PublishSummaryData("REQ-1", "pending", null, null, null));
        when(repository.findByRequirementNo(1L, "REQ-1")).thenReturn(requirement("已确认范围", "原始需求"));
        when(generator.generate("原始需求", "已确认范围"))
                .thenReturn(Flux.error(new IllegalStateException("provider failed")));

        service.generate(1L, "REQ-1").onErrorComplete().blockLast();

        verify(repository).updatePublishSummaryStatus(1L, "REQ-1", "failed", "publish-summary-v2");
    }

    private RequirementData requirement(String aiDescription, String userDescription) {
        return new RequirementData(1L, "REQ-1", "REQ", "需求", userDescription, aiDescription,
                "draft", null, LocalDateTime.now(), LocalDateTime.now(), 0, 0, List.of());
    }
}
