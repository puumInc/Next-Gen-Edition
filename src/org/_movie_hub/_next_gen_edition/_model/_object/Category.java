package org._movie_hub._next_gen_edition._model._object;

import com.google.gson.Gson;
import com.google.gson.JsonArray;

import java.io.Serializable;

/**
 * @author Mandela aka puumInc
 */

public class Category implements Serializable {

    public static final long serialVersionUID = 4L;

    private String name;
    private JsonArray media;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public JsonArray getMedia() {
        return media;
    }

    public void setMedia(JsonArray media) {
        this.media = media;
    }

    @Override
    public String toString() {
        return new Gson().toJson(this, Category.class);
    }
}
