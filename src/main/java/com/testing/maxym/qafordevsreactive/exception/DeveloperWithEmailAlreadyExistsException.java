package com.testing.maxym.qafordevsreactive.exception;

public class DeveloperWithEmailAlreadyExistsException extends ApiException {
    public DeveloperWithEmailAlreadyExistsException(String message, String errorCode) {
        super(message, errorCode);
    }
}
