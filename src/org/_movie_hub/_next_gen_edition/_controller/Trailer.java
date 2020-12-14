package org._movie_hub._next_gen_edition._controller;

import animatefx.animation.SlideOutRight;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.VBox;
import org._movie_hub._next_gen_edition._custom.Watchdog;
import org.jetbrains.annotations.Contract;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

/**
 * @author Mandela aka puumInc
 */

@SuppressWarnings("unused")
public class Trailer extends Watchdog implements Initializable {

    protected static String path;
    private String myPath;

    @FXML
    private Label pathTF;

    @FXML
    private Label sizeTF;

    @FXML
    void delete_directory(ActionEvent event) {
        final JsonElement jsonElement = new Gson().toJsonTree(pathTF.getParent().getParent().getId(), String.class);
        try {
            final JsonArray jsonArray = get_app_details_as_object(new File(format_path_name_to_current_os(TRAILERS_JSON_FILE)));
            for (JsonElement jsonElement1 : jsonArray) {
                if (jsonElement1.equals(jsonElement)) {
                    jsonArray.remove(jsonElement);
                    if (write_jsonArray_to_file(jsonArray, format_path_name_to_current_os(TRAILERS_JSON_FILE))) {
                        Home.listOfTrailerIds.remove(new File(myPath).getName());
                        final JsonArray jsonArray1 = make_array_from_map(Home.listOfTrailerIds);
                        if (write_jsonArray_to_file(jsonArray1, format_path_name_to_current_os(TRAILER_KEY_JSON_FILE))) {
                            update_trailer_and_playlist_key();
                            VBox vBox = (VBox) pathTF.getParent().getParent().getParent();
                            Node currentNode = pathTF.getParent().getParent();
                            new SlideOutRight(currentNode).play();
                            Platform.runLater(() -> {
                                VBox.clearConstraints(currentNode);
                                vBox.getChildren().remove(currentNode);
                            });
                        } else {
                            error_message("Incomplete!", "The trailer keys were not updated, please restart the app!").show();
                        }
                    } else {
                        error_message("Failed!", "The path was not deleted!").show();
                    }
                    break;
                }
            }
        } catch (IOException ex) {
            ex.printStackTrace();
            new Thread(write_stack_trace(ex)).start();
            Platform.runLater(() -> programmer_error(ex).show());
        }
        event.consume();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        myPath = Trailer.path;
        pathTF.getParent().getParent().setId(myPath);
        final File file = new File(myPath);
        pathTF.setText(file.getName());
        final Tooltip tooltip = pathTF.getTooltip();
        tooltip.setText(myPath);
        pathTF.setTooltip(tooltip);
        sizeTF.setTooltip(null);
        sizeTF.setText(format_file_or_directory_size(file));
        final JsonArray jsonArray = make_array_from_map(Home.listOfTrailerIds);
        if (write_jsonArray_to_file(jsonArray, format_path_name_to_current_os(TRAILER_KEY_JSON_FILE))) {
            update_trailer_and_playlist_key();
        }
        new Thread(check_my_path()).start();
    }

    @Contract(value = " -> new", pure = true)
    private Task<Object> check_my_path() {
        return new Task<Object>() {
            @Override
            protected Object call() {
                final File file = new File(myPath);
                while (true) {
                    try {
                        if (file.exists()) {
                            if (!pathTF.getText().equals(file.getName())) {
                                Platform.runLater(() -> {
                                    pathTF.setText(file.getName());
                                    sizeTF.setText(format_file_or_directory_size(file));
                                });
                            }
                        } else {
                            if (pathTF.getText().equals(file.getName())) {
                                Platform.runLater(() -> {
                                    pathTF.setText(myPath.concat("\t\t( * Path is BROKEN * )"));
                                    sizeTF.setText(format_file_or_directory_size(file));
                                });
                            }
                        }
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        break;
                    }
                }
                return null;
            }
        };
    }

}
