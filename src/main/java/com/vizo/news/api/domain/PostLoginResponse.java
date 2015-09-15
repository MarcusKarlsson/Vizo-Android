package com.vizo.news.api.domain;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

import com.vizo.news.domain.VizoUser;

/**
 * Created by MarksUser on 4/7/2015.
 *
 * @author nine3_marks
 */
public class PostLoginResponse {

    @SerializedName("result")
    public boolean result;

    @SerializedName("user")
    public VizoUser user;

    @SerializedName("error")
    public String errorMessage;

    @SerializedName("preferences")
    public UserSettings preferences;

    /**
     * Get user settings
     *
     * @return Return VizoSettings object
     */
    public VizoSettings getUserSettings() {
        VizoSettings settings = null;
        Gson gson = new Gson();
        if (preferences != null) {
            try {
                settings = gson.fromJson(preferences.settings, VizoSettings.class);
            } catch (Exception e) {
                settings = null;
            }
        }
        return settings;
    }

    private class UserSettings {

        @SerializedName("preference_settings")
        public String settings;

    }
}
