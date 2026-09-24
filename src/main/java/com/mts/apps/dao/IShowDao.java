package com.mts.apps.dao;

import com.mts.apps.model.Movie;
import com.mts.apps.model.Show;
import com.mts.apps.model.Theatre;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public interface IShowDao {

    int addShow(Show show) throws SQLException;

    Show getShow(Theatre theatre, LocalDate showDate, String showSlot) throws SQLException;

    List<Show> getAllShows() throws SQLException;

    List<Show> getUpcomingShowsByMovie(Movie movie) throws SQLException;

    List<Show> getOverlappingShows(Theatre theatre, LocalDate showDate,
                                   LocalTime startTime, LocalTime endTime) throws SQLException;

    boolean deleteShow(Theatre theatre, LocalDate showDate, String showSlot) throws SQLException;
}