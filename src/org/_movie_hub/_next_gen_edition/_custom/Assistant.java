package org._movie_hub._next_gen_edition._custom;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Alert;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import org._movie_hub._next_gen_edition.Main;
import org._movie_hub._next_gen_edition._enum.OperatingSystem;
import org._movie_hub._next_gen_edition._object.Category;
import org._movie_hub._next_gen_edition._object.JobPackage;
import org._movie_hub._next_gen_edition._object.Media;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.net.*;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.*;

/**
 * @author Mandela aka puumInc
 */

public abstract class Assistant {

    protected final String TRAILER_KEY_JSON_FILE = Main.RESOURCE_PATH.getAbsolutePath().concat("\\_config\\trailerKey.json");
    private final String APP_JSON_FILE = Main.RESOURCE_PATH.getAbsolutePath().concat("\\_config\\app.json");

    protected final String EXTENSIONS_SUPPORTED_BY_VLC = ".asx:.dts:.gxf:.m2v:.m3u:.m4v:.mpeg1:.mpeg2:.mts:.mxf:.pls:.divx:.dv:.flv:.m1v:.m2ts:.mkv:.mov:.mpeg4:.ts:.vlc:.vob:.3g2:.avi:.mpeg:.mpg:.m4a:.3gp:.srt:.wmv:.asf:.mp4:.m4p";
    protected final static String EXTENSIONS_FOR_MOVIES_ONLY = ".m4v:.mpeg1:.mpeg2:.flv:.mkv:.mov:.mpeg4:.vob:.avi:.mpeg:.m4a:.3gp:.mp4:.m4p";

    protected final static HashMap<String, JobPackage> LIST_OF_COPY_THREADS = new HashMap<>();
    protected final static HashMap<String, JobPackage> STRING_JOB_PACKAGE_HASH_MAP_FOR_UPLOAD = new HashMap<>();
    protected final static ObservableList<String> listOfStartedThreads = FXCollections.observableArrayList();

    protected final static com.github.cliftonlabs.json_simple.JsonObject MOVIE_LIST = new com.github.cliftonlabs.json_simple.JsonObject();
    protected final static com.github.cliftonlabs.json_simple.JsonObject SERIES_LIST = new com.github.cliftonlabs.json_simple.JsonObject();
    protected final static List<String> LIST_OF_SELECTED_MOVIES = new ArrayList<>();
    protected final static List<String> LIST_OF_SELECTED_SERIES = new ArrayList<>();

    protected static HashMap<String, HashMap<String, String>> listOfTrailerIds;

    protected static String taskRequested;

    protected final InetAddress get_first_nonLoopback_address(boolean preferIpv4, boolean preferIPv6) throws SocketException, UnknownHostException {
        InetAddress result = InetAddress.getLocalHost();
        Enumeration<NetworkInterface> networkInterfaceEnumeration = NetworkInterface.getNetworkInterfaces();
        while (networkInterfaceEnumeration.hasMoreElements()) {
            NetworkInterface networkInterface = networkInterfaceEnumeration.nextElement();
            for (Enumeration<InetAddress> inetAddressEnumeration = networkInterface.getInetAddresses(); inetAddressEnumeration.hasMoreElements(); ) {
                InetAddress inetAddress = inetAddressEnumeration.nextElement();
                if (!inetAddress.isLoopbackAddress()) {
                    if (inetAddress instanceof Inet4Address) {
                        if (preferIPv6) {
                            continue;
                        }
                        result = inetAddress;
                        break;
                    }
                    if (inetAddress instanceof Inet6Address) {
                        if (preferIpv4) {
                            continue;
                        }
                        result = inetAddress;
                        break;
                    }
                }
            }
            if (result != null) break;
        }
        return result;
    }

    protected String format_path_name_to_current_os(String myPath) {
        String myOperatingSystemSlash = get_slash_for_my_os();
        if (myOperatingSystemSlash != null) {
            if (!myOperatingSystemSlash.equals(OperatingSystem.WINDOWS.getSlash())) {
                myPath = myPath.replace(OperatingSystem.WINDOWS.getSlash(), myOperatingSystemSlash);
            }
        }
        return myPath;
    }

    private String get_slash_for_my_os() {
        String OS = System.getProperty("os.name").toLowerCase();
        OperatingSystem myOperatingSystem = Arrays.stream(OperatingSystem.values()).filter(operatingSystem -> OS.contains(operatingSystem.getOs())).findFirst().orElse(null);
        if (myOperatingSystem == null) {
            return null;
        } else {
            return myOperatingSystem.getSlash();
        }
    }

