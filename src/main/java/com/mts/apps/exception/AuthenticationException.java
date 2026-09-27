package com.mts.apps.exception;

/** Login failed, e.g. "Wrong email or password". */
public class AuthenticationException extends MtsException {

    public AuthenticationException(String message) {
        super(message);
    }
}