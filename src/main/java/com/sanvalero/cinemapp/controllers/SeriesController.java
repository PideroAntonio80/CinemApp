package com.sanvalero.cinemapp.controllers;

import com.sanvalero.cinemapp.domain.Serie;
import com.sanvalero.cinemapp.domain.Trailer;
import com.sanvalero.cinemapp.service.MovieService;
import com.sanvalero.cinemapp.util.AlertUtils;
import javafx.animation.PauseTransition;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.util.Duration;
import rx.schedulers.Schedulers;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

/**
 * Creado por @ author: Pedro Orós
 * el 30/03/2021
 */
public class SeriesController implements Initializable {

    public TextField tfSearch;
    public TableView<Serie> tvData;
    public ImageView ivImage;
    public Label lStatus;
    public Label lTitle;
    public Label lDate;
    public Label lVotes;
    public Label lRate;
    public TextArea taOverview;
    public ProgressBar pbLoading;
    public WebView wvTrailer;
    public VBox vbSeries;
    public VBox vbMovies;

    private WebEngine engine;

    private double zoom = 1;

    private MovieService movieService;

    private ObservableList<Serie> mySeries = FXCollections.observableArrayList();
    private ObservableList<Trailer> myTrailers = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        putTableColumnsSeries();

        engine = wvTrailer.getEngine();

        movieService = new MovieService();

        completeLoad();
    }

    @FXML
    public void all(ActionEvent event) {
        tvData.getItems().clear();
        completeLoad();
    }

    @FXML
    public void search(ActionEvent event) {
        String option = tfSearch.getText();

        if (option.equals("")) {
            AlertUtils.mostrarError("Debes rellenar el campo de texto");
            return;
        }

        //tvData.getItems().clear();

        loadingSeriesFilter(option);
    }

    @FXML
    public void showData() {
        Serie serie = tvData.getSelectionModel().getSelectedItem();

        Image image = null;

        lTitle.setWrapText(true);
        lTitle.setText(serie.getOriginal_name());
        lDate.setText(serie.getFirst_air_date());
        lVotes.setText(serie.getVote_count());
        lRate.setText(serie.getVote_average());
        taOverview.setWrapText(true);
        taOverview.setMaxSize(265, 400);
        taOverview.setText(serie.getOverview());

        try {
            URL urlBasePicture = new URL("https://image.tmdb.org/t/p/original" + serie.getPoster_path());

            BufferedInputStream in = new BufferedInputStream(urlBasePicture.openStream());

            image = new Image(in, 278, 228, false, false);
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
        ivImage.setImage(image);
    }

    @FXML
    public void trailer() {
        Serie serie = tvData.getSelectionModel().getSelectedItem();
        /*int serieId = serie.getId();
        String baseUrlTrailer = "https://www.youtube.com/watch?v=";

        movieService.getTrailerSerie(serieId)
                .map(SeriesTrailerResults::getResults)
                .flatMap(Observable::from)
                .doOnCompleted(() -> System.out.println("Trailer descargado"))
                .subscribeOn(Schedulers.from(Executors.newCachedThreadPool()))
                .subscribe(t -> myTrailers.add(t));

        engine.load(baseUrlTrailer + myTrailers.get(0).getKey());*/

        int serieId = serie.getId();

        String baseUrlTrailer = "https://www.youtube.com/watch?v=";
        String trailer = movieService.getTrailerSerie(serieId).get(0).getKey();
        engine.load(baseUrlTrailer + trailer);
    }

    @FXML
    public void zoomUp() {
        zoom += 0.1;
        wvTrailer.setZoom(zoom);
    }

    @FXML
    public void zoomDown() {
        zoom -= 0.1;
        wvTrailer.setZoom(zoom);
    }

    public void putTableColumnsSeries() {
        Field[] fields = Serie.class.getDeclaredFields();
        for (Field field : fields) {
            if (field.getName().equals("id") || field.getName().equals("overview") || field.getName().equals("poster_path"))
                continue;

            TableColumn<Serie, String> column = new TableColumn<>(field.getName());
            column.setCellValueFactory(new PropertyValueFactory<>(field.getName()));
            tvData.getColumns().add(column);
        }
        tvData.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }

    public void loadingSeries() {
        tvData.setItems(mySeries);

        movieService.getAllSeries()
                .doOnCompleted(() -> System.out.println("Listado de series descargado"))
                .subscribeOn(Schedulers.from(Executors.newCachedThreadPool()))
                .subscribe(sar -> {sar.getResults();
                    mySeries.addAll(sar.getResults());
                });
    }

    public void completeLoad() {
        pbLoading.setVisible(true);
        CompletableFuture.runAsync(() -> {
            pbLoading.setProgress(ProgressIndicator.INDETERMINATE_PROGRESS);
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            loadingSeries();})
                .whenComplete((string, throwable) -> pbLoading.setVisible(false));
    }

    public void loadingSeriesFilter(String optionSearch) {

        List<Serie> series = mySeries.stream()
                .filter(serie -> serie.getOriginal_name().contains(optionSearch))
                .distinct()
                .collect(Collectors.toList());

        tvData.setItems(FXCollections.observableArrayList(series));
    }

    public void transitionLabel(int segundos) {
        lStatus.setVisible(true);
        PauseTransition visiblePause = new PauseTransition((Duration.seconds(segundos)));
        visiblePause.setOnFinished(event -> lStatus.setVisible(false));
        visiblePause.play();
    }

    public void loadingInfo() {
        lStatus.setText("Recargando...");
        transitionLabel(2);
    }
}
