package com.yogotry.global.exception;

public class InvalidAuthorizationCodeException extends RuntimeException {
    public InvalidAuthorizationCodeException() {
        super();
    }

    public InvalidAuthorizationCodeException(String message) {
        super(message);
    }

    public InvalidAuthorizationCodeException(String message, Throwable cause) {
        super(message, cause);
    }

    public InvalidAuthorizationCodeException(Throwable cause) {
        super(cause);
    }
}
