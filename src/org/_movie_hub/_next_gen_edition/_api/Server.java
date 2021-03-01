package org._movie_hub._next_gen_edition._api;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import javafx.application.Platform;
import org._movie_hub._next_gen_edition._api._response_model.StandardResponse;
import org._movie_hub._next_gen_edition._api._response_model.StatusResponse;
import org._movie_hub._next_gen_edition._custom.Watchdog;
import org._movie_hub._next_gen_edition._object.Category;
import org._movie_hub._next_gen_edition._object.JobPackage;
import org._movie_hub._next_gen_edition._object.Media;

import java.io.*;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static spark.Spark.get;

/**
 * @author Mandela aka puumInc
 */

public class Server extends Watchdog {

    /**
     * Accessible url is <a href=http://localhost:4567/movieHub/api><strong>Core URL</strong></a>
     */

    private static final List<String> listOfActiveTasks = new ArrayList<>();

    private final String CONTEXT_PATH = "/movieHub/api";

    public Server() {
        app_details();
        download();
        provide_list();
        watch();
    }

    public void app_details() {
        get(CONTEXT_PATH.concat("/"), (((request, response) -> {
            response.type("application/json");
            final JsonObject jsonObject = get_app_details_as_object();
            if (jsonObject.size() == 0) {
                jsonObject.add("edition", new Gson().toJsonTree("Next Gen Edition", String.class));
                jsonObject.add("version", new Gson().toJsonTree("1.0.0", String.class));
                jsonObject.add("shopName", new Gson().toJsonTree("<puum.inc()/>", String.class));
            }
            return new Gson().toJson(new StandardResponse(StatusResponse.SUCCESS, new Gson().toJsonTree(jsonObject, JsonObject.class)));
        })));
    }

    private void download() {
        get(CONTEXT_PATH.concat("/download"), ((request, response) -> {
            response.type("application/json");
            response.status(HttpURLConnection.HTTP_OK);
            try {
                final JsonArray jsonArray = get_list_of_pending_uploads();
                if (jsonArray.size() == 0) {
                    return new Gson().toJson(new StandardResponse(StatusResponse.WARNING, "No packages to download were found!"));
                } else {
                    return new Gson().toJson(new StandardResponse(StatusResponse.SUCCESS, new Gson().toJsonTree(jsonArray, JsonArray.class)));
                }
            } catch (Exception exception) {
                exception.printStackTrace();
                new Thread(write_stack_trace(exception)).start();
                Platform.runLater(() -> programmer_error(exception).show());
                return new Gson().toJson(new StandardResponse(StatusResponse.ERROR, exception.toString()));
            }
        }));
        get(CONTEXT_PATH.concat("/download/details/:packageName"), ((request, response) -> {
            response.type("application/json");
            final String packageName = request.params(":packageName");
            response.status(HttpURLConnection.HTTP_OK);
            try {
                if (STRING_JOB_PACKAGE_HASH_MAP_FOR_UPLOAD.containsKey(packageName)) {
                    final JobPackage jobPackage = STRING_JOB_PACKAGE_HASH_MAP_FOR_UPLOAD.get(packageName);
                    return new Gson().toJson(new StandardResponse(StatusResponse.SUCCESS, new Gson().toJsonTree(jobPackage, JobPackage.class)));
                } else {
                    return new Gson().toJson(new StandardResponse(StatusResponse.WARNING, "The package does not exist!"));
                }
            } catch (Exception exception) {
                exception.printStackTrace();
                new Thread(write_stack_trace(exception)).start();
                Platform.runLater(() -> programmer_error(exception).show());
                return new Gson().toJson(new StandardResponse(StatusResponse.ERROR, exception.toString()));
            }
        }));
        get(CONTEXT_PATH.concat("/download/:packageName/:fileName"), ((request, response) -> {
            response.type("file/*");
            final String packageName = request.params(":packageName");
            final String fileName = request.params(":fileName");
            response.status(HttpURLConnection.HTTP_OK);
            try {
                if (STRING_JOB_PACKAGE_HASH_MAP_FOR_UPLOAD.containsKey(packageName)) {
                    final JobPackage jobPackage = STRING_JOB_PACKAGE_HASH_MAP_FOR_UPLOAD.get(packageName);
                    taskRequested = packageName;
                    listOfActiveTasks.add(taskRequested);

                    String targetPath = null;
                    for (String pathToUploadingFile : jobPackage.getAllMediaPaths()) {
                        final File file = new File(pathToUploadingFile);
                        if (file.exists()) {
                            if (file.getName().equals(fileName)) {
                                targetPath = file.getAbsolutePath();
                                break;
                            }
                        }
                    }
                    if (targetPath == null) {
                        response.type("application/json");
                        return new Gson().toJson(new StandardResponse(StatusResponse.WARNING, fileName.concat(" does not exist!")));
                    }
                    response.raw().setContentLengthLong(new File(targetPath).length());
                    DataOutputStream dataOutputStream = new DataOutputStream(new BufferedOutputStream(response.raw().getOutputStream()));
                    DataInputStream dataInputStream = new DataInputStream(new BufferedInputStream(new FileInputStream(targetPath)));
                    int count;
                    byte[] buffer = new byte[8192];
                    while ((count = dataInputStream.read(buffer)) > 0) {
                        dataOutputStream.write(buffer, 0, count);
                        jobPackage.setByteSent(jobPackage.getByteSent() + buffer.length);
                        STRING_JOB_PACKAGE_HASH_MAP_FOR_UPLOAD.replace(jobPackage.getName(), jobPackage);
                    }
                    dataInputStream.close();
                    dataOutputStream.close();
                    return new Gson().toJson(new StandardResponse(StatusResponse.SUCCESS));
                } else {
                    return new Gson().toJson(new StandardResponse(StatusResponse.WARNING, "The file you have requested does not exist!"));
                }
            } catch (Exception exception) {
                exception.printStackTrace();
                new Thread(write_stack_trace(exception)).start();
                Platform.runLater(() -> programmer_error(exception).show());
                response.type("application/json");
                return new Gson().toJson(new StandardResponse(StatusResponse.ERROR, exception.toString()));
            }
        }));
    }

