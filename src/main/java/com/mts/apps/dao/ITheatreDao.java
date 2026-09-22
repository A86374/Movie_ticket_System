package com.mts.apps.dao;

import com.mts.apps.model.Theatre;

import java.sql.SQLException;
import java.util.List;

public interface ITheatreDao {

    int addTheatre(Theatre theatre) throws SQLException;

    Theatre getTheatreById(int theatreId) throws SQLException;

    List<Theatre> getAllTheatres() throws SQLException;

    boolean updateTheatre(Theatre theatre) throws SQLException;

    boolean deleteTheatre(int theatreId) throws SQLException;
}
