package com.mts.apps.service;

import com.mts.apps.dao.IMovieDao;
import com.mts.apps.exception.MtsException;
import com.mts.apps.model.Movie;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
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
public class MovieServiceImplTest {

    @Mock
    private IMovieDao movieDao;              // fake DAO, never touches MySQL

    @InjectMocks
    private MovieServiceImpl movieService;   // real service, Mockito calls new MovieServiceImpl(movieDao)

    // ---------------------------------------------------------------- addMovie

    @Test
    public void addMovie_validMovie_isSaved() throws Exception {
        Movie kalki = movie("Kalki 2898 AD", 180);
        when(movieDao.getMovieByTitle("Kalki 2898 AD")).thenReturn(null);   // title not taken yet

        movieService.addMovie(kalki);

        verify(movieDao).addMovie(kalki);
    }

    @Test
    public void addMovie_duration30_isAccepted() throws Exception {
        Movie shortFilm = movie("Short Film", 30);

        movieService.addMovie(shortFilm);

        verify(movieDao).addMovie(shortFilm);
    }

    @Test
    public void addMovie_durationBelow30_isRejected() throws Exception {
        Movie tooShort = movie("Short Film", 29);

        MtsException e = assertThrows(MtsException.class, () -> movieService.addMovie(tooShort));

        assertEquals("Duration must be between 30 and 300 minutes", e.getMessage());
        verify(movieDao, never()).addMovie(any());
    }

    @Test
    public void addMovie_durationAbove300_isRejected() throws Exception {
        Movie tooLong = movie("Long Film", 301);

        MtsException e = assertThrows(MtsException.class, () -> movieService.addMovie(tooLong));

        assertEquals("Duration must be between 30 and 300 minutes", e.getMessage());
        verify(movieDao, never()).addMovie(any());
    }

    @Test
    public void addMovie_emptyTitle_isRejected() throws Exception {
        Movie noTitle = movie("   ", 148);

        MtsException e = assertThrows(MtsException.class, () -> movieService.addMovie(noTitle));

        assertEquals("Title cannot be empty", e.getMessage());
        verifyNoInteractions(movieDao);
    }

    @Test
    public void addMovie_duplicateTitle_isRejected() throws Exception {
        Movie inception = movie("Inception", 148);
        when(movieDao.getMovieByTitle("Inception")).thenReturn(movie("Inception", 148));   // already in the DB

        MtsException e = assertThrows(MtsException.class, () -> movieService.addMovie(inception));

        assertEquals("A movie called Inception already exists", e.getMessage());
        verify(movieDao, never()).addMovie(any());
    }

    @Test
    public void addMovie_titleWithSpaces_isTrimmed() throws Exception {
        Movie inception = movie("  Inception  ", 148);

        movieService.addMovie(inception);

        assertEquals("Inception", inception.getTitle());
        verify(movieDao).getMovieByTitle("Inception");
    }

    @Test
    public void addMovie_databaseDown_givesFriendlyMessage() throws Exception {
        Movie inception = movie("Inception", 148);
        when(movieDao.getMovieByTitle("Inception"))
                .thenThrow(new SQLException("Communications link failure"));

        MtsException e = assertThrows(MtsException.class, () -> movieService.addMovie(inception));

        assertEquals("Could not add the movie, please try again", e.getMessage());
    }

    // ---------------------------------------------------------------- getMovieByTitle

    @Test
    public void getMovieByTitle_existingTitle_returnsMovie() throws Exception {
        Movie inception = movie("Inception", 148);
        when(movieDao.getMovieByTitle("Inception")).thenReturn(inception);

        assertSame(inception, movieService.getMovieByTitle("Inception"));
    }

    @Test
    public void getMovieByTitle_unknownTitle_isRejected() throws Exception {
        when(movieDao.getMovieByTitle("RajaRani")).thenReturn(null);

        MtsException e = assertThrows(MtsException.class, () -> movieService.getMovieByTitle("RajaRani"));

        assertEquals("No movie found with the title RajaRani", e.getMessage());
    }

    @Test
    public void getMovieByTitle_emptyTitle_isRejected() throws Exception {
        MtsException e = assertThrows(MtsException.class, () -> movieService.getMovieByTitle("  "));

        assertEquals("Please enter a movie title", e.getMessage());
        verifyNoInteractions(movieDao);
    }

    // ---------------------------------------------------------------- getAllMovies

    @Test
    public void getAllMovies_moviesExist_returnsAll() throws Exception {
        List<Movie> movies = Arrays.asList(movie("Inception", 148), movie("Kalki 2898 AD", 180));
        when(movieDao.getAllMovies()).thenReturn(movies);

        assertEquals(2, movieService.getAllMovies().size());
    }

    @Test
    public void getAllMovies_noMovies_isRejected() throws Exception {
        when(movieDao.getAllMovies()).thenReturn(Collections.emptyList());

        MtsException e = assertThrows(MtsException.class, () -> movieService.getAllMovies());

        assertEquals("No movies have been added yet", e.getMessage());
    }

    // ---------------------------------------------------------------- helper

    // a movie with every field filled in, the way the admin would type it
    private Movie movie(String title, int duration) {
        Movie m = new Movie();
        m.setTitle(title);
        m.setLanguage("Telugu");
        m.setGenre("Sci-Fi");
        m.setDuration(duration);
        m.setReleaseDate(LocalDate.of(2024, 6, 27));
        return m;
    }
}