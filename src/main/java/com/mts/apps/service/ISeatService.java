package com.mts.apps.service;

import com.mts.apps.exception.MtsException;
import com.mts.apps.model.Seat;
import java.math.BigDecimal;
import java.util.List;

/** Seat management, including Add Seats. */
public interface ISeatService {

    /** Adds count seats to a row, for example row A with 5 gives A1 to A5. Rejects anything beyond the theatre capacity. Returns how many were added. */
    int addSeats(int theatreId, String row, int count, String seatType, BigDecimal price) throws MtsException;

    /** Adds one seat. Seat type must be SILVER, GOLD or PLATINUM and the number must be new in that theatre. */
    int addSeat(Seat seat) throws MtsException;

    /** One seat. */
    Seat getSeatById(int seatId) throws MtsException;

    /** All seats of a theatre. */
    List<Seat> getSeatsByTheatre(int theatreId) throws MtsException;

    /** Changes the seat type or price. */
    boolean updateSeat(Seat seat) throws MtsException;

    /** Removes a seat. */
    boolean deleteSeat(int seatId) throws MtsException;

}
