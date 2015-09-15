package com.vizo.news.domain;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;
import com.vizo.news.database.DatabaseConstants;
import com.vizo.news.database.DatabaseProvider;
import com.vizo.news.utils.LocalStorage;

import java.util.ArrayList;

public class VizoUGlance implements Parcelable {

    @SerializedName("id")
    public String glanceId;
    public String category_id;
    public String title;

    @SerializedName("glance_text")
    public String description;

    public String image_url;

    @SerializedName("image_url2")
    public String image_sub_url;

    public String image_title;
    public String image_credit;
    public String image_caption;

    @SerializedName("lang")
    public String language;

    public String syncState;

    @SerializedName("stared")
    public int isFavorite;
    public int state_of_day;

    public String poster_name;
    public String poster_avatar;
    public String poster_email;

    @SerializedName("posted_date")
    public String modified_date;

    public Integer vote_count;

    /**
     * Required function for implementing Parceable. (We will not need to use
     * this)
     */
    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(glanceId);
        dest.writeString(category_id);
        dest.writeString(title);
        dest.writeString(description);
        dest.writeString(image_url);
        dest.writeString(image_sub_url);
        dest.writeString(image_title);
        dest.writeString(image_credit);
        dest.writeString(image_caption);
        dest.writeString(modified_date);
        dest.writeString(language);
        dest.writeInt(isFavorite);
        dest.writeInt(state_of_day);
    }

    private VizoUGlance(Parcel in) {
        this.glanceId = in.readString();
        this.category_id = in.readString();
        this.title = in.readString();
        this.description = in.readString();
        this.image_url = in.readString();
        this.image_sub_url = in.readString();
        this.title = in.readString();
        this.image_credit = in.readString();
        this.image_caption = in.readString();
        this.modified_date = in.readString();
        this.language = in.readString();
        this.isFavorite = in.readInt();
        this.state_of_day = in.readInt();
    }

    public VizoUGlance() {
    }

    /**
     * Creator that regenerates the object.
     */
    public static final Creator<VizoUGlance> CREATOR = new Creator<VizoUGlance>() {
        public VizoUGlance createFromParcel(Parcel in) {
            return new VizoUGlance(in);
        }

        public VizoUGlance[] newArray(int size) {
            return new VizoUGlance[size];
        }
    };

    /**
     * Get short description of the glance
     *
     * @return The shortened description with the length of 150
     */
    public String getShortDescription() {
        String shortDescription = description;
        if (description != null && description.length() > 150) {
            shortDescription = String.format("%s...", description.substring(0, 150));
        }
        return shortDescription;
    }

    /**
     * Convert glance object instance to content values
     *
     * @return content values
     */
    public ContentValues toContentValues() {
        ContentValues values = new ContentValues();
        values.put(DatabaseConstants.GLANCE_ID, this.glanceId);
        values.put(DatabaseConstants.CATEGORY_ID, this.category_id);
        values.put(DatabaseConstants.TITLE, this.title);
        values.put(DatabaseConstants.DESCRIPTION, this.description);
        values.put(DatabaseConstants.IMAGE_URL, this.image_url);
        values.put(DatabaseConstants.IMAGE_SUB_URL, this.image_sub_url);
        values.put(DatabaseConstants.IMAGE_CREDIT, this.image_credit);
        values.put(DatabaseConstants.MODIFIED_DATE, this.modified_date);
        values.put(DatabaseConstants.LANG, this.language);
        values.put(DatabaseConstants.SYNC_STATE, this.syncState);
        values.put(DatabaseConstants.IS_FAVORITE, this.isFavorite);
        values.put(DatabaseConstants.STATE_OF_DAY, this.state_of_day);
        return values;
    }

    /**
     * Get glance object from cursor
     *
     * @param cursor cursor object
     * @return glance object
     */
    public static VizoUGlance fromCursor(Cursor cursor) {
        VizoUGlance glance = new VizoUGlance();
        glance.glanceId = cursor.getString(cursor.getColumnIndex(DatabaseConstants.GLANCE_ID));
        glance.category_id = cursor.getString(cursor.getColumnIndex(DatabaseConstants.CATEGORY_ID));
        glance.title = cursor.getString(cursor.getColumnIndex(DatabaseConstants.TITLE));
        glance.description = cursor.getString(cursor.getColumnIndex(DatabaseConstants.DESCRIPTION));
        glance.image_url = cursor.getString(cursor.getColumnIndex(DatabaseConstants.IMAGE_URL));
        glance.image_sub_url = cursor.getString(cursor.getColumnIndex(DatabaseConstants.IMAGE_SUB_URL));
        glance.image_credit = cursor.getString(cursor.getColumnIndex(DatabaseConstants.IMAGE_CREDIT));
        glance.modified_date = cursor.getString(cursor.getColumnIndex(DatabaseConstants.MODIFIED_DATE));
        glance.language = cursor.getString(cursor.getColumnIndex(DatabaseConstants.LANG));
        glance.syncState = cursor.getString(cursor.getColumnIndex(DatabaseConstants.SYNC_STATE));
        glance.isFavorite = cursor.getInt(cursor.getColumnIndex(DatabaseConstants.IS_FAVORITE));
        glance.state_of_day = cursor.getInt(cursor.getColumnIndex(DatabaseConstants.STATE_OF_DAY));
        return glance;
    }

    /**
     * Insert or update glance to database
     *
     * @param context Context object
     */
    public void insertOrUpdate(Context context) {

        ContentValues values = this.toContentValues();
        ContentResolver contentResolver = context.getContentResolver();

        String selection = DatabaseConstants.GLANCE_ID + " =?";
        String args[] = {this.glanceId};
        Cursor cursor = contentResolver.query(
                DatabaseProvider.GLANCES_URI, null, selection, args, null);

        // Check if the glance already exist in the db
        if (cursor != null && cursor.getCount() > 0) {

            // Update glance
            contentResolver.update(DatabaseProvider.GLANCES_URI, values, selection, args);
        } else {

            // If not exist, add a new one
            contentResolver.insert(DatabaseProvider.GLANCES_URI, values);
        }

        if (cursor != null) {
            cursor.close();
        }
    }

    /**
     * Add current glance object instance to database as glanced item
     *
     * @param context Context object
     */
    public void addAsGlanced(Context context) {

        ContentValues values = this.toContentValues();
        ContentResolver contentResolver = context.getContentResolver();

        String selection = DatabaseConstants.GLANCE_ID + " =?";
        String args[] = {this.glanceId};
        Cursor cursor = contentResolver.query(DatabaseProvider.GLANCED_URI,
                null, selection, args, null);

        // Check if already added as a glanced item
        if (cursor != null && cursor.getCount() > 0) {

            // If already inserted, update
            contentResolver.update(DatabaseProvider.GLANCED_URI, values,
                    selection, args);
        } else {

            // If not registered, insert item
            contentResolver.insert(DatabaseProvider.GLANCED_URI, values);
        }

        if (cursor != null) {
            cursor.close();
        }
    }

    /**
     * Check if this object is already glanced
     *
     * @param context Context object
     * @return true if glanced, otherwise false
     */
    public boolean isGlanced(Context context) {
        ContentResolver contentResolver = context.getContentResolver();
        String selection = DatabaseConstants.GLANCE_ID + " =?"
                + " AND " + DatabaseConstants.SYNC_STATE + " IS NOT ?";
        String args[] = {glanceId, DatabaseConstants.DELETE_REQUIRED};

        boolean isGlanced = false;
        Cursor cursor = contentResolver.query(DatabaseProvider.GLANCED_URI,
                null, selection, args, null);
        if (cursor != null && cursor.getCount() > 0) {
            isGlanced = true;
        }

        if (cursor != null) {
            cursor.close();
        }

        return isGlanced;
    }

    /**
     * Check if the glance is favorite
     *
     * @param context Context object
     * @return true if favorite, otherwise false
     */
    public boolean isFavorite(Context context) {
        ContentResolver contentResolver = context.getContentResolver();
        String selection = DatabaseConstants.GLANCE_ID + " =?"
                + " AND " + DatabaseConstants.SYNC_STATE + " IS NOT ?";
        String args[] = {glanceId, DatabaseConstants.DELETE_REQUIRED};

        isFavorite = 0;
        Cursor cursor = contentResolver.query(DatabaseProvider.GLANCED_URI,
                null, selection, args, null);
        if (cursor != null && cursor.getCount() > 0 && cursor.moveToFirst()) {
            isFavorite = fromCursor(cursor).isFavorite;
        }

        if (cursor != null) {
            cursor.close();
        }

        return isFavorite == 1;
    }

    /**
     * Remove item from Glanced Table after synchronization
     *
     * @param context Context object
     */
    public void deleteFromGlanced(Context context) {
        ContentResolver contentResolver = context.getContentResolver();
        String selection = DatabaseConstants.GLANCE_ID + "=?";
        String args[] = {glanceId};

        contentResolver.delete(DatabaseProvider.GLANCED_URI, selection, args);
    }

    /**
     * Get all pending (non-synchronized) glanced items
     *
     * @param context Context object
     * @return Array of glances that are glanced
     */
    public static ArrayList<VizoUGlance> getPendingGlancedItems(Context context) {
        ArrayList<VizoUGlance> glances = new ArrayList<>();
        ContentResolver contentResolver = context.getContentResolver();

        String selection = DatabaseConstants.SYNC_STATE + " IS NOT NULL";

        Cursor cursor = contentResolver.query(DatabaseProvider.GLANCED_URI,
                null, selection, null, null);
        if (cursor != null && cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                glances.add(VizoUGlance.fromCursor(cursor));
            }
        }

        if (cursor != null) {
            cursor.close();
        }

        return glances;
    }

    /**
     * Get all glances in State Of The Day
     *
     * @param context Context object
     * @return Array of glances in State Of The Day
     */
    public static ArrayList<VizoUGlance> getStateOfDayGlances(Context context) {
        ArrayList<VizoUGlance> glances = new ArrayList<>();
        ContentResolver contentResolver = context.getContentResolver();

        String selection = DatabaseConstants.STATE_OF_DAY + " =1";

        Cursor cursor = contentResolver.query(DatabaseProvider.GLANCES_URI,
                null, selection, null, null);
        if (cursor != null && cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                glances.add(VizoUGlance.fromCursor(cursor));
            }
        }

        if (cursor != null) {
            cursor.close();
        }

        return glances;
    }

    /**
     * Clear all glanced items of current edition
     *
     * @param context Context object
     */
    public static void clearGlancedHistory(Context context) {
        ContentResolver contentResolver = context.getContentResolver();

        // Check if user is logged-in user
        String accessToken = null;
        VizoUser user = LocalStorage.getInstance().loadSavedAuthUserInfo();
        if (user != null) {
            accessToken = user.access_token;
        }

        if (accessToken == null) {

            // If the user is local user
            // than we can simply remove items of current edition from database
            String selection = DatabaseConstants.LANG + " =?";
            String args[] = {LocalStorage.getInstance().loadAppLanguage()};
            contentResolver.delete(DatabaseProvider.GLANCED_URI, selection, args);

        } else {

            // If the user is logged-in user, we should set sync flag
            String selection = DatabaseConstants.LANG + " =?";
            String args[] = {LocalStorage.getInstance().loadAppLanguage()};

            ContentValues values = new ContentValues();
            values.put(DatabaseConstants.SYNC_STATE, DatabaseConstants.DELETE_REQUIRED);
            contentResolver.update(DatabaseProvider.GLANCED_URI, values, selection, args);

        }
    }

    /**
     * Clear all glances in Glances table
     *
     * @param context Context object
     */
    public static void clearTable(Context context) {
        ContentResolver contentResolver = context.getContentResolver();
        contentResolver.delete(DatabaseProvider.GLANCES_URI, null, null);
    }

}

