package com.mts.apps.exception;

/**
 * Thrown by the service layer. Carries a message the user can read,
 * while the original SQLException is kept as the cause for the log.
 */
public class MtsException extends Exception {

    public MtsException(String message) {
        super(message);
    }

    public MtsException(String message, Throwable cause) {
        super(message, cause);
    }
}