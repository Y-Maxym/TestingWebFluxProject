package com.testing.maxym.qafordevsreactive.exception;

public class DeveloperNotFoundException extends ApiException {
    public DeveloperNotFoundException(String message, String errorCode) {
        super(message, errorCode);
    }
}
