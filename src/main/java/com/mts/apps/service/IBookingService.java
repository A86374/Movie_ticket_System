package com.mts.apps.service;

import com.mts.apps.exception.MtsException;
import com.mts.apps.model.Booking;
import com.mts.apps.model.Seat;
import com.mts.apps.model.Show;
import com.mts.apps.model.User;

import java.util.List;

public interface IBookingService {

    /** US-11 - seats of this show that nobody is holding. */
    List<Seat> getAvailableSeats(Show show) throws MtsException;

    /** US-12 - books 1 to 10 seats as PENDING. Returns the saved booking with its id. */
    Booking bookSeats(User user, Show show, List<String> seatNumbers) throws MtsException;

    /** US-15 - every booking of the logged in customer. */
    List<Booking> getMyBookings(User user) throws MtsException;

    /** The logged in customer's bookings with one status, e.g. PENDING to pay later. */
    List<Booking> getMyBookingsByStatus(User user, String status) throws MtsException;

    /** The seats inside a booking. Empty for a cancelled booking. */
    List<Seat> getSeatsOfBooking(Booking booking) throws MtsException;

    /** US-16 - own booking only, before the show starts. */
    void cancelBooking(User user, Booking booking) throws MtsException;

    /** Admin - every booking. */
    List<Booking> getAllBookings() throws MtsException;

    /** Admin - every booking with one status. */
    List<Booking> getBookingsByStatus(String status) throws MtsException;
}