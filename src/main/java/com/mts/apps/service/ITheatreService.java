package com.mts.apps.service;

import com.mts.apps.exception.MtsException;
import com.mts.apps.model.Theatre;

import java.util.List;

public interface ITheatreService {

    void addTheatre(Theatre theatre) throws MtsException;

    Theatre getTheatreByName(String name) throws MtsException;

    List<Theatre> getAllTheatres() throws MtsException;

    void updateTheatre(Theatre theatre) throws MtsException;

    void deleteTheatre(String name) throws MtsException;
}