package com.yogotry.global.exception;

public class InvalidIdTokenException extends RuntimeException {
    public InvalidIdTokenException(String message) {
        super(message);
    }
}
