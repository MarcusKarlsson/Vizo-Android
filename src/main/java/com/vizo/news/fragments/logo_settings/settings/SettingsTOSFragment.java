package com.vizo.news.fragments.logo_settings.settings;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.vizo.news.R;
import com.vizo.news.activities.HomeSettingsActivity;
import com.vizo.news.activities.base.BaseActivity;
import com.vizo.news.fragments.base.BaseFragment;
import com.vizo.news.fragments.onboarding.TutorialsFragment;

/**
 * Custom fragment class for Terms of Service Page
 * <p/>
 * Created by fbuibish on 4/17/15.
 */
public class SettingsTOSFragment extends BaseFragment implements View.OnClickListener {

    private BaseActivity delegate;

    private View view;
    private boolean fromOnboarding = false;

    public static SettingsTOSFragment newInstance(boolean fromOnboarding) {
        SettingsTOSFragment fragment = new SettingsTOSFragment();
        Bundle bundle = new Bundle();
        bundle.putBoolean("from_onbaording", fromOnboarding);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        fromOnboarding = getArguments().getBoolean("from_onbaording");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        this.view = inflater.inflate(R.layout.fragment_settings_about_tos, container, false);
        return this.view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        delegate = (BaseActivity) getActivity();

        initViewAndClassMembers();
    }

    /**
     * Initialize view elements and class members
     */
    private void initViewAndClassMembers() {

        // Map view elements to event handlers
        view.findViewById(R.id.iv_back_button).setOnClickListener(this);
        view.findViewById(R.id.tv_agree_button).setOnClickListener(this);
        view.findViewById(R.id.tv_decline_button).setOnClickListener(this);

        // Show buttons area if needed
        if (fromOnboarding) {
            view.findViewById(R.id.ll_bottom_container).setVisibility(View.VISIBLE);
        } else {
            view.findViewById(R.id.ll_bottom_container).setVisibility(View.GONE);
        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.iv_back_button) {

            delegate.onBackPressed();
        } else if (v.getId() == R.id.tv_agree_button) {

            // Continue onboarding stories
            delegate.showFragment(R.id.fl_fragment_container, TutorialsFragment.newInstance(), true);
        } else if (v.getId() == R.id.tv_decline_button) {

            // Otherwise, return back to Editions page
            delegate.onBackPressed();
        }
    }
}
