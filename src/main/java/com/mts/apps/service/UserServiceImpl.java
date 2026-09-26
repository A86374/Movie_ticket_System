package com.mts.apps.service;

import com.mts.apps.dao.IUserDao;
import com.mts.apps.dao.UserDaoImpl;
import com.mts.apps.exception.MtsException;
import com.mts.apps.model.User;

import java.sql.SQLException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UserServiceImpl implements IUserService {

    private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);

    private static final String ROLE_CUSTOMER = "CUSTOMER";
    private static final String ROLE_ADMIN = "ADMIN";
    private static final int MIN_PASSWORD_LENGTH = 6;

    private final IUserDao userDao = new UserDaoImpl();

    @Override
    public void register(User user) throws MtsException {
        validate(user);
        try {
            if (userDao.getUserByEmail(user.getEmail()) != null) {
                logger.warn("Registration rejected, email already used: email={}",
                        user.getEmail());
                throw new MtsException("That email is already registered, please log in instead");
            }

            // the role is decided here, never accepted from the console
            user.setRole(ROLE_CUSTOMER);

            int id = userDao.addUser(user);
            logger.info("User registered: id={}, email={}, role={}",
                    id, user.getEmail(), user.getRole());

        } catch (SQLException e) {
            logger.error("register failed for email={}", user.getEmail(), e);
            throw new MtsException("Could not create the account, please try again", e);
        }
    }

    @Override
    public User login(String email, String password) throws MtsException {
        if (email == null || email.trim().isEmpty()) {
            throw new MtsException("Please enter your email");
        }
        if (password == null || password.isEmpty()) {
            throw new MtsException("Please enter your password");
        }
        String cleanEmail = email.trim().toLowerCase();
        try {
            User user = userDao.login(cleanEmail, password);
            if (user == null) {
                logger.warn("Failed login attempt for email={}", cleanEmail);
                throw new MtsException("Wrong email or password");
            }
            logger.info("Login successful: email={}, role={}", user.getEmail(), user.getRole());
            return user;

        } catch (SQLException e) {
            logger.error("login failed for email={}", cleanEmail, e);
            throw new MtsException("Could not log you in, please try again", e);
        }
    }

    @Override
    public User getUserByEmail(String email) throws MtsException {
        if (email == null || email.trim().isEmpty()) {
            throw new MtsException("Please enter an email address");
        }
        String cleanEmail = email.trim().toLowerCase();
        try {
            User user = userDao.getUserByEmail(cleanEmail);
            if (user == null) {
                throw new MtsException("No account found for " + cleanEmail);
            }
            return user;

        } catch (SQLException e) {
            logger.error("getUserByEmail failed for email={}", cleanEmail, e);
            throw new MtsException("Could not load the account, please try again", e);
        }
    }

    @Override
    public List<User> getAllUsers() throws MtsException {
        try {
            List<User> users = userDao.getAllUsers();
            if (users.isEmpty()) {
                throw new MtsException("No users have been registered yet");
            }
            return users;

        } catch (SQLException e) {
            logger.error("getAllUsers failed", e);
            throw new MtsException("Could not load the user list, please try again", e);
        }
    }

    @Override
    public void updateUser(User user) throws MtsException {
        validate(user);
        if (user.getUserId() <= 0) {
            throw new MtsException("The account to update was not loaded properly");
        }
        if (!ROLE_CUSTOMER.equals(user.getRole()) && !ROLE_ADMIN.equals(user.getRole())) {
            throw new MtsException("Role must be ADMIN or CUSTOMER");
        }
        try {
            if (!userDao.updateUser(user)) {
                logger.warn("Update matched no user: id={}", user.getUserId());
                throw new MtsException("That account no longer exists");
            }
            logger.info("User updated: id={}, email={}", user.getUserId(), user.getEmail());

        } catch (SQLException e) {
            logger.error("updateUser failed for id={}", user.getUserId(), e);
            throw new MtsException("Could not update the account, please try again", e);
        }
    }

    @Override
    public void deleteUser(String email) throws MtsException {
        if (email == null || email.trim().isEmpty()) {
            throw new MtsException("Please enter the email of the account to delete");
        }
        String cleanEmail = email.trim().toLowerCase();
        try {
            if (!userDao.deleteUser(cleanEmail)) {
                logger.warn("Delete matched no user: email={}", cleanEmail);
                throw new MtsException("No account found for " + cleanEmail);
            }
            logger.info("User deleted: email={}", cleanEmail);

        } catch (SQLException e) {
            logger.error("deleteUser failed for email={}", cleanEmail, e);
            throw new MtsException("Could not delete the account, it may have bookings", e);
        }
    }

    // US-01 - email valid and unused, phone exactly 10 digits, password at least 6 characters
    private void validate(User user) throws MtsException {
        if (user == null) {
            throw new MtsException("No account details were entered");
        }
        if (user.getName() == null || user.getName().trim().isEmpty()) {
            throw new MtsException("Name cannot be empty");
        }
        if (user.getEmail() == null || user.getEmail().trim().isEmpty()) {
            throw new MtsException("Email cannot be empty");
        }
        if (!isValidEmail(user.getEmail().trim())) {
            throw new MtsException("Please enter a valid email, for example kiran@mail.com");
        }
        if (user.getPhone() == null || !user.getPhone().trim().matches("\\d{10}")) {
            throw new MtsException("Phone must be exactly 10 digits");
        }
        if (user.getPassword() == null || user.getPassword().length() < MIN_PASSWORD_LENGTH) {
            throw new MtsException("Password must be at least "
                    + MIN_PASSWORD_LENGTH + " characters");
        }
        user.setName(user.getName().trim());
        user.setEmail(user.getEmail().trim().toLowerCase());
        user.setPhone(user.getPhone().trim());
    }

    // one @ with something on both sides, and a dot after the @
    private boolean isValidEmail(String email) {
        return email.matches("^[\\w.+-]+@[\\w-]+\\.[\\w.-]+$");
    }
}