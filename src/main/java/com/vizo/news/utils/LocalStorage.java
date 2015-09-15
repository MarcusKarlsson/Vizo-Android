package com.vizo.news.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.google.gson.Gson;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import com.google.gson.reflect.TypeToken;
import com.vizo.news.domain.VizoUser;

import org.json.JSONObject;

/**
 * Created by MarksUser on 3/26/2015.
 *
 * @author nine3_marks
 */
public class LocalStorage {

    private final SharedPreferences mSharedPreferences;
    private static LocalStorage sInstance;

    // Preference Constants for Flags
    public static final String TUTORIAL_VIEWED = "TUTORIAL_VIEWED";
    public static final String WALKED_THROUGH_HOME = "WALKED_THROUGH_HOME";
    public static final String WALKED_THROUGH_FULL = "WALKED_THROUGH_FULL";
    public static final String WALKED_THROUGH_GLANCED = "WALKED_THROUGH_GLANCED";
    public static final String WALKED_THROUGH_PREFERENCES = "WALKED_THROUGH_PREFERENCES";
    public static final String WALKED_THROUGH_REFRESH = "WALKED_THROUGH_REFRESH";
    public static final String IS_SHOW_TIPS = "IS_SHOW_TIPS";
    public static final String AUTO_TRANSLATE = "AUTO_TRANSLATE";
    public static final String TOKEN_REGISTERED = "TOKEN_REGISTERED";
    public static final String SYNCHRONIZED_PREFS = "SYNCHRONIZED_PREFS";
    public static final String NEW_GLANCES_POSTED = "NEW_GLANCES_POSTED";

    // Preference Constants for Offline Feature and User Info
    private final String CATEGORIES_WITH_GLANCES = "CATEGORIES_WITH_GLANCES";
    private final String AUTH_USER_INFO = "AUTH_USER_INFO";

    // Glance Preference Settings
    public static final String LEFT_PANEL_SETTINGS = "LEFT_PANEL_SETTINGS";
    public static final String RIGHT_PANEL_SETTINGS = "RIGHT_PANEL_SETTINGS";

    // Application language settings
    private static final String APP_LANGUAGE = "APP_LANGUAGE";

    // Preference for Google Cloud Messaging Registration ID
    private static final String GCM_DEVICE_TOKEN = "GCM_DEVICE_TOKEN";

    // Facebook account connection info
    private static final String FACEBOOK_INFO = "FACEBOOK_INFO";

    // Liked glances
    private static final String LIKED_GLANCES = "LIKED_GLANCES";
    private static final String DISLIKED_GLANCES = "DISLIKED_GLANCES";

    private LocalStorage(final Context context) {
        mSharedPreferences = PreferenceManager
                .getDefaultSharedPreferences(context);
    }

    public static LocalStorage init(final Context context) {
        if (sInstance == null) {
            sInstance = new LocalStorage(context);
        }
        return sInstance;
    }

    public static LocalStorage getInstance() {
        return sInstance;
    }

    public void setFlagValue(String key, boolean value) {
        mSharedPreferences.edit().putBoolean(key, value).commit();
    }

    public boolean getFlagValue(String key) {
        return mSharedPreferences.getBoolean(key, false);
    }

