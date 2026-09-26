package com.mts.apps.service;

import com.mts.apps.exception.MtsException;
import com.mts.apps.model.Seat;

import java.math.BigDecimal;
import java.util.List;

public interface ISeatService {

    /** Adds a new row, for example row A with 10 seats gives A1 to A10. */
    void addSeatRow(String theatreName, String rowLetter, int count,
                    String seatType, BigDecimal price) throws MtsException;

    List<Seat> getSeatsByTheatre(String theatreName) throws MtsException;

    void updateSeat(String theatreName, String seatNumber,
                    String seatType, BigDecimal price) throws MtsException;

    void deleteSeat(String theatreName, String seatNumber) throws MtsException;
}
