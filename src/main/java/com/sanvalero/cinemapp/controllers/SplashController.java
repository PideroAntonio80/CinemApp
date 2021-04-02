package com.sanvalero.cinemapp.controllers;

import com.sanvalero.cinemapp.util.R;
import javafx.animation.FadeTransition;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

/**
 * Creado por @ author: Pedro Orós
 * el 11/12/2020
 */
public class SplashController implements Initializable {
    public VBox vbSplash;
    public MediaView mvVideo;

    private MediaPlayer mediaPlayer;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        String urlVideo = "file:/C:/Users/shady/Documents/Proyectos_IntelliJ/CinemApp/src/main/resources/videos/mgm.mp4";

        Media media = new Media(urlVideo);

        mediaPlayer = new MediaPlayer(media);

        mvVideo.setFitHeight(500);
        mvVideo.setFitWidth(400);
        mvVideo.setMediaPlayer(mediaPlayer);
        mediaPlayer.setAutoPlay(true);

        FadeTransition fadeTransition = new FadeTransition(Duration.seconds(6), vbSplash);
        fadeTransition.setFromValue(1.0);
        fadeTransition.setToValue(1.0);
        fadeTransition.play();

        fadeTransition.setOnFinished(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                try {
                    Stage splashStage = (Stage) vbSplash.getScene().getWindow();
                    splashStage.hide();

                    Stage stage = new Stage();
                    FXMLLoader loader = new FXMLLoader();
                    loader.setLocation(R.getUI("cinemapp.fxml"));
                    loader.setController(new AppController());
                    VBox vBox = loader.load();

                    Scene scene = new Scene(vBox);
                    stage.setScene(scene);
                    stage.show();
                } catch (IOException ioe) {
                    ioe.printStackTrace();
                }
            }
        });
    }
}
