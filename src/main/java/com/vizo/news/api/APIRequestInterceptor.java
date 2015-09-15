package com.vizo.news.api;

import com.vizo.news.domain.VizoUser;
import com.vizo.news.utils.LocalStorage;

import retrofit.RequestInterceptor;

public class APIRequestInterceptor implements RequestInterceptor {

    @Override
    public void intercept(RequestFacade request) {

        // Include locale information in request header
        request.addHeader("Language", LocalStorage.getInstance().loadAppLanguage());

        // Include access token in request header
        String accessToken = null;
        VizoUser user = LocalStorage.getInstance().loadSavedAuthUserInfo();
        if (user != null) {
            accessToken = user.access_token;
        }

        if (accessToken != null) {
            request.addHeader("Authorization", String.format("Bearer %s", accessToken));
        }
    }
}