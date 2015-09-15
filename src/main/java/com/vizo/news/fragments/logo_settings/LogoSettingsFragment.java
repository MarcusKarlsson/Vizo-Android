package com.vizo.news.fragments.logo_settings;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.vizo.news.R;
import com.vizo.news.activities.HomeSettingsActivity;
import com.vizo.news.activities.VizoUActivity;
import com.vizo.news.domain.VizoUser;
import com.vizo.news.fragments.base.BaseFragment;
import com.vizo.news.fragments.logo_settings.account.AccountSettingsFragment;
import com.vizo.news.fragments.logo_settings.editions.EditionsFragment;
import com.vizo.news.fragments.logo_settings.glanced.GlancedFragment;
import com.vizo.news.fragments.logo_settings.settings.GeneralSettingsFragment;

/**
 * Created by MarksUser on 3/31/2015.
 * <p/>
 * Custom Fragment class for Home Logo Settings
 */
public class LogoSettingsFragment extends BaseFragment implements View.OnClickListener {

    // Member variables which refer view elements
    private View view;
    private TextView accountButton;
    private TextView editionsButton;
    private TextView glancedButton;
    private TextView settingsButton;
    private Button vizoUButton;

    private HomeSettingsActivity delegate;
    private boolean isAlreadyInflated = false;

    public static LogoSettingsFragment newInstance() {
        return new LogoSettingsFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        this.view = inflater.inflate(R.layout.fragment_logo_settings, container, false);
        return this.view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        delegate = (HomeSettingsActivity) getActivity();

        // setup fragment
        initViewAndClassMembers();

        this.view.post(new Runnable() {
            @Override
            public void run() {
                // start button animation
                startButtonsAnimation();
            }
        });
    }

    /**
     * Initialize view elements and class members
     */
    private void initViewAndClassMembers() {
        // Map view elements to class members
        accountButton = (TextView) view.findViewById(R.id.tv_account_button);
        editionsButton = (TextView) view.findViewById(R.id.tv_editions_button);
        glancedButton = (TextView) view.findViewById(R.id.tv_glanced_button);
        settingsButton = (TextView) view.findViewById(R.id.tv_settings_button);
        vizoUButton = (Button) view.findViewById(R.id.btn_vizou_button);

        vizoUButton.setEnabled(false);
        vizoUButton.setVisibility(View.GONE);
        // Map view elements to event handlers
        accountButton.setOnClickListener(this);
        editionsButton.setOnClickListener(this);
        glancedButton.setOnClickListener(this);
        settingsButton.setOnClickListener(this);
        vizoUButton.setOnClickListener(this);
        view.findViewById(R.id.tv_done_button).setOnClickListener(this);

        // Setup account info
        VizoUser user = localStorage.loadSavedAuthUserInfo();
        if (user != null) {
            accountButton.setText(String.format("%s %s", user.first_name, user.last_name));
        } else {
            accountButton.setText(R.string.no_account);
        }
    }

    /**
     * Implement button animations on launch
     */
    private void startButtonsAnimation() {

        int offset = view.getWidth() / 3;

        if (!isAlreadyInflated) {
            accountButton.animate().translationYBy(-offset).alpha(1).setStartDelay(100);
            editionsButton.animate().translationXBy(-offset).alpha(1).setStartDelay(200);
            glancedButton.animate().translationYBy(offset).alpha(1).setStartDelay(300);
            settingsButton.animate().translationXBy(offset).alpha(1).setStartDelay(400);
            vizoUButton.animate().alpha(1).setStartDelay(500);

            isAlreadyInflated = true;
        } else {
            accountButton.setAlpha(1);
            editionsButton.setAlpha(1);
            settingsButton.setAlpha(1);
            glancedButton.setAlpha(1);
            vizoUButton.setAlpha(1);
            accountButton.setTranslationY(-offset);
            editionsButton.setTranslationX(-offset);
            settingsButton.setTranslationX(offset);
            glancedButton.setTranslationY(offset);
        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == accountButton.getId()) {
            // Navigate to Account Settings Page
            delegate.showFragment(R.id.fl_fragment_container, AccountSettingsFragment.newInstance(), true);

        } else if (v.getId() == editionsButton.getId()) {
            // Navigate to Editions Page
            delegate.showFragment(R.id.fl_fragment_container, EditionsFragment.newInstance(), true);

        } else if (v.getId() == glancedButton.getId()) {
            // Navigate to Glanced Page
            delegate.showFragment(R.id.fl_fragment_container, GlancedFragment.newInstance(), true);

        } else if (v.getId() == settingsButton.getId()) {
            // Navigate to General Settings page
            delegate.showFragment(R.id.fl_fragment_container, GeneralSettingsFragment.newInstance(), true);

        } else if (v.getId() == vizoUButton.getId()) {
            // Navigate to VizoU Activity Page
            Intent intent = new Intent(delegate, VizoUActivity.class);
            startActivity(intent);

        } else if (v.getId() == R.id.tv_done_button) {
            // When user taps "done" button from logo setting screen,
            // go back to home screen
            delegate.closeWithResult();
        }
    }
}