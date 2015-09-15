package com.vizo.news.fragments.logo_settings.editions;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.Locale;

import com.vizo.news.R;
import com.vizo.news.VizoApplication;
import com.vizo.news.activities.HomeSettingsActivity;
import com.vizo.news.activities.OnboardingActivity;
import com.vizo.news.api.APIClient;
import com.vizo.news.api.domain.CategoriesWithGlancesResponse;
import com.vizo.news.fragments.base.BaseFragment;
import com.vizo.news.fragments.logo_settings.settings.SettingsTOSFragment;
import com.vizo.news.fragments.onboarding.TutorialsFragment;
import com.vizo.news.service.GCMRegisterService;
import com.vizo.news.ui.VizoTextView;
import com.vizo.news.utils.LocalStorage;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Custom fragment class for Editions Page
 * In this page, user can select language
 *
 * @author nine3_marks
 */
public class EditionsFragment extends BaseFragment implements View.OnClickListener {

    // View referencing class members
    private View view;
    private TextView tvBackButton, tvNextButton;
    private ImageView ivBackButton;
    private ListView lvEditions;

    private VizoTextView textEdition;
    private VizoTextView textChooseEdition;
    // editions list adapter
    private EditionsAdapter adapter;

    // Holds selected locale (edition), and locale item index on list
    private Locale selectedLocale;
    private int selectedIndex;

    // Array resources which refer the list of country codes and languages codes
    private String[] countryCodes;
    private String[] languageCodes;

    /**
     * Member which holds the instance of delegate activity
     */
    private HomeSettingsActivity delegate;

    public static EditionsFragment newInstance() {
        return new EditionsFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        this.view = inflater.inflate(R.layout.fragment_editions, container, false);
        return this.view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // initialize
        initViewAndClassMembers();
    }

