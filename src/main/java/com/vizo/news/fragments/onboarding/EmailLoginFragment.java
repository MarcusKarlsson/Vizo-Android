package com.vizo.news.fragments.onboarding;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.vizo.news.R;
import com.vizo.news.activities.OnboardingActivity;
import com.vizo.news.api.APIClient;
import com.vizo.news.api.domain.GetGlancedItemsResponse;
import com.vizo.news.api.domain.PostLoginResponse;
import com.vizo.news.api.domain.VizoSettings;
import com.vizo.news.domain.VizoGlance;
import com.vizo.news.fragments.base.BaseFragment;
import com.vizo.news.utils.CommonUtils;
import com.vizo.news.utils.LocalStorage;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

import com.amplitude.api.Amplitude;

/**
 * Created by MarksUser on 4/6/2015.
 *
 * @author nine3_marks
 */
public class EmailLoginFragment extends BaseFragment implements View.OnClickListener {

    private View view;
    private OnboardingActivity delegate;

    public static EmailLoginFragment newInstance() {
        return new EmailLoginFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        this.view = inflater.inflate(R.layout.fragment_email_login, container, false);
        return this.view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        delegate = (OnboardingActivity) getActivity();

        // Initialize view elements and class members
        initViewAndClassMembers();
    }

    private void initViewAndClassMembers() {
        View vi = this.view;

        // Map view elements to event handlers
        vi.findViewById(R.id.tv_back_button).setOnClickListener(this);
        vi.findViewById(R.id.tv_signup_button).setOnClickListener(this);
        vi.findViewById(R.id.tv_login_button).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.tv_back_button) {
            baseActivity.onBackPressed();
        } else if (v.getId() == R.id.tv_signup_button) {
            delegate.showFragment(R.id.fl_fragment_container, EmailSignUpFragment.newInstance(), true);
            Amplitude.getInstance().logEvent("EMAIL_SIGNUP");
        } else if (v.getId() == R.id.tv_login_button) {
            validateAndPostLogin();
            Amplitude.getInstance().logEvent("EMAIL_LOGIN");
        }
    }

    /**
     * Check user input and post Email Login Request
     * if valid
     */
    private void validateAndPostLogin() {
        EditText etEmail = (EditText) this.view.findViewById(R.id.et_email_address);
        EditText etPassword = (EditText) this.view.findViewById(R.id.et_password);
        String emailAddress = etEmail.getText().toString();
        String password = etPassword.getText().toString();
        CommonUtils helperUtils = CommonUtils.getInstance();
        if (emailAddress.length() == 0 || password.length() == 0) {
            helperUtils.showMessage("Please input all the fields");
            return;
        }
        if (!helperUtils.validateEmail(emailAddress)) {
            helperUtils.showMessage("Invalid Email, please try to input again");
            return;
        }

        progress.show();
        APIClient.getInstance(getActivity()).getApiService().postLoginRequest(emailAddress, password, mCallback);
    }

    private Callback<PostLoginResponse> mCallback = new Callback<PostLoginResponse>() {
        @Override
        public void success(PostLoginResponse postLoginResponse, Response response) {
            if (postLoginResponse.result) {
                CommonUtils.getInstance().showMessage("Login Success");
                LocalStorage.getInstance().saveAuthUserInfo(postLoginResponse.user);

                VizoSettings settings = postLoginResponse.getUserSettings();
                if (settings != null) {
                    if (settings.leftSettings != null) {
                        localStorage.saveGlancePreferenceSetting(LocalStorage.LEFT_PANEL_SETTINGS, settings.leftSettings);
                    }
                    if (settings.rightSettings != null) {
                        localStorage.saveGlancePreferenceSetting(LocalStorage.RIGHT_PANEL_SETTINGS, settings.rightSettings);
                    }
                }

                // Fetch glanced items
                APIClient.getInstance(getActivity()).getApiService().getGlancedItems(glancedCallback);
            } else {
                progress.dismiss();
                CommonUtils.getInstance().showMessage(postLoginResponse.errorMessage);
            }
        }

        @Override
        public void failure(RetrofitError error) {
            progress.dismiss();
            CommonUtils.getInstance().showMessage("Failed to connect, check your internet");
        }
    };

    private Callback<GetGlancedItemsResponse> glancedCallback = new Callback<GetGlancedItemsResponse>() {
        @Override
        public void success(GetGlancedItemsResponse vizoGlances, Response response) {
            progress.dismiss();
            for (VizoGlance glance : vizoGlances) {
                glance.addAsGlanced(getActivity());
            }

            delegate.checkAndNavigate(false);
        }

        @Override
        public void failure(RetrofitError error) {
            progress.dismiss();
            CommonUtils.getInstance().showMessage("Failed to fetch glanced items");
            LocalStorage.getInstance().saveAuthUserInfo(null);
        }
    };
}
