package com.mts.apps.dao;

import com.mts.apps.model.Booking;
import com.mts.apps.model.Seat;

import java.sql.SQLException;
import java.util.List;

public interface IBookingDao {

    /** Saves the booking and all its seats together in one transaction. Returns the booking id. */
    int addBooking(Booking booking, List<Seat> seats) throws SQLException;

    Booking getBookingById(int bookingId) throws SQLException;

    List<Booking> getAllBookings() throws SQLException;

    boolean updateBooking(Booking booking) throws SQLException;

    boolean deleteBooking(int bookingId) throws SQLException;

    List<Booking> getBookingsByUser(int userId) throws SQLException;

    boolean updateBookingStatus(int bookingId, String status) throws SQLException;
}