    private void watch() {
        get(CONTEXT_PATH.concat("/watch/trailer/:category/:mediaId"), ((request, response) -> {
            response.type("video/mp4");
            final String category = request.params(":category");
            final String mediaId = request.params(":mediaId");
            response.status(HttpURLConnection.HTTP_OK);
            try {
                if (listOfTrailerIds.get(category).containsKey(mediaId)) {
                    String path = null;
                    final String fileName = listOfTrailerIds.get(category).get(mediaId);
                    final JsonArray jsonArray = read_jsonArray_from_file(new File(format_path_name_to_current_os(TRAILERS_JSON_FILE)));
                    outer:
                    for (JsonElement jsonElement : jsonArray) {
                        final String olderPath = jsonElement.getAsString();
                        final JsonArray jsonArray1 = get_directory_of_sub_files_in_the_provided_folder(new File(olderPath));
                        for (JsonElement jsonElement1 : jsonArray1) {
                            final String filePath = jsonElement1.getAsString();
                            if (filePath.endsWith(fileName)) {
                                path = filePath;
                                break outer;
                            }
                        }
                    }
                    if (path == null) {
                        response.type("application/json");
                        return new Gson().toJson(new StandardResponse(StatusResponse.WARNING, fileName.concat(" does not exist!")));
                    }
                    File file = new File(path);
                    if (file.exists()) {
                        response.raw().setContentLengthLong(file.length());
                        DataOutputStream dataOutputStream = new DataOutputStream(new BufferedOutputStream(response.raw().getOutputStream()));
                        DataInputStream dataInputStream = new DataInputStream(new BufferedInputStream(new FileInputStream(file)));
                        int count;
                        byte[] buffer = new byte[8192];
                        while ((count = dataInputStream.read(buffer)) > 0) {
                            dataOutputStream.write(buffer, 0, count);
                        }
                        dataInputStream.close();
                        dataOutputStream.close();
                        return HttpURLConnection.HTTP_OK;
                    }
                }
                return HttpURLConnection.HTTP_NO_CONTENT;
            } catch (Exception exception) {
                exception.printStackTrace();
                new Thread(write_stack_trace(exception)).start();
                Platform.runLater(() -> programmer_error(exception).show());
                response.type("application/json");
                return new Gson().toJson(new StandardResponse(StatusResponse.ERROR, exception.toString()));
            }
        }));
    }

    private void provide_list() {
        get(CONTEXT_PATH.concat("/trailer"), ((request, response) -> {
            response.type("application/json");
            response.status(HttpURLConnection.HTTP_OK);
            try {
                final JsonArray jsonArray = get_trailer_list_with_readable_names(read_jsonArray_from_file(new File(format_path_name_to_current_os(TRAILER_KEY_JSON_FILE))));
                if (jsonArray.size() == 0) {
                    return new Gson().toJson(new StandardResponse(StatusResponse.WARNING, "No trailers are available!"));
                } else {
                    return new Gson().toJson(new StandardResponse(StatusResponse.SUCCESS, new Gson().toJsonTree(jsonArray, JsonArray.class)));
                }
            } catch (IOException exception) {
                exception.printStackTrace();
                new Thread(write_stack_trace(exception)).start();
                Platform.runLater(() -> programmer_error(exception).show());
                return new Gson().toJson(new StandardResponse(StatusResponse.ERROR, exception.toString()));
            }
        }));
    }

    private JsonArray get_trailer_list_with_readable_names(JsonArray trailerList) {
        final JsonArray jsonArray = new JsonArray();
        trailerList.forEach(jsonElement -> {
            Category category = new Gson().fromJson(jsonElement, Category.class);
            JsonArray jsonElements = new JsonArray();
            category.getMedia().forEach(jsonElement1 -> {
                Media media = new Gson().fromJson(jsonElement1, Media.class);
                media.setValue(format_file_name(media.getValue()));
                jsonElements.add(new Gson().toJsonTree(media, Media.class));
            });
            category.setMedia(jsonElements);
            jsonArray.add(new Gson().toJsonTree(category, Category.class));
        });
        return jsonArray;
    }

    private JsonArray get_list_of_pending_uploads() {
        final JsonArray jsonArray = new JsonArray();
        Set<String> stringSet = STRING_JOB_PACKAGE_HASH_MAP_FOR_UPLOAD.keySet();
        stringSet.forEach(key -> {
            if (!listOfActiveTasks.contains(key)) {
                jsonArray.add(new Gson().toJsonTree(key, String.class));
            }
        });
        return jsonArray;
    }

}
