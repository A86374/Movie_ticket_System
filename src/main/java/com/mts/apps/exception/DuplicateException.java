package com.mts.apps.exception;

/** The thing being added already exists, e.g. "A movie called Inception already exists". */
public class DuplicateException extends MtsException {

    public DuplicateException(String message) {
        super(message);
    }
}