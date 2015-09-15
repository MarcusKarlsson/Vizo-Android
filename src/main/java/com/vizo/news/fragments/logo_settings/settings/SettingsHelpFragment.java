package com.vizo.news.fragments.logo_settings.settings;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.vizo.news.R;
import com.vizo.news.activities.HomeSettingsActivity;
import com.vizo.news.fragments.base.BaseFragment;

/**
 * Custom Fragment class for Help (settings)
 *
 * @author nine3_marks
 */
public class SettingsHelpFragment extends BaseFragment implements View.OnClickListener {

    private HomeSettingsActivity delegate;

    private View view;

    public static SettingsHelpFragment newInstance() {
        return new SettingsHelpFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        this.view = inflater.inflate(R.layout.fragment_settings_help, container, false);
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

        // Map view elements to event handlers
        vi.findViewById(R.id.iv_back_button).setOnClickListener(this);
        vi.findViewById(R.id.rl_faq_button).setOnClickListener(this);
        vi.findViewById(R.id.rl_support_button).setOnClickListener(this);
        vi.findViewById(R.id.rl_feedback_button).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {

        if (v.getId() == R.id.iv_back_button) {

            baseActivity.onBackPressed();

        } else if (v.getId() == R.id.rl_faq_button) {

            // Navigate to FAQ screen
            delegate.showFragment(R.id.fl_fragment_container, SettingsFAQFragment.newInstance(-1), true);

        } else if (v.getId() == R.id.rl_support_button) {

            // Open Email application and populate values
            constructEmail();

        } else if (v.getId() == R.id.rl_feedback_button) {

            // Open Email application and populate values
            constructEmail();
        }
    }

    private void constructEmail() {
        /* Create the Intent */
        final Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);

        /* Fill it with Data */
        emailIntent.setType("plain/text");
        emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL, new String[]{"help@vizonews.com"});
        emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Subject");
        emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, "Text");

        /* Send it off to the Activity-Chooser */
        delegate.startActivity(Intent.createChooser(emailIntent, "Send mail..."));
    }
}
