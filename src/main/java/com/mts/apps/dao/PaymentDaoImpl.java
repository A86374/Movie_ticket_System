package com.mts.apps.dao;

import com.mts.apps.model.Booking;
import com.mts.apps.model.Movie;
import com.mts.apps.model.Payment;
import com.mts.apps.model.Show;
import com.mts.apps.model.Theatre;
import com.mts.apps.model.User;
import com.mts.apps.util.JdbcUtil;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PaymentDaoImpl implements IPaymentDao {

    // payments -> bookings -> users, and bookings -> shows -> theatres, movies
    // name exists in theatres AND users, so both are aliased
    private static final String SELECT_BASE =
            "SELECT p.payment_id, p.amount, p.payment_method, p.payment_status, p.payment_date, "
                    + "b.booking_id, b.booking_date, b.total_amount, b.booking_status, "
                    + "u.user_id, u.name AS user_name, u.email, u.phone, u.password, u.role, "
                    + "sh.show_id, sh.show_date, sh.show_slot, sh.start_time, sh.end_time, "
                    + "t.theatre_id, t.name AS theatre_name, t.city, t.address, t.total_seats, "
                    + "m.movie_id, m.title, m.language, m.genre, m.duration, m.release_date "
                    + "FROM payments p "
                    + "JOIN bookings b ON p.booking_id  = b.booking_id "
                    + "JOIN users u    ON b.user_id     = u.user_id "
                    + "JOIN shows sh   ON b.show_id     = sh.show_id "
                    + "JOIN theatres t ON sh.theatre_id = t.theatre_id "
                    + "JOIN movies m   ON sh.movie_id   = m.movie_id ";

    private static final String INSERT_PAYMENT =
            "INSERT INTO payments (booking_id, amount, payment_method, payment_status) "
                    + "VALUES (?, ?, ?, ?)";

    // only a PENDING booking can become CONFIRMED
    private static final String CONFIRM_BOOKING =
            "UPDATE bookings SET booking_status = 'CONFIRMED' "
                    + "WHERE booking_id = ? AND booking_status = 'PENDING'";

    private static final String SELECT_PAYMENT_BY_BOOKING =
            SELECT_BASE + "WHERE p.booking_id = ?";

    private static final String SELECT_ALL_PAYMENTS =
            SELECT_BASE + "ORDER BY p.payment_date DESC";

    private static final Logger logger = LoggerFactory.getLogger(PaymentDaoImpl.class);

    private final JdbcUtil jdbcUtil = new JdbcUtil();

    // CREATE - payment row and booking status change together, or neither happens
    @Override
    public int addPayment(Payment payment) throws SQLException {
        int bookingId = payment.getBooking().getBookingId();
        Connection con = null;
        try {
            con = jdbcUtil.getConnectionObject();
            con.setAutoCommit(false);
            logger.debug("Transaction started for payment on bookingId={}", bookingId);

            int paymentId;
            try (PreparedStatement ps = con.prepareStatement(INSERT_PAYMENT,
                    Statement.RETURN_GENERATED_KEYS)) {
                ps.setInt(1, bookingId);
                ps.setBigDecimal(2, payment.getAmount());
                ps.setString(3, payment.getPaymentMethod());
                ps.setString(4, payment.getPaymentStatus());
                ps.executeUpdate();

                try (ResultSet keys = ps.getGeneratedKeys()) {
                    paymentId = keys.next() ? keys.getInt(1) : 0;
                }
            }

            try (PreparedStatement ps = con.prepareStatement(CONFIRM_BOOKING)) {
                ps.setInt(1, bookingId);
                if (ps.executeUpdate() != 1) {
                    throw new SQLException("Booking " + bookingId + " is not PENDING");
                }
            }

            con.commit();
            logger.info("Payment {} saved and booking {} confirmed, amount={}, method={}",
                    paymentId, bookingId, payment.getAmount(), payment.getPaymentMethod());
            return paymentId;

        } catch (SQLException e) {
            if (con != null) {
                con.rollback();
            }
            logger.error("Rolled back payment for bookingId={}", bookingId, e);
            throw e;

        } finally {
            if (con != null) {
                con.setAutoCommit(true);
                con.close();
            }
        }
    }

    // READ one - booking_id is UNIQUE, so a booking has at most one payment
    @Override
    public Payment getPaymentByBooking(Booking booking) throws SQLException {
        try (Connection con = jdbcUtil.getConnectionObject();
             PreparedStatement ps = con.prepareStatement(SELECT_PAYMENT_BY_BOOKING)) {

            ps.setInt(1, booking.getBookingId());
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapRow(rs) : null;
            }
        }
    }

    // READ all - admin view
    @Override
    public List<Payment> getAllPayments() throws SQLException {
        List<Payment> payments = new ArrayList<>();
        try (Connection con = jdbcUtil.getConnectionObject();
             PreparedStatement ps = con.prepareStatement(SELECT_ALL_PAYMENTS);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                payments.add(mapRow(rs));
            }
        }
        return payments;
    }

    // turns the current row into a Payment with its whole Booking filled in
    private Payment mapRow(ResultSet rs) throws SQLException {
        Payment payment = new Payment();
        payment.setPaymentId(rs.getInt("payment_id"));
        payment.setBooking(mapBooking(rs));
        payment.setAmount(rs.getBigDecimal("amount"));
        payment.setPaymentMethod(rs.getString("payment_method"));
        payment.setPaymentStatus(rs.getString("payment_status"));
        payment.setPaymentDate(rs.getTimestamp("payment_date").toLocalDateTime());
        return payment;
    }

    private Booking mapBooking(ResultSet rs) throws SQLException {
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
}