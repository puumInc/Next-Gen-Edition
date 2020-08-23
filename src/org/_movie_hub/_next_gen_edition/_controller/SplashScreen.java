package org._movie_hub._next_gen_edition._controller;

import animatefx.animation.FadeIn;
import animatefx.animation.FadeOut;
import com.jfoenix.controls.JFXProgressBar;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.util.Duration;
import org._movie_hub._next_gen_edition.Main;
import org._movie_hub._next_gen_edition._custom.Watchdog;
import org._movie_hub._next_gen_edition._server.Services;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ResourceBundle;

/**
 * @author Mandela aka puumInc
 */
public class SplashScreen extends Watchdog implements Initializable {

    @FXML
    private AnchorPane movieSelectionPane;

    @FXML
    private AnchorPane splashScreenPane;

    @FXML
    private JFXProgressBar splashProgBar;

    @FXML
    private Label threadUpdate;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            splashProgBar.setProgress(0);
            splashProgBar.progressProperty().unbind();
            Task<Object> objectTask = loading_progress();
            splashProgBar.progressProperty().bind(objectTask.progressProperty());
            objectTask.setOnSucceeded(event -> {
                splashProgBar.progressProperty().unbind();
                new FadeOut(splashScreenPane).play();
                movieSelectionPane.toFront();
                new FadeIn(movieSelectionPane).setDelay(Duration.seconds(0.5)).play();
            });
            objectTask.setOnFailed(event -> {
                splashProgBar.progressProperty().unbind();
                error_message("Awww!", "Something went wrong while loading application").show();
            });
            objectTask.exceptionProperty().addListener(((observable, oldValue, newValue) -> {
                if (newValue != null) {
                    Exception e = (Exception) newValue;
                    e.printStackTrace();
                    programmer_error(e).show();
                    new Thread(write_log("\n\n" + time_stamp() + ": The following Exception occurred,\n" + e, null)).start();
                    new Thread(write_stack_trace(e)).start();
                }
            }));
            new Thread(objectTask).start();
        } catch (Exception e) {
            e.printStackTrace();
            programmer_error(e).show();
            new Thread(write_stack_trace(e)).start();
        }
    }

    @NotNull
    @Contract(value = " -> new", pure = true)
    private Task<Object> show_home() {
        return new Task<Object>() {
            @Override
            protected Object call() {
                ObservableList<Node> nodeObservableList = movieSelectionPane.getChildren();
                for (Node node : nodeObservableList) {
                    Platform.runLater(() -> {
                        AnchorPane.clearConstraints(node);
                        movieSelectionPane.getChildren().remove(node);
                    });
                }
                try {
                    Node node = FXMLLoader.load(getClass().getResource("/org/_movie_hub/_next_gen_edition/_fxml/homeUI.fxml"));
                    Platform.runLater(() -> movieSelectionPane.getChildren().add(node));
                } catch (IOException e) {
                    e.printStackTrace();
                    new Thread(write_stack_trace(e)).start();
                    Platform.runLater(() -> programmer_error(e).show());
                }
                return null;
            }
        };
    }

    @Contract(pure = true)
    private @NotNull Runnable start_my_ip_server_app() {
        return () -> {
            try {
                ProcessBuilder processBuilder = new ProcessBuilder();
                processBuilder.command(Main.RESOURCE_PATH.getParentFile().getAbsolutePath().concat("\\_support\\Ip_address\\").concat("Ip_address.exe"));
                processBuilder.redirectErrorStream(true);
                Main.process = processBuilder.start();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(Main.process.getInputStream()));
                String someText;
                while (true) {
                    someText = bufferedReader.readLine();
                    if (someText == null) {
                        break;
                    }
                    new Thread(write_log("cmd:// attempting to start ip address provider.\n".concat(someText), null)).start();
                }
            } catch (IOException e) {
                e.printStackTrace();
                new Thread(write_stack_trace(e)).start();
                Platform.runLater(() -> programmer_error(e).show());
            }
        };
    }

    @NotNull
    @Contract(value = " -> new", pure = true)
    private Task<Object> loading_progress() {
        final String[] appInfo = new String[]{"Please wait as i freshen up :)", "Version 1. 0. 0", "I was designed and developed by < puum . inc ( ) / >", "The server has been switched on", "Almost done..."};
        return new Task<Object>() {
            @Override
            protected Object call() {
                int steps = 0;
                while (steps <= 100) {
                    try {
                        updateProgress(steps, 100);
                        switch (steps) {
                            case 0:
                                Platform.runLater(() -> threadUpdate.setText(appInfo[0]));
                                break;
                            case 20:
                                Platform.runLater(() -> threadUpdate.setText(appInfo[1]));
                                break;
                            case 40:
                                Platform.runLater(() -> threadUpdate.setText(appInfo[2]));
                                break;
                            case 60:
                                new Services();
                                Platform.runLater(() -> {
                                    new Thread(start_my_ip_server_app()).start();
                                    threadUpdate.setText(appInfo[3]);
                                });
                                break;
                            case 80:
                                new Thread(show_home()).start();
                                Platform.runLater(() -> threadUpdate.setText(appInfo[4]));
                                break;
                        }
                        Thread.sleep(100);
                        steps++;
                    } catch (InterruptedException e) {
                        new Thread(write_stack_trace(e)).start();
                        Platform.runLater(() -> programmer_error(e).show());
                        break;
                    }
                }
                return null;
            }
        };
    }

}
