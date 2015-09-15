package com.vizo.news.api.domain;

import com.google.gson.annotations.SerializedName;
import com.vizo.news.domain.VizoGlance;

import java.util.ArrayList;

/**
 * Element of categoriesWithGlances response
 * <p/>
 * Created by nine3_marks on 5/15/2015.
 */
public class VizoCategoryResponse {

    @SerializedName("id")
    public String categoryId;

    @SerializedName("name")
    public String category_name;

    @SerializedName("image_url")
    public String imageUrl;

    @SerializedName("glances")
    public ArrayList<VizoGlance> glances;
}
