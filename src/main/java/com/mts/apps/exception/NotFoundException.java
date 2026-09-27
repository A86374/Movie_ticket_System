package com.mts.apps.exception;

/** The thing asked for does not exist, e.g. "No movie found with the title RajaRani". */
public class NotFoundException extends MtsException {

    public NotFoundException(String message) {
        super(message);
    }
}