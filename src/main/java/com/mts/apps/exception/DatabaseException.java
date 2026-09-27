package com.mts.apps.exception;

/** MySQL failed. Always carries the original SQLException as the cause, for the logs. */
public class DatabaseException extends MtsException {

    public DatabaseException(String message, Throwable cause) {
        super(message, cause);
    }
}