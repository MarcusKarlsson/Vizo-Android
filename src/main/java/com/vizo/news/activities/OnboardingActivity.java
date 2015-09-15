package com.vizo.news.activities;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.os.AsyncTask;
import android.os.Bundle;

import android.util.Base64;
import android.util.Log;
import android.widget.HorizontalScrollView;
import android.widget.Toast;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.twitter.sdk.android.Twitter;
import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.TwitterSession;
import com.twitter.sdk.android.core.identity.TwitterAuthClient;
import com.twitter.sdk.android.core.models.User;
import com.vizo.news.R;
import com.vizo.news.activities.base.BaseActivity;
import com.vizo.news.api.APIClient;
import com.vizo.news.api.APIConstants;
import com.vizo.news.api.domain.GetGlancedItemsResponse;
import com.vizo.news.api.domain.PostLoginResponse;
import com.vizo.news.api.domain.VizoSettings;
import com.vizo.news.domain.VizoCategory;
import com.vizo.news.domain.VizoGlance;
import com.vizo.news.fragments.logo_settings.editions.EditionsFragment;
import com.vizo.news.fragments.onboarding.SplashFragment;
import com.vizo.news.service.GCMRegisterService;
import com.vizo.news.utils.CommonUtils;
import com.vizo.news.utils.Constants;
import com.vizo.news.utils.LocalStorage;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Created by MarksUser on 3/21/2015.
 *
 * @author nine3_marks
 */
public class OnboardingActivity extends BaseActivity {

    private HorizontalScrollView svPanorama;
    private Timer timer;
    private int scrollX = 0;
    private boolean slideForward = true;

