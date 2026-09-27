package com.mts.apps.service;

import com.mts.apps.dao.IBookingDao;
import com.mts.apps.exception.MtsException;
import com.mts.apps.model.Booking;
import com.mts.apps.model.Movie;
import com.mts.apps.model.Seat;
import com.mts.apps.model.Show;
import com.mts.apps.model.Theatre;
import com.mts.apps.model.User;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class BookingServiceImplTest {

    @Mock
    private IBookingDao bookingDao;              // fake booking DAO

    @InjectMocks
    private BookingServiceImpl bookingService;   // real service, Mockito calls new BookingServiceImpl(bookingDao)

    private final User kiran = user(1, "Kiran");
    private final User meghana = user(2, "Meghana");
    private final Show tomorrowShow = kalkiShow(LocalDate.now().plusDays(1));

    private final Seat a1 = seat("A1", "250.00");
    private final Seat a2 = seat("A2", "250.00");
    private final Seat b1 = seat("B1", "150.00");

    // ---------------------------------------------------------------- bookSeats: happy path

    @Test
    public void bookSeats_twoFreeSeats_createsPendingBooking() throws Exception {
        when(bookingDao.getAvailableSeats(tomorrowShow)).thenReturn(Arrays.asList(a1, a2, b1));
        when(bookingDao.addBooking(any(Booking.class), eq(Arrays.asList(a1, a2)))).thenReturn(7);

        Booking booking = bookingService.bookSeats(kiran, tomorrowShow, Arrays.asList("a1", " A2 "));

        assertEquals(7, booking.getBookingId());
        assertEquals("PENDING", booking.getBookingStatus());
        assertEquals(new BigDecimal("500.00"), booking.getTotalAmount());   // 250 + 250
        assertSame(kiran, booking.getUser());
        assertSame(tomorrowShow, booking.getShow());
    }

    @Test
    public void bookSeats_sameSeatTypedTwice_isBookedOnce() throws Exception {
        when(bookingDao.getAvailableSeats(tomorrowShow)).thenReturn(Arrays.asList(a1, a2, b1));
        when(bookingDao.addBooking(any(Booking.class), eq(Collections.singletonList(a1)))).thenReturn(8);

        Booking booking = bookingService.bookSeats(kiran, tomorrowShow, Arrays.asList("A1", "a1"));

        assertEquals(new BigDecimal("250.00"), booking.getTotalAmount());
    }

    @Test
    public void bookSeats_exactly10Seats_isAccepted() throws Exception {
        List<Seat> tenSeats = new ArrayList<>();
        List<String> tenNumbers = new ArrayList<>();
        for (int i = 1; i <= 10; i++) {
            tenSeats.add(seat("C" + i, "150.00"));
            tenNumbers.add("C" + i);
        }
        when(bookingDao.getAvailableSeats(tomorrowShow)).thenReturn(tenSeats);
        when(bookingDao.addBooking(any(Booking.class), eq(tenSeats))).thenReturn(9);

        Booking booking = bookingService.bookSeats(kiran, tomorrowShow, tenNumbers);

        assertEquals(new BigDecimal("1500.00"), booking.getTotalAmount());   // 10 x 150
    }

    // ---------------------------------------------------------------- bookSeats: rejected

    @Test
    public void bookSeats_noSeatsChosen_isRejected() throws Exception {
        MtsException e = assertThrows(MtsException.class,
                () -> bookingService.bookSeats(kiran, tomorrowShow, Arrays.asList("", "  ")));

        assertEquals("Choose between 1 and 10 seats", e.getMessage());
        verifyNoInteractions(bookingDao);
    }

    @Test
    public void bookSeats_11Seats_isRejected() throws Exception {
        List<String> elevenNumbers = new ArrayList<>();
        for (int i = 1; i <= 11; i++) {
            elevenNumbers.add("C" + i);
        }

        MtsException e = assertThrows(MtsException.class,
                () -> bookingService.bookSeats(kiran, tomorrowShow, elevenNumbers));

        assertEquals("Choose between 1 and 10 seats", e.getMessage());
        verifyNoInteractions(bookingDao);
    }

    @Test
    public void bookSeats_seatAlreadyTaken_isRejected() throws Exception {
        when(bookingDao.getAvailableSeats(tomorrowShow)).thenReturn(Arrays.asList(a2, b1));   // A1 is gone

        MtsException e = assertThrows(MtsException.class,
                () -> bookingService.bookSeats(kiran, tomorrowShow, Arrays.asList("A1", "A2")));

        assertEquals("Seat A1 is not available for this show", e.getMessage());
        verify(bookingDao, never()).addBooking(any(), any());
    }

    @Test
    public void bookSeats_soldOutShow_isRejected() throws Exception {
        when(bookingDao.getAvailableSeats(tomorrowShow)).thenReturn(Collections.emptyList());

        MtsException e = assertThrows(MtsException.class,
                () -> bookingService.bookSeats(kiran, tomorrowShow, Arrays.asList("A1")));

        assertEquals("This show is sold out", e.getMessage());
        verify(bookingDao, never()).addBooking(any(), any());
    }

    @Test
    public void bookSeats_seatTakenAtTheLastSecond_givesClearMessage() throws Exception {
        // Kiran and Meghana both saw A1 free; Meghana's insert landed first,
        // so MySQL rejects Kiran's with error 1062 from UNIQUE(show_id, seat_id)
        when(bookingDao.getAvailableSeats(tomorrowShow)).thenReturn(Arrays.asList(a1, a2));
        when(bookingDao.addBooking(any(Booking.class), eq(Collections.singletonList(a1))))
                .thenThrow(new SQLException("Duplicate entry '3-1' for key 'booked_seats'", "23000", 1062));

        MtsException e = assertThrows(MtsException.class,
                () -> bookingService.bookSeats(kiran, tomorrowShow, Arrays.asList("A1")));

        assertEquals("One of those seats was just booked by someone else, please choose again",
                e.getMessage());
    }

    @Test
    public void bookSeats_databaseDown_givesFriendlyMessage() throws Exception {
        when(bookingDao.getAvailableSeats(tomorrowShow)).thenReturn(Arrays.asList(a1, a2));
        when(bookingDao.addBooking(any(Booking.class), eq(Collections.singletonList(a1))))
                .thenThrow(new SQLException("Communications link failure"));

        MtsException e = assertThrows(MtsException.class,
                () -> bookingService.bookSeats(kiran, tomorrowShow, Arrays.asList("A1")));

        assertEquals("Could not complete the booking, please try again", e.getMessage());
    }

    // ---------------------------------------------------------------- cancelBooking

    @Test
    public void cancelBooking_ownBookingBeforeTheShow_isCancelled() throws Exception {
        Booking booking = booking(kiran, tomorrowShow, "CONFIRMED");
        when(bookingDao.cancelBooking(booking)).thenReturn(true);

        bookingService.cancelBooking(kiran, booking);

        verify(bookingDao).cancelBooking(booking);
    }

    @Test
    public void cancelBooking_someoneElsesBooking_isRejected() throws Exception {
        Booking kiransBooking = booking(kiran, tomorrowShow, "CONFIRMED");

        MtsException e = assertThrows(MtsException.class,
                () -> bookingService.cancelBooking(meghana, kiransBooking));

        assertEquals("You can only cancel your own bookings", e.getMessage());
        verifyNoInteractions(bookingDao);
    }

    @Test
    public void cancelBooking_alreadyCancelled_isRejected() throws Exception {
        Booking cancelled = booking(kiran, tomorrowShow, "CANCELLED");

        MtsException e = assertThrows(MtsException.class,
                () -> bookingService.cancelBooking(kiran, cancelled));

        assertEquals("Booking 7 is already cancelled", e.getMessage());
        verifyNoInteractions(bookingDao);
    }

    @Test
    public void cancelBooking_showAlreadyStarted_isRejected() throws Exception {
        Show yesterdayShow = kalkiShow(LocalDate.now().minusDays(1));
        Booking oldBooking = booking(kiran, yesterdayShow, "CONFIRMED");

        MtsException e = assertThrows(MtsException.class,
                () -> bookingService.cancelBooking(kiran, oldBooking));

        assertEquals("This show has already started and cannot be cancelled", e.getMessage());
        verifyNoInteractions(bookingDao);
    }

    @Test
    public void cancelBooking_bookingGoneFromDatabase_isRejected() throws Exception {
        Booking booking = booking(kiran, tomorrowShow, "PENDING");
        when(bookingDao.cancelBooking(booking)).thenReturn(false);   // no row matched

        MtsException e = assertThrows(MtsException.class,
                () -> bookingService.cancelBooking(kiran, booking));

        assertEquals("That booking no longer exists", e.getMessage());
    }

    // ---------------------------------------------------------------- reading bookings

    @Test
    public void getMyBookings_none_isRejected() throws Exception {
        when(bookingDao.getBookingsByUser(kiran)).thenReturn(Collections.emptyList());

        MtsException e = assertThrows(MtsException.class, () -> bookingService.getMyBookings(kiran));

        assertEquals("You have no bookings yet", e.getMessage());
    }

    @Test
    public void getMyBookingsByStatus_noPending_isRejected() throws Exception {
        when(bookingDao.getBookingsByUserAndStatus(kiran, "PENDING")).thenReturn(Collections.emptyList());

        MtsException e = assertThrows(MtsException.class,
                () -> bookingService.getMyBookingsByStatus(kiran, " pending "));   // cleaned to PENDING

        assertEquals("You have no PENDING bookings", e.getMessage());
    }

    @Test
    public void getMyBookingsByStatus_unknownStatus_isRejected() throws Exception {
        MtsException e = assertThrows(MtsException.class,
                () -> bookingService.getMyBookingsByStatus(kiran, "DONE"));

        assertEquals("Status must be PENDING, CONFIRMED or CANCELLED", e.getMessage());
        verifyNoInteractions(bookingDao);
    }

    @Test
    public void getAllBookings_none_isRejected() throws Exception {
        when(bookingDao.getAllBookings()).thenReturn(Collections.emptyList());

        MtsException e = assertThrows(MtsException.class, () -> bookingService.getAllBookings());

        assertEquals("No bookings have been made yet", e.getMessage());
    }

    // ---------------------------------------------------------------- helpers

    private User user(int id, String name) {
        User u = new User();
        u.setUserId(id);
        u.setName(name);
        u.setEmail(name.toLowerCase() + "@mail.com");
        u.setRole("CUSTOMER");
        return u;
    }

    // Kalki 2898 AD at PVR Inorbit, FIRST_SHOW 18:30 to 21:30, on the given date
    private Show kalkiShow(LocalDate date) {
        Movie kalki = new Movie();
        kalki.setTitle("Kalki 2898 AD");
        kalki.setDuration(180);
        Theatre pvr = new Theatre();
        pvr.setTheatreId(1);
        pvr.setName("PVR Inorbit");

        Show s = new Show();
        s.setShowId(3);
        s.setMovie(kalki);
        s.setTheatre(pvr);
        s.setShowDate(date);
        s.setShowSlot("FIRST_SHOW");
        s.setStartTime(LocalTime.of(18, 30));
        s.setEndTime(LocalTime.of(21, 30));
        return s;
    }

    private Seat seat(String number, String price) {
        Seat s = new Seat();
        s.setSeatNumber(number);
        s.setSeatType("GOLD");
        s.setPrice(new BigDecimal(price));
        return s;
    }

    // booking #7, as it comes back from the database
    private Booking booking(User owner, Show show, String status) {
        Booking b = new Booking();
        b.setBookingId(7);
        b.setUser(owner);
        b.setShow(show);
        b.setTotalAmount(new BigDecimal("500.00"));
        b.setBookingStatus(status);
        return b;
    }
}