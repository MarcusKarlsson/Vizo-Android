package com.vizo.news.api;

import retrofit.RestAdapter;

import android.content.Context;
import android.util.Log;

public class APIClient {

    private static final String TAG = "APIClient";

    private static APIClient mInstance = null;

    private Context mApplicationContext;

    private APIService mAPIService;

    private APIRequestInterceptor mRequestInterceptor;

    /**
     * Default constructor to create Instance
     */
    private APIClient() {
    }

    /**
     * Sets the Application {@link android.content.Context} and {@link APIRequestInterceptor}
     *
     * @param context
     */
    private void init(Context context) {
        Log.d(TAG, "init()....");
        mApplicationContext = context.getApplicationContext();

        mRequestInterceptor = new APIRequestInterceptor();

        mAPIService = new RestAdapter.Builder()
                .setRequestInterceptor(mRequestInterceptor)
                .setEndpoint(APIConstants.REST_HOST).build()
                .create(APIService.class);
    }

    public static APIClient getInstance(Context context) {
        Log.d(TAG, "getInstance()....");
        if (mInstance == null) {
            Log.d(TAG, "getInstance() - mInstance == null");
            mInstance = new APIClient();
            mInstance.init(context);
        }
        return mInstance;
    }

    public APIService getApiService() {
        Log.d(TAG, "getApiService()....");
        return mAPIService;
    }

}