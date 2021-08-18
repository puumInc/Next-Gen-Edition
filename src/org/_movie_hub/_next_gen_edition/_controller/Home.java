package org._movie_hub._next_gen_edition._controller;

import animatefx.animation.*;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXTextArea;
import com.jfoenix.controls.JFXTextField;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TextInputDialog;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import org._movie_hub._next_gen_edition.Main;
import org._movie_hub._next_gen_edition._custom.Email;
import org._movie_hub._next_gen_edition._model._object.History;
import org._movie_hub._next_gen_edition._model._object.JobPackage;
import org.apache.commons.io.FileUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Pattern;

/**
 * @author Mandela aka puumInc
 */

public class Home extends Email implements Initializable {

    private final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd-MMM-yyyy");
    private String recentDirectory = "";
    private int moviesCount = 0;
    private int seriesCount = 0;

    @FXML
    private StackPane aboutPage;

    @FXML
    private StackPane historyPage;

    @FXML
    private VBox historyBox;

    @FXML
    private StackPane emailPage;

    @FXML
    private JFXTextField senderEmailTF;

    @FXML
    private JFXTextArea emailMessageTA;

    @FXML
    private StackPane trailerPage;

    @FXML
    private VBox trailerBox;

    @FXML
    private JFXButton trailerBtn;

    @FXML
    private JFXTextField trailersPathTF;

    @FXML
    private StackPane bridgePane;

    @FXML
    private StackPane tasksPane;

    @FXML
    private AnchorPane jobPane;

    @FXML
    private VBox taskBox;

    @FXML
    private Label emptyListLbl;

    @FXML
    private StackPane questionPane;

    @FXML
    private HBox optionsBox;

    @FXML
    private StackPane homePane;

    @FXML
    private StackPane seriesPane;

    @FXML
    private JFXTextField seriesPathTF;

    @FXML
    private VBox seriesBox;

    @FXML
    private StackPane moviesPane;

    @FXML
    private JFXButton forMovies;

    @FXML
    private JFXTextField moviesPathTF;

    @FXML
    private JFXButton directoryForMovies;

    @FXML
    private VBox moviesBox;

    @FXML
    private VBox menuVBox;

    @FXML
    void add_dragged_files(DragEvent dragEvent) {
        try {
            if (dragEvent.getGestureSource() != dragEvent && dragEvent.getDragboard().hasFiles()) {
                /* allow for both copying and moving, whatever user chooses */
                dragEvent.acceptTransferModes(TransferMode.COPY_OR_MOVE);

                /* let the source know whether the string was successfully
                 * transferred and used
                 * */
                Dragboard dragboard = dragEvent.getDragboard();
                boolean itsComplete = false;
                if (dragboard.hasFiles()) {
                    itsComplete = true;
                }
                dragEvent.setDropCompleted(itsComplete);
            }
            dragEvent.consume();
        } catch (Exception e) {
            e.printStackTrace();
            programmer_error(e).show();
            new Thread(write_stack_trace(e)).start();
        }
    }

    @FXML
    void check_files_being_dragged(DragEvent dragEvent) {
        VBox myVBox = (VBox) dragEvent.getSource();
        try {
            Dragboard dragboard = dragEvent.getDragboard();
            if (!dragboard.hasFiles()) {
                dragEvent.consume();
                return;
            }
            List<File> dragBoardFiles = dragboard.getFiles();
            if (dragBoardFiles != null) {
                List<File> fileList = new ArrayList<>();
                List<File> folderList = new ArrayList<>();
                for (File draggedFileOrFolder : dragBoardFiles) {
                    if (draggedFileOrFolder.isFile()) {
                        if (can_be_used_by_vlc(draggedFileOrFolder)) {
                            fileList.add(draggedFileOrFolder);
                        }
                    } else if (draggedFileOrFolder.isDirectory()) {
                        if (!the_file_or_folder_has_zero_bytes(draggedFileOrFolder)) {
                            folderList.add(draggedFileOrFolder);
                        }
                    }
                }
                if (dragBoardFiles.size() > 0) {
                    if (myVBox.equals(moviesBox)) {
                        if (verify_selected_movie_files(fileList)) {
                            add_movie_directory();
                        }
                    } else if (myVBox.equals(seriesBox)) {
                        if (verify_selected_series_folders_files(folderList)) {
                            add_series_directory();
                        }
                    }
                }
                dragboard.clear();
            }
            dragEvent.consume();
        } catch (Exception e) {
            e.printStackTrace();
            programmer_error(e).show();
            new Thread(write_stack_trace(e)).start();
        }
    }

