package com.sanvalero.cinemapp.service;

import com.sanvalero.cinemapp.domain.*;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import rx.Observable;

import java.util.List;

/**
 * Creado por @ author: Pedro Orós
 * el 29/03/2021
 */
public interface MovieApiService {

    @GET("movie/popular?api_key=7e20756ea67b5217bad3146ba5b0c0e2&language=es-ES&page=1")
    Call<MoviesApiResults> getAllMovies();

    @GET("tv/popular?api_key=7e20756ea67b5217bad3146ba5b0c0e2&language=es-ES&page=1")
    Observable<SeriesApiResults> getAllSeries();

    @GET("genre/movie/list?api_key=7e20756ea67b5217bad3146ba5b0c0e2&language=es-ES")
    Observable<List<Genre>> getAllGenre();

    @GET("movie/{movie_id}/videos?api_key=7e20756ea67b5217bad3146ba5b0c0e2&language=en-US")
    Call<MoviesTrailerResults> getTrailerByIdMovie(@Path("movie_id") Integer id);

    @GET("tv/{tv_id}/videos?api_key=7e20756ea67b5217bad3146ba5b0c0e2&language=en-US")
    Call<SeriesTrailerResults> getTrailerByIdSerie(@Path("tv_id") Integer id);
}
