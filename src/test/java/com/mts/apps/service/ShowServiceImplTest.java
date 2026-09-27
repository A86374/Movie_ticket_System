package com.mts.apps.service;

import com.mts.apps.dao.ISeatDao;
import com.mts.apps.dao.IShowDao;
import com.mts.apps.exception.MtsException;
import com.mts.apps.model.Movie;
import com.mts.apps.model.Show;
import com.mts.apps.model.Theatre;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ShowServiceImplTest {

    @Mock
    private IShowDao showDao;                   // fake show DAO

    @Mock
    private ISeatDao seatDao;                   // fake seat DAO, for "does the theatre have seats"

    @Mock
    private IMovieService movieService;         // fake movie service, for finding the movie by title

    @Mock
    private ITheatreService theatreService;     // fake theatre service, for finding the theatre by name

    @InjectMocks
    private ShowServiceImpl showService;        // real service, built with all four fakes

    // the service refuses dates in the past, so every good show is scheduled for tomorrow
    private final LocalDate tomorrow = LocalDate.now().plusDays(1);

    // ---------------------------------------------------------------- scheduleShow: happy path

    @Test
    public void scheduleShow_validShow_isSavedWithCorrectTimes() throws Exception {
        Movie inception = movie("Inception", 148);
        Theatre pvr = pvr();
        when(movieService.getMovieByTitle("Inception")).thenReturn(inception);
        when(theatreService.getTheatreByName("PVR Inorbit")).thenReturn(pvr);
        when(seatDao.countSeatsByTheatre(pvr)).thenReturn(6);
        when(showDao.getOverlappingShows(pvr, tomorrow, LocalTime.of(18, 30), LocalTime.of(20, 58)))
                .thenReturn(Collections.emptyList());                        // no clash

        showService.scheduleShow("Inception", "PVR Inorbit", tomorrow, "FIRST_SHOW");

        ArgumentCaptor<Show> saved = ArgumentCaptor.forClass(Show.class);    // catch the Show the service built
        verify(showDao).addShow(saved.capture());
        Show show = saved.getValue();
        assertSame(inception, show.getMovie());
        assertSame(pvr, show.getTheatre());
        assertEquals(tomorrow, show.getShowDate());
        assertEquals("FIRST_SHOW", show.getShowSlot());
        assertEquals(LocalTime.of(18, 30), show.getStartTime());
        assertEquals(LocalTime.of(20, 58), show.getEndTime());             // 18:30 + 148 minutes
    }

    @Test
    public void scheduleShow_slotInSmallLetters_isAccepted() throws Exception {
        Movie inception = movie("Inception", 148);
        Theatre pvr = pvr();
        when(movieService.getMovieByTitle("Inception")).thenReturn(inception);
        when(theatreService.getTheatreByName("PVR Inorbit")).thenReturn(pvr);
        when(seatDao.countSeatsByTheatre(pvr)).thenReturn(6);

        showService.scheduleShow("Inception", "PVR Inorbit", tomorrow, "  matinee ");

        ArgumentCaptor<Show> saved = ArgumentCaptor.forClass(Show.class);
        verify(showDao).addShow(saved.capture());
        assertEquals("MATINEE", saved.getValue().getShowSlot());
        assertEquals(LocalTime.of(12, 0), saved.getValue().getStartTime());
    }

    // ---------------------------------------------------------------- scheduleShow: rejected

    @Test
    public void scheduleShow_unknownSlot_isRejected() throws Exception {
        MtsException e = assertThrows(MtsException.class,
                () -> showService.scheduleShow("Inception", "PVR Inorbit", tomorrow, "EVENING"));

        assertEquals("Slot must be MORNING, MATINEE, FIRST_SHOW or SECOND_SHOW", e.getMessage());
        verifyNoInteractions(movieService, theatreService, seatDao, showDao);
    }

    @Test
    public void scheduleShow_noDate_isRejected() throws Exception {
        MtsException e = assertThrows(MtsException.class,
                () -> showService.scheduleShow("Inception", "PVR Inorbit", null, "FIRST_SHOW"));

        assertEquals("Please enter a show date", e.getMessage());
        verifyNoInteractions(movieService, theatreService, seatDao, showDao);
    }

    @Test
    public void scheduleShow_dateInThePast_isRejected() throws Exception {
        LocalDate yesterday = LocalDate.now().minusDays(1);
        when(movieService.getMovieByTitle("Inception")).thenReturn(movie("Inception", 148));
        when(theatreService.getTheatreByName("PVR Inorbit")).thenReturn(pvr());

        MtsException e = assertThrows(MtsException.class,
                () -> showService.scheduleShow("Inception", "PVR Inorbit", yesterday, "FIRST_SHOW"));

        assertEquals("A show cannot be scheduled in the past", e.getMessage());
        verify(showDao, never()).addShow(any());
    }

    @Test
    public void scheduleShow_wouldEndAfterMidnight_isRejected() throws Exception {
        when(movieService.getMovieByTitle("Kalki 2898 AD")).thenReturn(movie("Kalki 2898 AD", 180));
        when(theatreService.getTheatreByName("PVR Inorbit")).thenReturn(pvr());

        MtsException e = assertThrows(MtsException.class,
                () -> showService.scheduleShow("Kalki 2898 AD", "PVR Inorbit", tomorrow, "SECOND_SHOW"));

        // 21:00 + 180 minutes = 00:00, the next day
        assertEquals("Kalki 2898 AD runs 180 minutes and would end after midnight in the SECOND_SHOW slot",
                e.getMessage());
        verify(showDao, never()).addShow(any());
    }

    @Test
    public void scheduleShow_theatreWithoutSeats_isRejected() throws Exception {
        Theatre pvr = pvr();
        when(movieService.getMovieByTitle("Inception")).thenReturn(movie("Inception", 148));
        when(theatreService.getTheatreByName("PVR Inorbit")).thenReturn(pvr);
        when(seatDao.countSeatsByTheatre(pvr)).thenReturn(0);

        MtsException e = assertThrows(MtsException.class,
                () -> showService.scheduleShow("Inception", "PVR Inorbit", tomorrow, "FIRST_SHOW"));

        assertEquals("Add seats to PVR Inorbit before scheduling a show there", e.getMessage());
        verify(showDao, never()).addShow(any());
    }

    @Test
    public void scheduleShow_clashWithLongerShowBefore_isRejected() throws Exception {
        Movie kalki = movie("Kalki 2898 AD", 180);
        Theatre pvr = pvr();
        Show kalkiFirstShow = show(kalki, pvr, "FIRST_SHOW", LocalTime.of(18, 30), LocalTime.of(21, 30));
        when(movieService.getMovieByTitle("Inception")).thenReturn(movie("Inception", 148));
        when(theatreService.getTheatreByName("PVR Inorbit")).thenReturn(pvr);
        when(seatDao.countSeatsByTheatre(pvr)).thenReturn(6);
        // Inception in SECOND_SHOW is 21:00 to 23:28, but Kalki is still running until 21:30
        when(showDao.getOverlappingShows(pvr, tomorrow, LocalTime.of(21, 0), LocalTime.of(23, 28)))
                .thenReturn(Collections.singletonList(kalkiFirstShow));

        MtsException e = assertThrows(MtsException.class,
                () -> showService.scheduleShow("Inception", "PVR Inorbit", tomorrow, "SECOND_SHOW"));

        assertEquals("Clashes with Kalki 2898 AD (FIRST_SHOW, 18:30 to 21:30)", e.getMessage());
        verify(showDao, never()).addShow(any());
    }

    @Test
    public void scheduleShow_unknownMovie_passesTheMovieErrorOn() throws Exception {
        when(movieService.getMovieByTitle("RajaRani"))
                .thenThrow(new MtsException("No movie found with the title RajaRani"));

        MtsException e = assertThrows(MtsException.class,
                () -> showService.scheduleShow("RajaRani", "PVR Inorbit", tomorrow, "FIRST_SHOW"));

        assertEquals("No movie found with the title RajaRani", e.getMessage());
        verifyNoInteractions(showDao);
    }

    @Test
    public void scheduleShow_databaseDown_givesFriendlyMessage() throws Exception {
        Theatre pvr = pvr();
        when(movieService.getMovieByTitle("Inception")).thenReturn(movie("Inception", 148));
        when(theatreService.getTheatreByName("PVR Inorbit")).thenReturn(pvr);
        when(seatDao.countSeatsByTheatre(pvr)).thenThrow(new SQLException("Communications link failure"));

        MtsException e = assertThrows(MtsException.class,
                () -> showService.scheduleShow("Inception", "PVR Inorbit", tomorrow, "FIRST_SHOW"));

        assertEquals("Could not schedule the show, please try again", e.getMessage());
    }

    // ---------------------------------------------------------------- getAllShows

    @Test
    public void getAllShows_showsExist_returnsAll() throws Exception {
        Theatre pvr = pvr();
        List<Show> shows = Arrays.asList(
                show(movie("Inception", 148), pvr, "MATINEE", LocalTime.of(12, 0), LocalTime.of(14, 28)),
                show(movie("Kalki 2898 AD", 180), pvr, "FIRST_SHOW", LocalTime.of(18, 30), LocalTime.of(21, 30)));
        when(showDao.getAllShows()).thenReturn(shows);

        assertEquals(2, showService.getAllShows().size());
    }

    @Test
    public void getAllShows_noShows_isRejected() throws Exception {
        when(showDao.getAllShows()).thenReturn(Collections.emptyList());

        MtsException e = assertThrows(MtsException.class, () -> showService.getAllShows());

        assertEquals("No shows have been scheduled yet", e.getMessage());
    }

    // ---------------------------------------------------------------- getUpcomingShows

    @Test
    public void getUpcomingShows_showsExist_returnsThem() throws Exception {
        Movie inception = movie("Inception", 148);
        List<Show> shows = Collections.singletonList(
                show(inception, pvr(), "FIRST_SHOW", LocalTime.of(18, 30), LocalTime.of(20, 58)));
        when(movieService.getMovieByTitle("Inception")).thenReturn(inception);
        when(showDao.getUpcomingShowsByMovie(inception)).thenReturn(shows);

        assertSame(shows, showService.getUpcomingShows("Inception"));
    }

    @Test
    public void getUpcomingShows_movieWithNoShows_isRejected() throws Exception {
        Movie rajaRani = movie("RajaRani", 145);
        when(movieService.getMovieByTitle("RajaRani")).thenReturn(rajaRani);
        when(showDao.getUpcomingShowsByMovie(rajaRani)).thenReturn(Collections.emptyList());

        MtsException e = assertThrows(MtsException.class, () -> showService.getUpcomingShows("RajaRani"));

        assertEquals("No upcoming shows for RajaRani", e.getMessage());
    }

    // ---------------------------------------------------------------- helpers

    private Movie movie(String title, int duration) {
        Movie m = new Movie();
        m.setTitle(title);
        m.setLanguage("Telugu");
        m.setDuration(duration);
        return m;
    }

    private Theatre pvr() {
        Theatre t = new Theatre();
        t.setTheatreId(1);
        t.setName("PVR Inorbit");
        t.setCity("Hyderabad");
        t.setTotalSeats(6);
        return t;
    }

    private Show show(Movie movie, Theatre theatre, String slot, LocalTime start, LocalTime end) {
        Show s = new Show();
        s.setMovie(movie);
        s.setTheatre(theatre);
        s.setShowDate(tomorrow);
        s.setShowSlot(slot);
        s.setStartTime(start);
        s.setEndTime(end);
        return s;
    }
}