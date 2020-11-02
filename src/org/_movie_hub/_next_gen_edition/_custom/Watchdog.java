package org._movie_hub._next_gen_edition._custom;

import animatefx.animation.Shake;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import javafx.concurrent.Task;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import org._movie_hub._next_gen_edition.Main;
import org._movie_hub._next_gen_edition._object.History;
import org.controlsfx.control.Notifications;
import org.jetbrains.annotations.Contract;

import javax.imageio.ImageIO;
import java.awt.*;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * @author Mandela aka puumInc
 */

public class Watchdog extends Assistant {

    private final String PATH_TO_INFO_FOLDER = Main.RESOURCE_PATH.getAbsolutePath().concat("\\_watchDog\\_activity");
    protected final String ACTIVITY_JSON_FILE = PATH_TO_INFO_FOLDER.concat("\\_advanced\\activity_log.json");
    protected final String TRAILERS_JSON_FILE = Main.RESOURCE_PATH.getAbsolutePath().concat("\\_config\\trailer.json");

    protected final Task<Object> write_log(String string, History history) {
        return new Task<Object>() {
            @Override
            protected Object call() {
                activityLog(history, string);
                return false;
            }
        };
    }

    protected final void activityLog(History history, String info) {
        write_a_basic_activity(info);
        if (history != null) {
            write_advanced_staff(history);
        }
    }

