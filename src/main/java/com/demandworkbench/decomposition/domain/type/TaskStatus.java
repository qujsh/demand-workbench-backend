package com.demandworkbench.decomposition.domain.type;

import java.util.Arrays;
import lombok.Getter;

@Getter
public enum TaskStatus {
    QUEUED("queued", "排队中", 0),
    PROCESSING("processing", "处理中", 1),
    SUCCESS("success", "成功", 2),
    FAILED("failed", "失败", 3);

    private final String code;
    private final String msg;
    private final int value;

    TaskStatus(String code, String msg, int value) {
        this.code = code;
        this.msg = msg;
        this.value = value;
    }

    public static TaskStatus fromCode(String code) {
        return Arrays.stream(values())
                .filter(status -> status.code.equals(code))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown task status code: " + code));
    }
}
