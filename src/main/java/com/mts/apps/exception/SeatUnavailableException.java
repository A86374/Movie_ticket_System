package com.mts.apps.exception;

/** The seat cannot be booked, e.g. "Seat A1 is not available for this show". */
public class SeatUnavailableException extends MtsException {

    public SeatUnavailableException(String message) {
        super(message);
    }

    // used when MySQL rejects a seat someone else booked a moment earlier (error 1062)
    public SeatUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}