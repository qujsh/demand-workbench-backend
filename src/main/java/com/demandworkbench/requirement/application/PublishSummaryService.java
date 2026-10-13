package com.demandworkbench.requirement.application;

import com.demandworkbench.requirement.domain.type.PublishSummaryStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Flux;

@Service
public class PublishSummaryService {
    public static final String PROMPT_VERSION = "publish-summary-v1";

    private final RequirementRepository repository;
    private final PublishSummaryGenerator generator;

    public PublishSummaryService(RequirementRepository repository, PublishSummaryGenerator generator) {
        this.repository = repository;
        this.generator = generator;
    }

    /**
     * 查询需求当前保存的发布简介及生成状态。
     */
    public PublishSummaryData find(String requirementNo) {
        var summary = repository.findPublishSummary(requirementNo);
        if (summary == null) {
            throw new RequirementException(RequirementErrorCode.REQUIREMENT_NOT_FOUND);
        }
        return summary;
    }

    /**
     * 在尚无正式结果时流式生成发布简介；已有正式结果时直接返回已保存内容。
     */
    public Flux<String> generate(String requirementNo) {
        return Flux.defer(() -> {
            var existing = find(requirementNo);
            if (StringUtils.hasText(existing.content())) {
                return Flux.just(existing.content());
            }

            var requirement = repository.findByRequirementNo(requirementNo);
            var source = StringUtils.hasText(requirement.aiDescription())
                    ? requirement.aiDescription()
                    : requirement.userDescription();
            if (!StringUtils.hasText(source)) {
                return Flux.error(new RequirementException(RequirementErrorCode.PUBLISH_SUMMARY_SOURCE_EMPTY));
            }

            repository.updatePublishSummaryStatus(
                    requirementNo, PublishSummaryStatus.GENERATING.getCode(), PROMPT_VERSION);
            var content = new StringBuilder();
            return generator.generate(source)
                    .filter(StringUtils::hasLength)
                    .doOnNext(content::append)
                    .doOnComplete(() -> saveCompleted(requirementNo, content.toString()))
                    .doOnError(error -> repository.updatePublishSummaryStatus(
                            requirementNo, PublishSummaryStatus.FAILED.getCode(), PROMPT_VERSION));
        });
    }

    private void saveCompleted(String requirementNo, String content) {
        if (!StringUtils.hasText(content)) {
            repository.updatePublishSummaryStatus(
                    requirementNo, PublishSummaryStatus.FAILED.getCode(), PROMPT_VERSION);
            throw new RequirementException(RequirementErrorCode.PUBLISH_SUMMARY_GENERATION_FAILED);
        }
        repository.savePublishSummary(requirementNo, content.trim(), PROMPT_VERSION);
    }
}
