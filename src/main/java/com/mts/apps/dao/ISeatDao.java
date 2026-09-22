package com.mts.apps.dao;

import com.mts.apps.model.Seat;

import java.sql.SQLException;
import java.util.List;

public interface ISeatDao {

    int addSeat(Seat seat) throws SQLException;

    Seat getSeatById(int seatId) throws SQLException;

    List<Seat> getSeatsByTheatre(int theatreId) throws SQLException;

    boolean updateSeat(Seat seat) throws SQLException;

    boolean deleteSeat(int seatId) throws SQLException;

    /** One seat by its number inside a theatre, for example A1. */
    Seat getSeatByNumber(int theatreId, String seatNumber) throws SQLException;

    /** How many seats a theatre already has, used for the capacity check. */
    int countSeats(int theatreId) throws SQLException;

    /** Seats of the show's theatre that no active booking of that show is holding. */
    List<Seat> getAvailableSeats(int showId) throws SQLException;
}
