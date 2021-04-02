package com.sanvalero.cinemapp.domain;

import lombok.*;

/**
 * Creado por @ author: Pedro Orós
 * el 29/03/2021
 */

@Data
@Builder
public class Movie {

    private int id;
    private String original_title;
    private String release_date;
    private int vote_count;
    private double vote_average;
    private String overview;
    private String poster_path;
    private int[] genre_ids;
}
