package com.sanvalero.cinemapp.tasks;

import com.sanvalero.cinemapp.domain.Movie;
import com.sanvalero.cinemapp.service.MovieService;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.scene.control.TableView;

import java.util.List;

/**
 * Creado por @ author: Pedro Orós
 * el 31/03/2021
 */
public class LoadMoviesTask extends Task {

    private final TableView<Movie> tableView;

    private MovieService movieService;

    public LoadMoviesTask(TableView<Movie> tableView) {
        this.tableView = tableView;
    }

    @Override
    protected Integer call() throws Exception {
        List<Movie> movies = movieService.getAllMovies();
        tableView.setItems(FXCollections.observableArrayList(movies));
        return null;
    }
}
