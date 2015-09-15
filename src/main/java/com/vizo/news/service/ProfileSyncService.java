package com.vizo.news.service;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import com.vizo.news.api.APIClient;
import com.vizo.news.api.domain.GeneralResponse;
import com.vizo.news.api.domain.GetGlancedItemsResponse;
import com.vizo.news.api.domain.VizoSettings;
import com.vizo.news.database.DatabaseConstants;
import com.vizo.news.domain.VizoGlance;
import com.vizo.news.domain.VizoUser;
import com.vizo.news.utils.LocalStorage;

import java.util.ArrayList;

/**
 * Custom service which synchronizes user profile with server
 * <p/>
 * Created by nine3_marks on 5/9/2015.
 */
public class ProfileSyncService extends IntentService {

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     */
    public ProfileSyncService() {
        super("ProfileSyncService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        // Get saved access token
        String accessToken = null;
        VizoUser user = LocalStorage.getInstance().loadSavedAuthUserInfo();
        if (user != null) {
            accessToken = user.access_token;
        }

        if (accessToken == null) {
            // If user has not logged in, we should not start sync process
            return;
        }

        // Check if preference settings is synchronized with server
        // and synchronize if needed
        if (!LocalStorage.getInstance().getFlagValue(LocalStorage.SYNCHRONIZED_PREFS)) {
            synchronizePreferenceSettings();
        }

        synchronizeGlancedStatus();

    }

    /**
     * Perform synchronization for User Settings
     */
    private void synchronizePreferenceSettings() {

        VizoSettings settings = new VizoSettings();
        settings.leftSettings = LocalStorage.getInstance().loadSavedGlancePreferences(LocalStorage.LEFT_PANEL_SETTINGS);
        settings.rightSettings = LocalStorage.getInstance().loadSavedGlancePreferences(LocalStorage.RIGHT_PANEL_SETTINGS);

        // Generate JSON value to be saved on server
        try {
            String json = settings.getJSONString();
            GeneralResponse response = APIClient.getInstance(getBaseContext())
                    .getApiService().postUserProfile("preference_settings", json);
            if (response.result) {
                LocalStorage.getInstance().setFlagValue(LocalStorage.SYNCHRONIZED_PREFS, true);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Perform synchronization for glanced items
     */
    private void synchronizeGlancedStatus() {

        // Get all glanced items which are saved locally
        ArrayList<VizoGlance> glancedItems = VizoGlance.getPendingGlancedItems(getBaseContext());

        if (glancedItems.size() == 0) {

            // Fetch remote glanced items
            try {
                GetGlancedItemsResponse response = APIClient.getInstance(getBaseContext())
                        .getApiService().getGlancedItems();
                for (VizoGlance glance : response) {
                    glance.addAsGlanced(getBaseContext());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        } else {

            for (VizoGlance glance : glancedItems) {
                int favorite = glance.isFavorite(getBaseContext()) ? 1 : 0;
                if (glance.syncState.equals(DatabaseConstants.UPLOAD_REQUIRED)) {

                    Log.e("ProfileSyncService", "upload glance: " + glance.glanceId);

                    // Upload glanced item to server if needed
                    try {
                        boolean result = true;
                        GeneralResponse response = APIClient.getInstance(getBaseContext())
                                .getApiService().postGlancedItem(glance.glanceId, favorite);
                        if (!response.result) {
                            result = false;
                        }
                        if (result) {
                            glance.syncState = null;
                            glance.addAsGlanced(getBaseContext());
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                } else if (glance.syncState.equals(DatabaseConstants.DELETE_REQUIRED)) {

                    Log.e("ProfileSyncService", "delete glance: " + glance.glanceId);

                    // The removed glanced item should be deleted on the server
                    try {
                        boolean result = true;
                        GeneralResponse response = APIClient.getInstance(getBaseContext())
                                .getApiService().removeGlancedItems(glance.glanceId);
                        if (!response.result) {
                            result = false;
                        }
                        if (result) {
                            glance.deleteFromGlanced(getBaseContext());
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}
