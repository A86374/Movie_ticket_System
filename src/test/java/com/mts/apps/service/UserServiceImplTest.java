package com.mts.apps.service;

import com.mts.apps.dao.IUserDao;
import com.mts.apps.exception.MtsException;
import com.mts.apps.model.User;

import java.sql.SQLException;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class UserServiceImplTest {

    @Mock
    private IUserDao userDao;              // fake DAO, never touches MySQL

    @InjectMocks
    private UserServiceImpl userService;   // real service, Mockito calls new UserServiceImpl(userDao)

    // ---------------------------------------------------------------- register: happy path

    @Test
    public void register_validUser_isSavedAsCustomer() throws Exception {
        User kiran = kiran();
        when(userDao.getUserByEmail("kiran@mail.com")).thenReturn(null);   // email not used yet

        userService.register(kiran);

        assertEquals("CUSTOMER", kiran.getRole());
        verify(userDao).addUser(kiran);
    }

    @Test
    public void register_userAsksForAdmin_stillBecomesCustomer() throws Exception {
        User sneaky = kiran();
        sneaky.setRole("ADMIN");

        userService.register(sneaky);

        assertEquals("CUSTOMER", sneaky.getRole());
    }

    @Test
    public void register_emailWithCapitalsAndSpaces_isCleaned() throws Exception {
        User meghana = user("Meghana", "  Meghana.K@Gmail.com  ", "9123456780", "meghana1");

        userService.register(meghana);

        assertEquals("meghana.k@gmail.com", meghana.getEmail());
        verify(userDao).getUserByEmail("meghana.k@gmail.com");
    }

    @Test
    public void register_password6Characters_isAccepted() throws Exception {
        User sixChars = user("Kiran", "kiran@mail.com", "9876543210", "abc123");

        userService.register(sixChars);

        verify(userDao).addUser(sixChars);
    }

    // ---------------------------------------------------------------- register: rejected input

    @Test
    public void register_emptyName_isRejected() throws Exception {
        User noName = user("   ", "kiran@mail.com", "9876543210", "kiran123");

        MtsException e = assertThrows(MtsException.class, () -> userService.register(noName));

        assertEquals("Name cannot be empty", e.getMessage());
        verifyNoInteractions(userDao);
    }

    @Test
    public void register_badEmailFormats_areRejected() throws Exception {
        String[] badEmails = {"kiran@mail", "kiran.mail.com", "kiran @mail.com", "@mail.com"};

        for (String bad : badEmails) {
            User u = user("Kiran", bad, "9876543210", "kiran123");

            MtsException e = assertThrows("should reject " + bad, MtsException.class,
                    () -> userService.register(u));

            assertEquals("Please enter a valid email, for example kiran@mail.com", e.getMessage());
        }
        verifyNoInteractions(userDao);
    }

    @Test
    public void register_phoneNot10Digits_isRejected() throws Exception {
        String[] badPhones = {"98765", "98765432101", "98765abcde"};

        for (String bad : badPhones) {
            User u = user("Kiran", "kiran@mail.com", bad, "kiran123");

            MtsException e = assertThrows("should reject " + bad, MtsException.class,
                    () -> userService.register(u));

            assertEquals("Phone must be exactly 10 digits", e.getMessage());
        }
        verifyNoInteractions(userDao);
    }

    @Test
    public void register_passwordShorterThan6_isRejected() throws Exception {
        User shortPassword = user("Kiran", "kiran@mail.com", "9876543210", "abc12");

        MtsException e = assertThrows(MtsException.class, () -> userService.register(shortPassword));

        assertEquals("Password must be at least 6 characters", e.getMessage());
        verifyNoInteractions(userDao);
    }

    @Test
    public void register_duplicateEmail_isRejected() throws Exception {
        User kiranAgain = kiran();
        when(userDao.getUserByEmail("kiran@mail.com")).thenReturn(kiran());   // already registered

        MtsException e = assertThrows(MtsException.class, () -> userService.register(kiranAgain));

        assertEquals("That email is already registered, please log in instead", e.getMessage());
        verify(userDao, never()).addUser(kiranAgain);
    }

    @Test
    public void register_databaseDown_givesFriendlyMessage() throws Exception {
        User kiran = kiran();
        when(userDao.getUserByEmail("kiran@mail.com"))
                .thenThrow(new SQLException("Communications link failure"));

        MtsException e = assertThrows(MtsException.class, () -> userService.register(kiran));

        assertEquals("Could not create the account, please try again", e.getMessage());
    }

    // ---------------------------------------------------------------- login

    @Test
    public void login_correctDetails_returnsUser() throws Exception {
        User kiran = kiran();
        when(userDao.login("kiran@mail.com", "kiran123")).thenReturn(kiran);

        assertSame(kiran, userService.login("kiran@mail.com", "kiran123"));
    }

    @Test
    public void login_emailWithCapitalsAndSpaces_stillWorks() throws Exception {
        User kiran = kiran();
        when(userDao.login("kiran@mail.com", "kiran123")).thenReturn(kiran);

        assertSame(kiran, userService.login("  Kiran@Mail.com ", "kiran123"));
    }

    @Test
    public void login_wrongPassword_isRejected() throws Exception {
        when(userDao.login("kiran@mail.com", "wrong99")).thenReturn(null);

        MtsException e = assertThrows(MtsException.class,
                () -> userService.login("kiran@mail.com", "wrong99"));

        assertEquals("Wrong email or password", e.getMessage());
    }

    @Test
    public void login_password_isPassedExactlyAsTyped() throws Exception {
        when(userDao.login("kiran@mail.com", "Kiran123")).thenReturn(null);

        assertThrows(MtsException.class, () -> userService.login("kiran@mail.com", "Kiran123"));

        verify(userDao).login("kiran@mail.com", "Kiran123");   // capital K kept, never lowercased
    }

    @Test
    public void login_emptyEmail_isRejected() throws Exception {
        MtsException e = assertThrows(MtsException.class, () -> userService.login("  ", "kiran123"));

        assertEquals("Please enter your email", e.getMessage());
        verifyNoInteractions(userDao);
    }

    @Test
    public void login_emptyPassword_isRejected() throws Exception {
        MtsException e = assertThrows(MtsException.class, () -> userService.login("kiran@mail.com", ""));

        assertEquals("Please enter your password", e.getMessage());
        verifyNoInteractions(userDao);
    }

    @Test
    public void login_databaseDown_givesFriendlyMessage() throws Exception {
        when(userDao.login("kiran@mail.com", "kiran123"))
                .thenThrow(new SQLException("Communications link failure"));

        MtsException e = assertThrows(MtsException.class,
                () -> userService.login("kiran@mail.com", "kiran123"));

        assertEquals("Could not log you in, please try again", e.getMessage());
    }

    // ---------------------------------------------------------------- helpers

    private User kiran() {
        return user("Kiran", "kiran@mail.com", "9876543210", "kiran123");
    }

    // a user filled in the way the Register menu would fill it
    private User user(String name, String email, String phone, String password) {
        User u = new User();
        u.setName(name);
        u.setEmail(email);
        u.setPhone(phone);
        u.setPassword(password);
        return u;
    }
}