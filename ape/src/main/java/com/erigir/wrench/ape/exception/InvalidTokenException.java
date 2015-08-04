package com.erigir.wrench.ape.exception;

/**
 * Created by cweiss on 7/18/15.
 */
public class InvalidTokenException extends Exception {

    public InvalidTokenException() {
    }

    public InvalidTokenException(String message) {
        super(message);
    }

    public InvalidTokenException(String message, Throwable cause) {
        super(message, cause);
    }

    public InvalidTokenException(Throwable cause) {
        super(cause);
    }

    public InvalidTokenException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
