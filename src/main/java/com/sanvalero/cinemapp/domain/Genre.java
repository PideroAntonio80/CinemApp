package com.sanvalero.cinemapp.domain;

import lombok.*;

/**
 * Creado por @ author: Pedro Orós
 * el 30/03/2021
 */

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class Genre {

    private int id;
    private String name;
}
