package com.demandworkbench.decomposition.domain.type;

import java.util.Arrays;
import lombok.Getter;

@Getter
public enum TaskStage {
    QUEUED("queued", "等待处理", 0),
    AI_GENERATING("ai_generating", "AI 生成中", 1),
    VALIDATING("validating", "校验中", 2),
    SAVING("saving", "保存中", 3),
    COMPLETED("completed", "已完成", 4),
    FAILED("failed", "失败", 5);

    private final String code;
    private final String msg;
    private final int value;

    TaskStage(String code, String msg, int value) {
        this.code = code;
        this.msg = msg;
        this.value = value;
    }

    public static TaskStage fromCode(String code) {
        return Arrays.stream(values())
                .filter(stage -> stage.code.equals(code))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown task stage code: " + code));
    }
}
