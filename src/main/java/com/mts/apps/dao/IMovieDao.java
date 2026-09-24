package com.mts.apps.dao;

import com.mts.apps.model.Movie;

import java.sql.SQLException;
import java.util.List;

public interface IMovieDao {

    Movie getMovieByTitle(String title) throws SQLException;
    int addMovie(Movie movie) throws SQLException;

    List<Movie> getAllMovies() throws SQLException;

    boolean deleteMovie(String title) throws SQLException;

    boolean updateMovie(Movie movie) throws SQLException;


}
