package org._movie_hub._next_gen_edition._controller;

import animatefx.animation.SlideOutRight;
import com.google.gson.Gson;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.VBox;
import org._movie_hub._next_gen_edition._custom.Watchdog;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;

/**
 * @author Mandela aka puumInc
 */
@SuppressWarnings("unused")
public class SelectedFile extends Watchdog implements Initializable {

    protected static String serial;
    protected static String path;
    protected static String category;

    @FXML
    private Label pathTF;

    @FXML
    private Label sizeTF;

    @FXML
    private void delete_directory(ActionEvent event) {
        final String[] strings = pathTF.getParent().getParent().getId().split("_");
        final String id = strings[0];
        final String type = strings[1];
        if (type.equals("movie")) {
            MOVIE_LIST.remove(id);
            LIST_OF_SELECTED_MOVIES.remove(pathTF.getTooltip().getText());
        } else {
            SERIES_LIST.remove(id);
            LIST_OF_SELECTED_SERIES.remove(pathTF.getTooltip().getText());
        }
        VBox vBox = (VBox) pathTF.getParent().getParent().getParent();
        Node currentNode = pathTF.getParent().getParent();
        new SlideOutRight(currentNode).play();
        Platform.runLater(() -> {
            VBox.clearConstraints(currentNode);
            vBox.getChildren().remove(currentNode);
        });
        event.consume();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        pathTF.getParent().getParent().setId(serial.concat("_".concat(category)));
        final File file = new File(path);
        pathTF.setText(format_file_name(file.getName()));
        Tooltip tooltip = pathTF.getTooltip();
        tooltip.setText(path);
        pathTF.setTooltip(tooltip);
        if (category.equals("movie")) {
            sizeTF.setText(format_file_or_directory_size(file));
        } else {
            final com.google.gson.JsonObject jsonObject = get_details_about_number_of_seasons_and_episodes_of_a_series(file);
            sizeTF.setText(format_file_or_directory_size(file));
            final Tooltip tooltip1 = sizeTF.getTooltip();
            Gson gson = new Gson();
            final int episodesCount = gson.fromJson(jsonObject.get("episodes"), Integer.class);
            final int seasonsCount = gson.fromJson(jsonObject.get("seasons"), Integer.class);
            tooltip1.setText(seasonsCount + " Season(s) with " + episodesCount + " valid files");
            sizeTF.setTooltip(tooltip1);
        }
    }

}
