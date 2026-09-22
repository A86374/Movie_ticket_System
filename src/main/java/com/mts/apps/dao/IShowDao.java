package com.mts.apps.dao;

import com.mts.apps.model.Show;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public interface IShowDao {

    int addShow(Show show) throws SQLException;

    Show getShowById(int showId) throws SQLException;

    List<Show> getAllShows() throws SQLException;

    boolean updateShow(Show show) throws SQLException;

    boolean deleteShow(int showId) throws SQLException;

    /** Upcoming shows of one movie, used for the movie timings screen. */
    List<Show> getShowsByMovie(int movieId) throws SQLException;

    /** True when the timing clashes with another show in the same theatre on that date. */
    boolean hasOverlap(int theatreId, LocalDate date, LocalTime start, LocalTime end) throws SQLException;
}
