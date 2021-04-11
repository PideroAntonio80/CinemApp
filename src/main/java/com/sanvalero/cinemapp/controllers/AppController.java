package com.sanvalero.cinemapp.controllers;

import com.sanvalero.cinemapp.domain.Movie;
import com.sanvalero.cinemapp.service.MovieService;
import com.sanvalero.cinemapp.util.AlertUtils;
import com.sanvalero.cinemapp.util.R;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
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

import java.io.*;
import java.lang.reflect.Field;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.Comparator;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

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
                loadingInfo("Recargando...");

                break;

            case "Puntos(Descendente)":
                CompletableFuture.runAsync(this::loadingByRateDesc);
                loadingInfo("Recargando...");

                break;

            case "Votos (Ascendente)":
                CompletableFuture.runAsync(this::loadingByVotesAsc);
                loadingInfo("Recargando...");

                break;

            case "Puntos (Ascendente)":
                CompletableFuture.runAsync(this::loadingByRateAsc);
                loadingInfo("Recargando...");

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

            image = new Image(in, 289, 238, false, false);
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
        export();
        loadingInfo("Datos transferidos");
    }

    @FXML
    public void exportToZip(ActionEvent Event) throws ExecutionException, InterruptedException {
        File file = export();

        CompletableFuture.supplyAsync(() -> file.getAbsolutePath().concat(".zip"))
                .thenAccept(System.out::println)
                .whenComplete((unused, throwable) -> {
                    System.out.println("Archivo .zip generado en: " + file.getAbsolutePath().concat(".zip"));
                    Platform.runLater(() -> {
                        compressToZip(file);
                        loadingInfo("Datos comprimidos");
                    });
                }).get();
    }

    public File export() {
        File file = null;
        FileChooser fileChooser = new FileChooser();
        file = fileChooser.showSaveDialog(null);

        try {
            FileWriter fileWriter = new FileWriter(file + ".csv");
            CSVPrinter printer = new CSVPrinter(fileWriter, CSVFormat.TDF.withHeader("Nombre;", "Fecha;", "Votos;","Puntos;"));
            for (Movie movie : moviesList) {
                printer.printRecord(movie.getOriginal_title(), ';', movie.getRelease_date(), ';', movie.getVote_count(), ';', movie.getVote_average());
            }
            printer.close();

        } catch (IOException ioe) {
            ioe.printStackTrace();
            AlertUtils.mostrarError("Error al exportar los datos");
        }
        return file;
    }

    public void compressToZip(File file) {

        try {
            FileOutputStream fos = new FileOutputStream(file.getAbsolutePath().concat(".zip"));
            ZipOutputStream zos = new ZipOutputStream(fos);
            FileInputStream fis = new FileInputStream(file.getAbsolutePath().concat(".csv"));
            ZipEntry zipEntry = new ZipEntry(file.getName().concat(".csv"));

            zos.putNextEntry(zipEntry);

            byte[] bytes = new byte[1024];
            int length;
            while ((length = fis.read(bytes)) >=0){
                zos.write(bytes, 0, length);
            }
            zos.close();
            fis.close();
            fos.close();

            Files.delete(Path.of(file.getAbsolutePath().concat(".csv")));

        } catch (IOException ex) {
            AlertUtils.mostrarError("Error al exportar  Zip");
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

        moviesList = moviesByVotes;

        tvData.setItems(FXCollections.observableArrayList(moviesByVotes));
    }

    public void loadingByRateDesc() {
        List<Movie> movies = movieService.getAllMovies();
        List<Movie> moviesByRate = movies.stream()
                .sorted(Comparator.comparing(Movie::getVote_average).reversed())
                .collect(Collectors.toList());

        moviesList = moviesByRate;

        tvData.setItems(FXCollections.observableArrayList(moviesByRate));
    }

    public void loadingByVotesAsc() {
        List<Movie> movies = movieService.getAllMovies();
        List<Movie> moviesByVotes = movies.stream()
                .sorted(Comparator.comparing(Movie::getVote_count))
                .collect(Collectors.toList());

        moviesList = moviesByVotes;

        tvData.setItems(FXCollections.observableArrayList(moviesByVotes));
    }

    public void loadingByRateAsc() {
        List<Movie> movies = movieService.getAllMovies();
        List<Movie> moviesByRate = movies.stream()
                .sorted(Comparator.comparing(Movie::getVote_average))
                .collect(Collectors.toList());

        moviesList = moviesByRate;

        tvData.setItems(FXCollections.observableArrayList(moviesByRate));
    }

    public void transitionLabel(int segundos) {
        lStatus.setVisible(true);
        PauseTransition visiblePause = new PauseTransition((Duration.seconds(segundos)));
        visiblePause.setOnFinished(event -> lStatus.setVisible(false));
        visiblePause.play();
    }

    public void loadingInfo(String message) {
        lStatus.setText(message);
        transitionLabel(2);
    }

}
