package com.mts.apps.dao;

import com.mts.apps.model.Seat;
import com.mts.apps.model.Theatre;
import com.mts.apps.util.JdbcUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class SeatDaoImpl implements ISeatDao {

    private static final String INSERT_SEAT =
            "INSERT INTO seats (theatre_id, seat_number, seat_type, price) VALUES (?, ?, ?, ?)";

    private static final String SELECT_SEAT =
            "SELECT s.*, t.name, t.city, t.address, t.total_seats "
                    + "FROM seats s JOIN theatres t ON s.theatre_id = t.theatre_id "
                    + "WHERE s.theatre_id = ? AND s.seat_number = ?";

    // A1, A2 ... A10 in number order, not text order
    private static final String SELECT_SEATS_BY_THEATRE =
            "SELECT s.*, t.name, t.city, t.address, t.total_seats "
                    + "FROM seats s JOIN theatres t ON s.theatre_id = t.theatre_id "
                    + "WHERE s.theatre_id = ? "
                    + "ORDER BY LEFT(s.seat_number, 1), CAST(SUBSTRING(s.seat_number, 2) AS UNSIGNED)";

    private static final String COUNT_SEATS_BY_THEATRE =
            "SELECT COUNT(*) FROM seats WHERE theatre_id = ?";

    private static final String UPDATE_SEAT =
            "UPDATE seats SET seat_type = ?, price = ? WHERE seat_id = ?";

    private static final String DELETE_SEAT =
            "DELETE FROM seats WHERE theatre_id = ? AND seat_number = ?";

    private final JdbcUtil jdbcUtil = new JdbcUtil();

    // CREATE - saves a seat and returns the id MySQL gave it
    @Override
    public int addSeat(Seat seat) throws SQLException {
        try (Connection con = jdbcUtil.getConnectionObject();
             PreparedStatement ps = con.prepareStatement(INSERT_SEAT, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, seat.getTheatre().getTheatreId());
            ps.setString(2, seat.getSeatNumber());
            ps.setString(3, seat.getSeatType());
            ps.setBigDecimal(4, seat.getPrice());
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                return keys.next() ? keys.getInt(1) : 0;
            }
        }
    }

    // READ one - a seat number only makes sense inside one theatre
    @Override
    public Seat getSeat(Theatre theatre, String seatNumber) throws SQLException {
        try (Connection con = jdbcUtil.getConnectionObject();
             PreparedStatement ps = con.prepareStatement(SELECT_SEAT)) {

            ps.setInt(1, theatre.getTheatreId());
            ps.setString(2, seatNumber);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapRow(rs) : null;
            }
        }
    }

    // READ all seats of one theatre
    @Override
    public List<Seat> getSeatsByTheatre(Theatre theatre) throws SQLException {
        List<Seat> seats = new ArrayList<>();
        try (Connection con = jdbcUtil.getConnectionObject();
             PreparedStatement ps = con.prepareStatement(SELECT_SEATS_BY_THEATRE)) {

            ps.setInt(1, theatre.getTheatreId());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    seats.add(mapRow(rs));
                }
            }
        }
        return seats;
    }

    // how many seats the theatre already has - used for the capacity check
    @Override
    public int countSeatsByTheatre(Theatre theatre) throws SQLException {
        try (Connection con = jdbcUtil.getConnectionObject();
             PreparedStatement ps = con.prepareStatement(COUNT_SEATS_BY_THEATRE)) {

            ps.setInt(1, theatre.getTheatreId());
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    // UPDATE - only type and price can change, not the seat number
    @Override
    public boolean updateSeat(Seat seat) throws SQLException {
        try (Connection con = jdbcUtil.getConnectionObject();
             PreparedStatement ps = con.prepareStatement(UPDATE_SEAT)) {

            ps.setString(1, seat.getSeatType());
            ps.setBigDecimal(2, seat.getPrice());
            ps.setInt(3, seat.getSeatId());
            return ps.executeUpdate() == 1;
        }
    }

    // DELETE - true when the seat was removed
    @Override
    public boolean deleteSeat(Theatre theatre, String seatNumber) throws SQLException {
        try (Connection con = jdbcUtil.getConnectionObject();
             PreparedStatement ps = con.prepareStatement(DELETE_SEAT)) {

            ps.setInt(1, theatre.getTheatreId());
            ps.setString(2, seatNumber);
            return ps.executeUpdate() == 1;
        }
    }

    // turns the current row into a Seat with its Theatre filled in
    private Seat mapRow(ResultSet rs) throws SQLException {
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