    private GoogleCloudMessaging gcm;

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.activity_onboarding);

        svPanorama = (HorizontalScrollView) findViewById(R.id.hsv_splash_panorama);
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        int offset = 1;
                        if (slideForward) {
                            scrollX += offset;
                            if (scrollX > 1000)
                                slideForward = false;
                        } else {
                            scrollX -= offset;
                            if (scrollX < 0)
                                slideForward = true;
                        }
                        svPanorama.scrollTo(scrollX, 0);
                    }
                });
            }
        }, 0, 10);

        try {
            PackageInfo info = getPackageManager().getPackageInfo(
                    "com.vizo.news",
                    PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                Log.e("KeyHash:", Base64.encodeToString(md.digest(), Base64.DEFAULT));
            }
        } catch (PackageManager.NameNotFoundException e) {

        } catch (NoSuchAlgorithmException e) {

        }
        showFragment(R.id.fl_fragment_container, SplashFragment.newInstance());
    }

    public void checkAndNavigate(boolean addToBackStack) {
        if (LocalStorage.getInstance().getFlagValue(LocalStorage.TUTORIAL_VIEWED)) {
            navigateToHome();
        } else {
            if (addToBackStack) {
                showFragment(R.id.fl_fragment_container, EditionsFragment.newInstance(), true);
            } else {
                showFragment(R.id.fl_fragment_container, EditionsFragment.newInstance());
            }
        }
    }

    public boolean isCategoryValid(){
        boolean bValid = true;

        ArrayList<VizoCategory> allCategories = VizoCategory.getAllCategories(this);

        if(allCategories == null || allCategories.size() == 0){
            bValid = false;
        }else {
            bValid = true;
        }

        return bValid;
    }
    public void navigateToHome() {

        if(isCategoryValid() == false){
            Toast.makeText(this, "Failed to load categories. Please check network connection!", Toast.LENGTH_LONG).show();
            return;
        }
        Intent intent = new Intent(OnboardingActivity.this, HomeScreenActivity.class);
        startActivity(intent);
        finish();


        // Check if the device is registered on the server
        if (!localStorage.getFlagValue(LocalStorage.TOKEN_REGISTERED)) {
            if (localStorage.getDeviceToken() == null) {

                // Register to Google Cloud Messaging
                // this should be done in background thread
                registerInBackground();
                return;
            }
        }

        // If the device already registered
        // than we should request with current edition for update language for push
        if (localStorage.getDeviceToken() != null) {
            postDeviceToken();
        }
    }

    // Post request to update device token info on server
    private void postDeviceToken() {
        new AsyncTask<Void, Void, String>() {

            @Override
            protected void onPreExecute() {
            }

            @Override
            protected String doInBackground(Void... params) {
                Intent mIntent = new Intent(getApplicationContext(), GCMRegisterService.class);
                startService(mIntent);
                return null;
            }

            @Override
            protected void onPostExecute(String result) {

            }
        }.execute(null, null, null);

    }

    public void stopPanorama() {
        timer.cancel();
    }

    /**
     * Registers the application with GCM servers asynchronously
     */
    private void registerInBackground() {
        new AsyncTask<Void, Void, String>() {

            @Override
            protected void onPreExecute() {
            }

            @Override
            protected String doInBackground(Void... params) {
                String message;
                try {
                    if (gcm == null) {
                        gcm = GoogleCloudMessaging
                                .getInstance(OnboardingActivity.this);
                    }
                    message = gcm.register(Constants.SENDER_ID);
                } catch (IOException e) {
                    message = null;
                }
                return message;
            }

            @Override
            protected void onPostExecute(String result) {
                if (result == null) {
                    return;
                }

                // Save device token to Preferences
                localStorage.saveDeviceToken(result);

                // Start service to register device-token to Vizo
                postDeviceToken();
            }
        }.execute(null, null, null);
    }

    public void performTwitterLogin() {
        authorizeTwitterWithCallback(new Callback<TwitterSession>() {
            @Override
            public void success(Result<TwitterSession> result) {
                progress.show();
                Twitter.getApiClient().getAccountService()
                        .verifyCredentials(true, false, new Callback<User>() {
                            @Override
                            public void success(Result<User> result) {
                                User user = result.data;
                                String twitterId = user.idStr;

                                // Twitter does not support API to fetch user's
                                // email address
                                // we will use generated email address
                                String email = user.name + "@twitter.com";
                                String firstName = user.name;
                                String avatarImage = user.profileImageUrl;
                                APIClient.getInstance(OnboardingActivity.this)
                                        .getApiService()
                                        .postCreateUserRequest(
                                                email,
                                                null,
                                                twitterId,
                                                null,
                                                firstName,
                                                null,
                                                avatarImage,
                                                mCallback);
                            }

                            @Override
                            public void failure(TwitterException e) {
                                progress.dismiss();
                                CommonUtils.getInstance().showMessage("Twitter login failed");
                            }
                        });
            }

            @Override
            public void failure(TwitterException e) {
                if(e.getMessage().contains("cancel")){

                }else {
                    CommonUtils.getInstance().showMessage("Twitter login failed");
                }
            }
        });
    }

    private retrofit.Callback<PostLoginResponse> mCallback = new retrofit.Callback<PostLoginResponse>() {
        @Override
        public void success(PostLoginResponse postLoginResponse, Response response) {
            if (postLoginResponse.result) {
                LocalStorage.getInstance().saveAuthUserInfo(postLoginResponse.user);

                VizoSettings settings = postLoginResponse.getUserSettings();
                if (settings != null) {
                    if (settings.leftSettings != null) {
                        localStorage.saveGlancePreferenceSetting(LocalStorage.LEFT_PANEL_SETTINGS,
                                settings.leftSettings);
                    }
                    if (settings.rightSettings != null) {
                        localStorage.saveGlancePreferenceSetting(LocalStorage.RIGHT_PANEL_SETTINGS,
                                settings.rightSettings);
                    }
                }

                // Fetch glanced items
                APIClient.getInstance(OnboardingActivity.this).getApiService()
                        .getGlancedItems(glancedCallback);
            } else {
                progress.dismiss();
                CommonUtils.getInstance().showMessage(postLoginResponse.errorMessage);
            }
        }

        @Override
        public void failure(RetrofitError error) {
            progress.dismiss();
            CommonUtils.getInstance().showMessage("Twitter Login Failed");
        }
    };

    private retrofit.Callback<GetGlancedItemsResponse> glancedCallback = new retrofit.Callback<GetGlancedItemsResponse>() {
        @Override
        public void success(GetGlancedItemsResponse vizoGlances, Response response) {
            progress.dismiss();
            for (VizoGlance glance : vizoGlances) {
                glance.addAsGlanced(OnboardingActivity.this);
            }

            checkAndNavigate(false);
        }

        @Override
        public void failure(RetrofitError error) {
            progress.dismiss();
            CommonUtils.getInstance().showMessage("Failed to fetch glanced items");
            LocalStorage.getInstance().saveAuthUserInfo(null);
        }
    };
}
