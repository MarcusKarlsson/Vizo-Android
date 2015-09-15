package com.vizo.news.fragments.onboarding;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginResult;
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

import org.json.JSONObject;

import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Created by MarksUser on 4/2/2015.
 *
 * @author nine3_marks
 */
public class WelcomeFragment extends BaseFragment implements View.OnClickListener {

    private OnboardingActivity delegate;

    // Members which holds the reference of view elements
    private View view;

    private retrofit.Callback<PostLoginResponse> mCallback = new retrofit.Callback<PostLoginResponse>() {
        @Override
        public void success(PostLoginResponse postLoginResponse, Response response) {
            if (postLoginResponse.result) {
                LocalStorage.getInstance().saveAuthUserInfo(postLoginResponse.user);

                VizoSettings settings = postLoginResponse.getUserSettings();
                if (settings != null) {
                    if (settings.leftSettings != null) {
                        localStorage.saveGlancePreferenceSetting(LocalStorage.LEFT_PANEL_SETTINGS,
                                settings.leftSettings);
                    }
                    if (settings.rightSettings != null) {
                        localStorage.saveGlancePreferenceSetting(LocalStorage.RIGHT_PANEL_SETTINGS,
                                settings.rightSettings);
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
            CommonUtils.getInstance().showMessage("Facebook Login Failed");
        }
    };

    private retrofit.Callback<GetGlancedItemsResponse> glancedCallback = new retrofit.Callback<GetGlancedItemsResponse>() {
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

    public static WelcomeFragment newInstance() {
        return new WelcomeFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        this.view = inflater.inflate(R.layout.fragment_welcome, container, false);
        return this.view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        delegate = (OnboardingActivity) getActivity();

        // Initialize view and class members
        initViewAndClassMembers();
    }

    private void initViewAndClassMembers() {
        View vi = this.view;

        // Map view elements to event handlers
        vi.findViewById(R.id.tv_skip_button).setOnClickListener(this);
        vi.findViewById(R.id.btn_facebook_login).setOnClickListener(this);
        vi.findViewById(R.id.btn_twitter_login).setOnClickListener(this);
        vi.findViewById(R.id.btn_login_email).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.tv_skip_button) {
            delegate.checkAndNavigate(true);
        } else if (v.getId() == R.id.btn_facebook_login) {
            delegate.connectFacebookWithCallback(loginCallback);
        } else if (v.getId() == R.id.btn_twitter_login) {
            delegate.performTwitterLogin();
        } else if (v.getId() == R.id.btn_login_email) {
            delegate.showFragment(R.id.fl_fragment_container, EmailLoginFragment.newInstance(), true);
        }
    }

    private FacebookCallback<LoginResult> loginCallback = new FacebookCallback<LoginResult>() {
        @Override
        public void onSuccess(LoginResult loginResult) {
            GraphRequest request = GraphRequest.newMeRequest(
                    loginResult.getAccessToken(),
                    new GraphRequest.GraphJSONObjectCallback() {

                        @Override
                        public void onCompleted(JSONObject jsonObject,
                                                GraphResponse graphResponse) {
                            String facebookId = jsonObject.optString("id");
                            String email = jsonObject.optString("email");
                            String firstName = jsonObject.optString("first_name");
                            String lastName = jsonObject.optString("last_name");
                            String avatarImage = "https://graph.facebook.com/" +
                                    facebookId + "/picture?type=large";
                            APIClient.getInstance(baseActivity)
                                    .getApiService()
                                    .postCreateUserRequest(
                                            email,
                                            facebookId,
                                            null,
                                            null,
                                            firstName,
                                            lastName,
                                            avatarImage,
                                            mCallback);
                            localStorage.saveFacebookInfo(jsonObject);
                        }
                    }
            );

            Bundle params = new Bundle();
            params.putString("fields", "id,name,email,first_name,last_name");
            request.setParameters(params);

            // Fetch user profile from Facebook
            progress.show();
            request.executeAsync();
        }

        @Override
        public void onCancel() {
        }

        @Override
        public void onError(FacebookException e) {
            CommonUtils.getInstance().showMessage("Facebook Login Failed");
        }
    };
}
