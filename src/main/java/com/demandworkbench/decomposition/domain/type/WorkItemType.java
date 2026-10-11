package com.demandworkbench.decomposition.domain.type;

public enum WorkItemType {
    MODULE("module"),
    FEATURE("feature"),
    WORK("work"),
    SUBTASK("subtask");

    private final String value;

    WorkItemType(String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }

    public static WorkItemType atLevel(int level) {
        return values()[level];
    }

    public static WorkItemType fromValue(String value) {
        for (var type : values()) {
            if (type.value.equals(value)) return type;
        }
        throw new IllegalArgumentException("Unknown work item type: " + value);
    }
}
