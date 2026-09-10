package com.exception;

public class CacheSynchronizationException extends RuntimeException {
    public CacheSynchronizationException(String message, Exception cause) {
        super(message, cause);
    }
}
