package com.vizo.news.api.domain;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

/**
 * Custom object class for Article Source
 * <p/>
 * Created by nine3_marks on 3/31/2015.
 */
public class ArticleSourcesResponse extends ArrayList<ArticleSourcesResponse.VizoArticleSource> {

    public static class VizoArticleSource {

        @SerializedName("id")
        public String sourceId;
        public String address;
        public String logo_url;
        public Integer must_translate;
    }
}
