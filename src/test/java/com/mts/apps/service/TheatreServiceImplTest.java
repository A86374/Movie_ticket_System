package com.mts.apps.service;

import com.mts.apps.dao.ISeatDao;
import com.mts.apps.dao.ITheatreDao;
import com.mts.apps.exception.MtsException;
import com.mts.apps.model.Theatre;

import java.sql.SQLException;
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
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class TheatreServiceImplTest {

    @Mock
    private ITheatreDao theatreDao;          // fake theatre DAO

    @Mock
    private ISeatDao seatDao;                // fake seat DAO, used by the capacity check

    @InjectMocks
    private TheatreServiceImpl theatreService;   // real service, Mockito calls new TheatreServiceImpl(theatreDao, seatDao)

    // ---------------------------------------------------------------- addTheatre

    @Test
    public void addTheatre_validTheatre_isSaved() throws Exception {
        Theatre pvr = theatre("PVR Inorbit", "Hyderabad", 6);
        when(theatreDao.getTheatreByName("PVR Inorbit")).thenReturn(null);   // name not taken yet

        theatreService.addTheatre(pvr);

        verify(theatreDao).addTheatre(pvr);
    }

    @Test
    public void addTheatre_capacity1_isAccepted() throws Exception {
        Theatre tiny = theatre("Mini Screen", "Hyderabad", 1);

        theatreService.addTheatre(tiny);

        verify(theatreDao).addTheatre(tiny);
    }

    @Test
    public void addTheatre_nameAndCityWithSpaces_areTrimmed() throws Exception {
        Theatre amb = theatre("  AMB Cinemas  ", "  Hyderabad ", 4);

        theatreService.addTheatre(amb);

        assertEquals("AMB Cinemas", amb.getName());
        assertEquals("Hyderabad", amb.getCity());
        verify(theatreDao).getTheatreByName("AMB Cinemas");
    }

    @Test
    public void addTheatre_emptyName_isRejected() throws Exception {
        Theatre noName = theatre("   ", "Hyderabad", 6);

        MtsException e = assertThrows(MtsException.class, () -> theatreService.addTheatre(noName));

        assertEquals("Theatre name cannot be empty", e.getMessage());
        verifyNoInteractions(theatreDao);
    }

    @Test
    public void addTheatre_emptyCity_isRejected() throws Exception {
        Theatre noCity = theatre("PVR Inorbit", "", 6);

        MtsException e = assertThrows(MtsException.class, () -> theatreService.addTheatre(noCity));

        assertEquals("City cannot be empty", e.getMessage());
        verifyNoInteractions(theatreDao);
    }

    @Test
    public void addTheatre_zeroOrNegativeCapacity_isRejected() throws Exception {
        int[] badCapacities = {0, -5};

        for (int bad : badCapacities) {
            Theatre t = theatre("PVR Inorbit", "Hyderabad", bad);

            MtsException e = assertThrows("should reject capacity " + bad, MtsException.class,
                    () -> theatreService.addTheatre(t));

            assertEquals("Total seats must be more than zero", e.getMessage());
        }
        verifyNoInteractions(theatreDao);
    }

    @Test
    public void addTheatre_duplicateName_isRejected() throws Exception {
        Theatre pvrAgain = theatre("PVR Inorbit", "Hyderabad", 6);
        when(theatreDao.getTheatreByName("PVR Inorbit"))
                .thenReturn(theatre("PVR Inorbit", "Hyderabad", 6));   // already in the DB

        MtsException e = assertThrows(MtsException.class, () -> theatreService.addTheatre(pvrAgain));

        assertEquals("A theatre called PVR Inorbit already exists", e.getMessage());
        verify(theatreDao, never()).addTheatre(any());
    }

    @Test
    public void addTheatre_databaseDown_givesFriendlyMessage() throws Exception {
        Theatre pvr = theatre("PVR Inorbit", "Hyderabad", 6);
        when(theatreDao.getTheatreByName("PVR Inorbit"))
                .thenThrow(new SQLException("Communications link failure"));

        MtsException e = assertThrows(MtsException.class, () -> theatreService.addTheatre(pvr));

        assertEquals("Could not add the theatre, please try again", e.getMessage());
    }

    // ---------------------------------------------------------------- getTheatreByName

    @Test
    public void getTheatreByName_existingName_returnsTheatre() throws Exception {
        Theatre pvr = theatre("PVR Inorbit", "Hyderabad", 6);
        when(theatreDao.getTheatreByName("PVR Inorbit")).thenReturn(pvr);

        assertSame(pvr, theatreService.getTheatreByName("PVR Inorbit"));
    }

    @Test
    public void getTheatreByName_unknownName_isRejected() throws Exception {
        when(theatreDao.getTheatreByName("INOX GVK One")).thenReturn(null);

        MtsException e = assertThrows(MtsException.class,
                () -> theatreService.getTheatreByName("INOX GVK One"));

        assertEquals("No theatre found with the name INOX GVK One", e.getMessage());
    }

    @Test
    public void getTheatreByName_emptyName_isRejected() throws Exception {
        MtsException e = assertThrows(MtsException.class, () -> theatreService.getTheatreByName("  "));

        assertEquals("Please enter a theatre name", e.getMessage());
        verifyNoInteractions(theatreDao);
    }

    // ---------------------------------------------------------------- getAllTheatres

    @Test
    public void getAllTheatres_theatresExist_returnsAll() throws Exception {
        List<Theatre> theatres = Arrays.asList(
                theatre("PVR Inorbit", "Hyderabad", 6), theatre("AMB Cinemas", "Hyderabad", 4));
        when(theatreDao.getAllTheatres()).thenReturn(theatres);

        assertEquals(2, theatreService.getAllTheatres().size());
    }

    @Test
    public void getAllTheatres_noTheatres_isRejected() throws Exception {
        when(theatreDao.getAllTheatres()).thenReturn(Collections.emptyList());

        MtsException e = assertThrows(MtsException.class, () -> theatreService.getAllTheatres());

        assertEquals("No theatres have been added yet", e.getMessage());
    }

    // ---------------------------------------------------------------- updateTheatre: capacity vs seats

    @Test
    public void updateTheatre_capacityBelowExistingSeats_isRejected() throws Exception {
        Theatre pvr = savedPvr(4);                               // admin tries to shrink to 4
        when(seatDao.countSeatsByTheatre(pvr)).thenReturn(6);    // but 6 seats already exist

        MtsException e = assertThrows(MtsException.class, () -> theatreService.updateTheatre(pvr));

        assertEquals("PVR Inorbit already has 6 seats, capacity cannot be less than that",
                e.getMessage());
        verify(theatreDao, never()).updateTheatre(any());
    }

    @Test
    public void updateTheatre_capacityEqualToSeats_isAccepted() throws Exception {
        Theatre pvr = savedPvr(6);
        when(seatDao.countSeatsByTheatre(pvr)).thenReturn(6);
        when(theatreDao.updateTheatre(pvr)).thenReturn(true);

        theatreService.updateTheatre(pvr);

        verify(theatreDao).updateTheatre(pvr);
    }

    @Test
    public void updateTheatre_notLoadedFromDatabase_isRejected() throws Exception {
        Theatre neverSaved = theatre("PVR Inorbit", "Hyderabad", 6);   // id is still 0

        MtsException e = assertThrows(MtsException.class, () -> theatreService.updateTheatre(neverSaved));

        assertEquals("The theatre to update was not loaded properly", e.getMessage());
        verifyNoInteractions(theatreDao, seatDao);
    }

    @Test
    public void updateTheatre_deletedMeanwhile_isRejected() throws Exception {
        Theatre pvr = savedPvr(6);
        when(theatreDao.updateTheatre(pvr)).thenReturn(false);   // no row matched

        MtsException e = assertThrows(MtsException.class, () -> theatreService.updateTheatre(pvr));

        assertEquals("That theatre no longer exists", e.getMessage());
    }

    // ---------------------------------------------------------------- helpers

    // a theatre filled in the way the Add theatre menu would fill it
    private Theatre theatre(String name, String city, int totalSeats) {
        Theatre t = new Theatre();
        t.setName(name);
        t.setCity(city);
        t.setAddress("Inorbit Mall, Madhapur");
        t.setTotalSeats(totalSeats);
        return t;
    }

    // PVR Inorbit as it comes back from the database, with its id
    private Theatre savedPvr(int totalSeats) {
        Theatre t = theatre("PVR Inorbit", "Hyderabad", totalSeats);
        t.setTheatreId(1);
        return t;
    }
}