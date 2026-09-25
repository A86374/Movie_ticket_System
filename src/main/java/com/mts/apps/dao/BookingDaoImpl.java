package com.mts.apps.dao;

import com.mts.apps.model.Booking;
import com.mts.apps.model.Movie;
import com.mts.apps.model.Seat;
import com.mts.apps.model.Show;
import com.mts.apps.model.Theatre;
import com.mts.apps.model.User;
import com.mts.apps.util.JdbcUtil;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BookingDaoImpl implements IBookingDao {

    // bookings -> users, and bookings -> shows -> theatres, movies
    // name exists in theatres AND users, so both are aliased
    private static final String SELECT_BASE =
            "SELECT b.booking_id, b.booking_date, b.total_amount, b.booking_status, "
                    + "u.user_id, u.name AS user_name, u.email, u.phone, u.password, u.role, "
                    + "sh.show_id, sh.show_date, sh.show_slot, sh.start_time, sh.end_time, "
                    + "t.theatre_id, t.name AS theatre_name, t.city, t.address, t.total_seats, "
                    + "m.movie_id, m.title, m.language, m.genre, m.duration, m.release_date "
                    + "FROM bookings b "
                    + "JOIN users u    ON b.user_id     = u.user_id "
                    + "JOIN shows sh   ON b.show_id     = sh.show_id "
                    + "JOIN theatres t ON sh.theatre_id = t.theatre_id "
                    + "JOIN movies m   ON sh.movie_id   = m.movie_id ";

    private static final String INSERT_BOOKING =
            "INSERT INTO bookings (show_id, user_id, total_amount, booking_status) "
                    + "VALUES (?, ?, ?, ?)";

    private static final String INSERT_BOOKED_SEAT =
            "INSERT INTO booked_seats (booking_id, show_id, seat_id) VALUES (?, ?, ?)";

    private static final String SELECT_BOOKINGS_BY_USER =
            SELECT_BASE + "WHERE b.user_id = ? ORDER BY b.booking_date DESC";

    private static final String SELECT_BOOKINGS_BY_USER_AND_STATUS =
            SELECT_BASE + "WHERE b.user_id = ? AND b.booking_status = ? "
                    + "ORDER BY b.booking_date DESC";

    private static final String SELECT_ALL_BOOKINGS =
            SELECT_BASE + "ORDER BY b.booking_date DESC";

    private static final String SELECT_BOOKINGS_BY_STATUS =
            SELECT_BASE + "WHERE b.booking_status = ? ORDER BY b.booking_date DESC";

    // seats of the show's theatre that are not already held for this show
    private static final String SELECT_AVAILABLE_SEATS =
            "SELECT s.*, t.name, t.city, t.address, t.total_seats "
                    + "FROM seats s JOIN theatres t ON s.theatre_id = t.theatre_id "
                    + "WHERE s.theatre_id = ? "
                    + "AND s.seat_id NOT IN (SELECT seat_id FROM booked_seats WHERE show_id = ?) "
                    + "ORDER BY s.seat_number";

    private static final String SELECT_SEATS_BY_BOOKING =
            "SELECT s.*, t.name, t.city, t.address, t.total_seats "
                    + "FROM booked_seats bs "
                    + "JOIN seats s    ON bs.seat_id   = s.seat_id "
                    + "JOIN theatres t ON s.theatre_id = t.theatre_id "
                    + "WHERE bs.booking_id = ? ORDER BY s.seat_number";

    private static final String DELETE_BOOKED_SEATS =
            "DELETE FROM booked_seats WHERE booking_id = ?";

    private static final String CANCEL_BOOKING =
            "UPDATE bookings SET booking_status = 'CANCELLED' WHERE booking_id = ?";

    private static final String REFUND_PAYMENT =
            "UPDATE payments SET payment_status = 'REFUNDED' "
                    + "WHERE booking_id = ? AND payment_status = 'SUCCESS'";

    private static final Logger logger = LoggerFactory.getLogger(BookingDaoImpl.class);

    private final JdbcUtil jdbcUtil = new JdbcUtil();

    // ---------------------------------------------------------------- transactions

    // CREATE - the booking row and every seat row land together, or none of them do
    @Override
    public int addBooking(Booking booking, List<Seat> seats) throws SQLException {
        int showId = booking.getShow().getShowId();
        int userId = booking.getUser().getUserId();
        Connection con = null;
        try {
            con = jdbcUtil.getConnectionObject();
            con.setAutoCommit(false);
            logger.debug("Transaction started for booking, showId={}, userId={}, seats={}",
                    showId, userId, seats.size());

            int bookingId;
            try (PreparedStatement ps = con.prepareStatement(INSERT_BOOKING,
                    Statement.RETURN_GENERATED_KEYS)) {
                ps.setInt(1, showId);
                ps.setInt(2, userId);
                ps.setBigDecimal(3, booking.getTotalAmount());
                ps.setString(4, booking.getBookingStatus());
                ps.executeUpdate();

                try (ResultSet keys = ps.getGeneratedKeys()) {
                    bookingId = keys.next() ? keys.getInt(1) : 0;
                }
            }

            // one row per seat, sent as a single batch
            try (PreparedStatement ps = con.prepareStatement(INSERT_BOOKED_SEAT)) {
                for (Seat seat : seats) {
                    ps.setInt(1, bookingId);
                    ps.setInt(2, showId);
                    ps.setInt(3, seat.getSeatId());
                    ps.addBatch();
                }
                ps.executeBatch();
            }

            con.commit();
            logger.info("Booking {} saved with {} seats for showId={}, userId={}, total={}",
                    bookingId, seats.size(), showId, userId, booking.getTotalAmount());
            return bookingId;

        } catch (SQLIntegrityConstraintViolationException e) {
            // UNIQUE (show_id, seat_id) fired - somebody already holds one of these seats
            if (con != null) {
                con.rollback();
            }
            logger.error("Rolled back booking, a seat is already held for showId={}", showId, e);
            throw e;

        } catch (SQLException e) {
            if (con != null) {
                con.rollback();
            }
            logger.error("Rolled back booking for showId={}, userId={}", showId, userId, e);
            throw e;

        } finally {
            if (con != null) {
                con.setAutoCommit(true);
                con.close();
            }
        }
    }

    // CANCEL - free the seats, mark the booking, refund the payment, all together
    @Override
    public boolean cancelBooking(Booking booking) throws SQLException {
        int bookingId = booking.getBookingId();
        Connection con = null;
        try {
            con = jdbcUtil.getConnectionObject();
            con.setAutoCommit(false);
            logger.debug("Transaction started to cancel bookingId={}", bookingId);

            int seatsFreed;
            try (PreparedStatement ps = con.prepareStatement(DELETE_BOOKED_SEATS)) {
                ps.setInt(1, bookingId);
                seatsFreed = ps.executeUpdate();
            }

            int bookingsChanged;
            try (PreparedStatement ps = con.prepareStatement(CANCEL_BOOKING)) {
                ps.setInt(1, bookingId);
                bookingsChanged = ps.executeUpdate();
            }

            // affects 0 rows when the booking was never paid, which is fine
            int refunded;
            try (PreparedStatement ps = con.prepareStatement(REFUND_PAYMENT)) {
                ps.setInt(1, bookingId);
                refunded = ps.executeUpdate();
            }

            con.commit();
            logger.info("Booking {} cancelled, {} seats freed, payment refunded={}",
                    bookingId, seatsFreed, refunded == 1);
            return bookingsChanged == 1;

        } catch (SQLException e) {
            if (con != null) {
                con.rollback();
            }
            logger.error("Rolled back cancellation of bookingId={}", bookingId, e);
            throw e;

        } finally {
            if (con != null) {
                con.setAutoCommit(true);
                con.close();
            }
        }
    }

    // ---------------------------------------------------------------- reads

    @Override
    public List<Booking> getBookingsByUser(User user) throws SQLException {
        List<Booking> bookings = new ArrayList<>();
        try (Connection con = jdbcUtil.getConnectionObject();
             PreparedStatement ps = con.prepareStatement(SELECT_BOOKINGS_BY_USER)) {

            ps.setInt(1, user.getUserId());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    bookings.add(mapRow(rs));
                }
            }
        }
        return bookings;
    }

    @Override
    public List<Booking> getBookingsByUserAndStatus(User user, String bookingStatus)
            throws SQLException {
        List<Booking> bookings = new ArrayList<>();
        try (Connection con = jdbcUtil.getConnectionObject();
             PreparedStatement ps = con.prepareStatement(SELECT_BOOKINGS_BY_USER_AND_STATUS)) {

            ps.setInt(1, user.getUserId());
            ps.setString(2, bookingStatus);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    bookings.add(mapRow(rs));
                }
            }
        }
        return bookings;
    }

    @Override
    public List<Booking> getAllBookings() throws SQLException {
        List<Booking> bookings = new ArrayList<>();
        try (Connection con = jdbcUtil.getConnectionObject();
             PreparedStatement ps = con.prepareStatement(SELECT_ALL_BOOKINGS);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                bookings.add(mapRow(rs));
            }
        }
        return bookings;
    }

    @Override
    public List<Booking> getBookingsByStatus(String bookingStatus) throws SQLException {
        List<Booking> bookings = new ArrayList<>();
        try (Connection con = jdbcUtil.getConnectionObject();
             PreparedStatement ps = con.prepareStatement(SELECT_BOOKINGS_BY_STATUS)) {

            ps.setString(1, bookingStatus);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    bookings.add(mapRow(rs));
                }
            }
        }
        return bookings;
    }

    @Override
    public List<Seat> getAvailableSeats(Show show) throws SQLException {
        List<Seat> seats = new ArrayList<>();
        try (Connection con = jdbcUtil.getConnectionObject();
             PreparedStatement ps = con.prepareStatement(SELECT_AVAILABLE_SEATS)) {

            ps.setInt(1, show.getTheatre().getTheatreId());
            ps.setInt(2, show.getShowId());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    seats.add(mapSeat(rs));
                }
            }
        }
        return seats;
    }

    @Override
    public List<Seat> getSeatsByBooking(Booking booking) throws SQLException {
        List<Seat> seats = new ArrayList<>();
        try (Connection con = jdbcUtil.getConnectionObject();
             PreparedStatement ps = con.prepareStatement(SELECT_SEATS_BY_BOOKING)) {

            ps.setInt(1, booking.getBookingId());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    seats.add(mapSeat(rs));
                }
            }
        }
        return seats;
    }

    // ---------------------------------------------------------------- row mapping

    private Booking mapRow(ResultSet rs) throws SQLException {
        Booking booking = new Booking();
        booking.setBookingId(rs.getInt("booking_id"));
        booking.setShow(mapShow(rs));
        booking.setUser(mapUser(rs));
        booking.setBookingDate(rs.getTimestamp("booking_date").toLocalDateTime());
        booking.setTotalAmount(rs.getBigDecimal("total_amount"));
        booking.setBookingStatus(rs.getString("booking_status"));
        return booking;
    }

    private Show mapShow(ResultSet rs) throws SQLException {
        Theatre theatre = new Theatre();
        theatre.setTheatreId(rs.getInt("theatre_id"));
        theatre.setName(rs.getString("theatre_name"));
        theatre.setCity(rs.getString("city"));
        theatre.setAddress(rs.getString("address"));
        theatre.setTotalSeats(rs.getInt("total_seats"));

        Movie movie = new Movie();
        movie.setMovieId(rs.getInt("movie_id"));
        movie.setTitle(rs.getString("title"));
        movie.setLanguage(rs.getString("language"));
        movie.setGenre(rs.getString("genre"));
        movie.setDuration(rs.getInt("duration"));
        Date releaseDate = rs.getDate("release_date");
        movie.setReleaseDate(releaseDate == null ? null : releaseDate.toLocalDate());

        Show show = new Show();
        show.setShowId(rs.getInt("show_id"));
        show.setTheatre(theatre);
        show.setMovie(movie);
        show.setShowDate(rs.getDate("show_date").toLocalDate());
        show.setShowSlot(rs.getString("show_slot"));
        show.setStartTime(rs.getTime("start_time").toLocalTime());
        show.setEndTime(rs.getTime("end_time").toLocalTime());
        return show;
    }

    private User mapUser(ResultSet rs) throws SQLException {
        User user = new User();
        user.setUserId(rs.getInt("user_id"));
        user.setName(rs.getString("user_name"));
        user.setEmail(rs.getString("email"));
        user.setPhone(rs.getString("phone"));
        user.setPassword(rs.getString("password"));
        user.setRole(rs.getString("role"));
        return user;
    }

    // used by the two seat queries, where the theatre columns are not aliased
    private Seat mapSeat(ResultSet rs) throws SQLException {
        Theatre theatre = new Theatre();
        theatre.setTheatreId(rs.getInt("theatre_id"));
        theatre.setName(rs.getString("name"));
        theatre.setCity(rs.getString("city"));
        theatre.setAddress(rs.getString("address"));
        theatre.setTotalSeats(rs.getInt("total_seats"));

        Seat seat = new Seat();
        seat.setSeatId(rs.getInt("seat_id"));
        seat.setTheatre(theatre);
        seat.setSeatNumber(rs.getString("seat_number"));
        seat.setSeatType(rs.getString("seat_type"));
        seat.setPrice(rs.getBigDecimal("price"));
        return seat;
    }
}