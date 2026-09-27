package com.mts.apps.service;

import com.mts.apps.dao.ISeatDao;
import com.mts.apps.exception.MtsException;
import com.mts.apps.model.Seat;
import com.mts.apps.model.Theatre;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class SeatServiceImplTest {

    @Mock
    private ISeatDao seatDao;                   // fake seat DAO

    @Mock
    private ITheatreService theatreService;     // fake theatre service, for finding the theatre by name

    @InjectMocks
    private SeatServiceImpl seatService;        // real service, Mockito calls new SeatServiceImpl(seatDao, theatreService)

    private static final BigDecimal GOLD_PRICE = new BigDecimal("250.00");

    // ---------------------------------------------------------------- addSeatRow: happy path

    @Test
    public void addSeatRow_newRow_addsEverySeat() throws Exception {
        Theatre pvr = pvr(6);
        when(theatreService.getTheatreByName("PVR Inorbit")).thenReturn(pvr);
        when(seatDao.getSeatsByTheatre(pvr)).thenReturn(Collections.emptyList());   // no seats yet

        seatService.addSeatRow("PVR Inorbit", "A", 3, "GOLD", GOLD_PRICE);

        ArgumentCaptor<Seat> saved = ArgumentCaptor.forClass(Seat.class);   // catches all 3 seats
        verify(seatDao, times(3)).addSeat(saved.capture());
        List<Seat> seats = saved.getAllValues();
        assertEquals("A1", seats.get(0).getSeatNumber());
        assertEquals("A2", seats.get(1).getSeatNumber());
        assertEquals("A3", seats.get(2).getSeatNumber());
        for (Seat seat : seats) {
            assertSame(pvr, seat.getTheatre());
            assertEquals("GOLD", seat.getSeatType());
            assertEquals(GOLD_PRICE, seat.getPrice());
        }
    }

    @Test
    public void addSeatRow_smallLettersAndSpaces_areCleaned() throws Exception {
        Theatre pvr = pvr(6);
        when(theatreService.getTheatreByName("PVR Inorbit")).thenReturn(pvr);

        seatService.addSeatRow("PVR Inorbit", " b ", 2, " silver ", new BigDecimal("150.00"));

        ArgumentCaptor<Seat> saved = ArgumentCaptor.forClass(Seat.class);
        verify(seatDao, times(2)).addSeat(saved.capture());
        assertEquals("B1", saved.getAllValues().get(0).getSeatNumber());
        assertEquals("SILVER", saved.getAllValues().get(0).getSeatType());
    }

    @Test
    public void addSeatRow_fillsCapacityExactly_isAccepted() throws Exception {
        Theatre pvr = pvr(6);
        when(theatreService.getTheatreByName("PVR Inorbit")).thenReturn(pvr);
        when(seatDao.getSeatsByTheatre(pvr)).thenReturn(seats(pvr, "A1", "A2", "A3", "A4"));   // 4 of 6 used

        seatService.addSeatRow("PVR Inorbit", "B", 2, "SILVER", new BigDecimal("150.00"));

        verify(seatDao, times(2)).addSeat(any());
    }

    // ---------------------------------------------------------------- addSeatRow: rejected

    @Test
    public void addSeatRow_rowNotASingleLetter_isRejected() throws Exception {
        String[] badRows = {"AB", "1", "", "@"};

        for (String bad : badRows) {
            MtsException e = assertThrows("should reject row '" + bad + "'", MtsException.class,
                    () -> seatService.addSeatRow("PVR Inorbit", bad, 3, "GOLD", GOLD_PRICE));

            assertEquals("Row must be a single letter from A to Z", e.getMessage());
        }
        verifyNoInteractions(seatDao, theatreService);
    }

    @Test
    public void addSeatRow_zeroSeats_isRejected() throws Exception {
        MtsException e = assertThrows(MtsException.class,
                () -> seatService.addSeatRow("PVR Inorbit", "A", 0, "GOLD", GOLD_PRICE));

        assertEquals("A row needs at least one seat", e.getMessage());
        verifyNoInteractions(seatDao, theatreService);
    }

    @Test
    public void addSeatRow_unknownSeatType_isRejected() throws Exception {
        MtsException e = assertThrows(MtsException.class,
                () -> seatService.addSeatRow("PVR Inorbit", "A", 3, "DIAMOND", GOLD_PRICE));

        assertEquals("Seat type must be SILVER, GOLD or PLATINUM", e.getMessage());
        verifyNoInteractions(seatDao, theatreService);
    }

    @Test
    public void addSeatRow_zeroOrNegativePrice_isRejected() throws Exception {
        String[] badPrices = {"0", "-10.00"};

        for (String bad : badPrices) {
            MtsException e = assertThrows("should reject price " + bad, MtsException.class,
                    () -> seatService.addSeatRow("PVR Inorbit", "A", 3, "GOLD", new BigDecimal(bad)));

            assertEquals("Price must be more than zero", e.getMessage());
        }
        verifyNoInteractions(seatDao, theatreService);
    }

    @Test
    public void addSeatRow_rowAlreadyExists_isRejected() throws Exception {
        Theatre pvr = pvr(6);
        when(theatreService.getTheatreByName("PVR Inorbit")).thenReturn(pvr);
        when(seatDao.getSeatsByTheatre(pvr)).thenReturn(seats(pvr, "A1", "A2", "A3"));

        MtsException e = assertThrows(MtsException.class,
                () -> seatService.addSeatRow("PVR Inorbit", "A", 2, "GOLD", GOLD_PRICE));

        assertEquals("Row A already exists in PVR Inorbit", e.getMessage());
        verify(seatDao, never()).addSeat(any());
    }

    @Test
    public void addSeatRow_moreSeatsThanCapacity_isRejected() throws Exception {
        Theatre pvr = pvr(6);
        when(theatreService.getTheatreByName("PVR Inorbit")).thenReturn(pvr);
        when(seatDao.getSeatsByTheatre(pvr)).thenReturn(seats(pvr, "A1", "A2", "A3", "A4"));   // only 2 left

        MtsException e = assertThrows(MtsException.class,
                () -> seatService.addSeatRow("PVR Inorbit", "B", 3, "SILVER", new BigDecimal("150.00")));

        assertEquals("Only 2 more seats can be added to PVR Inorbit", e.getMessage());
        verify(seatDao, never()).addSeat(any());
    }

    @Test
    public void addSeatRow_unknownTheatre_passesTheTheatreErrorOn() throws Exception {
        when(theatreService.getTheatreByName("INOX GVK One"))
                .thenThrow(new MtsException("No theatre found with the name INOX GVK One"));

        MtsException e = assertThrows(MtsException.class,
                () -> seatService.addSeatRow("INOX GVK One", "A", 3, "GOLD", GOLD_PRICE));

        assertEquals("No theatre found with the name INOX GVK One", e.getMessage());
        verifyNoInteractions(seatDao);
    }

    @Test
    public void addSeatRow_databaseDown_givesFriendlyMessage() throws Exception {
        Theatre pvr = pvr(6);
        when(theatreService.getTheatreByName("PVR Inorbit")).thenReturn(pvr);
        when(seatDao.getSeatsByTheatre(pvr)).thenThrow(new SQLException("Communications link failure"));

        MtsException e = assertThrows(MtsException.class,
                () -> seatService.addSeatRow("PVR Inorbit", "A", 3, "GOLD", GOLD_PRICE));

        assertEquals("Could not add the seats, please try again", e.getMessage());
    }

    // ---------------------------------------------------------------- getSeatsByTheatre

    @Test
    public void getSeatsByTheatre_seatsExist_returnsThem() throws Exception {
        Theatre pvr = pvr(6);
        List<Seat> seats = seats(pvr, "A1", "A2");
        when(theatreService.getTheatreByName("PVR Inorbit")).thenReturn(pvr);
        when(seatDao.getSeatsByTheatre(pvr)).thenReturn(seats);

        assertSame(seats, seatService.getSeatsByTheatre("PVR Inorbit"));
    }

    @Test
    public void getSeatsByTheatre_noSeatsYet_isRejected() throws Exception {
        Theatre pvr = pvr(6);
        when(theatreService.getTheatreByName("PVR Inorbit")).thenReturn(pvr);
        when(seatDao.getSeatsByTheatre(pvr)).thenReturn(Collections.emptyList());

        MtsException e = assertThrows(MtsException.class, () -> seatService.getSeatsByTheatre("PVR Inorbit"));

        assertEquals("No seats have been added to PVR Inorbit yet", e.getMessage());
    }

    // ---------------------------------------------------------------- helpers

    private Theatre pvr(int capacity) {
        Theatre t = new Theatre();
        t.setTheatreId(1);
        t.setName("PVR Inorbit");
        t.setCity("Hyderabad");
        t.setTotalSeats(capacity);
        return t;
    }

    // seats already saved in the theatre, e.g. seats(pvr, "A1", "A2")
    private List<Seat> seats(Theatre theatre, String... numbers) {
        Seat[] list = new Seat[numbers.length];
        for (int i = 0; i < numbers.length; i++) {
            Seat s = new Seat();
            s.setTheatre(theatre);
            s.setSeatNumber(numbers[i]);
            s.setSeatType("GOLD");
            s.setPrice(GOLD_PRICE);
            list[i] = s;
        }
        return Arrays.asList(list);
    }
}