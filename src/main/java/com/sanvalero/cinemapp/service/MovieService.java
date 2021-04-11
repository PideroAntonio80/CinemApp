package com.sanvalero.cinemapp.service;

import com.sanvalero.cinemapp.domain.*;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import rx.Observable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.sanvalero.cinemapp.util.Constants.URL;

/**
 * Creado por @ author: Pedro Orós
 * el 29/03/2021
 */
public class MovieService {

    private MovieApiService api;

    public MovieService() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(URL)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .build();

        api = retrofit.create(MovieApiService.class);
    }

    public List<Movie> getAllMovies() {
        Call<MoviesApiResults> allMovies = api.getAllMovies();
        try {
            Response<MoviesApiResults> response = allMovies.execute();
            if (response.body() != null) {
                return new ArrayList<>(response.body().getResults());
            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
        return null;
    }

    public Observable<SeriesApiResults> getAllSeries() {
        return api.getAllSeries();
    }

    public Observable<List<Genre>> getAllGenre() {
        return api.getAllGenre();
    }

    public List<Trailer> getTrailerMovie(int id) {
        Call<MoviesTrailerResults> trailer = api.getTrailerByIdMovie(id);
        try {
            Response<MoviesTrailerResults> response = trailer.execute();
            if (response.body() != null) {
                return new ArrayList<>(response.body().getResults());
            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
        return null;
    }

    public List<Trailer> getTrailerSerie(int id) {
        Call<SeriesTrailerResults> trailer = api.getTrailerByIdSerie(id);
        try {
            Response<SeriesTrailerResults> response = trailer.execute();
            if (response.body() != null) {
                return new ArrayList<>(response.body().getResults());
            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
        return null;
    }
}
