package com.damu.UserService.exception;

import lombok.Getter;

@Getter
public class UserServiceException extends RuntimeException {
    private final String errorCode;
    private final int status;

    public UserServiceException(String message, String errorCode, int status) {
        super(message);
        this.errorCode = errorCode;
        this.status = status;
    }
}
