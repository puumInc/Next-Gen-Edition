package org._movie_hub._next_gen_edition._controller;

import animatefx.animation.SlideOutLeft;
import com.github.cliftonlabs.json_simple.JsonObject;
import com.jfoenix.controls.JFXProgressBar;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import org._movie_hub._next_gen_edition._api.Server;
import org._movie_hub._next_gen_edition._custom.Watchdog;
import org._movie_hub._next_gen_edition._object.History;
import org._movie_hub._next_gen_edition._object.JobPackage;
import org.apache.commons.io.FileUtils;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author Mandela aka puumInc
 */
public class MyTasks extends Watchdog implements Initializable {

    protected static String taskName;
    private final AtomicBoolean isPaused = new AtomicBoolean();
    private final JsonObject copiedItems = new JsonObject();
    private JobPackage myJobPackage;
    private Thread thread;
    private String TIME_IT_STARTED;
    private File QR_CODE_IMAGE_FILE;

    @FXML
    private Label taskNameLbl;

    @FXML
    private JFXProgressBar taskStatusBar;

    @FXML
    private Label sizeDifferenceLbl;

    @FXML
    private void cancel_copy_task(ActionEvent event) {
        try {
            if (thread == null) {
                STRING_JOB_PACKAGE_HASH_MAP_FOR_UPLOAD.remove(taskName);
            } else {
                thread.interrupt();
                thread.join();
            }
            final Node currentNode = taskNameLbl.getParent().getParent().getParent();
            final VBox vBox = (VBox) currentNode.getParent();
            new SlideOutLeft(currentNode).play();
            Platform.runLater(() -> {
                new SlideOutLeft(currentNode);
                VBox.clearConstraints(currentNode);
                vBox.getChildren().remove(currentNode);
            });
            information_message(taskNameLbl.getText().concat(" has been stopped!"));
        } catch (Exception e) {
            e.printStackTrace();
            programmer_error(e).show();
            new Thread(write_stack_trace(e)).start();
        }
        event.consume();
    }

    @FXML
    private void pause_or_resume_copy_task(ActionEvent event) {
        information_message("Hey there, this feature is still under development. We will notify you on completion!");
        /*if (event != null) {
            if (isPaused.get()) {
                if (thread != null) {
                    isPaused.set(false);
                    System.out.println("thread.getState(): " + thread.getState());
                    if (thread.getState().equals(Thread.State.WAITING)) {
                        thread.notify();
                        new Home().warning_message("Resumed!", "The job has been resumed").show();
                    } else {
                        new Home().warning_message("#####", "The job had NOT been STOPPED!").show();
                    }
                }
            } else {
                if (thread != null) {
                    isPaused.set(true);
                }
            }
        }*/
        event.consume();
    }

