package com.mts.apps.dao;

import com.mts.apps.model.User;

import java.sql.SQLException;
import java.util.List;

public interface IUserDao {

    int addUser(User user) throws SQLException;

    User getUserById(int userId) throws SQLException;

    List<User> getAllUsers() throws SQLException;

    boolean updateUser(User user) throws SQLException;

    boolean deleteUser(int userId) throws SQLException;

    User login(String email, String password) throws SQLException;

    boolean emailExists(String email) throws SQLException;
}
