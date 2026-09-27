package com.mts.apps.service;

import com.mts.apps.dao.ISeatDao;
import com.mts.apps.dao.IShowDao;
import com.mts.apps.dao.SeatDaoImpl;
import com.mts.apps.dao.ShowDaoImpl;
import com.mts.apps.exception.BusinessRuleException;
import com.mts.apps.exception.DatabaseException;
import com.mts.apps.exception.MtsException;
import com.mts.apps.exception.NotFoundException;
import com.mts.apps.exception.ValidationException;
import com.mts.apps.model.Movie;
import com.mts.apps.model.Show;
import com.mts.apps.model.Theatre;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ShowServiceImpl implements IShowService {

    private static final Logger logger = LoggerFactory.getLogger(ShowServiceImpl.class);

    // the four fixed screening slots and when each one starts
    private static final Map<String, LocalTime> SLOT_START = Map.of(
            "MORNING",     LocalTime.of(8, 30),
            "MATINEE",     LocalTime.of(12, 0),
            "FIRST_SHOW",  LocalTime.of(18, 30),
            "SECOND_SHOW", LocalTime.of(21, 0));

    private final IShowDao showDao;
    private final ISeatDao seatDao;
    private final IMovieService movieService;
    private final ITheatreService theatreService;

    // the app uses this one - it creates the real DAOs and services, exactly like before
    public ShowServiceImpl() {
        this(new ShowDaoImpl(), new SeatDaoImpl(), new MovieServiceImpl(), new TheatreServiceImpl());
    }

    // the tests use this one - they pass in Mockito fakes
    public ShowServiceImpl(IShowDao showDao, ISeatDao seatDao,
                           IMovieService movieService, ITheatreService theatreService) {
        this.showDao = showDao;
        this.seatDao = seatDao;
        this.movieService = movieService;
        this.theatreService = theatreService;
    }

    // FEATURE 7.2 - US-09, a show can never clash with another in the same theatre
    @Override
    public void scheduleShow(String movieTitle, String theatreName,
                             LocalDate showDate, String showSlot) throws MtsException {
        if (showDate == null) {
            throw new ValidationException("Please enter a show date");
        }
        String slot = checkSlot(showSlot);
        Movie movie = movieService.getMovieByTitle(movieTitle);
        Theatre theatre = theatreService.getTheatreByName(theatreName);

        LocalTime start = SLOT_START.get(slot);
        LocalTime end = start.plusMinutes(movie.getDuration());

        if (LocalDateTime.of(showDate, start).isBefore(LocalDateTime.now())) {
            throw new BusinessRuleException("A show cannot be scheduled in the past");
        }
        if (!end.isAfter(start)) {
            throw new BusinessRuleException(movie.getTitle() + " runs " + movie.getDuration()
                    + " minutes and would end after midnight in the " + slot + " slot");
        }

        try {
            if (seatDao.countSeatsByTheatre(theatre) == 0) {
                throw new BusinessRuleException("Add seats to " + theatre.getName()
                        + " before scheduling a show there");
            }

            List<Show> clashes = showDao.getOverlappingShows(theatre, showDate, start, end);
            if (!clashes.isEmpty()) {
                Show other = clashes.get(0);
                logger.warn("Show clash rejected: theatre={}, date={}, new={}-{}, existing={} {}-{}",
                        theatre.getName(), showDate, start, end,
                        other.getMovie().getTitle(), other.getStartTime(), other.getEndTime());
                throw new BusinessRuleException("Clashes with " + other.getMovie().getTitle()
                        + " (" + other.getShowSlot() + ", " + other.getStartTime()
                        + " to " + other.getEndTime() + ")");
            }

            Show show = new Show();
            show.setTheatre(theatre);
            show.setMovie(movie);
            show.setShowDate(showDate);
            show.setShowSlot(slot);
            show.setStartTime(start);
            show.setEndTime(end);

            showDao.addShow(show);
            logger.info("Show scheduled: {} at {} on {} {} ({}-{})",
                    movie.getTitle(), theatre.getName(), showDate, slot, start, end);

        } catch (SQLException e) {
            logger.error("scheduleShow failed: movie={}, theatre={}, date={}, slot={}",
                    movieTitle, theatreName, showDate, slot, e);
            throw new DatabaseException("Could not schedule the show, please try again", e);
        }
    }

    @Override
    public List<Show> getAllShows() throws MtsException {
        try {
            List<Show> shows = showDao.getAllShows();
            if (shows.isEmpty()) {
                throw new NotFoundException("No shows have been scheduled yet");
            }
            return shows;

        } catch (SQLException e) {
            logger.error("getAllShows failed", e);
            throw new DatabaseException("Could not load the shows, please try again", e);
        }
    }

    @Override
    public List<Show> getUpcomingShows(String movieTitle) throws MtsException {
        Movie movie = movieService.getMovieByTitle(movieTitle);
        try {
            List<Show> shows = showDao.getUpcomingShowsByMovie(movie);
            if (shows.isEmpty()) {
                throw new NotFoundException("No upcoming shows for " + movie.getTitle());
            }
            return shows;

        } catch (SQLException e) {
            logger.error("getUpcomingShows failed: movie={}", movieTitle, e);
            throw new DatabaseException("Could not load the shows, please try again", e);
        }
    }

    @Override
    public void deleteShow(String theatreName, LocalDate showDate,
                           String showSlot) throws MtsException {
        if (showDate == null) {
            throw new ValidationException("Please enter the show date");
        }
        String slot = checkSlot(showSlot);
        Theatre theatre = theatreService.getTheatreByName(theatreName);

        try {
            if (!showDao.deleteShow(theatre, showDate, slot)) {
                throw new NotFoundException("No " + slot + " show at " + theatre.getName()
                        + " on " + showDate);
            }
            logger.info("Show deleted: theatre={}, date={}, slot={}",
                    theatre.getName(), showDate, slot);

        } catch (SQLException e) {
            logger.error("deleteShow failed: theatre={}, date={}, slot={}",
                    theatreName, showDate, slot, e);
            throw new DatabaseException("Could not delete the show, it may already have bookings", e);
        }
    }

    private String checkSlot(String showSlot) throws MtsException {
        String slot = showSlot == null ? "" : showSlot.trim().toUpperCase();
        if (!SLOT_START.containsKey(slot)) {
            throw new ValidationException("Slot must be MORNING, MATINEE, FIRST_SHOW or SECOND_SHOW");
        }
        return slot;
    }
}