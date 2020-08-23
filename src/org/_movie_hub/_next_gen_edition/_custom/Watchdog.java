package org._movie_hub._next_gen_edition._custom;

import animatefx.animation.Shake;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import javafx.concurrent.Task;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import org._movie_hub._next_gen_edition.Main;
import org._movie_hub._next_gen_edition._controller.Home;
import org._movie_hub._next_gen_edition._object.Category;
import org._movie_hub._next_gen_edition._object.History;
import org._movie_hub._next_gen_edition._object.Media;
import org.apache.commons.lang3.RandomStringUtils;
import org.controlsfx.control.Notifications;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.imageio.ImageIO;
import java.awt.*;
import java.io.*;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

/**
 * @author Mandela aka puumInc
 */

public abstract class Watchdog {


    private final String PATH_TO_INFO_FOLDER = Main.RESOURCE_PATH.getAbsolutePath() + "\\_watchDog\\_activity";
    protected final String ACTIVITY_JSON_FILE = PATH_TO_INFO_FOLDER.concat("\\_advanced\\activity_log.json");
    protected final String TRAILERS_JSON_FILE = Main.RESOURCE_PATH.getAbsolutePath().concat("\\_config\\trailer.json");
    protected final String TRAILER_KEY_JSON_FILE = Main.RESOURCE_PATH.getAbsolutePath().concat("\\_config\\trailerKey.json");
    protected final String APP_JSON_FILE = Main.RESOURCE_PATH.getAbsolutePath().concat("\\_config\\app.json");

    protected JsonArray get_app_details_as_object(File file) {
        try {
            final BufferedReader bufferedReader = new BufferedReader(new FileReader(file));
            final JsonArray jsonArray = new Gson().fromJson(bufferedReader, JsonArray.class);
            bufferedReader.close();
            return jsonArray;
        } catch (IOException ex) {
            ex.printStackTrace();
            programmer_error(ex).show();
        }
        return new JsonArray();
    }

    protected Alert show_qr_image_for_upload(File imageFile) throws FileNotFoundException {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.initOwner(Main.stage);
        alert.setTitle("Movie hub");
        alert.setHeaderText("Scan the QR code so that the upload can begin");
        alert.setContentText("Expand to see the the qr image");
        ImageView imageView = new ImageView();
        imageView.setFitHeight(500);
        imageView.setFitWidth(500);
        imageView.setImage(new Image(new FileInputStream(imageFile)));
        StackPane stackPane = new StackPane();
        stackPane.getChildren().add(imageView);
        stackPane.setMaxSize(imageView.getFitWidth(), imageView.getFitHeight());
        stackPane.setStyle("-fx-background-color: #FFFFFF;");
        alert.getDialogPane().setExpandableContent(stackPane);
        return alert;
    }

    protected @Nullable File get_a_qr_image_file_that_has_an_embedded_logo(final String jobName) {
        try {
            final QRCodeWriter qrCodeWriter = new QRCodeWriter();
            final BitMatrix bitMatrix = qrCodeWriter.encode(jobName, BarcodeFormat.QR_CODE, 720, 720);
            //write to png file
            final File file = new File(Main.RESOURCE_PATH.getAbsolutePath().concat("\\_address\\").concat(RandomStringUtils.randomAlphabetic(10)).concat(".png"));
            final Path path = FileSystems.getDefault().getPath(file.getAbsolutePath());
            MatrixToImageWriter.writeToPath(bitMatrix, "PNG", path);
            return file;
        } catch (WriterException | IOException e) {
            e.printStackTrace();
            new Thread(write_stack_trace(e)).start();
            programmer_error(e).show();
        }
        return null;
    }

    protected JsonObject get_app_details_as_object() {
        try {
            final BufferedReader bufferedReader = new BufferedReader(new FileReader(new File(APP_JSON_FILE)));
            final JsonObject jsonObject = new Gson().fromJson(bufferedReader, JsonObject.class);
            bufferedReader.close();
            return jsonObject;
        } catch (IOException ex) {
            ex.printStackTrace();
            programmer_error(ex).show();
        }
        return new JsonObject();
    }

    protected void update_trailer_and_playlist_key() {
        Home.listOfTrailerIds = make_map(TRAILER_KEY_JSON_FILE);
    }

    protected @NotNull JsonArray extract_from_hashmap(@NotNull HashMap<String, String> stringStringHashMap) {
        JsonArray jsonArray = new JsonArray();
        stringStringHashMap.forEach((key, value) -> {
            Media media = new Media();
            media.setKey(key);
            media.setValue(new File(value).getName());
            jsonArray.add(new Gson().toJsonTree(media, Media.class));
        });
        return jsonArray;
    }

    protected JsonArray make_array_from_map(@NotNull HashMap<String, HashMap<String, String>> stringHashMapHashMap) {
        JsonArray jsonArray = new JsonArray();
        stringHashMapHashMap.forEach((key, value) -> {
            final Category category = new Category();
            category.setName(key);
            category.setMedia(extract_from_hashmap(value));
            jsonArray.add(new Gson().toJsonTree(category, Category.class));
        });
        return jsonArray;
    }

