package com.mts.apps.dao;

import com.mts.apps.model.User;
import com.mts.apps.util.JdbcUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class UserDaoImpl implements IUserDao {

    private static final String INSERT_USER =
            "INSERT INTO users (name, email, phone, password, role) VALUES (?, ?, ?, ?, ?)";

    private static final String SELECT_USER_BY_EMAIL =
            "SELECT * FROM users WHERE email = ?";

    private static final String SELECT_USER_BY_EMAIL_AND_PASSWORD =
            "SELECT * FROM users WHERE email = ? AND password = ?";

    private static final String SELECT_ALL_USERS =
            "SELECT * FROM users ORDER BY name";

    private static final String UPDATE_USER =
            "UPDATE users SET name = ?, email = ?, phone = ?, password = ?, role = ? WHERE user_id = ?";

    private static final String DELETE_USER_BY_EMAIL =
            "DELETE FROM users WHERE email = ?";

    private final JdbcUtil jdbcUtil = new JdbcUtil();

    // CREATE - saves a user and returns the id MySQL gave it
    @Override
    public int addUser(User user) throws SQLException {
        try (Connection con = jdbcUtil.getConnectionObject();
             PreparedStatement ps = con.prepareStatement(INSERT_USER, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, user.getName());
            ps.setString(2, user.getEmail());
            ps.setString(3, user.getPhone());
            ps.setString(4, user.getPassword());
            ps.setString(5, user.getRole());
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                return keys.next() ? keys.getInt(1) : 0;
            }
        }
    }

    // READ one - returns null when no user has that email
    @Override
    public User getUserByEmail(String email) throws SQLException {
        try (Connection con = jdbcUtil.getConnectionObject();
             PreparedStatement ps = con.prepareStatement(SELECT_USER_BY_EMAIL)) {

            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapRow(rs) : null;
            }
        }
    }

    // LOGIN - returns the user when email and password match, null otherwise
    @Override
    public User login(String email, String password) throws SQLException {
        try (Connection con = jdbcUtil.getConnectionObject();
             PreparedStatement ps = con.prepareStatement(SELECT_USER_BY_EMAIL_AND_PASSWORD)) {

            ps.setString(1, email);
            ps.setString(2, password);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapRow(rs) : null;
            }
        }
    }

    // READ all
    @Override
    public List<User> getAllUsers() throws SQLException {
        List<User> users = new ArrayList<>();
        try (Connection con = jdbcUtil.getConnectionObject();
             PreparedStatement ps = con.prepareStatement(SELECT_ALL_USERS);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                users.add(mapRow(rs));
            }
        }
        return users;
    }

    // UPDATE - true when exactly one row was changed
    @Override
    public boolean updateUser(User user) throws SQLException {
        try (Connection con = jdbcUtil.getConnectionObject();
             PreparedStatement ps = con.prepareStatement(UPDATE_USER)) {

            ps.setString(1, user.getName());
            ps.setString(2, user.getEmail());
            ps.setString(3, user.getPhone());
            ps.setString(4, user.getPassword());
            ps.setString(5, user.getRole());
            ps.setInt(6, user.getUserId());
            return ps.executeUpdate() == 1;
        }
    }

    // DELETE - true when the user was removed
    @Override
    public boolean deleteUser(String email) throws SQLException {
        try (Connection con = jdbcUtil.getConnectionObject();
             PreparedStatement ps = con.prepareStatement(DELETE_USER_BY_EMAIL)) {

            ps.setString(1, email);
            return ps.executeUpdate() == 1;
        }
    }

    // turns the current row of the ResultSet into a User object
    private User mapRow(ResultSet rs) throws SQLException {
        User user = new User();
        user.setUserId(rs.getInt("user_id"));
        user.setName(rs.getString("name"));
        user.setEmail(rs.getString("email"));
        user.setPhone(rs.getString("phone"));
        user.setPassword(rs.getString("password"));
        user.setRole(rs.getString("role"));
        return user;
    }
}