package com.mts.apps.dao;

import com.mts.apps.model.Theatre;
import com.mts.apps.util.JdbcUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class TheatreDaoImpl implements ITheatreDao {

    private static final String INSERT_THEATRE =
            "INSERT INTO theatres (name, city, address, total_seats) VALUES (?, ?, ?, ?)";

    private static final String SELECT_THEATRE_BY_NAME =
            "SELECT * FROM theatres WHERE name = ?";

    private static final String SELECT_ALL_THEATRES =
            "SELECT * FROM theatres ORDER BY name";

    private static final String UPDATE_THEATRE =
            "UPDATE theatres SET name = ?, city = ?, address = ?, total_seats = ? WHERE theatre_id = ?";

    private static final String DELETE_THEATRE_BY_NAME =
            "DELETE FROM theatres WHERE name = ?";

    private final JdbcUtil jdbcUtil = new JdbcUtil();

    // CREATE - saves a theatre and returns the id MySQL gave it
    @Override
    public int addTheatre(Theatre theatre) throws SQLException {
        try (Connection con = jdbcUtil.getConnectionObject();
             PreparedStatement ps = con.prepareStatement(INSERT_THEATRE, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, theatre.getName());
            ps.setString(2, theatre.getCity());
            ps.setString(3, theatre.getAddress());
            ps.setInt(4, theatre.getTotalSeats());
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                return keys.next() ? keys.getInt(1) : 0;
            }
        }
    }

    // READ one - returns null when no theatre has that name
    @Override
    public Theatre getTheatreByName(String name) throws SQLException {
        try (Connection con = jdbcUtil.getConnectionObject();
             PreparedStatement ps = con.prepareStatement(SELECT_THEATRE_BY_NAME)) {

            ps.setString(1, name);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapRow(rs) : null;
            }
        }
    }

    // READ all
    @Override
    public List<Theatre> getAllTheatres() throws SQLException {
        List<Theatre> theatres = new ArrayList<>();
        try (Connection con = jdbcUtil.getConnectionObject();
             PreparedStatement ps = con.prepareStatement(SELECT_ALL_THEATRES);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                theatres.add(mapRow(rs));
            }
        }
        return theatres;
    }

    // UPDATE - true when exactly one row was changed
    @Override
    public boolean updateTheatre(Theatre theatre) throws SQLException {
        try (Connection con = jdbcUtil.getConnectionObject();
             PreparedStatement ps = con.prepareStatement(UPDATE_THEATRE)) {

            ps.setString(1, theatre.getName());
            ps.setString(2, theatre.getCity());
            ps.setString(3, theatre.getAddress());
            ps.setInt(4, theatre.getTotalSeats());
            ps.setInt(5, theatre.getTheatreId());
            return ps.executeUpdate() == 1;
        }
    }

    // DELETE - true when the theatre was removed
    @Override
    public boolean deleteTheatre(String name) throws SQLException {
        try (Connection con = jdbcUtil.getConnectionObject();
             PreparedStatement ps = con.prepareStatement(DELETE_THEATRE_BY_NAME)) {

            ps.setString(1, name);
            return ps.executeUpdate() == 1;
        }
    }

    // turns the current row of the ResultSet into a Theatre object
    private Theatre mapRow(ResultSet rs) throws SQLException {
        Theatre theatre = new Theatre();
        theatre.setTheatreId(rs.getInt("theatre_id"));
        theatre.setName(rs.getString("name"));
        theatre.setCity(rs.getString("city"));
        theatre.setAddress(rs.getString("address"));
        theatre.setTotalSeats(rs.getInt("total_seats"));
        return theatre;
    }
}