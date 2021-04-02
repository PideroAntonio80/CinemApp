package com.sanvalero.cinemapp.domain;

import lombok.*;

/**
 * Creado por @ author: Pedro Orós
 * el 30/03/2021
 */

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class Serie {

    private int id;
    private String original_name;
    private String first_air_date;
    private String vote_count;
    private String vote_average;
    private String overview;
    private String poster_path;
}
