package org._movie_hub._next_gen_edition._controller;

import animatefx.animation.*;
import com.github.cliftonlabs.json_simple.JsonObject;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
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
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import org._movie_hub._next_gen_edition.Main;
import org._movie_hub._next_gen_edition._custom.Email;
import org._movie_hub._next_gen_edition._object.History;
import org._movie_hub._next_gen_edition._object.Job;
import org.apache.commons.io.FileUtils;
import org.jetbrains.annotations.NotNull;

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

@SuppressWarnings("unused")
public class Home extends Email implements Initializable {

    public final static HashMap<String, Job> listOfCopyThreads = new HashMap<>();
    public final static String EXTENSIONS_FOR_MOVIES_ONLY = ".m4v:.mpeg1:.mpeg2:.flv:.mkv:.mov:.mpeg4:.vob:.avi:.mpeg:.m4a:.3gp:.mp4:.m4p";
    public final static HashMap<String, Job> listOfUploadThreads = new HashMap<>();
    protected final static ObservableList<String> listOfStartedThreads = FXCollections.observableArrayList();
    protected final static JsonObject movieList = new JsonObject();
    protected final static JsonObject seriesList = new JsonObject();
    protected final static List<String> listOfSelectedMovies = new ArrayList<>();
    protected final static List<String> listOfSelectedSeries = new ArrayList<>();
    public static HashMap<String, HashMap<String, String>> listOfTrailerIds;
    protected final String EXTENSIONS_SUPPORTED_BY_VLC = ".asx:.dts:.gxf:.m2v:.m3u:.m4v:.mpeg1:.mpeg2:.mts:.mxf:.pls:.divx:.dv:.flv:.m1v:.m2ts:.mkv:.mov:.mpeg4:.ts:.vlc:.vob:.3g2:.avi:.mpeg:.mpg:.m4a:.3gp:.srt:.wmv:.asf:.mp4:.m4p";
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
    void allow_to_copy_to_removable_drive(ActionEvent event) {
        final String TIME_IT_STARTED = "[".concat(new SimpleDateFormat("HH:mm:ss:SSS").format(Calendar.getInstance().getTime())).concat("]");
        try {
            new FadeOut(questionPane).play();
            tasksPane.toFront();
            new FadeIn(tasksPane).play();
            if (listOfSelectedMovies.isEmpty() && listOfSelectedSeries.isEmpty() && !Home.listOfCopyThreads.isEmpty()) {
                return;
            } else if (listOfSelectedMovies.isEmpty() && listOfSelectedSeries.isEmpty()) {
                if (taskBox.getChildren().size() < 1) {
                    if (emptyListLbl.getOpacity() < 1) {
                        new FadeOut(jobPane).play();
                        emptyListLbl.toFront();
                        new FadeIn(emptyListLbl).play();
                    }
                    new Wobble(emptyListLbl).play();
                }
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
                String jobName = create_new_name_for_copy_jobs(sizeOfWhatWeWantToCopy);
                if (jobName != null) {
                    final double cost = get_cost_of_selected_movies_or_series_or_both(get_pricing());
                    if (show_information_of_the_amount_the_customer_should_pay_and_set_account_number_it_should_be_paid_to(jobName, cost)) {
                        if (Home.listOfCopyThreads.containsKey(jobName)) {
                            error_message("Given task name exists!", "Please type a unique name for the task").show();
                        } else {
                            destinationFolder = new File(destinationFolder.getAbsolutePath().concat("\\").concat(jobName));
                            destinationFolder = get_name_of_the_new_destination_folder_after_its_duplicate_is_found(destinationFolder);
                            jobName = destinationFolder.getName();
                            FileUtils.forceMkdir(destinationFolder);
                            final File chosenDestination = destinationFolder;
                            final ObservableList<String> allMediaPaths = FXCollections.observableArrayList();
                            allMediaPaths.addAll(listOfSelectedMovies);
                            allMediaPaths.addAll(listOfSelectedSeries);
                            final Job job = new Job();
                            job.setForUpload(false);
                            job.setDestinationFolder(chosenDestination);
                            job.setJobName(jobName);
                            job.setCost(cost);
                            job.setAllMediaPaths(allMediaPaths);
                            job.setSourceSize(get_size_of_the_selected_movies_and_series(allMediaPaths));
                            listOfSelectedMovies.clear();
                            listOfSelectedSeries.clear();
                            movieList.clear();
                            seriesList.clear();
                            if (!listOfStartedThreads.contains(jobName)) {
                                Home.listOfCopyThreads.put(jobName, job);
                                try {
                                    final String END_TIME = "[".concat(new SimpleDateFormat("HH:mm:ss:SSS").format(Calendar.getInstance().getTime())).concat("]");
                                    final History history = new History();
                                    history.setTimeWhenItStarted(TIME_IT_STARTED);
                                    history.setTimeWhenItStopped(END_TIME);
                                    history.setDate(get_date());
                                    history.setJobName(job.getJobName().concat(" (COPY_2_DRIVE)"));
                                    history.setListOfMedia(new ArrayList<>());
                                    history.setStatus("CREATED");
                                    final List<String> objectList = job.getAllMediaPaths();
                                    for (String pathToMedia : objectList) {
                                        history.getListOfMedia().add(new File(pathToMedia).getName());
                                    }
                                    new Thread(write_log("New Copy to drive", history)).start();
                                    MyTasks.taskName = jobName;
                                    final Node node = FXMLLoader.load(getClass().getResource("/org/_movie_hub/_next_gen_edition/_fxml/taskUI.fxml"));
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
                        error_message_alert("Billing declined!", "The job will NOT be started because the customer has not approved the job to start").show();
                    }
                } else {
                    error_message_alert("No task name was provided!", "Please ensure you enter the name of the task to continue").show();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            programmer_error(e).show();
            new Thread(write_stack_trace(e)).start();
        }
    }

    @FXML
    void allow_to_upload_to_a_mobile_phone(ActionEvent event) {
        final String TIME_IT_STARTED = "[".concat(new SimpleDateFormat("HH:mm:ss:SSS").format(Calendar.getInstance().getTime())).concat("]");
        if (listOfSelectedMovies.isEmpty() && listOfSelectedSeries.isEmpty()) {
            warning_message("Incomplete!", "Please select at least one movie file or series folder ot continue").show();
        }
        try {
            new FadeOut(questionPane).play();
            tasksPane.toFront();
            new FadeIn(tasksPane).play();
            if (listOfSelectedMovies.isEmpty() && listOfSelectedSeries.isEmpty() && !Home.listOfUploadThreads.isEmpty()) {
                return;
            } else if (listOfSelectedMovies.isEmpty() && listOfSelectedSeries.isEmpty()) {
                if (taskBox.getChildren().size() < 1) {
                    if (emptyListLbl.getOpacity() < 1) {
                        new FadeOut(jobPane).play();
                        emptyListLbl.toFront();
                        new FadeIn(emptyListLbl).play();
                    }
                    new Wobble(emptyListLbl).play();
                }
                return;
            } else {
                if (jobPane.getOpacity() < 1) {
                    new FadeOut(emptyListLbl).play();
                    jobPane.toFront();
                    new FadeIn(jobPane).play();
                }
            }
            final String sizeOfWhatWeWantToCopy = get_formatted_size_of_selected_list_of_media();
            final String jobName = create_new_name_for_copy_jobs(sizeOfWhatWeWantToCopy);
            if (jobName != null) {
                final double cost = get_cost_of_selected_movies_or_series_or_both(get_pricing());
                if (show_information_of_the_amount_the_customer_should_pay_and_set_account_number_it_should_be_paid_to(jobName, cost)) {
                    if (Home.listOfUploadThreads.containsKey(jobName)) {
                        error_message("Given task name exists!", "Please type a unique name for the task").show();
                    } else {
                        final ObservableList<String> allMediaPaths = FXCollections.observableArrayList();
                        allMediaPaths.addAll(listOfSelectedMovies);
                        allMediaPaths.addAll(listOfSelectedSeries);
                        final Job job = new Job();
                        job.setForUpload(true);
                        job.setJobName(jobName);
                        job.setCost(cost);
                        job.setAllMediaPaths(allMediaPaths);
                        job.setSourceSize(get_size_of_the_selected_movies_and_series(allMediaPaths));
                        listOfSelectedMovies.clear();
                        listOfSelectedSeries.clear();
                        movieList.clear();
                        seriesList.clear();
                        if (!listOfStartedThreads.contains(jobName)) {
                            Home.listOfUploadThreads.put(jobName, job);
                            try {
                                final String END_TIME = "[".concat(new SimpleDateFormat("HH:mm:ss:SSS").format(Calendar.getInstance().getTime())).concat("]");
                                final History history = new History();
                                history.setTimeWhenItStarted(TIME_IT_STARTED);
                                history.setTimeWhenItStopped(END_TIME);
                                history.setDate(get_date());
                                history.setJobName(job.getJobName().concat(" (UPLOAD)"));
                                history.setListOfMedia(new ArrayList<>());
                                history.setStatus("CREATED");
                                final List<String> objectList = job.getAllMediaPaths();
                                for (String pathToMedia : objectList) {
                                    history.getListOfMedia().add(new File(pathToMedia).getName());
                                }
                                new Thread(write_log("New Upload", history)).start();
                                MyTasks.taskName = jobName;
                                final Node node = FXMLLoader.load(getClass().getResource("/org/_movie_hub/_next_gen_edition/_fxml/taskUI.fxml"));
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
                    error_message_alert("Billing declined!", "The job will NOT be started because the customer has not approved the job to start").show();
                }
            } else {
                error_message_alert("No task name was provided!", "Please ensure you enter the name of the task to continue").show();
            }
        } catch (Exception e) {
            e.printStackTrace();
            programmer_error(e).show();
            new Thread(write_stack_trace(e)).start();
        }
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
    }

    @FXML
    void choose_directory(@NotNull ActionEvent event) {
        try {
            if (event.getSource().equals(trailerBtn)) {
                if (verify_trailers()) {
                    display_trailers(get_app_details_as_object(new File(TRAILERS_JSON_FILE)));
                    new RubberBand(((Node) event.getSource())).play();
                }
            } else if (event.getSource().equals(forMovies)) {
                if (verify_movie()) {
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
    }

    @FXML
    void go_back_to_home_page(ActionEvent event) {
        if (homePane.getOpacity() < 1) {
            hide_the_current_pane();
            homePane.toFront();
            new FadeInRight(homePane).play();
        }
    }

    @FXML
    void go_back_to_select_an_action(ActionEvent event) {
        new FadeOut(tasksPane).play();
        questionPane.toFront();
        new FadeIn(questionPane).play();
    }

    @FXML
    void go_back_to_movie(ActionEvent event) {
        new FadeOutRight(seriesPane).play();
        moviesPane.toFront();
        new FadeInLeft(moviesPane).play();
    }

    @FXML
    void go_to_series(ActionEvent event) {
        new FadeOutLeft(moviesPane).play();
        seriesPane.toFront();
        new FadeInRight(seriesPane).play();
    }

    @FXML
    void place_directory_into_cart(@NotNull ActionEvent event) {
        boolean isAMovie = true;
        try {
            if (event.getSource().equals(directoryForMovies)) {
                if (moviesPathTF.getText().trim().isEmpty() || moviesPathTF.getText() == null) {
                    empty_and_null_pointer_message(moviesPathTF).show();
                    return;
                }
                add_movie_directory();
            } else {
                isAMovie = false;
                add_series_directory();
            }
        } catch (NullPointerException e) {
            if (isAMovie) {
                if (verify_movie()) {
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
    }

    @FXML
    void show_about_page(ActionEvent actionEvent) {
        show_menu();
        if (aboutPage.getOpacity() < 1) {
            hide_the_current_pane();
            aboutPage.toFront();
            new FadeInRight(aboutPage).play();
        }
        hide_menu();
    }

    @FXML
    void send_mail_to_developer(ActionEvent event) {
        if (senderEmailTF.getText().isEmpty() || senderEmailTF.getText().trim().isEmpty() || senderEmailTF.getText() == null) {
            empty_and_null_pointer_message(senderEmailTF).show();
            return;
        }
        if (!email_is_in_correct_format(senderEmailTF.getText().trim())) {
            error_message("Bad email!", "Kindly ensure that the email you have provided is in the correct formant").show();
            return;
        }
        if (emailMessageTA.getText().isEmpty() || emailMessageTA.getText().trim().isEmpty() || emailMessageTA.getText() == null) {
            empty_and_null_pointer_message(emailMessageTA).show();
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
        task.exceptionProperty().addListener(((observable, oldValue, newValue) -> {
            if (newValue != null) {
                Exception e = (Exception) newValue;
                e.printStackTrace();
                programmer_error(e).show();
                new Thread(write_stack_trace(e)).start();
            }
        }));
        new Thread(task).start();
    }

    @FXML
    void show_email_page(ActionEvent event) {
        if (emailPage.getOpacity() < 1) {
            hide_the_current_pane();
            emailPage.toFront();
            new FadeInRight(emailPage).play();
        }
        hide_menu();
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
    }

    @FXML
    void show_home_page(ActionEvent event) {
        if (homePane.getOpacity() < 1) {
            hide_the_current_pane();
            homePane.toFront();
            new FadeInRight(homePane).play();
        }
        hide_menu();
    }

    @FXML
    void show_menu_options(ActionEvent event) {
        show_menu();
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
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            final ObservableList<Node> observableList = optionsBox.getChildren();
            for (Node node : observableList) {
                if (node instanceof JFXButton) {
                    final JFXButton jfxButton = (JFXButton) node;
                    final Pulse pulse = new Pulse(jfxButton);
                    jfxButton.setOnMouseEntered(event -> pulse.play());
                    jfxButton.setOnMouseExited(event -> pulse.stop());
                }
            }

            hide_menu();

            final Task<Object> objectTask = automate_history();
            objectTask.exceptionProperty().addListener(((observable, oldValue, newValue) -> {
                if (newValue != null) {
                    Exception e = (Exception) newValue;
                    e.printStackTrace();
                    programmer_error(e).show();
                    new Thread(write_stack_trace(e)).start();
                }
            }));
            new Thread(objectTask).start();

            update_trailer_and_playlist_key();
            Platform.runLater(() -> display_trailers(get_app_details_as_object(new File(TRAILERS_JSON_FILE))));

            moviesPathTF.setOnAction(event -> {
                verify_movie();
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

    public @NotNull Task<Boolean> send_email(final String receiverEmail, final String text) {
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
                return false;
            }
        };
    }

    private @NotNull Task<Object> automate_history() {
        return new Task<Object>() {
            @Override
            protected Object call() {
                while (true) {
                    try {
                        Platform.runLater(() -> display_all_history(create_history_list(get_app_details_as_object(new File(ACTIVITY_JSON_FILE)))));
                        Thread.sleep(5000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        break;
                    }
                }
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
                    final Node node = FXMLLoader.load(getClass().getResource("/org/_movie_hub/_next_gen_edition/_fxml/trailerDirectoryUI.fxml"));
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

    private @NotNull Boolean verify_trailers() {
        if (!trailersPathTF.getText().trim().isEmpty() || trailersPathTF.getText() != null) {
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
            if(file.exists()) {
                directoryChooser.setInitialDirectory(file);
            }
        }
        final File trailersFolder = directoryChooser.showDialog(Main.stage);
        if (trailersFolder != null) {
            if (the_file_has_zero_bytes(trailersFolder)) {
                error_message("Hmmh, empty folder!", "Movie hub has just found that the folder is empty").show();
                return false;
            }
            final JsonObject jsonObject = new SelectedMedia().get_details_about_number_of_seasons_and_episodes_of_a_series(trailersFolder);
            if (Integer.parseInt(jsonObject.get("episodes").toString()) == 0) {
                error_message("Hmmh, folder is empty!", "Movie hub could not find any valid video files in the folder").show();
                return false;
            }
            JsonElement jsonElement = new Gson().toJsonTree(trailersFolder.getAbsolutePath(), String.class);
            JsonArray jsonArray = get_app_details_as_object(new File(TRAILERS_JSON_FILE));
            for (JsonElement jsonElement1 : jsonArray) {
                if (jsonElement1.equals(jsonElement)) {
                    error_message("Hmmh, duplicate found!", "Movie hub already has the path you have just selected").show();
                    return false;
                }
            }
            jsonArray.add(jsonElement);
            if (!write_jsonArray_to_file(jsonArray, TRAILERS_JSON_FILE)) {
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
            Home.listOfTrailerIds.put(trailersFolder.getName(), stringStringHashMap);
            return true;
        } else {
            error_message("Invalid directory!", "Sadly no folder has been selected.").show();
            return false;
        }
    }

    private @NotNull Map<LocalDate, JsonArray> create_history_list(@NotNull JsonArray jsonInfoArray) {
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

    private void display_all_history(@NotNull Map<LocalDate, JsonArray> localDateJsonArrayMap) {
        localDateJsonArrayMap.forEach((date, histories) -> {
            try {
                ObservableList<Node> nodeObservableList = historyBox.getChildren();
                for (Node node : nodeObservableList) {
                    Platform.runLater(() -> {
                        VBox.clearConstraints(node);
                        historyBox.getChildren().remove(node);
                    });
                }
                final com.google.gson.JsonObject jsonObject = new com.google.gson.JsonObject();
                jsonObject.add("date", new Gson().toJsonTree(dateTimeFormatter.format(date), String.class));
                jsonObject.add("history", new Gson().toJsonTree(histories, JsonArray.class));
                MyHistory.jsonObject = jsonObject;
                Node node = FXMLLoader.load(getClass().getResource("/org/_movie_hub/_next_gen_edition/_fxml/historyUI.fxml"));
                Platform.runLater(() -> historyBox.getChildren().add(node));
                MyHistory.jsonObject = null;
            } catch (IOException e) {
                e.printStackTrace();
                new Thread(write_stack_trace(e)).start();
                Platform.runLater(() -> programmer_error(e).show());
            }
        });
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

    @NotNull
    private File get_name_of_the_new_destination_folder_after_its_duplicate_is_found(@NotNull File destinationFolder) {
        if (destinationFolder.exists()) {
            destinationFolder = new File(destinationFolder.getAbsolutePath().concat("_").concat(String.format("%.0f", Math.random())));
            get_name_of_the_new_destination_folder_after_its_duplicate_is_found(destinationFolder);
            error_message("Hmmh, a duplicate destination found!", "Worry not because i just created a new one for you, i gotcha :)").graphic(new ImageView(new Image("/org/_movie_hub/_next_gen_edition/_images/icons8_Error_48px.png"))).position(Pos.BASELINE_RIGHT).show();
        }
        return destinationFolder;
    }

    @NotNull
    private String get_formatted_size_of_selected_list_of_media() {
        final ObservableList<String> allMediaPaths = FXCollections.observableArrayList();
        if (!listOfSelectedMovies.isEmpty()) {
            allMediaPaths.addAll(listOfSelectedMovies);
        }
        if (!listOfSelectedSeries.isEmpty()) {
            allMediaPaths.addAll(listOfSelectedSeries);
        }
        double bytes = 0;
        for (String mediaFilePath : allMediaPaths) {
            bytes += new SelectedMedia().get_size_of_the_provided_file_or_folder(new File(mediaFilePath));
        }
        return new SelectedMedia().make_bytes_more_presentable(bytes);
    }

    private Double get_pricing() {
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

    @NotNull
    private Double get_cost_of_selected_movies_or_series_or_both(final double costPerMedia) {
        double result = 0;
        final ObservableList<String> allMediaPaths = FXCollections.observableArrayList();
        if (!listOfSelectedMovies.isEmpty()) {
            allMediaPaths.addAll(listOfSelectedMovies);
        }
        if (!listOfSelectedSeries.isEmpty()) {
            allMediaPaths.addAll(listOfSelectedSeries);
        }
        if (!allMediaPaths.isEmpty()) {
            result = costPerMedia * allMediaPaths.size();
        }
        return result;
    }

    private Boolean show_information_of_the_amount_the_customer_should_pay_and_set_account_number_it_should_be_paid_to(final String accountName, final double cost) {
        final Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.initOwner(Main.stage);
        alert.setTitle(accountName);
        alert.setHeaderText("Cost is Ksh ".concat(String.format("%,.1f", cost)).concat("."));
        alert.setContentText("Click \"Start\" to begin copying the selected media.");
        final ButtonType okayBtn = new ButtonType("Start");
        final ButtonType cancelBtn = new ButtonType("Cancel the job.");
        alert.getButtonTypes().clear();
        alert.getButtonTypes().addAll(okayBtn, cancelBtn);
        final Optional<ButtonType> result = alert.showAndWait();
        return result.map(buttonType -> buttonType.equals(okayBtn)).orElse(false);
    }

    private double get_size_of_the_selected_movies_and_series(@NotNull ObservableList<String> allMediaPaths) {
        double bytes = 0;
        for (String mediaFilePath : allMediaPaths) {
            bytes += new SelectedMedia().get_size_of_the_provided_file_or_folder(new File(mediaFilePath));
        }
        return bytes;
    }

    private String create_new_name_for_copy_jobs(final String sizeOfList) {
        TextInputDialog textInputDialog = new TextInputDialog("job_" + Home.listOfCopyThreads.size());
        textInputDialog.initOwner(Main.stage);
        textInputDialog.setTitle("You are almost there...");
        textInputDialog.setHeaderText("List size is ".concat(sizeOfList));
        textInputDialog.setContentText("Please enter a unique name for this job: ");
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
                if (movieList.containsKey(serial)) {
                    final String path = (String) movieList.get(serial);
                    SelectedMedia.serial = serial;
                    SelectedMedia.path = path;
                    SelectedMedia.category = "movie";
                    final Node node = FXMLLoader.load(getClass().getResource("/org/_movie_hub/_next_gen_edition/_fxml/selectedMediaDirectoryUI.fxml"));
                    Platform.runLater(() -> moviesBox.getChildren().add(node));
                    new SlideInRight(node).play();
                    if (!listOfSelectedMovies.contains(SelectedMedia.path)) {
                        listOfSelectedMovies.add(SelectedMedia.path);
                    }
                    SelectedMedia.serial = "";
                    SelectedMedia.path = "";
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
                if (seriesList.containsKey(serial)) {
                    final String path = (String) seriesList.get(serial);
                    SelectedMedia.serial = serial;
                    SelectedMedia.path = path;
                    SelectedMedia.category = "series";
                    final Node node = FXMLLoader.load(getClass().getResource("/org/_movie_hub/_next_gen_edition/_fxml/selectedMediaDirectoryUI.fxml"));
                    Platform.runLater(() -> seriesBox.getChildren().add(node));
                    new SlideInRight(node).play();
                    if (!listOfSelectedSeries.contains(SelectedMedia.path)) {
                        listOfSelectedSeries.add(SelectedMedia.path);
                    }
                    SelectedMedia.serial = "";
                    SelectedMedia.path = "";
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            programmer_error(e).show();
            new Thread(write_stack_trace(e)).start();
        }
    }

    private boolean the_file_has_zero_bytes(@NotNull File file) {
        return new SelectedMedia().get_size_of_the_provided_file_or_folder(file) == 0;
    }

    @NotNull
    private Boolean verify_series() {
        if (!seriesPathTF.getText().trim().isEmpty() || seriesPathTF.getText() != null) {
            seriesPathTF.setText(seriesPathTF.getText().trim().replace("\"", ""));
            File file = new File(seriesPathTF.getText());
            if (file.isFile()) {
                recentDirectory = file.getParentFile().getAbsolutePath();
            } else if (file.isDirectory()) {
                recentDirectory = seriesPathTF.getText();
            }
        }
        final DirectoryChooser directoryChooser = new DirectoryChooser();
        if (!recentDirectory.isEmpty()) {
            directoryChooser.setInitialDirectory(new File(recentDirectory));
        }
        final File seriesFolder = directoryChooser.showDialog(Main.stage);
        if (seriesFolder != null) {
            if (the_file_has_zero_bytes(seriesFolder)) {
                error_message("Hmmh, empty folder!", "Movie hub has just found that the folder is empty").show();
                return false;
            }
            final JsonObject jsonObject = new SelectedMedia().get_details_about_number_of_seasons_and_episodes_of_a_series(seriesFolder);
            if (Integer.parseInt(jsonObject.get("episodes").toString()) == 0) {
                error_message("Hmmh, episode count error!", "Movie hub could not find any valid video files in the folder").show();
                return false;
            }
            if (listOfSelectedSeries.contains(seriesFolder.getAbsolutePath())) {
                error_message("Hmmh, duplicate found!", "Movie hub already has the path to the movie you have just selected").show();
                return false;
            }
            final String path = seriesFolder.getAbsolutePath();
            seriesPathTF.setText(path);
            seriesList.put(String.format("%d", ++seriesCount), path);
            recentDirectory = path;
            return true;
        } else {
            error_message("Invalid directory!", "Sadly no folder has been selected.").show();
            return false;
        }
    }

    @NotNull
    private Boolean verify_movie() {
        if (!moviesPathTF.getText().trim().isEmpty() || moviesPathTF.getText() != null) {
            moviesPathTF.setText(moviesPathTF.getText().trim().replace("\"", ""));
            final File file = new File(moviesPathTF.getText());
            if (file.isFile()) {
                //recentDirectory = file.getParentFile().getAbsolutePath();
                recentDirectory = file.getAbsolutePath();
            } else if (file.isDirectory()) {
                recentDirectory = moviesPathTF.getText();
            }
        }
        final FileChooser fileChooser = new FileChooser();
        final String[] extensions = EXTENSIONS_SUPPORTED_BY_VLC.replace(":", ":*").split(":");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Movie File", extensions));
        if (!recentDirectory.isEmpty()) {
            File file = new File(recentDirectory);
            if(file.exists()) {
                fileChooser.setInitialDirectory(file);
            }
        }
        final List<File> movieFiles = fileChooser.showOpenMultipleDialog(Main.stage);
        if (movieFiles == null) {
            error_message("Invalid directory!", "Sadly no directory was selected to save the selected media").show();
            return false;
        }
        for (File movieFile : movieFiles) {
            if (listOfSelectedMovies.contains(movieFile.getAbsolutePath())) {
                error_message("Hmmh, duplicate found!", "A movie you have selected has already been selected and it will be ignored, meanwhile stay tuned for more info").show();
                error_message("DETAILS", movieFile.getName().concat(" is the duplicate.")).show();
            } else {
                if (the_file_has_zero_bytes(movieFile)) {
                    error_message("Hmmh, a bad file found!", "A movie has zero bytes and it will be ignored, meanwhile stay tuned for more info").show();
                    error_message("DETAILS", movieFile.getName().concat(" has zero bytes.")).show();
                } else {
                    try {
                        final String path = movieFile.getAbsolutePath();
                        moviesPathTF.setText(path);
                        recentDirectory = movieFile.getParentFile().getAbsolutePath();
                        movieList.put(String.format("%d", ++moviesCount), path);
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
                if (seriesList.containsKey(serial)) {
                    final String path = (String) seriesList.get(serial);
                    SelectedMedia.serial = serial;
                    SelectedMedia.path = path;
                    SelectedMedia.category = "series";
                    final Node node = FXMLLoader.load(getClass().getResource("/org/_movie_hub/_next_gen_edition/_fxml/selectedMediaDirectoryUI.fxml"));
                    Platform.runLater(() -> seriesBox.getChildren().add(node));
                    new SlideInRight(node).play();
                    if (!listOfSelectedSeries.contains(SelectedMedia.path)) {
                        listOfSelectedSeries.add(SelectedMedia.path);
                    }
                    SelectedMedia.serial = "";
                    SelectedMedia.path = "";
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
                if (movieList.containsKey(serial)) {
                    final String path = (String) movieList.get(serial);
                    SelectedMedia.serial = serial;
                    SelectedMedia.path = path;
                    SelectedMedia.category = "movie";
                    final Node node = FXMLLoader.load(getClass().getResource("/org/_movie_hub/_next_gen_edition/_fxml/selectedMediaDirectoryUI.fxml"));
                    Platform.runLater(() -> moviesBox.getChildren().add(node));
                    new SlideInRight(node).play();
                    if (!listOfSelectedMovies.contains(SelectedMedia.path)) {
                        listOfSelectedMovies.add(SelectedMedia.path);
                    }
                    SelectedMedia.serial = "";
                    SelectedMedia.path = "";
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
