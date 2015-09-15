package com.vizo.news.fragments.logo_settings.settings;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.vizo.news.R;
import com.vizo.news.activities.HomeSettingsActivity;
import com.vizo.news.fragments.base.BaseFragment;
import com.amplitude.api.Amplitude;

/**
 * Custom Fragment class for About (settings)
 *
 * @author nine3_marks
 */
public class SettingsAboutFragment extends BaseFragment implements View.OnClickListener {

    private HomeSettingsActivity delegate;

    private View view;

    public static SettingsAboutFragment newInstance() {
        return new SettingsAboutFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        this.view = inflater.inflate(R.layout.fragment_settings_about, container, false);
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

        vi.findViewById(R.id.iv_back_button).setOnClickListener(this);
        vi.findViewById(R.id.rl_tos).setOnClickListener(this);
        vi.findViewById(R.id.rl_privacy).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.iv_back_button) {
            baseActivity.onBackPressed();
        } else if (v.getId() == R.id.rl_tos) {
            delegate.showFragment(R.id.fl_fragment_container, SettingsTOSFragment.newInstance(false), true);
            Amplitude.getInstance().logEvent("USER_READ_TOS");
        } else if (v.getId() == R.id.rl_privacy) {
            delegate.showFragment(R.id.fl_fragment_container, SettingsPrivacyFragment.newInstance(), true);
            Amplitude.getInstance().logEvent("USER_READ_PRIVACY_POLICY");
        }
    }
}
