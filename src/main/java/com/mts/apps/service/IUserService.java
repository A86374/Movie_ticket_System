package com.mts.apps.service;

import com.mts.apps.exception.MtsException;
import com.mts.apps.model.User;
import java.util.List;

/** Registration, login and user management. */
public interface IUserService {

    /** Validates email, 10 digit phone and 6+ character password, rejects a used email, saves with role CUSTOMER. */
    User register(User user) throws MtsException;

    /** Returns the user when email and password match, otherwise throws. */
    User login(String email, String password) throws MtsException;

    /** One user. */
    User getUserById(int userId) throws MtsException;

    /** Every user. */
    List<User> getAllUsers() throws MtsException;

    /** Validates and saves the changed details. */
    boolean updateUser(User user) throws MtsException;

    /** Removes a user. */
    boolean deleteUser(int userId) throws MtsException;

}