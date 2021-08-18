package org._movie_hub._next_gen_edition._controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import org._movie_hub._next_gen_edition._custom.Watchdog;
import org._movie_hub._next_gen_edition._model._object.History;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * @author Mandela aka puumInc
 */

public class MyActivity extends Watchdog implements Initializable {

    protected static History history;
    private History myHistory;

    @FXML
    private Label jobNameLbl;

    @FXML
    private Label timeStartLbl;

    @FXML
    private Label timeEndLbl;

    @FXML
    private Label statusLbl;

    @FXML
    void show_list_of_media(ActionEvent event) {
        show_list_of_copied_media("Name: ".concat(this.myHistory.getJobName()), generate_a_string_from_list_of_media_paths(this.myHistory.getListOfMedia())).show();
        event.consume();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        this.myHistory = MyActivity.history;
        jobNameLbl.setText(this.myHistory.getJobName());
        timeStartLbl.setText(beautify_time(this.myHistory.getTimeWhenItStarted()));
        timeEndLbl.setText(beautify_time(this.myHistory.getTimeWhenItStopped()));
        final String statusText = this.myHistory.getStatus();
        if (statusText.equalsIgnoreCase("incomplete")) {
            statusLbl.setStyle("-fx-text-fill: rgb(255, 255, 255);");
        }
        statusLbl.setText(statusText);
    }

    @Contract(pure = true)
    private @NotNull String beautify_time(@NotNull String time) {
        return time.replace(":", " : ");
    }

}
