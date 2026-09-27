package com.mts.apps.service;

import com.mts.apps.dao.IMovieDao;
import com.mts.apps.dao.MovieDaoImpl;
import com.mts.apps.exception.MtsException;
import com.mts.apps.model.Movie;

import java.sql.SQLException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MovieServiceImpl implements IMovieService {

    private static final Logger logger = LoggerFactory.getLogger(MovieServiceImpl.class);

    private static final int MIN_DURATION = 30;
    private static final int MAX_DURATION = 300;

    private final IMovieDao movieDao;

    // the app uses this one - it creates the real DAO, exactly like before
    public MovieServiceImpl() {
        this(new MovieDaoImpl());
    }

    // the tests use this one - they pass in a Mockito fake DAO
    public MovieServiceImpl(IMovieDao movieDao) {
        this.movieDao = movieDao;
    }

    @Override
    public void addMovie(Movie movie) throws MtsException {
        validate(movie);
        try {
            if (movieDao.getMovieByTitle(movie.getTitle()) != null) {
                logger.warn("Duplicate movie rejected: title={}", movie.getTitle());
                throw new MtsException("A movie called " + movie.getTitle() + " already exists");
            }

            int id = movieDao.addMovie(movie);
            logger.info("Movie added: id={}, title={}, duration={}",
                    id, movie.getTitle(), movie.getDuration());

        } catch (SQLException e) {
            logger.error("addMovie failed for title={}", movie.getTitle(), e);
            throw new MtsException("Could not add the movie, please try again", e);
        }
    }

    @Override
    public Movie getMovieByTitle(String title) throws MtsException {
        if (title == null || title.trim().isEmpty()) {
            throw new MtsException("Please enter a movie title");
        }
        try {
            Movie movie = movieDao.getMovieByTitle(title.trim());
            if (movie == null) {
                throw new MtsException("No movie found with the title " + title);
            }
            return movie;

        } catch (SQLException e) {
            logger.error("getMovieByTitle failed for title={}", title, e);
            throw new MtsException("Could not load the movie, please try again", e);
        }
    }

    @Override
    public List<Movie> getAllMovies() throws MtsException {
        try {
            List<Movie> movies = movieDao.getAllMovies();
            if (movies.isEmpty()) {
                throw new MtsException("No movies have been added yet");
            }
            return movies;

        } catch (SQLException e) {
            logger.error("getAllMovies failed", e);
            throw new MtsException("Could not load the movie list, please try again", e);
        }
    }

    @Override
    public void updateMovie(Movie movie) throws MtsException {
        validate(movie);
        if (movie.getMovieId() <= 0) {
            throw new MtsException("The movie to update was not loaded properly");
        }
        try {
            if (!movieDao.updateMovie(movie)) {
                logger.warn("Update matched no movie: id={}", movie.getMovieId());
                throw new MtsException("That movie no longer exists");
            }
            logger.info("Movie updated: id={}, title={}", movie.getMovieId(), movie.getTitle());

        } catch (SQLException e) {
            logger.error("updateMovie failed for id={}", movie.getMovieId(), e);
            throw new MtsException("Could not update the movie, please try again", e);
        }
    }

    @Override
    public void deleteMovie(String title) throws MtsException {
        if (title == null || title.trim().isEmpty()) {
            throw new MtsException("Please enter the movie title to delete");
        }
        try {
            if (!movieDao.deleteMovie(title.trim())) {
                logger.warn("Delete matched no movie: title={}", title);
                throw new MtsException("No movie found with the title " + title);
            }
            logger.info("Movie deleted: title={}", title);

        } catch (SQLException e) {
            logger.error("deleteMovie failed for title={}", title, e);
            throw new MtsException("Could not delete the movie, it may have shows scheduled", e);
        }
    }

    // every field the user typed, checked before the database is touched
    private void validate(Movie movie) throws MtsException {
        if (movie == null) {
            throw new MtsException("No movie details were entered");
        }
        if (movie.getTitle() == null || movie.getTitle().trim().isEmpty()) {
            throw new MtsException("Title cannot be empty");
        }
        if (movie.getLanguage() == null || movie.getLanguage().trim().isEmpty()) {
            throw new MtsException("Language cannot be empty");
        }
        if (movie.getDuration() < MIN_DURATION || movie.getDuration() > MAX_DURATION) {
            throw new MtsException("Duration must be between " + MIN_DURATION
                    + " and " + MAX_DURATION + " minutes");
        }
        movie.setTitle(movie.getTitle().trim());
        movie.setLanguage(movie.getLanguage().trim());
    }
}