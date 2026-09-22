package com.mts.apps.service;

import com.mts.apps.exception.MtsException;
import com.mts.apps.model.Seat;
import com.mts.apps.model.Show;
import java.util.List;

/** Show timings. */
public interface IShowService {

    /** Sets the end time from the movie duration, rejects a past time, a theatre with no seats, and any timing that overlaps another show in the same theatre. */
    int addShow(Show show) throws MtsException;

    /** One show with its movie and theatre. */
    Show getShowById(int showId) throws MtsException;

    /** Every show. */
    List<Show> getAllShows() throws MtsException;

    /** Upcoming timings of one movie. */
    List<Show> getShowsByMovie(int movieId) throws MtsException;

    /** Same checks as add. */
    boolean updateShow(Show show) throws MtsException;

    /** Removes a show that has no bookings. */
    boolean deleteShow(int showId) throws MtsException;

    /** Seats of the show that are not held by an active booking. */
    List<Seat> getAvailableSeats(int showId) throws MtsException;

}
