package com.vizo.news.domain;


import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

/**
 * Created by MarksUser on 4/7/2015.
 *
 * @author nine3_marks
 */
public class VizoUser implements Parcelable {

    @SerializedName("id")
    public int userId;
    public String access_token;
    public String email;

    @SerializedName("passwd")
    public String password;
    public String facebook_id;
    public String twitter_id;
    public String first_name;
    public String last_name;
    public String avatar_image;

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
        dest.writeInt(userId);
        dest.writeString(access_token);
        dest.writeString(email);
        dest.writeString(password);
        dest.writeString(facebook_id);
        dest.writeString(twitter_id);
        dest.writeString(first_name);
        dest.writeString(last_name);
        dest.writeString(avatar_image);
    }

    private VizoUser(Parcel in) {
        this.userId = in.readInt();
        this.access_token = in.readString();
        this.email = in.readString();
        this.password = in.readString();
        this.facebook_id = in.readString();
        this.twitter_id = in.readString();
        this.first_name = in.readString();
        this.last_name = in.readString();
        this.avatar_image = in.readString();
    }

    public VizoUser() {
    }

    /**
     * Creator that regenerates the object.
     */
    public static final Parcelable.Creator<VizoUser> CREATOR = new Parcelable.Creator<VizoUser>() {
        public VizoUser createFromParcel(Parcel in) {
            return new VizoUser(in);
        }

        public VizoUser[] newArray(int size) {
            return new VizoUser[size];
        }
    };
}
