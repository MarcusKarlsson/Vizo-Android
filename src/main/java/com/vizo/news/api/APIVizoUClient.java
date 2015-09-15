package com.vizo.news.api;

import retrofit.RestAdapter;

import android.content.Context;
import android.util.Log;

public class APIVizoUClient {

    private static final String TAG = "APIVizoUClient";

    private static APIVizoUClient mInstance = null;

    private Context mApplicationContext;

    private APIService mAPIService;

    private APIRequestInterceptor mRequestInterceptor;

    /**
     * Default constructor to create Instance
     */
    private APIVizoUClient() {
    }

    /**
     * Sets the Application {@link android.content.Context} and {@link APIRequestInterceptor}
     *
     * @param context Context object
     */
    private void init(Context context) {
        Log.d(TAG, "init()....");

        System.out.println("TESTING FROM GET API SERVICE");

        mApplicationContext = context.getApplicationContext();

        mRequestInterceptor = new APIRequestInterceptor();

        mAPIService = new RestAdapter.Builder()
                .setRequestInterceptor(mRequestInterceptor)
                .setEndpoint(APIConstants.REST_HOST_VIZOU).build()
                .create(APIService.class);
    }

    public static APIVizoUClient getInstance(Context context) {

        System.out.println("TESTING FROM GET API SERVICE");
        Log.d(TAG, "getInstance()....");
        if (mInstance == null) {
            Log.d(TAG, "getInstance() - mInstance == null");
            mInstance = new APIVizoUClient();
            mInstance.init(context);
        }
        return mInstance;
    }

    public APIService getApiService() {
        System.out.println("TESTING FROM GET API SERVICE");
        Log.d(TAG, "getApiService()....");
        return mAPIService;
    }

}