    public void saveAuthUserInfo(VizoUser authUser) {
        String json = null;
        Gson gson = new Gson();
        try {
            json = gson.toJson(authUser);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (json != null) {
            mSharedPreferences.edit().putString(AUTH_USER_INFO, json).commit();
        }
    }

    public VizoUser loadSavedAuthUserInfo() {
        Gson gson = new Gson();
        VizoUser authUser;
        try {
            String json = mSharedPreferences.getString(AUTH_USER_INFO, "");
            authUser = gson.fromJson(json, VizoUser.class);
            return authUser;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Saves glance preference settings to App Preference
     *
     * @param panel    The key value of App Preference
     * @param settings The glance preference settings
     */
    public void saveGlancePreferenceSetting(String panel, ArrayList<String> settings) {
        String json = null;
        Gson gson = new Gson();
        try {
            json = gson.toJson(settings);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (json != null) {
            mSharedPreferences.edit().putString(panel, json).commit();
        }
    }

    /**
     * Get glance preference settings from Shared Preference
     *
     * @param panel The key value of App Preference
     * @return Returns glance preference settings saved with key on Shared Preference
     */
    public ArrayList<String> loadSavedGlancePreferences(String panel) {
        Gson gson = new Gson();
        ArrayList<String> settings = null;
        try {
            String json = mSharedPreferences.getString(panel, "");
            Type listType = new TypeToken<ArrayList<String>>() {
            }.getType();
            settings = gson.fromJson(json, listType);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (settings == null) {
            settings = new ArrayList<>();
        }
        return settings;
    }

    /**
     * Save user selected language to App Preference
     *
     * @param languageCode user selected language code
     */
    public void saveAppLanguage(String languageCode) {
        mSharedPreferences.edit().putString(APP_LANGUAGE, languageCode).commit();
    }

    /**
     * Get saved user selected language code
     *
     * @return saved language code
     */
    public String loadAppLanguage() {
        return mSharedPreferences.getString(APP_LANGUAGE, null);
    }

    /**
     * Save GCM device token to preference
     *
     * @param deviceToken GCM registration token
     */
    public void saveDeviceToken(String deviceToken) {
        mSharedPreferences.edit().putString(GCM_DEVICE_TOKEN, deviceToken).commit();
    }

    /**
     * Get GCM device token saved to Preferences
     *
     * @return GCM registration token
     */
    public String getDeviceToken() {
        return mSharedPreferences.getString(GCM_DEVICE_TOKEN, null);
    }

    /**
     * Save facebook account info to SharedPreferences
     *
     * @param userInfo Facebook account info JSON object
     */
    public void saveFacebookInfo(JSONObject userInfo) {
        String json = null;
        Gson gson = new Gson();
        try {
            json = gson.toJson(userInfo);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (json != null) {
            mSharedPreferences.edit().putString(FACEBOOK_INFO, json).commit();
        }
    }

    /**
     * Get facebook account info saved in SharedPreferences
     *
     * @return Facebook account info of JSON object
     */
    public JSONObject getSavedFacebookInfo() {
        Gson gson = new Gson();
        JSONObject userInfo;
        try {
            String json = mSharedPreferences.getString(FACEBOOK_INFO, "");
            userInfo = gson.fromJson(json, JSONObject.class);
        } catch (Exception e) {
            e.printStackTrace();
            userInfo = null;
        }
        return userInfo;
    }

    /**
     * Save liked glance to SharedPreferences
     *
     * @param glanceId Glance ID in String type
     */
    public void saveGlanceAsLiked(String glanceId, boolean like) {

        String key = like ? LIKED_GLANCES : DISLIKED_GLANCES;
        Set<String> values;
        values = mSharedPreferences.getStringSet(key, null);
        if (values == null) {
            values = new HashSet<>();
        }

        values.add(glanceId);
        mSharedPreferences.edit().putStringSet(key, values).commit();
    }

    /**
     * Check whether the glance is liked
     *
     * @param glanceId Glance ID in String type
     * @return true if the glance is liked, otherwise false
     */
    public boolean isLiked(String glanceId) {
        Set<String> values = mSharedPreferences.getStringSet(LIKED_GLANCES, null);
        return values != null && values.contains(glanceId);
    }

    /**
     * Check whether the glance is disliked
     *
     * @param glanceId Glance ID in String type
     * @return true if the glance is liked, otherwise false
     */
    public boolean isDisliked(String glanceId) {
        Set<String> values = mSharedPreferences.getStringSet(DISLIKED_GLANCES, null);
        return values != null && values.contains(glanceId);
    }

}
