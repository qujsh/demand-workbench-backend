package com.demandworkbench.decomposition.domain.type;

import java.util.Arrays;
import lombok.Getter;

@Getter
public enum WorkItemType {
    MODULE("module", "模块", 0),
    FEATURE("feature", "功能", 1),
    WORK("work", "工作项", 2),
    SUBTASK("subtask", "子任务", 3);

    private final String code;
    private final String msg;
    private final int value;

    WorkItemType(String code, String msg, int value) {
        this.code = code;
        this.msg = msg;
        this.value = value;
    }

    public static WorkItemType atLevel(int level) {
        return Arrays.stream(values())
                .filter(type -> type.value == level)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown work item level: " + level));
    }

    public static WorkItemType fromCode(String code) {
        return Arrays.stream(values())
                .filter(type -> type.code.equals(code))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown work item type code: " + code));
    }
}
