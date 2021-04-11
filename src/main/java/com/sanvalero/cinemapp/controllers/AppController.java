package com.sanvalero.cinemapp.controllers;

import com.sanvalero.cinemapp.domain.Movie;
import com.sanvalero.cinemapp.service.MovieService;
import com.sanvalero.cinemapp.util.AlertUtils;
import com.sanvalero.cinemapp.util.R;
import javafx.animation.PauseTransition;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.URL;
import java.sql.SQLException;
import java.util.Comparator;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * Creado por @ author: Pedro Orós
 * el 29/03/2021
 */
public class AppController implements Initializable {

    public ComboBox<String> cbChoose;
    public TableView<Movie> tvData;
    public ImageView ivImage;
    public Label lStatus;
    public Label lTitle;
    public Label lDate;
    public Label lVotes;
    public Label lRate;
    public TextArea taOverview;
    public ProgressBar pbLoading;
    public WebView wvTrailer;
    public VBox vbMovies;
    public VBox vbSeries;

    private WebEngine engine;

    private double zoom = 1;

    private List<Movie> moviesList;

    private MovieService movieService;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        putTableColumnsMovies();

        String[] options = new String[]{"<<Fitrar>>", "Lista Películas", "Votos (Descendente)", "Puntos(Descendente)", "Votos (Ascendente)", "Puntos (Ascendente)"};
        cbChoose.setValue("<<Fitrar>>");
        cbChoose.setItems(FXCollections.observableArrayList(options));

        wvTrailer.setZoom(zoom);
        engine = wvTrailer.getEngine();

        movieService = new MovieService();

        CompletableFuture.runAsync(() -> {
            //lStatus.setText("Cargando Peliculas...");
            pbLoading.setProgress(ProgressIndicator.INDETERMINATE_PROGRESS);
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            loadingMovies();})
                .whenComplete((string, throwable) -> pbLoading.setVisible(false));
                /*.whenComplete((string, throwable) -> {
                    lStatus.setText("Carga Finalizada");
                    transitionLabel(2);});*/
    }

    @FXML
    public void start(ActionEvent event) {
        tvData.getItems().clear();
        String option = cbChoose.getValue();

        switch (option) {

            case "Lista Películas":
                pbLoading.setVisible(true);
                CompletableFuture.runAsync(() -> {
                    pbLoading.setProgress(ProgressIndicator.INDETERMINATE_PROGRESS);
                    try {
                        Thread.sleep(3000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    loadingMovies();})
                        .whenComplete((string, throwable) -> pbLoading.setVisible(false));

                break;

            case "Votos (Descendente)":
                CompletableFuture.runAsync(this::loadingByVotesDesc);
                loadingInfo();

                break;

            case "Puntos(Descendente)":
                CompletableFuture.runAsync(this::loadingByRateDesc);
                loadingInfo();

                break;

            case "Votos (Ascendente)":
                CompletableFuture.runAsync(this::loadingByVotesAsc);
                loadingInfo();

                break;

            case "Puntos (Ascendente)":
                CompletableFuture.runAsync(this::loadingByRateAsc);
                loadingInfo();

                break;

            default:
                AlertUtils.mostrarInformacion("Debes elegir una opción");

                break;
        }

    }

    @FXML
    public void showData() {
        Movie movie = tvData.getSelectionModel().getSelectedItem();

        Image image = null;

        lTitle.setWrapText(true);
        lTitle.setText(movie.getOriginal_title());
        lDate.setText(movie.getRelease_date());
        lVotes.setText(String.valueOf(movie.getVote_count()));
        lRate.setText(String.valueOf(movie.getVote_average()));
        taOverview.setWrapText(true);
        taOverview.setText(movie.getOverview());

        try {
            URL urlBasePicture = new URL("https://image.tmdb.org/t/p/original" + movie.getPoster_path());

            BufferedInputStream in = new BufferedInputStream(urlBasePicture.openStream());

            image = new Image(in, 336, 285, false, false);
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
        ivImage.setImage(image);
    }

    @FXML
    public void series(ActionEvent event) {
        try {
            Stage stage = new Stage();
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(R.getUI("seriesapp.fxml"));
            loader.setController(new SeriesController());
            VBox vBox = loader.load();

            Scene scene = new Scene(vBox);
            stage.setScene(scene);
            stage.show();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    @FXML
    public void trailer() {
        Movie movie = tvData.getSelectionModel().getSelectedItem();

        int movieId = movie.getId();

        String baseUrlTrailer = "https://www.youtube.com/watch?v=";
        String trailer = movieService.getTrailerMovie(movieId).get(0).getKey();
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

    @FXML
    public void export(ActionEvent Event) {
        try {
            FileChooser fileChooser = new FileChooser();
            File file = fileChooser.showSaveDialog(null);
            FileWriter fileWriter = new FileWriter(file);

            CSVPrinter printer = new CSVPrinter(fileWriter, CSVFormat.TDF.withHeader("Nombre;", "Fecha;", "Votos;","Puntos;", "Sinopsis;"));
            for (Movie movie : moviesList) {
                printer.printRecord(movie.getOriginal_title(), ';', movie.getRelease_date(), ';', movie.getVote_count(), ';', movie.getVote_average(), ';', movie.getOverview());
            }
            printer.close();
            lStatus.setText("Datos transferidos");
            transitionLabel(2);

        } catch (IOException ioe) {
            AlertUtils.mostrarError("Error al exportar los datos");
        }
    }

    public void putTableColumnsMovies() {
        Field[] fields = Movie.class.getDeclaredFields();
        for (Field field : fields) {
            if (field.getName().equals("id") || field.getName().equals("overview") || field.getName().equals("poster_path") || field.getName().equals("genre_ids"))
                continue;

            TableColumn<Movie, String> column = new TableColumn<>(field.getName());
            column.setCellValueFactory(new PropertyValueFactory<>(field.getName()));
            tvData.getColumns().add(column);
        }
        tvData.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }

    public void loadingMovies() {
        List<Movie> movies = movieService.getAllMovies();

        moviesList = movies;

        tvData.setItems(FXCollections.observableArrayList(movies));
    }

    public void loadingByVotesDesc() {
        List<Movie> movies = movieService.getAllMovies();
        List<Movie> moviesByVotes = movies.stream()
                .sorted(Comparator.comparing(Movie::getVote_count).reversed())
                .collect(Collectors.toList());

        moviesList = movies;

        tvData.setItems(FXCollections.observableArrayList(moviesByVotes));
    }

    public void loadingByRateDesc() {
        List<Movie> movies = movieService.getAllMovies();
        List<Movie> moviesByRate = movies.stream()
                //.filter(movie -> movie.getVote_average() > 0)
                .sorted(Comparator.comparing(Movie::getVote_average).reversed())
                .collect(Collectors.toList());

        moviesList = movies;

        tvData.setItems(FXCollections.observableArrayList(moviesByRate));
    }

    public void loadingByVotesAsc() {
        List<Movie> movies = movieService.getAllMovies();
        List<Movie> moviesByVotes = movies.stream()
                .sorted(Comparator.comparing(Movie::getVote_count))
                .collect(Collectors.toList());

        moviesList = movies;

        tvData.setItems(FXCollections.observableArrayList(moviesByVotes));
    }

    public void loadingByRateAsc() {
        List<Movie> movies = movieService.getAllMovies();
        List<Movie> moviesByRate = movies.stream()
                .sorted(Comparator.comparing(Movie::getVote_average))
                .collect(Collectors.toList());

        moviesList = movies;

        tvData.setItems(FXCollections.observableArrayList(moviesByRate));
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
