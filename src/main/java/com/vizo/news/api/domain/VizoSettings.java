package com.vizo.news.api.domain;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

/**
 * Custom class for user settings
 * <p/>
 * Created by nine3_marks on 5/9/2015.
 */
public class VizoSettings {

    @SerializedName("left")
    public ArrayList<String> leftSettings;

    @SerializedName("right")
    public ArrayList<String> rightSettings;

    @SerializedName("language")
    public String language;

    public String getJSONString() {
        Gson gson = new Gson();
        String json;
        try {
            json = gson.toJson(this, VizoSettings.class);
        } catch (Exception e) {
            json = null;
        }
        return json;
    }

}
