package com.vizo.news.fragments.logo_settings.settings;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.vizo.news.R;
import com.vizo.news.activities.HomeSettingsActivity;
import com.vizo.news.fragments.base.BaseFragment;

/**
 * Custom fragment class for About Settings Page
 * <p/>
 * Created by fbuibish on 4/17/15.
 */
public class SettingsPrivacyFragment extends BaseFragment implements View.OnClickListener {

    private HomeSettingsActivity delegate;

    private View view;

    public static SettingsPrivacyFragment newInstance() {
        return new SettingsPrivacyFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        this.view = inflater.inflate(R.layout.fragment_settings_about_privacy, container, false);
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
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.iv_back_button) {

            delegate.onBackPressed();

        }
    }
}
