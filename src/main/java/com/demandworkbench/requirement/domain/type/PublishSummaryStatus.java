package com.demandworkbench.requirement.domain.type;

import java.util.Arrays;
import lombok.Getter;

@Getter
public enum PublishSummaryStatus {
    PENDING("pending", "待生成", 0),
    GENERATING("generating", "生成中", 1),
    SUCCESS("success", "生成成功", 2),
    FAILED("failed", "生成失败", 3);

    private final String code;
    private final String msg;
    private final int value;

    PublishSummaryStatus(String code, String msg, int value) {
        this.code = code;
        this.msg = msg;
        this.value = value;
    }

    public static PublishSummaryStatus fromCode(String code) {
        return Arrays.stream(values())
                .filter(status -> status.code.equals(code))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown publish summary status: " + code));
    }
}
