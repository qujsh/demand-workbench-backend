package com.demandworkbench.requirement.application;

import reactor.core.publisher.Flux;

public interface PublishSummaryGenerator {
    /**
     * 根据已确认需求生成可直接发布的任务简介文本流。
     *
     * @param requirementText 已确认的需求正文
     * @return 按模型响应顺序输出的文本片段
     */
    Flux<String> generate(String requirementText);
}
