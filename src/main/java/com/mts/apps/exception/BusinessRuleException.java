package com.mts.apps.exception;

/** The input is fine but the action is not allowed right now, e.g. "Booking 7 is CONFIRMED and cannot be paid". */
public class BusinessRuleException extends MtsException {

    public BusinessRuleException(String message) {
        super(message);
    }
}