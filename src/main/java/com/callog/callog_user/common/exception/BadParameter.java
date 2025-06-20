package com.callog.callog_user.common.exception;

public class BadParameter extends ClientError {
    public BadParameter(String errorMessage) {
        this.errorCode = "BadParameter";
        this.errorMessage = errorMessage;
    }
}
