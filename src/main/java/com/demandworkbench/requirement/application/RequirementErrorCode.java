package com.demandworkbench.requirement.application;

import lombok.Getter;

@Getter
public enum RequirementErrorCode {
    INVALID_REQUIREMENT("S048102000", "需求内容无效"),
    REQUIREMENT_NOT_FOUND("S048102001", "需求不存在"),
    DECOMPOSITION_NOT_FOUND("S048102002", "拆解任务不存在"),
    DECOMPOSITION_NOT_COMPLETED("S048102003", "拆解任务尚未完成"),
    REQUIREMENT_SAVE_FAILED("S048102004", "需求保存失败"),
    PUBLISH_SUMMARY_SOURCE_EMPTY("S048102005", "需求内容为空，无法生成任务简介"),
    PUBLISH_SUMMARY_GENERATION_FAILED("S048102006", "任务简介生成失败"),
    INVALID_PUBLISH_TASK("S048102007", "发布任务内容无效"),
    PUBLISH_TASK_SAVE_FAILED("S048102008", "发布任务保存失败");

    private final String code;
    private final String msg;

    RequirementErrorCode(String code, String msg) {
        this.code = code;
        this.msg = msg;
    }
}
