package com.mts.apps.dao;

import com.mts.apps.model.Movie;
import com.mts.apps.model.Show;
import com.mts.apps.model.Theatre;
import com.mts.apps.util.JdbcUtil;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Time;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class ShowDaoImpl implements IShowDao {

    // every show query needs theatre and movie columns, so the select list is shared
    private static final String SELECT_BASE =
            "SELECT sh.show_id, sh.show_date, sh.show_slot, sh.start_time, sh.end_time, "
                    + "t.theatre_id, t.name, t.city, t.address, t.total_seats, "
                    + "m.movie_id, m.title, m.language, m.genre, m.duration, m.release_date "
                    + "FROM shows sh "
                    + "JOIN theatres t ON sh.theatre_id = t.theatre_id "
                    + "JOIN movies m ON sh.movie_id = m.movie_id ";

    private static final String INSERT_SHOW =
            "INSERT INTO shows (theatre_id, movie_id, show_date, show_slot, start_time, end_time) "
                    + "VALUES (?, ?, ?, ?, ?, ?)";

    private static final String SELECT_SHOW =
            SELECT_BASE + "WHERE sh.theatre_id = ? AND sh.show_date = ? AND sh.show_slot = ?";

    private static final String SELECT_ALL_SHOWS =
            SELECT_BASE + "ORDER BY sh.show_date, sh.start_time";

    private static final String SELECT_UPCOMING_SHOWS_BY_MOVIE =
            SELECT_BASE
                    + "WHERE sh.movie_id = ? "
                    + "AND (sh.show_date > CURDATE() OR (sh.show_date = CURDATE() AND sh.start_time > CURTIME())) "
                    + "ORDER BY sh.show_date, sh.start_time";

    private static final String SELECT_OVERLAPPING_SHOWS =
            SELECT_BASE
                    + "WHERE sh.theatre_id = ? AND sh.show_date = ? "
                    + "AND sh.start_time < ? AND sh.end_time > ?";

    private static final String DELETE_SHOW =
            "DELETE FROM shows WHERE theatre_id = ? AND show_date = ? AND show_slot = ?";

    private final JdbcUtil jdbcUtil = new JdbcUtil();

    // CREATE - saves a show and returns the id MySQL gave it
    @Override
    public int addShow(Show show) throws SQLException {
        try (Connection con = jdbcUtil.getConnectionObject();
             PreparedStatement ps = con.prepareStatement(INSERT_SHOW, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, show.getTheatre().getTheatreId());
            ps.setInt(2, show.getMovie().getMovieId());
            ps.setDate(3, Date.valueOf(show.getShowDate()));
            ps.setString(4, show.getShowSlot());
            ps.setTime(5, Time.valueOf(show.getStartTime()));
            ps.setTime(6, Time.valueOf(show.getEndTime()));
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                return keys.next() ? keys.getInt(1) : 0;
            }
        }
    }

    // READ one - theatre + date + slot is the natural key of a show
    @Override
    public Show getShow(Theatre theatre, LocalDate showDate, String showSlot) throws SQLException {
        try (Connection con = jdbcUtil.getConnectionObject();
             PreparedStatement ps = con.prepareStatement(SELECT_SHOW)) {

            ps.setInt(1, theatre.getTheatreId());
            ps.setDate(2, Date.valueOf(showDate));
            ps.setString(3, showSlot);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapRow(rs) : null;
            }
        }
    }

    // READ all - admin view
    @Override
    public List<Show> getAllShows() throws SQLException {
        List<Show> shows = new ArrayList<>();
        try (Connection con = jdbcUtil.getConnectionObject();
             PreparedStatement ps = con.prepareStatement(SELECT_ALL_SHOWS);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                shows.add(mapRow(rs));
            }
        }
        return shows;
    }

    // READ - only the shows of this movie that have not started yet
    @Override
    public List<Show> getUpcomingShowsByMovie(Movie movie) throws SQLException {
        List<Show> shows = new ArrayList<>();
        try (Connection con = jdbcUtil.getConnectionObject();
             PreparedStatement ps = con.prepareStatement(SELECT_UPCOMING_SHOWS_BY_MOVIE)) {

            ps.setInt(1, movie.getMovieId());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    shows.add(mapRow(rs));
                }
            }
        }
        return shows;
    }

    // READ - shows in this theatre on this date that clash with the given time range
    @Override
    public List<Show> getOverlappingShows(Theatre theatre, LocalDate showDate,
                                          LocalTime startTime, LocalTime endTime) throws SQLException {
        List<Show> shows = new ArrayList<>();
        try (Connection con = jdbcUtil.getConnectionObject();
             PreparedStatement ps = con.prepareStatement(SELECT_OVERLAPPING_SHOWS)) {

            ps.setInt(1, theatre.getTheatreId());
            ps.setDate(2, Date.valueOf(showDate));
            ps.setTime(3, Time.valueOf(endTime));     // existing start < new end
            ps.setTime(4, Time.valueOf(startTime));   // existing end   > new start
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    shows.add(mapRow(rs));
                }
            }
        }
        return shows;
    }

    // DELETE - true when the show was removed
    @Override
    public boolean deleteShow(Theatre theatre, LocalDate showDate, String showSlot) throws SQLException {
        try (Connection con = jdbcUtil.getConnectionObject();
             PreparedStatement ps = con.prepareStatement(DELETE_SHOW)) {

            ps.setInt(1, theatre.getTheatreId());
            ps.setDate(2, Date.valueOf(showDate));
            ps.setString(3, showSlot);
            return ps.executeUpdate() == 1;
        }
    }

    // turns the current row into a Show with its Theatre and Movie filled in
    private Show mapRow(ResultSet rs) throws SQLException {
        Theatre theatre = new Theatre();
        theatre.setTheatreId(rs.getInt("theatre_id"));
        theatre.setName(rs.getString("name"));
        theatre.setCity(rs.getString("city"));
        theatre.setAddress(rs.getString("address"));
        theatre.setTotalSeats(rs.getInt("total_seats"));

        Movie movie = new Movie();
        movie.setMovieId(rs.getInt("movie_id"));
        movie.setTitle(rs.getString("title"));
        movie.setLanguage(rs.getString("language"));
        movie.setGenre(rs.getString("genre"));
        movie.setDuration(rs.getInt("duration"));
        Date releaseDate = rs.getDate("release_date");
        movie.setReleaseDate(releaseDate == null ? null : releaseDate.toLocalDate());

        Show show = new Show();
        show.setShowId(rs.getInt("show_id"));
        show.setTheatre(theatre);
        show.setMovie(movie);
        show.setShowDate(rs.getDate("show_date").toLocalDate());
        show.setShowSlot(rs.getString("show_slot"));
        show.setStartTime(rs.getTime("start_time").toLocalTime());
        show.setEndTime(rs.getTime("end_time").toLocalTime());
        return show;
    }
}