package com.demandworkbench.decomposition.domain.type;

import java.util.Arrays;

public enum Priority {
    HIGH("高"),
    MEDIUM("中"),
    LOW("低"),
    UNSET("");

    private final String value;

    Priority(String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }

    public static String normalize(String value) {
        return Arrays.stream(values())
                .filter(priority -> priority.value.equals(value == null ? "" : value.trim()))
                .findFirst()
                .orElse(UNSET)
                .value;
    }
}
