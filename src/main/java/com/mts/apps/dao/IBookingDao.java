package com.mts.apps.dao;

import com.mts.apps.model.Booking;
import com.mts.apps.model.Seat;
import com.mts.apps.model.Show;
import com.mts.apps.model.User;

import java.sql.SQLException;
import java.util.List;

public interface IBookingDao {

    /** Saves the booking and all its seats together in one transaction. Returns the booking id. */
    int addBooking(Booking booking, List<Seat> seats) throws SQLException;

    /** Every booking of one customer, newest first. */
    List<Booking> getBookingsByUser(User user) throws SQLException;

    /** One customer's bookings filtered by PENDING, CONFIRMED or CANCELLED. */
    List<Booking> getBookingsByUserAndStatus(User user, String bookingStatus) throws SQLException;

    /** Admin view - every booking in the system. */
    List<Booking> getAllBookings() throws SQLException;

    /** Admin view filtered by status. */
    List<Booking> getBookingsByStatus(String bookingStatus) throws SQLException;

    /** Seats of this show that no active booking is holding. */
    List<Seat> getAvailableSeats(Show show) throws SQLException;

    /** The seats inside one booking. */
    List<Seat> getSeatsByBooking(Booking booking) throws SQLException;

    /** Frees the seats, marks the booking CANCELLED and the payment REFUNDED, one transaction. */
    boolean cancelBooking(Booking booking) throws SQLException;
}