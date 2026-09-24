package com.mts.apps.dao;

import com.mts.apps.model.User;

import java.sql.SQLException;
import java.util.List;

public interface IUserDao {

    int addUser(User user) throws SQLException;

    User getUserByEmail(String email) throws SQLException;

    User login(String email, String password) throws SQLException;

    List<User> getAllUsers() throws SQLException;

    boolean updateUser(User user) throws SQLException;

    boolean deleteUser(String email) throws SQLException;
}