    @NotNull
    protected HashMap<String, HashMap<String, String>> make_map(String fileName) {
        final HashMap<String, HashMap<String, String>> stringHashMapHashMap = new HashMap<>();
        final JsonArray jsonArray = get_app_details_as_object(new File(fileName));
        jsonArray.forEach(jsonElement -> {
            final Category category = new Gson().fromJson(jsonElement, Category.class);
            final HashMap<String, String> mediaNames = new HashMap<>();
            category.getMedia().forEach(jsonElement1 -> {
                final Media media = new Gson().fromJson(jsonElement1, Media.class);
                mediaNames.put(media.getKey(), media.getValue());
            });
            stringHashMapHashMap.put(category.getName(), mediaNames);
        });
        return stringHashMapHashMap;
    }

    protected @NotNull JsonArray get_directory_of_sub_files_in_the_provided_folder(@NotNull File file) {
        final JsonArray jsonArray = new JsonArray();
        if (file.isDirectory()) {
            final File[] fileList = file.listFiles();
            if (fileList != null) {
                for (File file1 : fileList) {
                    if (file1.isFile()) {
                        if (is_a_playable(file1)) {
                            jsonArray.add(new Gson().toJsonTree(file1.getAbsolutePath(), String.class));
                        }
                    } else {
                        jsonArray.addAll(get_directory_of_sub_files_in_the_provided_folder(file1));
                    }
                }
            }
        }
        return jsonArray;
    }

    protected @NotNull Boolean is_a_playable(File file) {
        final String[] extensions = Home.EXTENSIONS_FOR_MOVIES_ONLY.split(":");
        for (String string : extensions) {
            if (file.getName().endsWith(string) || file.getName().endsWith(string.toUpperCase())) {
                return true;
            }
        }
        return false;
    }

    protected String get_unique_word_as_key(@NotNull HashMap<String, String> stringStringHashMap) {
        String newKey = RandomStringUtils.randomAlphabetic(10);
        if (stringStringHashMap.isEmpty()) {
            return newKey;
        } else if (stringStringHashMap.containsKey(newKey)) {
            get_unique_word_as_key(stringStringHashMap);
        } else {
            return newKey;
        }
        return newKey;
    }

    protected @NotNull List<String> get_file_names(@NotNull List<String> mediaPaths) {
        final List<String> stringList = new ArrayList<>();
        mediaPaths.forEach(mediaPath -> {
            final File file = new File(mediaPath);
            stringList.addAll(get_list_of_file_names_from_the_given_file_type(file));
        });
        return stringList;
    }

    protected List<String> get_list_of_file_names_from_the_given_file_type(@NotNull File file) {
        final List<String> stringList = new ArrayList<>();
        if (file.exists()) {
            if (file.isDirectory()) {
                final File[] files = file.listFiles();
                for (File file1 : files) {
                    stringList.addAll(get_list_of_file_names_from_the_given_file_type(file1));
                }
            } else {
                if (file.isFile()) {
                    stringList.add(file.getName());
                }
            }
        } else {
            stringList.add("NOT FOUND >> ".concat(file.getAbsolutePath()));
        }
        return stringList;
    }

    protected @NotNull String generate_a_string_from_list_of_media_paths(@NotNull List<String> mediaList) {
        int index = 1;
        StringBuilder line = new StringBuilder();
        for (String media : mediaList) {
            final String str = index + " . ".concat(media.concat("\n"));
            if (line.length() == 0) {
                line = new StringBuilder(str);
            } else {
                line.append(str);
            }
            ++index;
        }
        return line.toString();
    }

    protected Boolean write_jsonArray_to_file(JsonArray jsonArray, String pathToJsonFile) {
        try {
            final File file = new File(pathToJsonFile);
            FileWriter fileWriter = new FileWriter(file);
            fileWriter.write(new Gson().toJson(jsonArray));
            fileWriter.close();
            fileWriter.close();
            return true;
        } catch (IOException ex) {
            ex.printStackTrace();
            programmer_error(ex).show();
        }
        return false;
    }

    protected Alert show_list_of_copied_media(String header, String text) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.initOwner(Main.stage);
        alert.setTitle("Movie hub");
        alert.setHeaderText(header);
        alert.setContentText("Click below to view the list");
        TextArea textArea = new TextArea(text);
        textArea.setEditable(false);
        textArea.setWrapText(true);
        textArea.setMaxWidth(Double.MAX_VALUE);
        textArea.setMaxHeight(Double.MAX_VALUE);
        alert.getDialogPane().setExpandableContent(textArea);
        return alert;
    }

    @NotNull
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
            File log = new File(PATH_TO_INFO_FOLDER.concat("\\_basic\\".concat(gate_date_for_file_name()).concat(".txt")));
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
            final File file = new File(ACTIVITY_JSON_FILE);
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
    protected @NotNull Runnable write_stack_trace(Exception exception) {
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

    @NotNull
    protected final Alert programmer_error(@NotNull Exception exception) {
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

    private @NotNull String gate_date_for_file_name() {
        return new SimpleDateFormat("dd-MMM-yyyy").format(Calendar.getInstance().getTime()).replaceAll("-", " ");
    }

    @NotNull
    protected String time_stamp() {
        return get_date() + " at " + new SimpleDateFormat("HH:mm:ss:SSS").format(Calendar.getInstance().getTime());
    }

    protected String get_date() {
        return new SimpleDateFormat("dd-MMM-yyyy").format(Calendar.getInstance().getTime());
    }

    @NotNull
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
            java.awt.image.BufferedImage bufferedImage = ImageIO.read(getClass().getResource("/org/_movie_hub/_next_gen_edition/_images/movieHubIcon.png"));
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
