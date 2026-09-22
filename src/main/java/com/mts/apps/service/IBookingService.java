package com.mts.apps.service;

import com.mts.apps.model.BookedSeat;
import com.mts.apps.model.Booking;
import java.util.List;

/** Booking and cancellation. */
public interface IBookingService {

    /** Checks the show has not started, 1 to 10 seats, every seat exists and is free, adds up the prices and saves the booking as PENDING with its seats. */
    Booking bookTickets(int userId, int showId, List<String> seatNumbers) throws MtsException;

    /** One booking. */
    Booking getBookingById(int bookingId) throws MtsException;

    /** Every booking, for the admin payment status list. */
    List<Booking> getAllBookings() throws MtsException;

    /** Bookings of the logged in customer. */
    List<Booking> getMyBookings(int userId) throws MtsException;

    /** Seats inside one booking. */
    List<BookedSeat> getBookedSeats(int bookingId) throws MtsException;

    /** Only the owner, only before the show starts. Sets the booking CANCELLED and a paid payment REFUNDED. */
    boolean cancelBooking(int userId, int bookingId) throws MtsException;

}
