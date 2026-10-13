package com.demandworkbench.decomposition.application;

import java.util.Arrays;
import lombok.Getter;

@Getter
public enum DecompositionErrorCode {
    INVALID_CONTENT("S048101001", "请求内容无效"),
    TASK_NOT_FOUND("S048101002", "拆解任务不存在"),
    TASK_QUEUE_FULL("S048101003", "任务队列繁忙，请稍后重试"),
    AI_REQUEST_FAILED("S048101004", "AI 服务请求失败"),
    INVALID_MODEL_OUTPUT("S048101005", "模型返回结果无效"),
    DECOMPOSITION_FAILED("S048101006", "需求拆解失败"),
    SERVER_RESTARTED("S048101007", "服务重启导致任务中断"),
    INTERNAL_ERROR("S048101008", "系统内部错误"),
    WORK_ITEM_NOT_FOUND("S048101009", "工作项不存在"),
    INVALID_WORK_ITEM_PARENT("S048101010", "工作项父子层级不正确");

    private final String code;
    private final String msg;

    DecompositionErrorCode(String code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public static DecompositionErrorCode fromCode(String code) {
        return Arrays.stream(values())
                .filter(errorCode -> errorCode.code.equals(code))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown decomposition error code: " + code));
    }
}
