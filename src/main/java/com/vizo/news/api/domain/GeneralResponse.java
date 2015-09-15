package com.vizo.news.api.domain;

import com.google.gson.annotations.SerializedName;

/**
 * Custom class which handles general response
 * <p/>
 * Created by nine3_marks on 5/5/2015.
 */
public class GeneralResponse {

    @SerializedName("msg")
    public String message;

    @SerializedName("result")
    public boolean result;
}
