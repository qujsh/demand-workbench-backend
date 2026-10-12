package com.demandworkbench.requirement.application;

import lombok.Getter;

@Getter
public enum RequirementErrorCode {
    INVALID_REQUIREMENT("S048102000", "需求内容无效"),
    REQUIREMENT_NOT_FOUND("S048102001", "需求不存在"),
    DECOMPOSITION_NOT_FOUND("S048102002", "拆解任务不存在"),
    DECOMPOSITION_NOT_COMPLETED("S048102003", "拆解任务尚未完成"),
    REQUIREMENT_SAVE_FAILED("S048102004", "需求保存失败");

    private final String code;
    private final String msg;

    RequirementErrorCode(String code, String msg) {
        this.code = code;
        this.msg = msg;
    }
}