    protected boolean the_file_or_folder_has_zero_bytes(File fileOrFolder) {
        return get_size_of_the_provided_file_or_folder(fileOrFolder) == 0;
    }

    protected List<File> get_sub_folders(File parentFolder) {
        List<File> folderList = new ArrayList<>();
        if (parentFolder.isDirectory()) {
            if (!the_file_or_folder_has_zero_bytes(parentFolder)) {
                for (File childFileOrFolder : parentFolder.listFiles()) {
                    if (childFileOrFolder.isDirectory()) {
                        if (the_file_or_folder_has_zero_bytes(childFileOrFolder)) {
                            folderList.addAll(get_sub_folders(childFileOrFolder));
                        } else {
                            folderList.add(childFileOrFolder);
                        }
                    }
                }
            }
        }
        return folderList;
    }

    protected boolean can_be_used_by_vlc(File file) {
        final String[] extensions = EXTENSIONS_SUPPORTED_BY_VLC.split(":");
        boolean result = false;
        for (String string : extensions) {
            if (file.getName().endsWith(string) || file.getName().endsWith(string.toUpperCase())) {
                result = true;
                break;
            }
        }
        return result;
    }

    protected JsonObject get_details_about_number_of_seasons_and_episodes_of_a_series(File folder) {
        int seasons, episodes = 0;
        List<File> subFolders = get_sub_folders(folder);
        if (subFolders.isEmpty()) {
            seasons = get_number_of_seasons(folder);
            episodes = get_number_of_valid_episodes(folder);
        } else {
            seasons = subFolders.size();
            for (File subFolder : subFolders) {
                episodes += get_number_of_valid_episodes(subFolder);
            }
        }

        final JsonObject jsonObject = new JsonObject();
        jsonObject.add("seasons", new Gson().toJsonTree(seasons, Integer.class));
        jsonObject.add("episodes", new Gson().toJsonTree(episodes, Integer.class));
        return jsonObject;
    }

    private boolean is_an_episode(File file) {
        final String[] extensions = EXTENSIONS_FOR_MOVIES_ONLY.split(":");
        for (String string : extensions) {
            if (StringUtils.endsWithIgnoreCase(file.getName(), string)) {
                return true;
            }
        }
        return false;
    }

    private int get_number_of_valid_episodes(File file) {
        int result = 0;
        if (file.isDirectory()) {
            final File[] fileList = file.listFiles();
            if (fileList != null) {
                for (File file1 : fileList) {
                    if (file1.isFile()) {
                        if (is_an_episode(file1)) {
                            ++result;
                        }
                    } else if (file1.isDirectory()) {
                        result += get_number_of_valid_episodes(file1);
                    }
                }
            }
        }
        return result;
    }

    private int get_number_of_seasons(File file) {
        int result = 0;
        if (file.isDirectory()) {
            final File[] fileList = file.listFiles();
            if (fileList != null) {
                boolean thereIsAFileOutsideTheSubfolderOfTheParentFolder = false;
                for (File file1 : fileList) {
                    if (file1.isDirectory()) {
                        ++result;
                    } else {
                        thereIsAFileOutsideTheSubfolderOfTheParentFolder = true;
                    }
                }
                if (thereIsAFileOutsideTheSubfolderOfTheParentFolder) {
                    ++result;
                }
            }
        }
        return result;
    }

    protected final String format_file_or_directory_size(File file) {
        if (file.exists()) {
            double bytes = 0;
            bytes += get_size_of_the_provided_file_or_folder(file);
            return make_bytes_more_presentable(bytes);
        } else {
            new Watchdog().error_message("Path is broken!", "It seems the path you have provided does not exist").show();
            return "Zero Bytes";
        }
    }