    @FXML
    private void show_task_files(ActionEvent event) {
        if (this.myJobPackage != null) {
            show_list_of_copied_media("You are viewing ".concat(this.myJobPackage.getName()), generate_a_string_from_list_of_media_paths(get_file_names(this.myJobPackage.getAllMediaPaths()))).show();
            if (this.QR_CODE_IMAGE_FILE != null) {
                try {
                    show_qr_image_for_upload(this.QR_CODE_IMAGE_FILE).show();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                    new Thread(write_stack_trace(e)).start();
                    programmer_error(e).show();
                }
            }
        }
        event.consume();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        System.out.println("LIST_OF_COPY_THREADS = " + LIST_OF_COPY_THREADS);
        isPaused.set(false);
        taskNameLbl.setText(MyTasks.taskName);
        if (LIST_OF_COPY_THREADS.containsKey(MyTasks.taskName)) {
            this.myJobPackage = LIST_OF_COPY_THREADS.get(MyTasks.taskName);
        } else if (STRING_JOB_PACKAGE_HASH_MAP_FOR_UPLOAD.containsKey(MyTasks.taskName)) {
            this.myJobPackage = STRING_JOB_PACKAGE_HASH_MAP_FOR_UPLOAD.get(MyTasks.taskName);
            this.QR_CODE_IMAGE_FILE = get_a_qr_image_file_that_has_an_embedded_logo(this.myJobPackage.getName());
        } else {
            taskNameLbl.setText(MyTasks.taskName.concat("  [ERR:NOT_FOUND]"));
            return;
        }
        taskStatusBar.setProgress(0);
        taskStatusBar.progressProperty().unbind();
        if (!this.myJobPackage.isForUpload()) {
            final Task<String> task = copy_selected_media_list(this.myJobPackage.getDestinationFolder(),
                    this.myJobPackage.getName(), this.myJobPackage.getAllMediaPaths());
            task.setOnSucceeded(event1 -> {
                if (task.getValue() != null) {
                    final History history = new History();
                    history.setTimeWhenItStarted(TIME_IT_STARTED);
                    history.setTimeWhenItStopped(task.getValue());
                    history.setDate(get_date());
                    history.setJobName(this.myJobPackage.getName().concat("(Cp2D)"));
                    history.setListOfMedia(new ArrayList<>());
                    history.setStatus("COMPLETE");
                    String path = "==================================================\n"
                            .concat("From: ".concat(TIME_IT_STARTED).concat("\n"))
                            .concat("To: ".concat(task.getValue()).concat("\n"))
                            .concat("JobPackage name: ".concat(myJobPackage.getName()).concat("\n"))
                            .concat("Cost Ksh ".concat(String.format("%,.1f", this.myJobPackage.getCost())).concat("\n"))
                            .concat("The following files were copied:\n");
                    int index = 1;
                    history.setListOfMedia(new ArrayList<>());
                    final List<?> objectList = (List<?>) copiedItems.get(this.myJobPackage.getName());
                    for (Object object : objectList) {
                        history.getListOfMedia().add(new File(object.toString()).getName());
                        path = path.concat(index + ".)".concat(new File(object.toString()).getName().concat("\n\t@SOURCE_PATH { ".concat(object.toString()).concat(" }\n"))));
                        ++index;
                    }
                    path = path.concat("\n\n");
                    new Thread(write_log(path, history)).start();
                    create_a_text_file_then_write_into_it_the_list_of_files_that_have_been_copied(this.myJobPackage.getName(),
                            path, this.myJobPackage.getDestinationFolder());
                    success_notification("List " + this.myJobPackage.getName() + " has been copied completely.").show();
                    copiedItems.remove(myJobPackage.getName());
                    LIST_OF_COPY_THREADS.remove(myJobPackage.getName());
                    final Node currentNode = taskNameLbl.getParent().getParent().getParent();
                    final VBox vBox = (VBox) currentNode.getParent();

                    new SlideOutLeft(currentNode).play();
                    Platform.runLater(() -> {
                        new SlideOutLeft(currentNode);
                        VBox.clearConstraints(currentNode);
                        vBox.getChildren().remove(currentNode);
                    });
                }
            });
            task.setOnFailed(event -> {
                LIST_OF_COPY_THREADS.remove(this.myJobPackage.getName());
                final String END_TIME = "[".concat(get_time()).concat("]");
                final History history = new History();
                history.setTimeWhenItStarted(TIME_IT_STARTED);
                history.setTimeWhenItStopped(END_TIME);
                history.setDate(get_date());
                history.setJobName(this.myJobPackage.getName().concat("(Cp2D)"));
                history.setListOfMedia(new ArrayList<>());
                history.setStatus("INCOMPLETE");
                String path = "==================================================\n"
                        .concat("From: ".concat(TIME_IT_STARTED).concat("\n"))
                        .concat("To: ".concat(END_TIME).concat("\n"))
                        .concat("JobPackage name: ".concat(this.myJobPackage.getName()).concat("\n"))
                        .concat("Cost Ksh ".concat(String.format("%,.1f", this.myJobPackage.getCost())).concat("\n\n"))
                        .concat("FAILED, Something happened!\n")
                        .concat("ERR>>This job was interrupted!\n\n")
                        .concat("The following files were expected to be copied:\n");
                int index = 1;
                final List<String> objectList = this.myJobPackage.getAllMediaPaths();
                for (String pathToMedia : objectList) {
                    history.getListOfMedia().add(new File(pathToMedia).getName());
                    path = path.concat(index + ".)".concat(new File(pathToMedia).getName().concat("\t\t@SOURCE_PATH { ".concat(pathToMedia).concat(" }\n"))));
                    ++index;
                }
                new Thread(write_log(path, history)).start();
                create_a_text_file_then_write_into_it_the_list_of_files_that_have_been_copied(this.myJobPackage.getName(), path, this.myJobPackage.getDestinationFolder());
                final Node currentNode = taskNameLbl.getParent().getParent().getParent();
                final VBox vBox = (VBox) currentNode.getParent();
                Platform.runLater(() -> {
                    new SlideOutLeft(currentNode);
                    VBox.clearConstraints(currentNode);
                    vBox.getChildren().remove(currentNode);
                });
                warning_message("\"".concat(this.myJobPackage.getName()).concat("\" did not complete!"), "Sadly some or all media failed to copy.").show();
            });
            task.setOnRunning(event -> Platform.runLater(() -> information_message(this.myJobPackage.getName() + " has been started. Its status is now ACTIVE")));
            task.exceptionProperty().addListener(((observable, oldValue, newValue) -> {
                if (newValue != null) {
                    Exception e = (Exception) newValue;
                    e.printStackTrace();
                    if (e.toString().equalsIgnoreCase("there is not enough space on disk")) {
                        Platform.runLater(() -> error_message("Incomplete!", "There is not enough space on disk").show());
                        return;
                    }
                    programmer_error(e).show();
                    new Thread(write_stack_trace(e)).start();
                }
            }));
            thread = new Thread(task);
        }
        Task<Object> animatedTask;
        taskStatusBar.setProgress(0);
        final String TIME_IT_STARTED = "[".concat(get_time()).concat("]");
        if (this.myJobPackage.isForUpload()) {
            animatedTask = copy_progress(this.myJobPackage.getSourceSize());
            animatedTask.setOnSucceeded(event -> {
                final String END_TIME = "[".concat(get_time()).concat("]");
                final History history = new History();
                history.setTimeWhenItStarted(TIME_IT_STARTED);
                history.setTimeWhenItStopped(END_TIME);
                history.setDate(get_date());
                history.setJobName(this.myJobPackage.getName().concat("(U)"));
                history.setListOfMedia(new ArrayList<>());
                history.setStatus("COMPLETE");
                final List<String> objectList = this.myJobPackage.getAllMediaPaths();
                for (String pathToMedia : objectList) {
                    history.getListOfMedia().add(new File(pathToMedia).getName());
                }
                new Thread(write_log("Upload ready!", history)).start();
                taskStatusBar.progressProperty().unbind();
                final Node currentNode = taskNameLbl.getParent().getParent().getParent();
                final VBox vBox = (VBox) currentNode.getParent();
                new SlideOutLeft(currentNode).play();
                Platform.runLater(() -> {
                    new SlideOutLeft(currentNode);
                    VBox.clearConstraints(currentNode);
                    vBox.getChildren().remove(currentNode);
                });
            });
            animatedTask.setOnFailed(event -> {
                final String END_TIME = "[".concat(get_time()).concat("]");
                final History history = new History();
                history.setTimeWhenItStarted(TIME_IT_STARTED);
                history.setTimeWhenItStopped(END_TIME);
                history.setDate(get_date());
                history.setJobName(this.myJobPackage.getName().concat("(Cp2D)"));
                history.setListOfMedia(new ArrayList<>());
                history.setStatus("INCOMPLETE");
                String path = "==================================================\n"
                        .concat("From: ".concat(TIME_IT_STARTED).concat("\n"))
                        .concat("To: ".concat(END_TIME).concat("\n"))
                        .concat("JobPackage name: ".concat(this.myJobPackage.getName()).concat("\n"))
                        .concat("Cost Ksh ".concat(String.format("%,.1f", this.myJobPackage.getCost())).concat("\n\n"))
                        .concat("FAILED, Something happened!\n")
                        .concat("ERR>>This job was interrupted!\n\n")
                        .concat("The following files were expected to be copied:\n");
                int index = 1;
                final List<String> objectList = this.myJobPackage.getAllMediaPaths();
                for (String pathToMedia : objectList) {
                    history.getListOfMedia().add(new File(pathToMedia).getName());
                    path = path.concat(index + ".)".concat(new File(pathToMedia).getName().concat("\t\t@SOURCE_PATH { ".concat(pathToMedia).concat(" }\n"))));
                    ++index;
                }
                new Thread(write_log(path, history)).start();
                final Node currentNode = taskNameLbl.getParent().getParent().getParent();
                final VBox vBox = (VBox) currentNode.getParent();
                Platform.runLater(() -> {
                    new SlideOutLeft(currentNode);
                    VBox.clearConstraints(currentNode);
                    vBox.getChildren().remove(currentNode);
                });
                warning_message("\"".concat(this.myJobPackage.getName()).concat("\" did not complete!"), "Sadly some or all media failed to upload to the customer's phone.").show();
            });
            animatedTask.exceptionProperty().addListener(((observable, oldValue, newValue) -> {
                if (newValue != null) {
                    Exception e = (Exception) newValue;
                    e.printStackTrace();
                    programmer_error(e).show();
                    new Thread(write_stack_trace(e)).start();
                }
            }));
        } else {
            animatedTask = copy_progress(this.myJobPackage.getSourceSize(), this.myJobPackage.getDestinationFolder());
        }
        taskStatusBar.progressProperty().bind(animatedTask.progressProperty());
        if (!this.myJobPackage.isForUpload()) {
            thread.start();
        }
        new Thread(animatedTask).start();
        listOfStartedThreads.remove(taskName);
    }