    @FXML
    void allow_to_copy_to_removable_drive(ActionEvent event) {
        final String TIME_IT_STARTED = "[".concat(new SimpleDateFormat("HH:mm:ss:SSS").format(Calendar.getInstance().getTime())).concat("]");
        try {
            new FadeOut(questionPane).play();
            tasksPane.toFront();
            new FadeIn(tasksPane).play();
            if (LIST_OF_SELECTED_MOVIES.isEmpty() && LIST_OF_SELECTED_SERIES.isEmpty() && !LIST_OF_COPY_THREADS.isEmpty()) {
                event.consume();
                return;
            } else if (LIST_OF_SELECTED_MOVIES.isEmpty() && LIST_OF_SELECTED_SERIES.isEmpty()) {
                if (taskBox.getChildren().size() == 0) {
                    if (emptyListLbl.getOpacity() < 1) {
                        new FadeOut(jobPane).play();
                        emptyListLbl.toFront();
                        new FadeIn(emptyListLbl).play();
                    }
                    new Wobble(emptyListLbl).play();
                }
                event.consume();
                return;
            } else {
                if (jobPane.getOpacity() < 1) {
                    new FadeOut(emptyListLbl).play();
                    jobPane.toFront();
                    new FadeIn(jobPane).play();
                }
            }
            final DirectoryChooser directoryChooser = new DirectoryChooser();
            File destinationFolder = directoryChooser.showDialog(Main.stage);
            if (destinationFolder != null) {
                final String sizeOfWhatWeWantToCopy = get_formatted_size_of_selected_list_of_media();
                String packageName = create_name_for_new_packages(sizeOfWhatWeWantToCopy);
                if (packageName != null) {
                    final double cost = get_cost_of_selected_movies_or_series_or_both(get_pricing());
                    if (show_information_of_the_amount_the_customer_should_pay_and_set_account_number_it_should_be_paid_to(packageName, cost)) {
                        if (LIST_OF_COPY_THREADS.containsKey(packageName)) {
                            error_message("Given task name exists!", "Please type a unique name for the task").show();
                        } else {
                            destinationFolder = new File(destinationFolder.getAbsolutePath().concat("\\").concat(packageName));
                            destinationFolder = get_name_of_the_new_destination_folder_after_its_duplicate_is_found(destinationFolder);
                            packageName = destinationFolder.getName();

                            FileUtils.forceMkdir(destinationFolder);

                            final ObservableList<String> allMediaPaths = FXCollections.observableArrayList();
                            for (String pathToSeriesFolder : LIST_OF_SELECTED_SERIES) {
                                File file = new File(pathToSeriesFolder);
                                JsonArray directoryOfSubFilesInTheProvidedFolder = get_directory_of_sub_files_in_the_provided_folder(file);
                                for (JsonElement jsonElement : directoryOfSubFilesInTheProvidedFolder) {
                                    String path = new Gson().fromJson(jsonElement, String.class);
                                    allMediaPaths.add(path);
                                }
                            }

                            final JobPackage jobPackage = new JobPackage();
                            jobPackage.setForUpload(false);
                            jobPackage.setDestinationFolder(destinationFolder);
                            jobPackage.setName(packageName);
                            jobPackage.setCost(cost);
                            jobPackage.setAllMediaPaths(allMediaPaths);
                            jobPackage.setSourceSize(get_size_of_the_selected_movies_and_series(allMediaPaths));
                            LIST_OF_SELECTED_MOVIES.clear();
                            LIST_OF_SELECTED_SERIES.clear();
                            MOVIE_LIST.clear();
                            SERIES_LIST.clear();
                            if (!listOfStartedThreads.contains(packageName)) {
                                LIST_OF_COPY_THREADS.put(packageName, jobPackage);
                                try {
                                    final String END_TIME = "[".concat(new SimpleDateFormat("HH:mm:ss:SSS").format(Calendar.getInstance().getTime())).concat("]");
                                    final History history = new History();
                                    history.setTimeWhenItStarted(TIME_IT_STARTED);
                                    history.setTimeWhenItStopped(END_TIME);
                                    history.setDate(get_date());
                                    history.setJobName(jobPackage.getName().concat(" (Cp2D)"));
                                    history.setListOfMedia(new ArrayList<>());
                                    history.setStatus("CREATED");
                                    final List<String> objectList = jobPackage.getAllMediaPaths();
                                    for (String pathToMedia : objectList) {
                                        history.getListOfMedia().add(new File(pathToMedia).getName());
                                    }
                                    new Thread(write_log("New Copy to drive", history)).start();
                                    MyTasks.taskName = packageName;
                                    final Node node = FXMLLoader.load(getClass().getResource("/_fxml/taskUI.fxml"));
                                    Platform.runLater(() -> {
                                        taskBox.getChildren().add(node);
                                        MyTasks.taskName = null;
                                    });
                                    Platform.runLater(() -> {
                                        update_movies_list();
                                        update_series_list();
                                    });
                                } catch (IOException e) {
                                    e.printStackTrace();
                                    programmer_error(e).show();
                                    new Thread(write_stack_trace(e)).start();
                                }
                            }
                        }
                    } else {
                        error_message_alert("Billing declined!", "The package will NOT be started because is payment has not been received").show();
                    }
                } else {
                    error_message_alert("No package name was provided!", "Please ensure you enter the name for it to continue").show();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            programmer_error(e).show();
            new Thread(write_stack_trace(e)).start();
        }
        event.consume();
    }

    @FXML
    void allow_to_upload_to_a_mobile_phone(ActionEvent event) {
        final String TIME_IT_STARTED = "[".concat(new SimpleDateFormat("HH:mm:ss:SSS").format(Calendar.getInstance().getTime())).concat("]");
        if (LIST_OF_SELECTED_MOVIES.isEmpty() && LIST_OF_SELECTED_SERIES.isEmpty()) {
            warning_message("Incomplete!", "Please select at least one movie file or series folder ot continue").show();
        }
        try {
            new FadeOut(questionPane).play();
            tasksPane.toFront();
            new FadeIn(tasksPane).play();
            if (LIST_OF_SELECTED_MOVIES.isEmpty() && LIST_OF_SELECTED_SERIES.isEmpty() && !STRING_JOB_PACKAGE_HASH_MAP_FOR_UPLOAD.isEmpty()) {
                event.consume();
                return;
            } else if (LIST_OF_SELECTED_MOVIES.isEmpty() && LIST_OF_SELECTED_SERIES.isEmpty()) {
                if (taskBox.getChildren().size() == 0) {
                    if (emptyListLbl.getOpacity() < 1) {
                        new FadeOut(jobPane).play();
                        emptyListLbl.toFront();
                        new FadeIn(emptyListLbl).play();
                    }
                    new Wobble(emptyListLbl).play();
                }
                event.consume();
                return;
            } else {
                if (jobPane.getOpacity() < 1) {
                    new FadeOut(emptyListLbl).play();
                    jobPane.toFront();
                    new FadeIn(jobPane).play();
                }
            }
            final String sizeOfWhatWeWantToCopy = get_formatted_size_of_selected_list_of_media();
            final String packageName = create_name_for_new_packages(sizeOfWhatWeWantToCopy);
            if (packageName != null) {
                final double cost = get_cost_of_selected_movies_or_series_or_both(get_pricing());
                if (show_information_of_the_amount_the_customer_should_pay_and_set_account_number_it_should_be_paid_to(packageName, cost)) {
                    if (STRING_JOB_PACKAGE_HASH_MAP_FOR_UPLOAD.containsKey(packageName)) {
                        error_message("Given package name exists!", "Please type a unique name").show();
                    } else {
                        final ObservableList<String> allMediaPaths = FXCollections.observableArrayList();
                        allMediaPaths.addAll(LIST_OF_SELECTED_MOVIES);
                        allMediaPaths.addAll(LIST_OF_SELECTED_SERIES);
                        final JobPackage jobPackage = new JobPackage();
                        jobPackage.setForUpload(true);
                        jobPackage.setName(packageName);
                        jobPackage.setCost(cost);
                        jobPackage.setAllMediaPaths(allMediaPaths);
                        jobPackage.setSourceSize(get_size_of_the_selected_movies_and_series(allMediaPaths));
                        LIST_OF_SELECTED_MOVIES.clear();
                        LIST_OF_SELECTED_SERIES.clear();
                        MOVIE_LIST.clear();
                        SERIES_LIST.clear();
                        if (!listOfStartedThreads.contains(packageName)) {
                            STRING_JOB_PACKAGE_HASH_MAP_FOR_UPLOAD.put(packageName, jobPackage);
                            try {
                                final String END_TIME = "[".concat(new SimpleDateFormat("HH:mm:ss:SSS").format(Calendar.getInstance().getTime())).concat("]");
                                final History history = new History();
                                history.setTimeWhenItStarted(TIME_IT_STARTED);
                                history.setTimeWhenItStopped(END_TIME);
                                history.setDate(get_date());
                                history.setJobName(jobPackage.getName().concat(" (U)"));
                                history.setListOfMedia(new ArrayList<>());
                                history.setStatus("CREATED");
                                final List<String> objectList = jobPackage.getAllMediaPaths();
                                for (String pathToMedia : objectList) {
                                    history.getListOfMedia().add(new File(pathToMedia).getName());
                                }
                                new Thread(write_log("New Upload", history)).start();
                                MyTasks.taskName = packageName;
                                final Node node = FXMLLoader.load(getClass().getResource("/_fxml/taskUI.fxml"));
                                Platform.runLater(() -> taskBox.getChildren().add(node));
                                Platform.runLater(() -> {
                                    update_movies_list();
                                    update_series_list();
                                });
                            } catch (IOException e) {
                                e.printStackTrace();
                                programmer_error(e).show();
                                new Thread(write_stack_trace(e)).start();
                            }
                        }
                    }
                } else {
                    error_message_alert("Billing declined!", "The package will NOT be started because it's not been paid for").show();
                }
            } else {
                error_message_alert("No package name was provided!", "Please ensure you enter the name of the package").show();
            }
        } catch (Exception e) {
            e.printStackTrace();
            programmer_error(e).show();
            new Thread(write_stack_trace(e)).start();
        }
        event.consume();
    }

    @FXML
    void confirm_action_for_selected_files(ActionEvent event) {
        if (bridgePane.getOpacity() < 1) {
            if (questionPane.getOpacity() < 1) {
                new FadeOut(tasksPane).play();
                questionPane.toFront();
                new FadeIn(questionPane).play();
            }
            new FadeOutRight(homePane).play();
            bridgePane.toFront();
            new FadeInLeft(bridgePane).play();
        }
        event.consume();
    }

    @FXML
    void choose_directory(ActionEvent event) {
        try {
            if (event.getSource().equals(trailerBtn)) {
                if (verify_trailers()) {
                    display_trailers(read_jsonArray_from_file(new File(format_path_name_to_current_os(TRAILERS_JSON_FILE))));
                    new RubberBand(((Node) event.getSource())).play();
                }
            } else if (event.getSource().equals(forMovies)) {
                if (the_selected_files_are_valid_movies()) {
                    add_movie_directory();
                    new RubberBand(((Node) event.getSource())).play();
                }
            } else {
                if (verify_series()) {
                    add_series_directory();
                    new RubberBand(((Node) event.getSource())).play();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            programmer_error(e).show();
            new Thread(write_stack_trace(e)).start();
        }
        event.consume();
    }

    @FXML
    void go_back_to_home_page(ActionEvent event) {
        if (homePane.getOpacity() < 1) {
            hide_the_current_pane();
            homePane.toFront();
            new FadeInRight(homePane).play();
        }
        event.consume();
    }

    @FXML
    void go_back_to_select_an_action(ActionEvent event) {
        new FadeOut(tasksPane).play();
        questionPane.toFront();
        new FadeIn(questionPane).play();
        event.consume();
    }

    @FXML
    void go_back_to_movie(ActionEvent event) {
        new FadeOutRight(seriesPane).play();
        moviesPane.toFront();
        new FadeInLeft(moviesPane).play();
        event.consume();
    }

    @FXML
    void go_to_series(ActionEvent event) {
        new FadeOutLeft(moviesPane).play();
        seriesPane.toFront();
        new FadeInRight(seriesPane).play();
        event.consume();
    }

    @FXML
    void place_directory_into_cart(ActionEvent event) {
        boolean isAMovie = true;
        try {
            if (event.getSource().equals(directoryForMovies)) {
                if (moviesPathTF.getText().trim().length() == 0 || moviesPathTF.getText() == null) {
                    empty_and_null_pointer_message(moviesPathTF).show();
                    event.consume();
                    return;
                }
                add_movie_directory();
            } else {
                if (seriesPathTF.getText().trim().length() == 0 || seriesPathTF.getText() == null) {
                    empty_and_null_pointer_message(seriesPathTF).show();
                    event.consume();
                    return;
                }
                isAMovie = false;
                add_series_directory();
            }
        } catch (NullPointerException e) {
            if (isAMovie) {
                if (the_selected_files_are_valid_movies()) {
                    add_movie_directory();
                    new RubberBand(((Node) event.getSource())).play();
                }
            } else {
                if (verify_series()) {
                    add_series_directory();
                    new RubberBand(((Node) event.getSource())).play();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            programmer_error(e).show();
            new Thread(write_stack_trace(e)).start();
        }
        event.consume();
    }

    @FXML
    void show_about_page(ActionEvent event) {
        show_menu();
        if (aboutPage.getOpacity() < 1) {
            hide_the_current_pane();
            aboutPage.toFront();
            new FadeInRight(aboutPage).play();
        }
        hide_menu();
        event.consume();
    }

    @FXML
    void send_mail_to_developer(ActionEvent event) {
        if (senderEmailTF.getText().isEmpty() || senderEmailTF.getText().trim().length() == 0 || senderEmailTF.getText() == null) {
            empty_and_null_pointer_message(senderEmailTF).show();
            event.consume();
            return;
        }
        if (!email_is_in_correct_format(senderEmailTF.getText().trim())) {
            error_message("Bad email!", "Kindly ensure that the email you have provided is in the correct formant").show();
            event.consume();
            return;
        }
        if (emailMessageTA.getText().isEmpty() || emailMessageTA.getText().trim().length() == 0 || emailMessageTA.getText() == null) {
            empty_and_null_pointer_message(emailMessageTA).show();
            event.consume();
            return;
        }
        information_message("Please wait...");
        final Task<Boolean> task = send_email(senderEmailTF.getText().trim(), emailMessageTA.getText().trim());
        task.setOnSucceeded(event1 -> {
            if (task.getValue()) {
                Platform.runLater(() -> {
                    success_notification("Message has been Sent").show();
                    senderEmailTF.setText("");
                    emailMessageTA.setText("");
                });
            } else {
                Platform.runLater(() -> error_message("Failed!", "Your message was NOT sent").show());
            }
        });
        task.setOnFailed(event1 -> Platform.runLater(() -> error_message("Failed!", "Your message was NOT sent").show()));
        new Thread(task).start();
        event.consume();
    }

    @FXML
    void show_email_page(ActionEvent event) {
        if (emailPage.getOpacity() < 1) {
            hide_the_current_pane();
            emailPage.toFront();
            new FadeInRight(emailPage).play();
        }
        hide_menu();
        event.consume();
    }

    @FXML
    void show_history_page(ActionEvent event) {
        show_menu();
        if (historyPage.getOpacity() < 1) {
            hide_the_current_pane();
            historyPage.toFront();
            new FadeInRight(historyPage).play();
        }
        hide_menu();
        event.consume();
    }

    @FXML
    void show_home_page(ActionEvent event) {
        if (homePane.getOpacity() < 1) {
            hide_the_current_pane();
            homePane.toFront();
            new FadeInRight(homePane).play();
        }
        hide_menu();
        event.consume();
    }

    @FXML
    void show_menu_options(ActionEvent event) {
        show_menu();
        event.consume();
    }

    @FXML
    void show_trailers_page(ActionEvent event) {
        show_menu();
        if (trailerPage.getOpacity() < 1) {
            hide_the_current_pane();
            trailerPage.toFront();
            new FadeInRight(trailerPage).play();
        }
        hide_menu();
        event.consume();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            optionsBox.getChildren().stream().filter(node -> node instanceof JFXButton).map(node -> (JFXButton) node).forEach(jfxButton -> {
                final Pulse pulse = new Pulse(jfxButton);
                jfxButton.setOnMouseEntered(event -> pulse.play());
                jfxButton.setOnMouseExited(event -> pulse.stop());
            });

            hide_menu();

            new Thread(automate_history()).start();

            update_trailer_and_playlist_key();
            Platform.runLater(() -> {
                display_trailers(read_jsonArray_from_file(new File(format_path_name_to_current_os(TRAILERS_JSON_FILE))));
            });

            moviesPathTF.setOnAction(event -> {
                the_selected_files_are_valid_movies();
                add_movie_directory();
            });
            seriesPathTF.setOnAction(event -> verify_series());
            trailersPathTF.setOnAction(event -> verify_trailers());
            senderEmailTF.textProperty().addListener(((observable, oldValue, newValue) -> {
                if (email_is_in_correct_format(newValue)) {
                    senderEmailTF.setStyle("-fx-text-fill : rgb(255, 255, 255);" +
                            "-jfx-unfocus-color :  rgb(255, 255, 255);" +
                            "-jfx-focus-color : linear-gradient(#FFB900, #F0D801);");
                } else {
                    senderEmailTF.setStyle("-fx-text-fill : rgb(241, 58, 58);" +
                            "-jfx-unfocus-color :  rgb(255, 255, 255);" +
                            "-jfx-focus-color : linear-gradient(#FFB900, #F0D801);");
                }
            }));
        } catch (Exception e) {
            e.printStackTrace();
            programmer_error(e).show();
            new Thread(write_stack_trace(e)).start();
        }
    }

    public Task<Boolean> send_email(final String receiverEmail, final String text) {
        return new Task<Boolean>() {
            @Override
            protected Boolean call() {
                if (inform_developer(receiverEmail, text)) {
                    if (send_automatic_reply_to_user(receiverEmail)) {
                        return true;
                    } else {
                        Platform.runLater(() -> error_message("Failed!", "Movie hub was not able to quickly reply to your message").show());
                    }
                } else {
                    Platform.runLater(() -> error_message("Failed!", "Your message was not sent to the developer").show());
                }
                this.exceptionProperty().addListener(((observable, oldValue, newValue) -> {
                    if (newValue != null) {
                        Exception e = (Exception) newValue;
                        e.printStackTrace();
                        programmer_error(e).show();
                        new Thread(write_stack_trace(e)).start();
                    }
                }));
                return false;
            }
        };
    }

    private Task<Object> automate_history() {
        return new Task<Object>() {
            @Override
            protected Object call() {
                while (true) {
                    try {
                        Platform.runLater(() -> {
                            display_all_history(create_history_list(read_jsonArray_from_file(new File(format_path_name_to_current_os(ACTIVITY_JSON_FILE)))));
                        });
                        Thread.sleep(5000);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    } catch (Exception e) {
                        e.printStackTrace();
                        break;
                    }
                }

                this.exceptionProperty().addListener(((observable, oldValue, newValue) -> {
                    if (newValue != null) {
                        Exception e = (Exception) newValue;
                        e.printStackTrace();
                        programmer_error(e).show();
                        new Thread(write_stack_trace(e)).start();
                    }
                }));
                return null;
            }
        };
    }

    private void display_trailers(JsonArray jsonArray) {
        final ObservableList<Node> nodeObservableList = trailerBox.getChildren();
        for (Node node : nodeObservableList) {
            Platform.runLater(() -> trailerBox.getChildren().remove(node));
        }
        if (jsonArray.size() > 0) {
            jsonArray.forEach(jsonElement -> {
                try {
                    Trailer.path = jsonElement.getAsString();
                    final Node node = FXMLLoader.load(getClass().getResource("/_fxml/trailerDirectoryUI.fxml"));
                    Platform.runLater(() -> trailerBox.getChildren().add(node));
                    new SlideInRight(node).play();
                    Trailer.path = null;
                } catch (IOException e) {
                    e.printStackTrace();
                    programmer_error(e).show();
                    new Thread(write_stack_trace(e)).start();
                }
            });
        }
        trailersPathTF.setText("");
    }

    public boolean email_is_in_correct_format(String param) {
        return Pattern.matches("^[\\w!#$%&’*+/=?`{|}~^-]+(?:\\.[\\w!#$%&’*+/=?`{|}~^-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,6}$", param);
    }

    private boolean verify_trailers() {
        if (trailersPathTF.getText().trim().length() > 0 || trailersPathTF.getText() != null) {
            trailersPathTF.setText(trailersPathTF.getText().trim().replace("\"", ""));
            File file = new File(trailersPathTF.getText());
            if (file.isFile()) {
                recentDirectory = file.getParentFile().getAbsolutePath();
            } else if (file.isDirectory()) {
                recentDirectory = trailersPathTF.getText();
            }
        }
        final DirectoryChooser directoryChooser = new DirectoryChooser();
        if (!recentDirectory.isEmpty()) {
            File file = new File(recentDirectory);
            if (file.exists()) {
                directoryChooser.setInitialDirectory(file);
            }
        }
        final File trailersFolder = directoryChooser.showDialog(Main.stage);
        if (trailersFolder != null) {
            if (the_file_or_folder_has_zero_bytes(trailersFolder)) {
                if (get_sub_folders(trailersFolder).isEmpty()) {
                    error_message("Hmmh, empty folder!", "Movie hub has just found that the folder is empty").show();
                    return false;
                }
            }
            final com.google.gson.JsonObject jsonObject = get_details_about_number_of_seasons_and_episodes_of_a_series(trailersFolder);
            if (new Gson().fromJson(jsonObject.get("episodes"), Integer.class) == 0) {
                error_message("Hmmh, folder is empty!", "Movie hub could not find any valid video files in the folder").show();
                return false;
            }
            JsonElement jsonElement = new Gson().toJsonTree(trailersFolder.getAbsolutePath(), String.class);
            JsonArray jsonArray = read_jsonArray_from_file(new File(format_path_name_to_current_os(TRAILERS_JSON_FILE)));
            for (JsonElement jsonElement1 : jsonArray) {
                if (jsonElement1.equals(jsonElement)) {
                    error_message("Hmmh, duplicate found!", "Movie hub already has the path you have just selected").show();
                    return false;
                }
            }
            jsonArray.add(jsonElement);
            if (!write_jsonArray_to_file(jsonArray, format_path_name_to_current_os(TRAILERS_JSON_FILE))) {
                error_message("Failed!", "The path was not deleted!");
            }
            final String path = trailersFolder.getAbsolutePath();
            trailersPathTF.setText(path);
            recentDirectory = path;
            final HashMap<String, String> stringStringHashMap = new HashMap<>();
            final JsonArray jsonArray1 = get_directory_of_sub_files_in_the_provided_folder(trailersFolder);
            for (JsonElement jsonElement1 : jsonArray1) {
                final String steamingMediaKey = get_unique_word_as_key(new HashMap<>());
                stringStringHashMap.put(steamingMediaKey, new Gson().fromJson(jsonElement1, String.class));
            }
            listOfTrailerIds.put(trailersFolder.getName(), stringStringHashMap);
            return true;
        } else {
            error_message("Invalid directory!", "Sadly no folder has been selected.").show();
        }
        return false;
    }

    private Map<LocalDate, JsonArray> create_history_list(JsonArray jsonInfoArray) {
        final HashMap<LocalDate, JsonArray> unorderedMap = new HashMap<>();
        jsonInfoArray.forEach(jsonElement -> {
            final History history = new Gson().fromJson(jsonElement, History.class);
            final LocalDate localDate = LocalDate.parse(history.getDate(), dateTimeFormatter);
            if (unorderedMap.containsKey(localDate)) {
                unorderedMap.get(localDate).add(new Gson().toJsonTree(history, History.class));
            } else {
                JsonArray jsonArray = new JsonArray();
                jsonArray.add(new Gson().toJsonTree(history, History.class));
                unorderedMap.put(localDate, jsonArray);
            }
        });
        return new TreeMap<>(unorderedMap).descendingMap();
    }

    private void display_all_history(Map<LocalDate, JsonArray> localDateJsonArrayMap) {
        int count = 7;
        for (Map.Entry<LocalDate, JsonArray> entry : localDateJsonArrayMap.entrySet()) {
            if (count == 0) {
                break;
            }
            LocalDate date = entry.getKey();
            JsonArray histories = entry.getValue();
            try {
                ObservableList<Node> nodeObservableList = historyBox.getChildren();
                for (Node node : nodeObservableList) {
                    Platform.runLater(() -> {
                        VBox.clearConstraints(node);
                        historyBox.getChildren().remove(node);
                    });
                }
                final JsonObject jsonObject = new JsonObject();
                jsonObject.add("date", new Gson().toJsonTree(dateTimeFormatter.format(date), String.class));
                jsonObject.add("history", new Gson().toJsonTree(histories, JsonArray.class));
                MyHistory.jsonObject = jsonObject;
                Node node = FXMLLoader.load(getClass().getResource("/_fxml/historyUI.fxml"));
                Platform.runLater(() -> historyBox.getChildren().add(node));
                MyHistory.jsonObject = null;
            } catch (IOException e) {
                e.printStackTrace();
                new Thread(write_stack_trace(e)).start();
                Platform.runLater(() -> programmer_error(e).show());
                break;
            }
            --count;
        }
    }

    private void hide_the_current_pane() {
        if (homePane.getOpacity() > 0) {
            new FadeOutLeft(homePane).play();
        } else if (trailerPage.getOpacity() > 0) {
            new FadeOutLeft(trailerPage).play();
        } else if (emailPage.getOpacity() > 0) {
            new FadeOutLeft(emailPage).play();
        } else if (historyPage.getOpacity() > 0) {
            new FadeOutLeft(historyPage).play();
        } else if (aboutPage.getOpacity() > 0) {
            new FadeOutLeft(aboutPage).play();
        } else {
            new FadeOutLeft(bridgePane).play();
        }
    }

    private void hide_menu() {
        if (menuVBox.getOpacity() > 0) {
            menuVBox.toBack();
            new FadeOutRight(menuVBox).play();
        }
    }

    private void show_menu() {
        if (menuVBox.getOpacity() < 1) {
            menuVBox.toFront();
            new FadeInRight(menuVBox).play();
        } else {
            hide_menu();
        }
    }

    private File get_name_of_the_new_destination_folder_after_its_duplicate_is_found(File destinationFolder) {
        if (destinationFolder.exists()) {
            destinationFolder = new File(destinationFolder.getAbsolutePath().concat("_").concat(String.format("%.0f", Math.random())));
            get_name_of_the_new_destination_folder_after_its_duplicate_is_found(destinationFolder);
            error_message("Hmmh, a duplicate destination found!", "Worry not because i just created a new one for you, i gotcha :)").graphic(new ImageView(new Image("/_images/icons8_Error_48px.png"))).position(Pos.BASELINE_RIGHT).show();
        }
        return destinationFolder;
    }

    private String get_formatted_size_of_selected_list_of_media() {
        final ObservableList<String> allMediaPaths = FXCollections.observableArrayList();
        if (!LIST_OF_SELECTED_MOVIES.isEmpty()) {
            allMediaPaths.addAll(LIST_OF_SELECTED_MOVIES);
        }
        if (!LIST_OF_SELECTED_SERIES.isEmpty()) {
            allMediaPaths.addAll(LIST_OF_SELECTED_SERIES);
        }
        double bytes = 0;
        for (String mediaFilePath : allMediaPaths) {
            bytes += get_size_of_the_provided_file_or_folder(new File(mediaFilePath));
        }
        return make_bytes_more_presentable(bytes);
    }

    private double get_pricing() {
        double result = 25;
        try {
            final BufferedReader bufferedReader = new BufferedReader(new FileReader(new File(Main.RESOURCE_PATH + "\\_config\\settings.json")));
            final com.google.gson.JsonObject jsonObject = new Gson().fromJson(bufferedReader, com.google.gson.JsonObject.class);
            bufferedReader.close();
            result = jsonObject.get("price").getAsDouble();
        } catch (IOException ex) {
            ex.printStackTrace();
            programmer_error(ex).show();
        }
        return result;
    }

    private double get_cost_of_selected_movies_or_series_or_both(final double costPerMedia) {
        double result = 0;
        final ObservableList<String> allMediaPaths = FXCollections.observableArrayList();
        if (!LIST_OF_SELECTED_MOVIES.isEmpty()) {
            allMediaPaths.addAll(LIST_OF_SELECTED_MOVIES);
        }
        if (!LIST_OF_SELECTED_SERIES.isEmpty()) {
            allMediaPaths.addAll(LIST_OF_SELECTED_SERIES);
        }
        if (!allMediaPaths.isEmpty()) {
            result = costPerMedia * allMediaPaths.size();
        }
        return result;
    }

    private boolean show_information_of_the_amount_the_customer_should_pay_and_set_account_number_it_should_be_paid_to(final String accountName, final double cost) {
        final Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.initOwner(Main.stage);
        alert.setTitle(accountName);
        alert.setHeaderText("Cost is Ksh ".concat(String.format("%,.1f", cost)).concat("."));
        alert.setContentText("Click \"Start\" to begin copying the selected media.");
        final ButtonType okayBtn = new ButtonType("Start");
        final ButtonType cancelBtn = new ButtonType("Cancel the package.");
        alert.getButtonTypes().clear();
        alert.getButtonTypes().addAll(okayBtn, cancelBtn);
        final Optional<ButtonType> result = alert.showAndWait();
        return result.map(buttonType -> buttonType.equals(okayBtn)).orElse(false);
    }

    private double get_size_of_the_selected_movies_and_series(ObservableList<String> allMediaPaths) {
        double bytes = 0;
        for (String mediaFilePath : allMediaPaths) {
            bytes += get_size_of_the_provided_file_or_folder(new File(mediaFilePath));
        }
        return bytes;
    }

    private String create_name_for_new_packages(final String sizeOfList) {
        TextInputDialog textInputDialog = new TextInputDialog("package_" + LIST_OF_COPY_THREADS.size());
        textInputDialog.initOwner(Main.stage);
        textInputDialog.setTitle("You are almost there...");
        textInputDialog.setHeaderText("List size is ".concat(sizeOfList));
        textInputDialog.setContentText("Please enter a unique name for this package: ");
        final Optional<String> userChoice = textInputDialog.showAndWait();
        return userChoice.orElse(null);
    }

    private void update_movies_list() {
        try {
            ObservableList<Node> nodeObservableList = moviesBox.getChildren();
            for (Node node : nodeObservableList) {
                Platform.runLater(() -> moviesBox.getChildren().remove(node));
            }
            for (int index = 1; index <= moviesCount; ++index) {
                final String serial = String.format("%d", index);
                if (MOVIE_LIST.containsKey(serial)) {
                    final String path = (String) MOVIE_LIST.get(serial);
                    SelectedFile.serial = serial;
                    SelectedFile.path = path;
                    SelectedFile.category = "movie";
                    final Node node = FXMLLoader.load(getClass().getResource("/_fxml/selectedMediaDirectoryUI.fxml"));
                    Platform.runLater(() -> moviesBox.getChildren().add(node));
                    new SlideInRight(node).play();
                    if (!LIST_OF_SELECTED_MOVIES.contains(SelectedFile.path)) {
                        LIST_OF_SELECTED_MOVIES.add(SelectedFile.path);
                    }
                    SelectedFile.serial = "";
                    SelectedFile.path = "";
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            programmer_error(e).show();
            new Thread(write_stack_trace(e)).start();
        }
    }

    private void update_series_list() {
        ObservableList<Node> nodeObservableList = seriesBox.getChildren();
        for (Node node : nodeObservableList) {
            Platform.runLater(() -> seriesBox.getChildren().remove(node));
        }
        try {
            for (int index = 1; index <= seriesCount; ++index) {
                final String serial = String.format("%d", index);
                if (SERIES_LIST.containsKey(serial)) {
                    final String path = (String) SERIES_LIST.get(serial);
                    SelectedFile.serial = serial;
                    SelectedFile.path = path;
                    SelectedFile.category = "series";
                    final Node node = FXMLLoader.load(getClass().getResource("/_fxml/selectedMediaDirectoryUI.fxml"));
                    Platform.runLater(() -> seriesBox.getChildren().add(node));
                    new SlideInRight(node).play();
                    if (!LIST_OF_SELECTED_SERIES.contains(SelectedFile.path)) {
                        LIST_OF_SELECTED_SERIES.add(SelectedFile.path);
                    }
                    SelectedFile.serial = "";
                    SelectedFile.path = "";
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            programmer_error(e).show();
            new Thread(write_stack_trace(e)).start();
        }
    }

    private boolean verify_series() {
        if (seriesPathTF.getText().trim().length() > 0 || seriesPathTF.getText() != null) {
            seriesPathTF.setText(seriesPathTF.getText().trim().replace("\"", ""));
            File file = new File(seriesPathTF.getText());
            if (file.exists()) {
                if (file.isFile()) {
                    recentDirectory = file.getParentFile().getAbsolutePath();
                } else if (file.isDirectory()) {
                    recentDirectory = seriesPathTF.getText();
                }
            }
        }
        final DirectoryChooser directoryChooser = new DirectoryChooser();
        if (!recentDirectory.isEmpty()) {
            directoryChooser.setInitialDirectory(new File(recentDirectory));
        }
        final File seriesFolder = directoryChooser.showDialog(Main.stage);
        if (seriesFolder != null) {
            List<File> folderList = new ArrayList<>();
            folderList.add(seriesFolder);
            return verify_selected_series_folders_files(folderList);
        } else {
            error_message("Invalid directory!", "Sadly no folder has been selected.").show();
            return false;
        }
    }

    private boolean verify_selected_series_folders_files(List<File> seriesFolders) {
        for (File seriesFolder : seriesFolders) {
            if (the_file_or_folder_has_zero_bytes(seriesFolder)) {
                if (get_sub_folders(seriesFolder).isEmpty()) {
                    error_message("Hmmh, empty folder!", "Movie hub has just found that the folder is empty").show();
                    return false;
                }
            }
            final com.google.gson.JsonObject jsonObject = get_details_about_number_of_seasons_and_episodes_of_a_series(seriesFolder);
            if (new Gson().fromJson(jsonObject.get("episodes"), Integer.class) == 0) {
                error_message("Hmmh, episode count error!", "Movie hub could not find any valid video files in the folder").show();
                return false;
            }
            if (LIST_OF_SELECTED_SERIES.contains(seriesFolder.getAbsolutePath())) {
                error_message("Hmmh, duplicate found!", "Movie hub already has the path to the movie you have just selected").show();
                return false;
            }
            final String path = seriesFolder.getAbsolutePath();
            seriesPathTF.setText(path);
            SERIES_LIST.put(String.format("%d", ++seriesCount), path);
            recentDirectory = path;
        }
        return true;
    }

    private boolean the_selected_files_are_valid_movies() {
        if (moviesPathTF.getText().trim().length() > 0 || moviesPathTF.getText() != null) {
            moviesPathTF.setText(moviesPathTF.getText().trim().replace("\"", ""));
            final File file = new File(moviesPathTF.getText());
            if (file.isFile()) {
                //recentDirectory = file.getParentFile().getAbsolutePath();
                recentDirectory = file.getParentFile().getAbsolutePath();
            } else if (file.isDirectory()) {
                recentDirectory = moviesPathTF.getText();
            }
        }
        final FileChooser fileChooser = new FileChooser();
        final String[] extensions = EXTENSIONS_SUPPORTED_BY_VLC.replace(":", ":*").split(":");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Movie File", extensions));
        if (!recentDirectory.isEmpty()) {
            File file = new File(recentDirectory);
            if (file.exists()) {
                fileChooser.setInitialDirectory(file);
            }
        }
        final List<File> movieFiles = fileChooser.showOpenMultipleDialog(Main.stage);
        return verify_selected_movie_files(movieFiles);
    }

    private boolean verify_selected_movie_files(List<File> movieFiles) {
        if (movieFiles == null) {
            error_message("Invalid directory!", "Sadly no directory was selected to save the selected media").show();
            return false;
        }
        for (File movieFile : movieFiles) {
            if (LIST_OF_SELECTED_MOVIES.contains(movieFile.getAbsolutePath())) {
                error_message("Hmmh, duplicate found!", "A movie you have selected has already been selected and it will be ignored, meanwhile stay tuned for more info").show();
                error_message("DETAILS", movieFile.getName().concat(" is the duplicate.")).show();
            } else {
                if (the_file_or_folder_has_zero_bytes(movieFile)) {
                    error_message("Hmmh, a bad file found!", "A movie has zero bytes and it will be ignored, meanwhile stay tuned for more info").show();
                    error_message("DETAILS", movieFile.getName().concat(" has zero bytes.")).show();
                } else {
                    try {
                        final String path = movieFile.getAbsolutePath();
                        moviesPathTF.setText(path);
                        recentDirectory = movieFile.getParentFile().getAbsolutePath();
                        MOVIE_LIST.put(String.format("%d", ++moviesCount), path);
                    } catch (Exception e) {
                        e.printStackTrace();
                        programmer_error(e).show();
                        return false;
                    }
                }
            }
        }
        return true;
    }

    private void add_series_directory() {
        final ObservableList<Node> nodeObservableList = seriesBox.getChildren();
        for (Node node : nodeObservableList) {
            Platform.runLater(() -> seriesBox.getChildren().remove(node));
        }
        try {
            for (int index = 1; index <= seriesCount; ++index) {
                final String serial = String.format("%d", index);
                if (SERIES_LIST.containsKey(serial)) {
                    final String path = (String) SERIES_LIST.get(serial);
                    SelectedFile.serial = serial;
                    SelectedFile.path = path;
                    SelectedFile.category = "series";
                    final Node node = FXMLLoader.load(getClass().getResource("/_fxml/selectedMediaDirectoryUI.fxml"));
                    Platform.runLater(() -> seriesBox.getChildren().add(node));
                    new SlideInRight(node).play();
                    if (!LIST_OF_SELECTED_SERIES.contains(SelectedFile.path)) {
                        LIST_OF_SELECTED_SERIES.add(SelectedFile.path);
                    }
                    SelectedFile.serial = "";
                    SelectedFile.path = "";
                }
            }
            seriesPathTF.setText("");
        } catch (IOException e) {
            e.printStackTrace();
            programmer_error(e).show();
            new Thread(write_stack_trace(e)).start();
        }
    }

    private void add_movie_directory() {
        final ObservableList<Node> nodeObservableList = moviesBox.getChildren();
        for (Node node : nodeObservableList) {
            Platform.runLater(() -> moviesBox.getChildren().remove(node));
        }
        try {
            for (int index = 1; index <= moviesCount; ++index) {
                final String serial = String.format("%d", index);
                if (MOVIE_LIST.containsKey(serial)) {
                    final String path = (String) MOVIE_LIST.get(serial);
                    SelectedFile.serial = serial;
                    SelectedFile.path = path;
                    SelectedFile.category = "movie";
                    final Node node = FXMLLoader.load(getClass().getResource("/_fxml/selectedMediaDirectoryUI.fxml"));
                    Platform.runLater(() -> moviesBox.getChildren().add(node));
                    new SlideInRight(node).play();
                    if (!LIST_OF_SELECTED_MOVIES.contains(SelectedFile.path)) {
                        LIST_OF_SELECTED_MOVIES.add(SelectedFile.path);
                    }
                    SelectedFile.serial = "";
                    SelectedFile.path = "";
                }
            }
            moviesPathTF.setText("");
        } catch (IOException e) {
            e.printStackTrace();
            programmer_error(e).show();
            new Thread(write_stack_trace(e)).start();
        }
    }

}
