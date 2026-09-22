package com.mts.apps.dao;

import com.mts.apps.model.BookedSeat;

import java.sql.SQLException;
import java.util.List;

public interface IBookedSeatDao {

    int addBookedSeat(BookedSeat bookedSeat) throws SQLException;

    List<BookedSeat> getBookedSeatsByBooking(int bookingId) throws SQLException;

    boolean deleteBookedSeat(int bookedSeatId) throws SQLException;

    /** True when an active booking of that show already holds the seat. */
    boolean isSeatBooked(int showId, int seatId) throws SQLException;
}