    /**
     * Initialize view elements and class members
     */
    private void initViewAndClassMembers() {

        // Fetch country and language codes array resource from arrays.xml
        countryCodes = getActivity().getResources().getStringArray(R.array.country_codes);
        languageCodes = getActivity().getResources().getStringArray(R.array.language_codes);

        // Map view elements to class members
        lvEditions = (ListView) view.findViewById(R.id.lv_editions);
        tvBackButton = (TextView) view.findViewById(R.id.tv_back_button);
        tvNextButton = (TextView) view.findViewById(R.id.tv_next_button);
        ivBackButton = (ImageView) view.findViewById(R.id.iv_back_button);

        textEdition = (VizoTextView) view.findViewById(R.id.textEditions);
        textChooseEdition = (VizoTextView) view.findViewById(R.id.textChooseAnEdition);
        textEdition.setText(R.string.Editions);
        textChooseEdition.setText(R.string.Choose_an_Edition);
        // Check and change view configuration
        if (getActivity() instanceof OnboardingActivity) {

            // If this page is launched as Onboarding
            // hide back icon, show literal back, next buttons
            tvBackButton.setVisibility(View.VISIBLE);
            tvNextButton.setVisibility(View.VISIBLE);
            ivBackButton.setVisibility(View.GONE);

        } else if (getActivity() instanceof HomeSettingsActivity) {

            // If this page is launched from Home Settings
            // show back icon only, hide literal back, next buttons
            tvBackButton.setVisibility(View.GONE);
            tvNextButton.setVisibility(View.GONE);
            ivBackButton.setVisibility(View.VISIBLE);

        }

        // Map view elements to event handlers
        tvBackButton.setOnClickListener(this);
        tvNextButton.setOnClickListener(this);
        ivBackButton.setOnClickListener(this);

        // Populate Editions list content
        adapter = new EditionsAdapter(getActivity());
        lvEditions.setAdapter(adapter);

        // Load user saved language code from local storage
        // if not saved, set english as default
        String savedLanguage = localStorage.loadAppLanguage();
        selectedIndex = 0;
        for (int i = 0; i < languageCodes.length; i++) {
            if (languageCodes[i].equals(savedLanguage)) {
                selectedIndex = i;
                break;
            }
        }
        selectedLocale = adapter.getItem(selectedIndex);

        // Check the selected edition when user selects
        lvEditions.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                // Check if user selects different language than default
                if (selectedIndex != position) {

                    // Notify user selection by check icon
                    selectedIndex = position;
                    selectedLocale = adapter.getItem(position);
                    adapter.notifyDataSetChanged();

                    // Change application locale
                    updateGlances();
                }
            }
        });

    }

    @Override
    public void onClick(View v) {

        if (v.getId() == R.id.tv_back_button) {

            baseActivity.onBackPressed();

        } else if (v.getId() == R.id.iv_back_button) {

            // When this page is launched from Home Settings,
            // tapping back icon should change settings
            // while there is not a "next" button
            updateLocale();
            baseActivity.onBackPressed();

        } else if (v.getId() == R.id.tv_next_button) {

            // When user taps "next" button, change app locale and navigate to Tutorial pages
            updateLocale();
            if (localStorage.loadAppLanguage().equals("he")) {
                baseActivity.showFragment(R.id.fl_fragment_container,
                        SettingsTOSFragment.newInstance(true), true);
            } else {
                baseActivity.showFragment(R.id.fl_fragment_container,
                        TutorialsFragment.newInstance(), true);
            }
        }
    }

    /**
     * We need to update glances after user change application locale
     */
    private void updateGlances() {

        // Save user selection to local storage
        localStorage.saveAppLanguage(languageCodes[selectedIndex]);

        // Turn on Auto Translate option
        localStorage.setFlagValue(LocalStorage.AUTO_TRANSLATE, true);

        // Load localized FAQ resources
        VizoApplication app = (VizoApplication) getActivity().getApplication();
        app.loadFAQsFromAssets();

        // fetch glances with locale info
        progress.show();
        APIClient.getInstance(getActivity()).getApiService().getCategoriesWithGlances(mCallback);
        if (getActivity() instanceof HomeSettingsActivity) {
            delegate = (HomeSettingsActivity) getActivity();
            delegate.needHomeScreenUpdate = true;
        }

        updateLocale();
        initViewAndClassMembers();
    }

    /**
     * Change application locale by selection
     */
    private void updateLocale() {

        // Set user selected locale as the default application locale
        Locale.setDefault(selectedLocale);
        Configuration config = new Configuration();
        config.locale = selectedLocale;
        getActivity().getBaseContext().getResources().updateConfiguration(
                config, getActivity().getBaseContext().getResources().getDisplayMetrics());
    }

    private Callback<CategoriesWithGlancesResponse> mCallback = new Callback<CategoriesWithGlancesResponse>() {
        @Override
        public void success(CategoriesWithGlancesResponse categoryResponses, Response response) {
            progress.dismiss();
            categoryResponses.saveToDatabase(getActivity());

            // We should update lang info for push here
            Intent mIntent = new Intent(baseActivity, GCMRegisterService.class);
            baseActivity.startService(mIntent);
        }

        @Override
        public void failure(RetrofitError error) {
            progress.dismiss();
        }
    };

    /**
     * Custom adapter for Editions List
     */
    private class EditionsAdapter extends BaseAdapter {

        private LayoutInflater inflater;

        public EditionsAdapter(Context context) {
            this.inflater = LayoutInflater.from(context);
        }

        @Override
        public int getCount() {
            return countryCodes.length;
        }

        @Override
        public Locale getItem(int position) {
            return getLocaleAtPosition(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            if (convertView == null) {
                convertView = inflater.inflate(R.layout.editions_item, parent, false);
            }

            // Fetch country icon from drawable resources
            ImageView ivItemImage = (ImageView) convertView.findViewById(R.id.iv_item_image);
            try {
                String drawableName = String.format("edition_%s", countryCodes[position].toLowerCase());
                int resourceId = getResources().getIdentifier(
                        drawableName, "drawable", getActivity().getPackageName());
                ivItemImage.setImageResource(resourceId);
            } catch (Exception e) {
                e.printStackTrace();
            }

            // Put full country name obtained from locale instantiated with ISO country code
            TextView tvItemTitle = (TextView) convertView.findViewById(R.id.tv_item_title);
            Locale locale = getLocaleAtPosition(position);
            tvItemTitle.setText(locale.getDisplayCountry());

            // Show/hide check icon in order to notify user selection
            ImageView ivCheck = (ImageView) convertView.findViewById(R.id.iv_check_icon);
            int visibility = position == selectedIndex ? View.VISIBLE : View.GONE;
            ivCheck.setVisibility(visibility);

            return convertView;
        }

        /**
         * Get actual locale by user selection (position)
         *
         * @param position selection position on the list
         * @return actual locale object
         */
        private Locale getLocaleAtPosition(int position) {
            return new Locale(languageCodes[position], countryCodes[position]);
        }
    }

}
