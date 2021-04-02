package com.sanvalero.cinemapp.controllers;

import com.sanvalero.cinemapp.domain.*;
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

import java.io.BufferedInputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import javafx.util.Duration;
import rx.Observable;
import rx.schedulers.Schedulers;

/**
 * Creado por @ author: Pedro Orós
 * el 30/03/2021
 */
public class SeriesController implements Initializable {

    public ComboBox<String> cbChoose;
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

        String[] options = new String[]{"<<Fitrar>>", "Lista Series", "Votos (Mayor a menor)", "Puntos (Mayor a menor)"};
        cbChoose.setValue("<<Fitrar>>");
        cbChoose.setItems(FXCollections.observableArrayList(options));

        engine = wvTrailer.getEngine();

        movieService = new MovieService();

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

    @FXML
    public void start(ActionEvent event) {
        String option = cbChoose.getValue();

        switch (option) {

            case "Lista Series":
                tvData.getItems().clear();
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

                break;

            case "Votos (Mayor a menor)":
                tvData.getItems().clear();
                CompletableFuture.runAsync(this::loadingByVotes);
                loadingInfo();

                break;

            case "Puntos (Mayor a menor)":
                tvData.getItems().clear();
                CompletableFuture.runAsync(this::loadingByRate);
                loadingInfo();

                break;

            default:
                AlertUtils.mostrarInformacion("Debes elegir una opción");

                break;
        }
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

            image = new Image(in, 336, 285, false, false);
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
        System.out.println(serieId);

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

    //TODO progressBar. Aquí y en pelis, ver si esta bien
    //TODO Más filtros
    //TODO Más Rx
    //TODO Revisar enunciado Actividad Aprendizaje de PSP para ver qué falta

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
        //List<Serie> series = null;

        movieService.getAllSeries()
                .doOnCompleted(() -> System.out.println("Listado de series descargado"))
                .subscribeOn(Schedulers.from(Executors.newCachedThreadPool()))
                .subscribe(sar -> {sar.getResults();
                    mySeries.addAll(sar.getResults());
                });
    }

    public void loadingByVotes() {
        tvData.setItems(mySeries);
        //List<Serie> series = null;
        Comparator<Serie> votesComparator = Comparator.comparing(Serie::getVote_count);

        movieService.getAllSeries()
                .map(SeriesApiResults::getResults)
                .doOnCompleted(() -> System.out.println("Listado de series descargado"))
                .subscribeOn(Schedulers.from(Executors.newCachedThreadPool()))
                .subscribe(ls -> {
                    mySeries.sort(votesComparator);
                    mySeries.addAll();
                });
    }

    public void loadingByRate() {
        tvData.setItems(mySeries);
        //List<Serie> series = null;

        movieService.getAllSeries()
                .doOnCompleted(() -> System.out.println("Listado de series descargado"))
                .subscribeOn(Schedulers.from(Executors.newCachedThreadPool()))
                .subscribe(sar -> {sar.getResults();
                    mySeries.addAll(sar.getResults());
                    mySeries.stream().sorted(Comparator.comparing(Serie::getVote_average).reversed());
                });
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
