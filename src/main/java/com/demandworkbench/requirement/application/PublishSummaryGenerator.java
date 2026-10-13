package com.demandworkbench.requirement.application;

import reactor.core.publisher.Flux;

public interface PublishSummaryGenerator {
    /**
     * 根据已确认需求生成可直接发布的任务简介文本流。
     *
     * @param userDescription 用户提交的原始需求，作为交付范围判断基准
     * @param confirmedAnalysis AI 分析中已确认的范围，不包含可选项和技术风险
     * @return 按模型响应顺序输出的文本片段
     */
    Flux<String> generate(String userDescription, String confirmedAnalysis);
}