    protected Task<Object> copy_progress(double jobSizeAsBytes) {
        final double TOTAL_SIZE_IN_MB = ((jobSizeAsBytes / 1024) / 1024);
        return new Task<Object>() {
            @Override
            protected Object call() {
                while (true) {
                    updateProgress(0, 1);
                    if (Server.taskRequested != null) {
                        if (STRING_JOB_PACKAGE_HASH_MAP_FOR_UPLOAD.containsKey(Server.taskRequested)) {
                            if (STRING_JOB_PACKAGE_HASH_MAP_FOR_UPLOAD.get(Server.taskRequested).getName().equals(taskName)) {
                                Server.taskRequested = null;
                                break;
                            }
                        }
                    }
                    Platform.runLater(() -> sizeDifferenceLbl.setText("Waiting to be claimed"));
                    for (int count = 0; count < 3; ++count) {
                        Platform.runLater(() -> sizeDifferenceLbl.setText(sizeDifferenceLbl.getText().concat(" . ")));
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        }
                    }
                    Platform.runLater(() -> sizeDifferenceLbl.setText("Waiting to be claimed"));
                }
                TIME_IT_STARTED = "[".concat(get_time()).concat("]");
                double megabytes = 0;
                while (megabytes <= TOTAL_SIZE_IN_MB) {
                    if (!isPaused.get()) {
                        try {
                            Thread.sleep(10);
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        } finally {
                            myJobPackage.setByteSent(STRING_JOB_PACKAGE_HASH_MAP_FOR_UPLOAD.get(taskName).getByteSent());
                            double bytes = myJobPackage.getByteSent();
                            double kilobytes = (bytes / 1024);
                            megabytes = (kilobytes / 1024);
                            updateProgress(megabytes, TOTAL_SIZE_IN_MB);
                            final double currentBytes = bytes;
                            Platform.runLater(() -> sizeDifferenceLbl.setText(
                                    "Uploaded "
                                            .concat(make_bytes_more_presentable(currentBytes)).concat(" of ")
                                            .concat(make_bytes_more_presentable(jobSizeAsBytes))
                                    )
                            );
                        }
                    }
                }
                return null;
            }
        };
    }

    protected void create_a_text_file_then_write_into_it_the_list_of_files_that_have_been_copied(String task, String message, @NotNull File destinationFolder) {
        try {
            BufferedWriter bw = null;
            final File file = new File(destinationFolder.getAbsolutePath().concat("\\MOVIE_HUB logs {customer copy}"));
            if (!file.exists()) {
                FileUtils.forceMkdir(file);
            }
            final File log = new File(file.getAbsolutePath().concat("\\".concat(task)).concat(".txt"));
            if (log.createNewFile()) {
                if (log.canWrite() & log.canRead()) {
                    FileWriter fw = new FileWriter(log, true);
                    bw = new BufferedWriter(fw);
                    bw.write(message);
                }
            }
            if (bw != null) {
                if (log.setWritable(false)) {
                    bw.close();
                }
            }
        } catch (IOException e) {
            new Thread(write_stack_trace(e)).start();
            e.printStackTrace();
            programmer_error(e).show();
        }
    }

    @NotNull
    protected Task<String> copy_selected_media_list(final File destinationPath, final String jobName, List<String> allMediaPaths) {
        TIME_IT_STARTED = "[".concat(get_time()).concat("]");
        final List<Object> objectList = new ArrayList<>();
        return new Task<String>() {
            @Override
            protected String call() {
                for (String mediaFilePath : allMediaPaths) {
                    final File source = new File(mediaFilePath);
                    try {
                        if (source.isFile()) {
                            if (can_be_used_by_vlc(source)) {
                                FileUtils.copyFileToDirectory(source, destinationPath);
                                objectList.add(source.getAbsolutePath());
                            }
                        } else {
                            if (source.isDirectory()) {
                                File[] files = source.listFiles();
                                if (files != null) {
                                    File newDestination = new File(destinationPath.getAbsolutePath().concat("\\".concat(source.getName())));
                                    FileUtils.forceMkdir(newDestination);
                                    for (File file : files) {
                                        if (can_be_used_by_vlc(file)) {
                                            FileUtils.copyFileToDirectory(file, newDestination);
                                            objectList.add(file.getAbsolutePath());
                                        }
                                    }
                                }
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                        if (e.toString().contains("are the same")) {
                            Platform.runLater(() -> error_message("File already exists", "Please select another file from else where other than the 'safe zone' files.").show());
                        } else if (e.toString().contains("not enough space on disk")) {
                            Platform.runLater(() -> error_message("Incomplete!", "There is not enough space on disk").show());
                        }
                        new Thread(write_stack_trace(e)).start();
                        Platform.runLater(() -> programmer_error(e).show());
                        return null;
                    } catch (Exception e) {
                        e.printStackTrace();
                        new Thread(write_stack_trace(e)).start();
                        Platform.runLater(() -> programmer_error(e).show());
                        return null;
                    }
                }
                if (!copiedItems.containsKey(jobName)) {
                    copiedItems.put(jobName, objectList);
                }
                return "[".concat(get_time()).concat("]");
            }
        };
    }

    @NotNull
    protected Task<Object> copy_progress(double jobSizeAsBytes, final File destinationFolder) {
        final double originalBytesBeforeCopy = get_folder_size(destinationFolder);
        final double TOTAL_SIZE_IN_MB = ((jobSizeAsBytes / 1024) / 1024);
        return new Task<Object>() {
            @Override
            protected Object call() {
                double megabytes = 0;
                while (megabytes <= TOTAL_SIZE_IN_MB) {
                    if (!isPaused.get()) {
                        try {
                            Thread.sleep(10);
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        } finally {
                            double bytes = get_folder_size(destinationFolder);
                            bytes -= originalBytesBeforeCopy;
                            double kilobytes = (bytes / 1024);
                            megabytes = (kilobytes / 1024);
                            updateProgress(megabytes, TOTAL_SIZE_IN_MB);
                            final double currentBytes = bytes;
                            Platform.runLater(() -> sizeDifferenceLbl.setText("Copied "
                                    .concat(make_bytes_more_presentable(currentBytes)).concat(" of ")
                                    .concat(make_bytes_more_presentable(jobSizeAsBytes))));
                        }
                    }
                }
                return null;
            }
        };
    }

    protected double get_folder_size(@NotNull File file) {
        double bytes = 0;
        final File[] fileList = file.listFiles();
        if (fileList != null) {
            for (File file1 : fileList) {
                if (file1.isFile()) {
                    if (can_be_used_by_vlc(file1)) {
                        bytes += file1.length();
                    }
                } else {
                    bytes += get_folder_size(file1);
                }
            }
        }
        return bytes;
    }

}
