package com.sanvalero.cinemapp.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Creado por @ author: Pedro Orós
 * el 31/03/2021
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MoviesTrailerResults {
    private int id;
    private List<Trailer> results;
}
