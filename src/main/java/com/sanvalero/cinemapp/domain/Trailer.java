package com.sanvalero.cinemapp.domain;

import lombok.*;

/**
 * Creado por @ author: Pedro Orós
 * el 31/03/2021
 */

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class Trailer {
    private String key;
    private String name;
    private String type;
}
