package com.demandworkbench.requirement.domain.type;

import lombok.Getter;

@Getter
public enum RequirementStage {
    DRAFT("draft", "草稿", 0),
    ANALYZING("analyzing", "分析中", 1),
    READY_TO_PUBLISH("ready_to_publish", "待发布", 2),
    PUBLISHED("published", "已发布", 3);

    private final String code;
    private final String msg;
    private final int value;

    RequirementStage(String code, String msg, int value) {
        this.code = code;
        this.msg = msg;
        this.value = value;
    }

    public static RequirementStage fromCode(String code) {
        for (var stage : values()) {
            if (stage.code.equals(code)) {
                return stage;
            }
        }
        throw new IllegalArgumentException("未知需求阶段: " + code);
    }
}
