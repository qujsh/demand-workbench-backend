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
        when(repository.findPublishSummary("REQ-1")).thenReturn(
                new PublishSummaryData("REQ-1", "pending", null, null, null));
        when(repository.findByRequirementNo("REQ-1")).thenReturn(requirement("已确认的 AI 需求", "原始需求"));
        when(generator.generate("已确认的 AI 需求")).thenReturn(Flux.just("精简", "任务简介"));

        var chunks = service.generate("REQ-1").collectList().block();

        assertThat(chunks).containsExactly("精简", "任务简介");
        verify(repository).updatePublishSummaryStatus("REQ-1", "generating", "publish-summary-v1");
        verify(repository).savePublishSummary("REQ-1", "精简任务简介", "publish-summary-v1");
    }

    @Test
    void shouldReturnSavedSummaryWithoutCallingGenerator() {
        when(repository.findPublishSummary("REQ-1")).thenReturn(
                new PublishSummaryData("REQ-1", "success", "已有简介", "publish-summary-v1",
                        LocalDateTime.now()));

        var chunks = service.generate("REQ-1").collectList().block();

        assertThat(chunks).containsExactly("已有简介");
        verify(generator, never()).generate(org.mockito.ArgumentMatchers.anyString());
    }

    @Test
    void shouldMarkFailedWhenGeneratorFails() {
        when(repository.findPublishSummary("REQ-1")).thenReturn(
                new PublishSummaryData("REQ-1", "pending", null, null, null));
        when(repository.findByRequirementNo("REQ-1")).thenReturn(requirement("", "原始需求"));
        when(generator.generate("原始需求")).thenReturn(Flux.error(new IllegalStateException("provider failed")));

        service.generate("REQ-1").onErrorComplete().blockLast();

        verify(repository).updatePublishSummaryStatus("REQ-1", "failed", "publish-summary-v1");
    }

    private RequirementData requirement(String aiDescription, String userDescription) {
        return new RequirementData(1L, "REQ-1", "REQ", "需求", userDescription, aiDescription,
                "ready_to_publish", null, LocalDateTime.now(), LocalDateTime.now(), 0, 0, List.of());
    }
}