    protected double get_size_of_the_provided_file_or_folder(File file) {
        double bytes = 0;
        if (file.isFile()) {
            if (can_be_used_by_vlc(file)) {
                bytes += file.length();
            }
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

    protected String format_file_name(final String fileName) {
        String result = fileName.toLowerCase();
        for (String extension : EXTENSIONS_SUPPORTED_BY_VLC.replace(".", "").split(":")) {
            if (result.contains(extension)) {
                result = result.replace(extension, "");
            }
            if (result.contains(".")) {
                result = result.replace(".", " ");
            }
        }
        result = StringUtils.capitalize(result.trim());
        return result;
    }

    protected JsonArray read_jsonArray_from_file(File file) throws IOException {
        final BufferedReader bufferedReader = new BufferedReader(new FileReader(file));
        final JsonArray jsonArray = new JsonArray();
        jsonArray.addAll(new Gson().fromJson(bufferedReader, JsonArray.class));
        bufferedReader.close();
        return jsonArray;
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

    protected File get_a_qr_image_file_that_has_an_embedded_logo(final String jobName) {
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
            new Thread(new Watchdog().write_stack_trace(e)).start();
            new Watchdog().programmer_error(e).show();
        }
        return null;
    }

    protected JsonObject get_app_details_as_object() {
        try {
            final BufferedReader bufferedReader = new BufferedReader(new FileReader(new File(format_path_name_to_current_os(APP_JSON_FILE))));
            final JsonObject jsonObject = new Gson().fromJson(bufferedReader, JsonObject.class);
            bufferedReader.close();
            return jsonObject;
        } catch (IOException ex) {
            ex.printStackTrace();
            Platform.runLater(() -> new Watchdog().programmer_error(ex).show());
        }
        return new JsonObject();
    }

    protected void update_trailer_and_playlist_key() {
        listOfTrailerIds = make_map(format_path_name_to_current_os(TRAILER_KEY_JSON_FILE));
    }

    protected JsonArray extract_from_hashmap(HashMap<String, String> stringStringHashMap) {
        JsonArray jsonArray = new JsonArray();
        stringStringHashMap.forEach((key, value) -> {
            Media media = new Media();
            media.setKey(key);
            media.setValue(new File(value).getName());
            jsonArray.add(new Gson().toJsonTree(media, Media.class));
        });
        return jsonArray;
    }

    protected JsonArray make_array_from_map(HashMap<String, HashMap<String, String>> stringHashMapHashMap) {
        JsonArray jsonArray = new JsonArray();
        stringHashMapHashMap.forEach((key, value) -> {
            final Category category = new Category();
            category.setName(key);
            category.setMedia(extract_from_hashmap(value));
            jsonArray.add(new Gson().toJsonTree(category, Category.class));
        });
        return jsonArray;
    }

    protected HashMap<String, HashMap<String, String>> make_map(String fileName) {
        final HashMap<String, HashMap<String, String>> stringHashMapHashMap = new HashMap<>();
        try {
            final JsonArray jsonArray = read_jsonArray_from_file(new File(fileName));
            jsonArray.forEach(jsonElement -> {
                final Category category = new Gson().fromJson(jsonElement, Category.class);
                final HashMap<String, String> mediaNames = new HashMap<>();
                category.getMedia().forEach(jsonElement1 -> {
                    final Media media = new Gson().fromJson(jsonElement1, Media.class);
                    mediaNames.put(media.getKey(), media.getValue());
                });
                stringHashMapHashMap.put(category.getName(), mediaNames);
            });
        } catch (IOException ex) {
            ex.printStackTrace();
            new Thread(new Watchdog().write_stack_trace(ex)).start();
            Platform.runLater(() -> new Watchdog().programmer_error(ex).show());
        }
        return stringHashMapHashMap;
    }

    protected JsonArray get_directory_of_sub_files_in_the_provided_folder(File file) {
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

    protected boolean is_a_playable(File file) {
        final String[] extensions = EXTENSIONS_FOR_MOVIES_ONLY.split(":");
        for (String string : extensions) {
            if (file.getName().endsWith(string) || file.getName().endsWith(string.toUpperCase())) {
                return true;
            }
        }
        return false;
    }

    protected String get_unique_word_as_key(HashMap<String, String> stringStringHashMap) {
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

    protected List<String> get_file_names(List<String> mediaPaths) {
        final List<String> stringList = new ArrayList<>();
        mediaPaths.forEach(mediaPath -> {
            final File file = new File(mediaPath);
            stringList.addAll(get_list_of_file_names_from_the_given_file_type(file));
        });
        return stringList;
    }

    protected List<String> get_list_of_file_names_from_the_given_file_type(File file) {
        final List<String> stringList = new ArrayList<>();
        if (file.exists()) {
            if (file.isDirectory()) {
                final File[] files = file.listFiles();
                if (files != null) {
                    for (File file1 : files) {
                        stringList.addAll(get_list_of_file_names_from_the_given_file_type(file1));
                    }
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

    protected String generate_a_string_from_list_of_media_paths(List<String> mediaList) {
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
            Platform.runLater(() -> new Watchdog().programmer_error(ex).show());
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

}
