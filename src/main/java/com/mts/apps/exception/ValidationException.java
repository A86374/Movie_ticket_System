package com.mts.apps.exception;

/** Input is in the wrong format or range, e.g. "Duration must be between 30 and 300 minutes". */
public class ValidationException extends MtsException {

    public ValidationException(String message) {
        super(message);
    }
}