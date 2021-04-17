package com.sanvalero.cinemapp.service;

import com.sanvalero.cinemapp.domain.MoviesApiResults;
import com.sanvalero.cinemapp.domain.MoviesTrailerResults;
import com.sanvalero.cinemapp.domain.SeriesApiResults;
import com.sanvalero.cinemapp.domain.SeriesTrailerResults;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import rx.Observable;

/**
 * Creado por @ author: Pedro Orós
 * el 29/03/2021
 */
public interface MovieApiService {

    @GET("movie/popular?api_key=7e20756ea67b5217bad3146ba5b0c0e2&language=es-ES&page=1")
    Call<MoviesApiResults> getAllMovies();

    @GET("movie/{movie_id}/videos?api_key=7e20756ea67b5217bad3146ba5b0c0e2&language=en-US")
    Call<MoviesTrailerResults> getTrailerByIdMovie(@Path("movie_id") Integer id);

    @GET("tv/popular?api_key=7e20756ea67b5217bad3146ba5b0c0e2&language=es-ES&page=1")
    Observable<SeriesApiResults> getAllSeries();

    @GET("tv/{tv_id}/videos?api_key=7e20756ea67b5217bad3146ba5b0c0e2&language=en-US")
    Observable<SeriesTrailerResults> getTrailerByIdSerie(@Path("tv_id") Integer id);
}