    private void write_a_basic_activity(String info) {
        BufferedWriter bw = null;
        try {
            File log = new File(format_path_name_to_current_os(
                    PATH_TO_INFO_FOLDER.concat("\\_basic\\".concat(gate_date_for_file_name()).concat(".txt"))
            ));
            if (!log.exists()) {
                FileWriter fw = new FileWriter(log, true);
                bw = new BufferedWriter(fw);
                bw.write("\nThis is a newly created file [ " + time_stamp() + " ].");
            }
            FileWriter fw = new FileWriter(log, true);
            bw = new BufferedWriter(fw);
            bw.write("\n" + info);
        } catch (IOException ex) {
            ex.printStackTrace();
            programmer_error(ex).show();
        } finally {
            try {
                if (bw != null) {
                    bw.close();
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                programmer_error(ex).show();
            }
        }
    }

    private void write_advanced_staff(History history) {
        try {
            final File file = new File(format_path_name_to_current_os(ACTIVITY_JSON_FILE));
            if (file.exists()) {
                final JsonArray jsonArray = get_app_details_as_object(file);
                jsonArray.add(new Gson().toJsonTree(history, History.class));
                FileWriter fileWriter = new FileWriter(file);
                fileWriter.write(new Gson().toJson(jsonArray));
                fileWriter.close();
            } else {
                FileWriter fileWriter = new FileWriter(file);
                final JsonArray jsonArray = new JsonArray();
                jsonArray.add(new Gson().toJsonTree(history, History.class));
                fileWriter.write(new Gson().toJson(jsonArray));
                fileWriter.close();
            }

        } catch (IOException ex) {
            ex.printStackTrace();
            programmer_error(ex).show();
        }
    }

    @Contract(value = "_ -> new", pure = true)
    protected Runnable write_stack_trace(Exception exception) {
        return () -> {
            BufferedWriter bw = null;
            try {
                final String PATH_TO_ERROR_FOLDER = Main.RESOURCE_PATH.getAbsolutePath() + "\\_watchDog\\_error\\";
                File log = new File(PATH_TO_ERROR_FOLDER.concat(gate_date_for_file_name().concat(" stackTrace_log.txt")));
                if (!log.exists()) {
                    FileWriter fw = new FileWriter(log);
                    fw.write("\nThis is a newly created file [ " + time_stamp() + " ].");
                }
                if (log.canWrite() & log.canRead()) {
                    FileWriter fw = new FileWriter(log, true);
                    bw = new BufferedWriter(fw);
                    StringWriter stringWriter = new StringWriter();
                    PrintWriter printWriter = new PrintWriter(stringWriter);
                    exception.printStackTrace(printWriter);
                    String exceptionText = stringWriter.toString();
                    bw.write("\n ##################################################################################################"
                            + " \n " + time_stamp()
                            + "\n " + exceptionText
                            + "\n\n");
                }
            } catch (IOException ex) {
                ex.printStackTrace();
                programmer_error(ex).show();
            } finally {
                try {
                    if (bw != null) {
                        bw.close();
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                    programmer_error(ex).show();
                }
            }
        };
    }

    protected final Alert programmer_error(Exception exception) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.initOwner(Main.stage);
        alert.setTitle("WATCH DOG");
        alert.setHeaderText("ERROR TYPE : " + exception.getClass());
        alert.setContentText("This dialog is a detailed explanation of the error that has occurred");
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        exception.printStackTrace(printWriter);
        String exceptionText = stringWriter.toString();
        Label label = new Label("The exception stacktrace was: ");
        TextArea textArea = new TextArea(exceptionText);
        textArea.setEditable(false);
        textArea.setWrapText(true);
        textArea.setMaxWidth(Double.MAX_VALUE);
        textArea.setMaxHeight(Double.MAX_VALUE);
        VBox vBox = new VBox();
        vBox.getChildren().add(label);
        vBox.getChildren().add(textArea);
        alert.getDialogPane().setExpandableContent(vBox);
        return alert;
    }

    private String gate_date_for_file_name() {
        return new SimpleDateFormat("dd-MMM-yyyy").format(Calendar.getInstance().getTime()).replaceAll("-", " ");
    }

    protected String time_stamp() {
        return get_date() + " at " + new SimpleDateFormat("HH:mm:ss:SSS").format(Calendar.getInstance().getTime());
    }

    protected String get_date() {
        return new SimpleDateFormat("dd-MMM-yyyy").format(Calendar.getInstance().getTime());
    }

    protected final Alert error_message_alert(String header, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.initOwner(Main.stage);
        alert.setTitle("Movie Hub encountered an Error");
        alert.setHeaderText(header);
        alert.setContentText(message);
        return alert;
    }

    protected final void information_message(String message) {
        try {
            SystemTray systemTray = SystemTray.getSystemTray();
            java.awt.image.BufferedImage bufferedImage = ImageIO.read(getClass().getResource("/org/_movie_hub/_next_gen_edition/_images/myIco_x1.png"));
            TrayIcon trayIcon = new TrayIcon(bufferedImage);
            trayIcon.setImageAutoSize(true);
            systemTray.add(trayIcon);
            trayIcon.displayMessage("Information", message, TrayIcon.MessageType.NONE);
        } catch (IOException | AWTException exception) {
            exception.printStackTrace();
            programmer_error(exception).show();
        }
    }

    protected final Notifications success_notification(String about) {
        return Notifications.create()
                .title("Success")
                .text(about)
                .position(Pos.TOP_LEFT)
                .hideAfter(Duration.seconds(5))
                .graphic(new ImageView(new Image("/org/_movie_hub/_next_gen_edition/_images/icons8_Ok_48px.png")));
    }

    protected final Notifications error_message(String title, String text) {
        Image image = new Image("/org/_movie_hub/_next_gen_edition/_images/icons8_Close_Window_48px.png");
        return Notifications.create()
                .title(title)
                .text(text)
                .graphic(new ImageView(image))
                .hideAfter(Duration.seconds(8))
                .position(Pos.TOP_RIGHT);
    }

    protected final Notifications warning_message(String title, String text) {
        Image image = new Image("/org/_movie_hub/_next_gen_edition/_images/icons8_Error_48px.png");
        return Notifications.create()
                .title(title)
                .text(text)
                .graphic(new ImageView(image))
                .hideAfter(Duration.seconds(8))
                .position(Pos.TOP_RIGHT);
    }

    protected final Notifications empty_and_null_pointer_message(Node node) {
        Image image = new Image("/org/_movie_hub/_next_gen_edition/_images/icons8_Error_48px.png");
        return Notifications.create()
                .title("Something is Missing")
                .text("Click Here to trace this Error.")
                .graphic(new ImageView(image))
                .hideAfter(Duration.seconds(8))
                .position(Pos.TOP_CENTER)
                .onAction(event -> {
                    new Shake(node).play();
                    node.requestFocus();
                });
    }

}
