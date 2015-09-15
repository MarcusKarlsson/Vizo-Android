package com.vizo.news.api.domain;

import com.google.gson.annotations.SerializedName;
import com.vizo.news.domain.VizoUGlance;

import java.util.ArrayList;

/**
 * Custom response class for VizoU endpoint
 * <p/>
 * Created by nine3_marks on 6/22/2015.
 */
public class VizoUGlancesResponse {

    @SerializedName("rows")
    public ArrayList<VizoUGlance> glances;

    @SerializedName("total")
    public int totalCount;
}
