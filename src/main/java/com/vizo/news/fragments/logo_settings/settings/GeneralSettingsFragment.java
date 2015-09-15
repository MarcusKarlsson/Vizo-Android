package com.vizo.news.fragments.logo_settings.settings;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Switch;

import com.vizo.news.R;
import com.vizo.news.activities.HomeSettingsActivity;
import com.vizo.news.fragments.base.BaseFragment;
import com.vizo.news.utils.LocalStorage;
import com.amplitude.api.Amplitude;


/**
 * Created by MarksUser on 4/1/2015.
 * <p/>
 * Custom Fragment class for General Settings
 */
public class GeneralSettingsFragment extends BaseFragment implements View.OnClickListener {

    // Holds activity instance
    private HomeSettingsActivity delegate;

    private View view;

    // Switch view elements
    private Switch tipsSwitch;
    private Switch translateSwitch;

    public static GeneralSettingsFragment newInstance() {
        return new GeneralSettingsFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        this.view = inflater.inflate(R.layout.fragment_general_settings, container, false);
        return this.view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        delegate = (HomeSettingsActivity) getActivity();

        initViewAndClassMembers();
    }

    /**
     * Initialize view elements and class members
     */
    private void initViewAndClassMembers() {
        View vi = this.view;

        // Map view elements to class members
        tipsSwitch = (Switch) vi.findViewById(R.id.sw_show_tips);
        translateSwitch = (Switch) vi.findViewById(R.id.sw_auto_translate);

        // Get status from Shared Preference and set values of Switches
        if (walkedAllPages()) {
            // We should turn off Show Tips after user has gone through all walkthroughs
            localStorage.setFlagValue(LocalStorage.IS_SHOW_TIPS, false);
        }
        tipsSwitch.setChecked(localStorage.getFlagValue(LocalStorage.IS_SHOW_TIPS));
        if (translateSwitch != null) {
            translateSwitch.setChecked(localStorage.getFlagValue(LocalStorage.AUTO_TRANSLATE));
        }

        // Map view elements to event handlers
        vi.findViewById(R.id.iv_back_button).setOnClickListener(this);
        vi.findViewById(R.id.rl_preferences_button).setOnClickListener(this);
        vi.findViewById(R.id.rl_about_vizo_button).setOnClickListener(this);
        vi.findViewById(R.id.rl_help_feedback_button).setOnClickListener(this);
        vi.findViewById(R.id.rl_rate_us_button).setOnClickListener(this);

        tipsSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                setShowTips(isChecked);
            }
        });
        if (translateSwitch != null) {
            translateSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    localStorage.setFlagValue(LocalStorage.AUTO_TRANSLATE, isChecked);
                }
            });
        }
    }

    @Override
    public void onClick(View v) {

        if (v.getId() == R.id.iv_back_button) {

            // navigate back to previous screen
            baseActivity.onBackPressed();

        } else if (v.getId() == R.id.rl_preferences_button) {

            // Navigate to Glance Preferences screen
            delegate.showFragment(R.id.fl_fragment_container, PreferencesFragment.newInstance(), true);

        } else if (v.getId() == R.id.rl_about_vizo_button) {

            // Navigate to About Vizo Screen
            delegate.showFragment(R.id.fl_fragment_container, SettingsAboutFragment.newInstance(), true);
            Amplitude.getInstance().logEvent("ABOUT_VIZO");

        } else if (v.getId() == R.id.rl_help_feedback_button) {

            // Navigate to Help & Feedback Screen
            delegate.showFragment(R.id.fl_fragment_container, SettingsHelpFragment.newInstance(), true);
            Amplitude.getInstance().logEvent("HELP");

        } else if (v.getId() == R.id.rl_rate_us_button) {

            // navigate or some work here for Rate Us
        }
    }

    /**
     * Check if show tips is available
     *
     * @return true if "show tips" is available, false otherwise
     */
    private boolean walkedAllPages() {
        return localStorage.getFlagValue(LocalStorage.WALKED_THROUGH_HOME)
                && localStorage.getFlagValue(LocalStorage.WALKED_THROUGH_FULL)
                && localStorage.getFlagValue(LocalStorage.WALKED_THROUGH_GLANCED)
                && localStorage.getFlagValue(LocalStorage.WALKED_THROUGH_PREFERENCES)
                && localStorage.getFlagValue(LocalStorage.WALKED_THROUGH_REFRESH);
    }

    /**
     * Set show tips
     *
     * @param showTips if true, set show tips on
     *                 if false, set show tips off
     */
    private void setShowTips(boolean showTips) {

        if (showTips) {
            localStorage.setFlagValue(LocalStorage.WALKED_THROUGH_HOME, false);
            localStorage.setFlagValue(LocalStorage.WALKED_THROUGH_FULL, false);
            localStorage.setFlagValue(LocalStorage.WALKED_THROUGH_GLANCED, false);
            localStorage.setFlagValue(LocalStorage.WALKED_THROUGH_PREFERENCES, false);
            localStorage.setFlagValue(LocalStorage.WALKED_THROUGH_REFRESH, false);
        }

        // Keep selected status for Show Tips
        localStorage.setFlagValue(LocalStorage.IS_SHOW_TIPS, showTips);
    }
}
