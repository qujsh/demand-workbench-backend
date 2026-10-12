package com.demandworkbench.requirement.application;

import lombok.Getter;

@Getter
public class RequirementException extends RuntimeException {
    private final RequirementErrorCode errorCode;

    public RequirementException(RequirementErrorCode errorCode) {
        super(errorCode.getMsg());
        this.errorCode = errorCode;
    }
}
