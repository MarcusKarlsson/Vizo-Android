package com.vizo.news.service;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import com.vizo.news.api.APIClient;
import com.vizo.news.api.domain.GeneralResponse;
import com.vizo.news.utils.Constants;
import com.vizo.news.utils.LocalStorage;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

/**
 * Custom receiver which is called after success register to GCM
 * <p/>
 * Created by nine3_marks on 5/5/2015.
 */
public class GCMRegisterService extends IntentService {

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     */
    public GCMRegisterService() {
        super("GCMRegisterService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        String registrationId = LocalStorage.getInstance().getDeviceToken();
        Log.d("GCMRegisterService", "Device Token: " + registrationId);

        boolean result;
        try {
            Date now = new Date();
            TimeZone tz = Calendar.getInstance().getTimeZone();
            String name = tz.getDisplayName(tz.inDaylightTime(now), TimeZone.SHORT);
            GeneralResponse response = APIClient
                    .getInstance(getBaseContext())
                    .getApiService()
                    .registerDeviceToken(registrationId, Constants.ANDROID_PLATFORM, "", name);
            result = response.result;
        } catch (Exception e) {
            result = false;
        }

        Log.d("GCMRegisterService", "Device token updated: " + (result ? "Success" : "Failure"));
        if (result) {
            LocalStorage.getInstance().setFlagValue(LocalStorage.TOKEN_REGISTERED, true);
        }
    }
}
