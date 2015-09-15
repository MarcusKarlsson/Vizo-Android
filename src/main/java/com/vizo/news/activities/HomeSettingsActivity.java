package com.vizo.news.activities;

import android.content.Intent;
import android.os.Bundle;

import com.vizo.news.R;
import com.vizo.news.activities.base.BaseActivity;
import com.vizo.news.domain.VizoGlance;
import com.vizo.news.fragments.logo_settings.LogoSettingsFragment;
import com.vizo.news.utils.Constants;

/**
 * Created by andrewstukey on 1/19/15.
 * <p/>
 * Modified by nine3_marks on 3/27/15
 */
public class HomeSettingsActivity extends BaseActivity {

    /**
     * Member variable which reflects flag status whether to update home screen or not
     */
    public boolean needHomeScreenUpdate = false;

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.activity_home_logo_settings);

        // Replace logo settings fragment as the root fragment
        showFragment(R.id.fl_fragment_container, LogoSettingsFragment.newInstance(), false, true);
        setCustomAnimation(CUSTOM_ANIMATIONS.SLIDE_FROM_RIGHT);
    }

    /**
     * Navigate to full glance page with selected glance
     *
     * @param glance Selected glance object
     */
    public void navigateToFullGlance(VizoGlance glance) {
        Intent intent = new Intent(HomeSettingsActivity.this, GlanceFullActivity.class);
        intent.putExtra("glance", glance);
        intent.putExtra("show_mode", Constants.SHOW_GLANCED);
        startActivity(intent);
    }

    /**
     * Close home settings with result code
     */
    public void closeWithResult() {
        Intent returnIntent = new Intent();
        returnIntent.putExtra("need_update", needHomeScreenUpdate);
        setResult(RESULT_OK, returnIntent);
        finish();
    }

}