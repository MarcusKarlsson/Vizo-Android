package com.vizo.news.service;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Process;

import com.google.android.gcm.GCMBaseIntentService;
import com.vizo.news.R;
import com.vizo.news.activities.HomeScreenActivity;
import com.vizo.news.activities.OnboardingActivity;
import com.vizo.news.utils.Constants;
import com.vizo.news.utils.LocalStorage;

/**
 * Custom GCM Service
 * <p/>
 * Created by nine3_marks on 5/04/2015.
 */
public class GCMIntentService extends GCMBaseIntentService {

    private Looper mServiceLooper;
    private Context mContext;

    public GCMIntentService() {
        super(Constants.SENDER_ID);
        HandlerThread thread = new HandlerThread(TAG + "HandlerThread",
                Process.THREAD_PRIORITY_BACKGROUND);
        thread.start();
        mServiceLooper = thread.getLooper();
    }

    @Override
    protected void onError(Context arg0, String arg1) {
    }

    @Override
    protected void onMessage(Context context, Intent intent) {
        mContext = context;
        Handler h = new Handler(mServiceLooper);
        final Bundle bundle = intent.getExtras();
        h.post(new Runnable() {
            @Override
            public void run() {
                generateNotification(mContext, bundle);
            }
        });

        LocalStorage.getInstance().setFlagValue(LocalStorage.NEW_GLANCES_POSTED, true);
        HomeScreenActivity homeActivity = HomeScreenActivity.instance;
        if (homeActivity != null) {
            homeActivity.showRefreshIcon();
        }
    }

    @Override
    protected void onRegistered(Context context, String registrationId) {
        if (registrationId != null) {

            // Save device token to Preferences
            LocalStorage.getInstance().saveDeviceToken(registrationId);

            // Start service to register device-token to Vizo
            Intent mIntent = new Intent(getApplicationContext(), GCMRegisterService.class);
            startService(mIntent);

        }
    }

    @Override
    protected void onUnregistered(Context context, String registrationId) {
    }

    /**
     * Issues a notification to inform the user that server has sent a message.
     */
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private static void generateNotification(Context context, Bundle bundle) {

        int icon = R.drawable.ic_launcher;
        long when = System.currentTimeMillis();
        NotificationManager notificationManager = (NotificationManager) context
                .getSystemService(Context.NOTIFICATION_SERVICE);

        String title = context.getString(R.string.app_name);
        Intent notificationIntent = new Intent(context, OnboardingActivity.class);
        notificationIntent.putExtra("category", bundle.getString("category"));

        // set intent so it does not start a new activity
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |
                Intent.FLAG_ACTIVITY_SINGLE_TOP);

        String message = bundle.getString("alert");

        PendingIntent intent = PendingIntent.getActivity(context, 0,
                notificationIntent, 0);
        Notification notification = new Notification.Builder(context)
                .setContentTitle(title).setContentText(message)
                .setSmallIcon(icon).setContentIntent(intent).setWhen(when)
                .setAutoCancel(true).build();

        // Play default notification sound
        notification.defaults |= Notification.DEFAULT_SOUND;

        // Vibrate if vibrate is enabled
        notification.defaults |= Notification.DEFAULT_VIBRATE;

        int time = (int) (System.currentTimeMillis());
        notificationManager.notify(time, notification);
    }
}

