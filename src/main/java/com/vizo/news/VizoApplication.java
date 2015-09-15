package com.vizo.news;

import android.app.Application;
import android.content.res.Configuration;

import com.droidux.core.components.DroidUxLibrary;
import com.facebook.FacebookSdk;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.nostra13.universalimageloader.cache.memory.impl.WeakMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

import com.twitter.sdk.android.Twitter;
import com.twitter.sdk.android.core.TwitterAuthConfig;

import io.fabric.sdk.android.Fabric;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Locale;

import com.vizo.news.domain.VizoFAQ;
import com.vizo.news.utils.CommonUtils;
import com.vizo.news.utils.Constants;
import com.vizo.news.utils.DateUtils;
import com.vizo.news.utils.LocalStorage;

/**
 * Application controller for Vizo Application
 * <p/>
 * Created by nine3_marks on 3/26/2015.
 */
public class VizoApplication extends Application {

    private ArrayList<VizoFAQ> vizoFAQs;

    @Override
    public void onCreate() {
        super.onCreate();

        TwitterAuthConfig authConfig = new TwitterAuthConfig(Constants.TWITTER_KEY,
                Constants.TWITTER_SECRET);
        Fabric.with(this, new Twitter(authConfig));

        // Initialize the singletons so their instances
        // are bound to the application process.
        initSingletons();

        // Determine default language info from the phone
        String language = LocalStorage.getInstance().loadAppLanguage();
        if (language == null) {
            language = Locale.getDefault().getLanguage();
            if (language.equals("he") || language.equals("iw")) {
                language = "he";
            } else {
                language = "en";
            }
            LocalStorage.getInstance().saveAppLanguage(language);
        }

        // load saved language settings and change global app locale
        Locale locale = new Locale(LocalStorage.getInstance().loadAppLanguage());
        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.locale = locale;
        getBaseContext().getResources().updateConfiguration(
                config, getBaseContext().getResources().getDisplayMetrics());

        loadFAQsFromAssets();

        // Register DroidUX component
        DroidUxLibrary.register(Constants.DROIDUX_LICENSE, this);

        // Initialize Facebook SDK for social media authentication
        FacebookSdk.sdkInitialize(getApplicationContext());
    }

    /**
     * Initialize singletons
     */
    private void initSingletons() {
        DisplayImageOptions defaultOptions = new DisplayImageOptions
                .Builder()
                .resetViewBeforeLoading(false)
                .cacheOnDisk(true)
                .build();
        ImageLoaderConfiguration config = new ImageLoaderConfiguration
                .Builder(getApplicationContext())
                .memoryCache(new WeakMemoryCache())
                .memoryCacheSizePercentage(50)
                .denyCacheImageMultipleSizesInMemory()
                .defaultDisplayImageOptions(defaultOptions)
                .build();
        ImageLoader.getInstance().init(config);

        CommonUtils.init(getApplicationContext());
        DateUtils.init(getApplicationContext());
        LocalStorage.init(getApplicationContext());
    }

    /**
     * Load vizo faqs from resource
     */
    public void loadFAQsFromAssets() {
        String assetName = String.format("faqs/faq_%s.json", LocalStorage.getInstance().loadAppLanguage());
        String json = CommonUtils.getInstance().loadJSONFromAssets(assetName);

        vizoFAQs = new ArrayList<>();
        Gson gson = new Gson();
        try {
            Type listType = new TypeToken<ArrayList<VizoFAQ>>() {
            }.getType();
            vizoFAQs = gson.fromJson(json, listType);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Getter function for VizoFAQs
     *
     * @return VizoFAQs
     */
    public ArrayList<VizoFAQ> getVizoFAQs() {
        return vizoFAQs;
    }

}
