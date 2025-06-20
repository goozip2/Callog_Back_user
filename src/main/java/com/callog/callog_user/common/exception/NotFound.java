package com.callog.callog_user.common.exception;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class NotFound extends ClientError {
    public NotFound(String errorMessage) {
        this.errorCode = "NotFound";
        this.errorMessage = errorMessage;
    }
}
