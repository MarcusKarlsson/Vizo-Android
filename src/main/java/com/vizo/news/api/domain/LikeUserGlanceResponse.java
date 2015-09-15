package com.vizo.news.api.domain;

import com.google.gson.annotations.SerializedName;

/**
 * Custom response for like/dislike user glance request
 * <p/>
 * Created by nine3_marks on 6/26/2015.
 */
public class LikeUserGlanceResponse {

    @SerializedName("result")
    public Boolean result;

    @SerializedName("new_count")
    public Integer new_count;
}
