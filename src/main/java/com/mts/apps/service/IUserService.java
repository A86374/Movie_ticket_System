package com.mts.apps.service;

import com.mts.apps.exception.MtsException;
import com.mts.apps.model.User;

import java.util.List;

public interface IUserService {

    /** Registers a new CUSTOMER. The role is set here, never taken from the user. */
    void register(User user) throws MtsException;

    /** Returns the logged in user, or throws if the credentials are wrong. */
    User login(String email, String password) throws MtsException;

    User getUserByEmail(String email) throws MtsException;

    List<User> getAllUsers() throws MtsException;

    void updateUser(User user) throws MtsException;

    void deleteUser(String email) throws MtsException;
}