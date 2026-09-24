package com.mts.apps.dao;

import com.mts.apps.model.Movie;
import com.mts.apps.util.JdbcUtil;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class MovieDaoImpl implements IMovieDao {

    private static final String INSERT_MOVIE =
            "INSERT INTO movies (title, language, genre, duration, release_date) VALUES (?, ?, ?, ?, ?)";

    private static final String SELECT_MOVIE_BY_TITLE =
            "SELECT * FROM movies WHERE title = ?";

    private static final String SELECT_ALL_MOVIES =
            "SELECT * FROM movies ORDER BY title";

    private static final String UPDATE_MOVIE =
            "UPDATE movies SET title = ?, language = ?, genre = ?, duration = ?, release_date = ? WHERE movie_id = ?";

    private static final String DELETE_MOVIE_BY_TITLE =
            "DELETE FROM movies WHERE title = ?";

    private final JdbcUtil jdbcUtil = new JdbcUtil();

    // CREATE - saves a movie and returns the id MySQL gave it
    @Override
    public int addMovie(Movie movie) throws SQLException {
        try (Connection con = jdbcUtil.getConnectionObject();
             PreparedStatement ps = con.prepareStatement(INSERT_MOVIE, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, movie.getTitle());
            ps.setString(2, movie.getLanguage());
            ps.setString(3, movie.getGenre());
            ps.setInt(4, movie.getDuration());
            ps.setDate(5, movie.getReleaseDate() == null ? null : Date.valueOf(movie.getReleaseDate()));
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                return keys.next() ? keys.getInt(1) : 0;
            }
        }
    }

    // READ one - returns null when no movie has that title
    @Override
    public Movie getMovieByTitle(String title) throws SQLException {
        try (Connection con = jdbcUtil.getConnectionObject();
             PreparedStatement ps = con.prepareStatement(SELECT_MOVIE_BY_TITLE)) {

            ps.setString(1, title);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapRow(rs) : null;
            }
        }
    }

    // READ all
    @Override
    public List<Movie> getAllMovies() throws SQLException {
        List<Movie> movies = new ArrayList<>();
        try (Connection con = jdbcUtil.getConnectionObject();
             PreparedStatement ps = con.prepareStatement(SELECT_ALL_MOVIES);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                movies.add(mapRow(rs));
            }
        }
        return movies;
    }

    // UPDATE - true when exactly one row was changed
    @Override
    public boolean updateMovie(Movie movie) throws SQLException {
        try (Connection con = jdbcUtil.getConnectionObject();
             PreparedStatement ps = con.prepareStatement(UPDATE_MOVIE)) {

            ps.setString(1, movie.getTitle());
            ps.setString(2, movie.getLanguage());
            ps.setString(3, movie.getGenre());
            ps.setInt(4, movie.getDuration());
            ps.setDate(5, movie.getReleaseDate() == null ? null : Date.valueOf(movie.getReleaseDate()));
            ps.setInt(6, movie.getMovieId());
            return ps.executeUpdate() == 1;
        }
    }

    // DELETE - true when the movie was removed
    @Override
    public boolean deleteMovie(String title) throws SQLException {
        try (Connection con = jdbcUtil.getConnectionObject();
             PreparedStatement ps = con.prepareStatement(DELETE_MOVIE_BY_TITLE)) {

            ps.setString(1, title);
            return ps.executeUpdate() == 1;
        }
    }

    // turns the current row of the ResultSet into a Movie object
    private Movie mapRow(ResultSet rs) throws SQLException {
        Movie movie = new Movie();
        movie.setMovieId(rs.getInt("movie_id"));
        movie.setTitle(rs.getString("title"));
        movie.setLanguage(rs.getString("language"));
        movie.setGenre(rs.getString("genre"));
        movie.setDuration(rs.getInt("duration"));
        Date releaseDate = rs.getDate("release_date");
        movie.setReleaseDate(releaseDate == null ? null : releaseDate.toLocalDate());
        return movie;
    }
}