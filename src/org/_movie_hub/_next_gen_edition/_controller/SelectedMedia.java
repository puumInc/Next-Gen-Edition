package org._movie_hub._next_gen_edition._controller;

import animatefx.animation.SlideOutRight;
import com.github.cliftonlabs.json_simple.JsonObject;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.VBox;
import org._movie_hub._next_gen_edition._custom.Watchdog;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;

/**
 * @author Mandela aka puumInc
 */
@SuppressWarnings("unused")
public class SelectedMedia extends Watchdog implements Initializable {

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
            Home.movieList.remove(id);
            Home.listOfSelectedMovies.remove(pathTF.getTooltip().getText());
        } else {
            Home.seriesList.remove(id);
            Home.listOfSelectedSeries.remove(pathTF.getTooltip().getText());
        }
        VBox vBox = (VBox) pathTF.getParent().getParent().getParent();
        Node currentNode = pathTF.getParent().getParent();
        new SlideOutRight(currentNode).play();
        Platform.runLater(() -> {
            VBox.clearConstraints(currentNode);
            vBox.getChildren().remove(currentNode);
        });
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        pathTF.getParent().getParent().setId(serial.concat("_".concat(category)));
        final File file = new File(path);
        pathTF.setText(file.getName());
        Tooltip tooltip = pathTF.getTooltip();
        tooltip.setText(path);
        pathTF.setTooltip(tooltip);
        if (category.equals("movie")) {
            sizeTF.setText(format_file_or_directory_size(file));
        } else {
            final JsonObject jsonObject = get_details_about_number_of_seasons_and_episodes_of_a_series(file);
            sizeTF.setText(format_file_or_directory_size(file));
            final Tooltip tooltip1 = sizeTF.getTooltip();
            final int episodesCount = (int) jsonObject.get("episodes");
            final int seasonsCount = (int) jsonObject.get("seasons");
            tooltip1.setText(seasonsCount + " Season(s) with " + episodesCount + " valid files");
            sizeTF.setTooltip(tooltip1);
        }
    }

    protected JsonObject get_details_about_number_of_seasons_and_episodes_of_a_series(File file) {
        final JsonObject jsonObject = new JsonObject();
        jsonObject.put("seasons", get_number_of_seasons(file));
        jsonObject.put("episodes", get_number_of_valid_episodes(file));
        return jsonObject;
    }

    private Integer get_number_of_seasons(@NotNull File file) {
        int result = 0;
        if (file.isDirectory()) {
            final File[] fileList = file.listFiles();
            if (fileList != null) {
                boolean thereIsAFileOutsideTheSubfoldersOfTheParentFolder = false;
                for (File file1 : fileList) {
                    if (file1.isDirectory()) {
                        ++result;
                    } else {
                        thereIsAFileOutsideTheSubfoldersOfTheParentFolder = true;
                    }
                }
                if (thereIsAFileOutsideTheSubfoldersOfTheParentFolder) {
                    ++result;
                }
            }
        }
        return result;
    }

    @NotNull
    private Integer get_number_of_valid_episodes(@NotNull File file) {
        int result = 0;
        if (file.isDirectory()) {
            final File[] fileList = file.listFiles();
            if (fileList != null) {
                for (File file1 : fileList) {
                    if (file1.isFile()) {
                        if (is_an_episode(file1)) {
                            ++result;
                        }
                    } else {
                        result += get_number_of_valid_episodes(file1);
                    }
                }
            }
        }
        return result;
    }

    private @NotNull Boolean is_an_episode(File file) {
        final String[] extensions = Home.EXTENSIONS_FOR_MOVIES_ONLY.split(":");
        for (String string : extensions) {
            if (file.getName().endsWith(string) || file.getName().endsWith(string.toUpperCase())) {
                return true;
            }
        }
        return false;
    }

    @NotNull
    protected final String format_file_or_directory_size(@NotNull File file) {
        if (file.exists()) {
            double bytes = 0;
            bytes += get_size_of_the_provided_file_or_folder(file);
            return make_bytes_more_presentable(bytes);
        } else {
            error_message("Path is broken!", "It seems the path you have provided does not exist").show();
            return "Zero Bytes";
        }
    }

    protected double get_size_of_the_provided_file_or_folder(@NotNull File file) {
        double bytes = 0;
        if (file.isFile()) {
            bytes = file.length();
        } else {
            if (file.isDirectory()) {
                final File[] fileList = file.listFiles();
                if (fileList != null) {
                    for (File file1 : fileList) {
                        bytes += get_size_of_the_provided_file_or_folder(file1);
                    }
                }
            }
        }
        return bytes;
    }

    @NotNull
    protected final String make_bytes_more_presentable(double bytes) {
        final double kilobytes = (bytes / 1024);
        final double megabytes = (kilobytes / 1024);
        final double gigabytes = (megabytes / 1024);
        final double terabytes = (gigabytes / 1024);
        final double petabytes = (terabytes / 1024);
        final double exabytes = (petabytes / 1024);
        final double zettabytes = (exabytes / 1024);
        final double yottabytes = (zettabytes / 1024);
        String result;
        if (((int) yottabytes) > 0) {
            result = String.format("%,.2f", yottabytes).concat(" YB");
            return result;
        }
        if (((int) zettabytes) > 0) {
            result = String.format("%,.2f", zettabytes).concat(" ZB");
            return result;
        }
        if (((int) exabytes) > 0) {
            result = String.format("%,.2f", exabytes).concat(" EB");
            return result;
        }
        if (((int) petabytes) > 0) {
            result = String.format("%,.2f", petabytes).concat(" PB");
            return result;
        }
        if (((int) terabytes) > 0) {
            result = String.format("%,.2f", terabytes).concat(" TB");
            return result;
        }
        if (((int) gigabytes) > 0) {
            result = String.format("%,.2f", gigabytes).concat(" GB");
            return result;
        }
        if (((int) megabytes) > 0) {
            result = String.format("%,.2f", megabytes).concat(" MB");
            return result;
        }
        if (((int) kilobytes) > 0) {
            result = String.format("%,.2f", kilobytes).concat(" KB");
            return result;
        }
        result = String.format("%,.0f", bytes).concat(" Bytes");
        return result;
    }

}
