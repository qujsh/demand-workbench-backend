package com.demandworkbench.decomposition.domain.type;

import java.util.Arrays;
import lombok.Getter;

@Getter
public enum Priority {
    UNSET("unset", "未设置", 0),
    LOW("low", "低", 1),
    MEDIUM("medium", "中", 2),
    HIGH("high", "高", 3);

    private final String code;
    private final String msg;
    private final int value;

    Priority(String code, String msg, int value) {
        this.code = code;
        this.msg = msg;
        this.value = value;
    }

    public static String normalize(String input) {
        var normalized = input == null ? "" : input.trim();
        if (normalized.isEmpty()) return UNSET.code;
        return Arrays.stream(values())
                .filter(priority -> priority.code.equalsIgnoreCase(normalized) || priority.msg.equals(normalized))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown priority: " + input))
                .code;
    }
}
