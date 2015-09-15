package com.vizo.news.domain;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

/**
 * Custom object for Vizo FAQ
 * <p/>
 * Created by nine3_marks on 4/27/2015.
 */
public class VizoFAQ {

    @SerializedName("title")
    public String title;

    @SerializedName("questions")
    public ArrayList<VizoQuestion> questions;

    public class VizoQuestion {

        @SerializedName("question")
        public String question;

        @SerializedName("answer")
        public String answer;

    }

}
