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

    private final JdbcUtil jdbcUtil = new JdbcUtil();

    // CREATE - saves a movie and returns the id MySQL gave it
    @Override
    public int addMovie(Movie movie) throws SQLException {
        String sql = "INSERT INTO movies (title, language, genre, duration, release_date) VALUES (?, ?, ?, ?, ?)";
        try (Connection con = jdbcUtil.getConnectionObject();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

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

    // READ one - returns null when the id does not exist
    @Override
    public Movie getMovieById(int movieId) throws SQLException {
        String sql = "SELECT * FROM movies WHERE movie_id = ?";
        try (Connection con = jdbcUtil.getConnectionObject();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, movieId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapRow(rs) : null;
            }
        }
    }

    // READ all
    @Override
    public List<Movie> getAllMovies() throws SQLException {
        List<Movie> movies = new ArrayList<>();
        String sql = "SELECT * FROM movies ORDER BY title";
        try (Connection con = jdbcUtil.getConnectionObject();
             PreparedStatement ps = con.prepareStatement(sql);
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
        String sql = "UPDATE movies SET title = ?, language = ?, genre = ?, duration = ?, release_date = ? "
                + "WHERE movie_id = ?";
        try (Connection con = jdbcUtil.getConnectionObject();
             PreparedStatement ps = con.prepareStatement(sql)) {

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
    public boolean deleteMovie(int movieId) throws SQLException {
        String sql = "DELETE FROM movies WHERE movie_id = ?";
        try (Connection con = jdbcUtil.getConnectionObject();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, movieId);
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
