package com.sanvalero.cinemapp.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Creado por @ author: Pedro Orós
 * el 30/03/2021
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SeriesApiResults {

    private Integer page;
    private List<Serie> results;
    private Integer total_pages;
    private Integer total_results;
}
