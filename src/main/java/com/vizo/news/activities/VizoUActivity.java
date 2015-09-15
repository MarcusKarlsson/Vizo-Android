package com.vizo.news.activities;

import android.os.Bundle;

import com.vizo.news.R;
import com.vizo.news.activities.base.BaseActivity;
import com.vizo.news.domain.VizoCategory;
import com.vizo.news.fragments.logo_settings.vizou.VizoUActivityFragment;

import java.io.File;

/**
 * Custom activity which manages VizoU module
 * <p/>
 * Created by nine3_marks on 6/25/2015.
 */
public class VizoUActivity extends BaseActivity {

    public String userGlanceDescription = "";
    public File glanceImageFile = null;
    public VizoCategory selectedCategory = null;

    private static final int MINIMUM_CHARACTERS = 350;
    private static final int MAXIMUM_CHARACTERS = 800;

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.activity_vizou);

        showFragment(R.id.fl_fragment_container, VizoUActivityFragment.newInstance());
    }

    /**
     * Generate characters number indicator string
     *
     * @param description Glance description content
     * @return Generated description character info
     */
    public String counterStringForDescription(String description) {
        String of = getResources().getString(R.string.of);
        String characterMinimum = getResources().getString(R.string.Character_minimum);
        String characterLeft = getResources().getString(R.string.Characters_remaining);
        if (description == null) {
            return String.format("0 %s 350 %s", of, characterMinimum);
        } else if (description.length() < MINIMUM_CHARACTERS) {
            String value = String.format("%d %s %d %s",
                    description.length(), of, MINIMUM_CHARACTERS, characterMinimum);
            return value;
        } else {
            String value = String.format("%d %s.",
                    MAXIMUM_CHARACTERS - description.length(), characterLeft);
            return value;
        }
    }
}
