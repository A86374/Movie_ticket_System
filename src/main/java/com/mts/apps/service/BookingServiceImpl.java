package com.mts.apps.service;

import com.mts.apps.dao.BookingDaoImpl;
import com.mts.apps.dao.IBookingDao;
import com.mts.apps.exception.MtsException;
import com.mts.apps.model.Booking;
import com.mts.apps.model.Seat;
import com.mts.apps.model.Show;
import com.mts.apps.model.User;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BookingServiceImpl implements IBookingService {

    private static final Logger logger = LoggerFactory.getLogger(BookingServiceImpl.class);

    private static final int MAX_SEATS_PER_BOOKING = 10;
    private static final int DUPLICATE_KEY = 1062;   // MySQL error code for a UNIQUE clash
    private static final String STATUS_PENDING = "PENDING";
    private static final String STATUS_CANCELLED = "CANCELLED";
    private static final List<String> STATUSES = List.of("PENDING", "CONFIRMED", "CANCELLED");

    private final IBookingDao bookingDao = new BookingDaoImpl();

    @Override
    public List<Seat> getAvailableSeats(Show show) throws MtsException {
        try {
            List<Seat> seats = bookingDao.getAvailableSeats(show);
            if (seats.isEmpty()) {
                throw new MtsException("This show is sold out");
            }
            return seats;

        } catch (SQLException e) {
            logger.error("getAvailableSeats failed for showId={}", show.getShowId(), e);
            throw new MtsException("Could not load the seats, please try again", e);
        }
    }

    // US-12 - the Java check gives the readable message, UNIQUE (show_id, seat_id) is the safety net
    @Override
    public Booking bookSeats(User user, Show show, List<String> seatNumbers) throws MtsException {
        Set<String> wanted = new LinkedHashSet<>();
        if (seatNumbers != null) {
            for (String number : seatNumbers) {
                if (number != null && !number.trim().isEmpty()) {
                    wanted.add(number.trim().toUpperCase());
                }
            }
        }
        if (wanted.isEmpty() || wanted.size() > MAX_SEATS_PER_BOOKING) {
            throw new MtsException("Choose between 1 and " + MAX_SEATS_PER_BOOKING + " seats");
        }

        Map<String, Seat> available = new HashMap<>();
        for (Seat seat : getAvailableSeats(show)) {
            available.put(seat.getSeatNumber(), seat);
        }

        List<Seat> chosen = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;
        for (String number : wanted) {
            Seat seat = available.get(number);
            if (seat == null) {
                throw new MtsException("Seat " + number + " is not available for this show");
            }
            chosen.add(seat);
            total = total.add(seat.getPrice());
        }

        Booking booking = new Booking();
        booking.setShow(show);
        booking.setUser(user);
        booking.setBookingDate(LocalDateTime.now());
        booking.setTotalAmount(total);
        booking.setBookingStatus(STATUS_PENDING);

        try {
            booking.setBookingId(bookingDao.addBooking(booking, chosen));
            return booking;

        } catch (SQLException e) {
            if (e.getErrorCode() == DUPLICATE_KEY) {
                // someone took one of these seats between the check above and the insert
                logger.warn("Seat taken at insert time: showId={}, seats={}", show.getShowId(), wanted);
                throw new MtsException(
                        "One of those seats was just booked by someone else, please choose again", e);
            }
            logger.error("bookSeats failed: showId={}, userId={}", show.getShowId(), user.getUserId());
            throw new MtsException("Could not complete the booking, please try again", e);
        }
    }

    @Override
    public List<Booking> getMyBookings(User user) throws MtsException {
        try {
            List<Booking> bookings = bookingDao.getBookingsByUser(user);
            if (bookings.isEmpty()) {
                throw new MtsException("You have no bookings yet");
            }
            return bookings;

        } catch (SQLException e) {
            logger.error("getMyBookings failed for userId={}", user.getUserId(), e);
            throw new MtsException("Could not load your bookings, please try again", e);
        }
    }

    @Override
    public List<Booking> getMyBookingsByStatus(User user, String status) throws MtsException {
        String s = checkStatus(status);
        try {
            List<Booking> bookings = bookingDao.getBookingsByUserAndStatus(user, s);
            if (bookings.isEmpty()) {
                throw new MtsException("You have no " + s + " bookings");
            }
            return bookings;

        } catch (SQLException e) {
            logger.error("getMyBookingsByStatus failed: userId={}, status={}", user.getUserId(), s, e);
            throw new MtsException("Could not load your bookings, please try again", e);
        }
    }

    @Override
    public List<Seat> getSeatsOfBooking(Booking booking) throws MtsException {
        try {
            return bookingDao.getSeatsByBooking(booking);   // empty for a cancelled booking

        } catch (SQLException e) {
            logger.error("getSeatsOfBooking failed for bookingId={}", booking.getBookingId(), e);
            throw new MtsException("Could not load the seats of this booking", e);
        }
    }

    // US-16 - own booking only, not already cancelled, before the show starts
    @Override
    public void cancelBooking(User user, Booking booking) throws MtsException {
        if (booking.getUser().getUserId() != user.getUserId()) {
            logger.warn("Cancel refused, not the owner: bookingId={}, userId={}",
                    booking.getBookingId(), user.getUserId());
            throw new MtsException("You can only cancel your own bookings");
        }
        if (STATUS_CANCELLED.equals(booking.getBookingStatus())) {
            throw new MtsException("Booking " + booking.getBookingId() + " is already cancelled");
        }
        Show show = booking.getShow();
        if (!LocalDateTime.of(show.getShowDate(), show.getStartTime()).isAfter(LocalDateTime.now())) {
            throw new MtsException("This show has already started and cannot be cancelled");
        }

        try {
            if (!bookingDao.cancelBooking(booking)) {
                throw new MtsException("That booking no longer exists");
            }

        } catch (SQLException e) {
            logger.error("cancelBooking failed for bookingId={}", booking.getBookingId());
            throw new MtsException("Could not cancel the booking, please try again", e);
        }
    }

    @Override
    public List<Booking> getAllBookings() throws MtsException {
        try {
            List<Booking> bookings = bookingDao.getAllBookings();
            if (bookings.isEmpty()) {
                throw new MtsException("No bookings have been made yet");
            }
            return bookings;

        } catch (SQLException e) {
            logger.error("getAllBookings failed", e);
            throw new MtsException("Could not load the bookings, please try again", e);
        }
    }

    @Override
    public List<Booking> getBookingsByStatus(String status) throws MtsException {
        String s = checkStatus(status);
        try {
            List<Booking> bookings = bookingDao.getBookingsByStatus(s);
            if (bookings.isEmpty()) {
                throw new MtsException("No " + s + " bookings");
            }
            return bookings;

        } catch (SQLException e) {
            logger.error("getBookingsByStatus failed: status={}", s, e);
            throw new MtsException("Could not load the bookings, please try again", e);
        }
    }

    private String checkStatus(String status) throws MtsException {
        String s = status == null ? "" : status.trim().toUpperCase();
        if (!STATUSES.contains(s)) {
            throw new MtsException("Status must be PENDING, CONFIRMED or CANCELLED");
        }
        return s;
    }
}