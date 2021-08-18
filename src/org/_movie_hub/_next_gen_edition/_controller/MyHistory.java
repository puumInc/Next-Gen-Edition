package org._movie_hub._next_gen_edition._controller;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import org._movie_hub._next_gen_edition._custom.Watchdog;
import org._movie_hub._next_gen_edition._model._object.History;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

/**
 * @author Mandela aka puumInc
 */

public class MyHistory extends Watchdog implements Initializable {

    protected static JsonObject jsonObject;

    @FXML
    private Label dateLbl;

    @FXML
    private VBox activitiesBox;


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        final JsonObject myJsonObject = jsonObject;
        final String date = new Gson().fromJson(myJsonObject.get("date"), String.class);
        dateLbl.setText(date);

        final JsonArray jsonArray = new Gson().fromJson(myJsonObject.get("history"), JsonArray.class);
        jsonArray.forEach(jsonElement -> {
            final History history = new Gson().fromJson(jsonElement, History.class);
            try {
                MyActivity.history = history;
                Node node = FXMLLoader.load(getClass().getResource("/_fxml/activityUI.fxml"));
                Platform.runLater(() -> activitiesBox.getChildren().add(node));
                MyActivity.history = null;
            } catch (IOException e) {
                e.printStackTrace();
                new Thread(write_stack_trace(e)).start();
                Platform.runLater(() -> programmer_error(e).show());
            }
        });
    }
}
