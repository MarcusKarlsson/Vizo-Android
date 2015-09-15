package com.vizo.news.fragments.onboarding;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;

import com.vizo.news.R;
import com.vizo.news.activities.OnboardingActivity;
import com.vizo.news.activities.base.BaseActivity;
import com.vizo.news.api.APIClient;
import com.vizo.news.api.domain.CategoriesWithGlancesResponse;
import com.vizo.news.domain.VizoUser;
import com.vizo.news.fragments.base.BaseFragment;
import com.vizo.news.utils.LocalStorage;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Created by MarksUser on 4/2/2015.
 *
 * @author nine3_marks
 */
public class SplashFragment extends BaseFragment {

    private View view;
    private ImageView iconLogo;

    private static final float ROTATE_FROM = 0.0f;
    private static final float ROTATE_TO = -10.0f * 360.0f;

    public static SplashFragment newInstance() {
        return new SplashFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        this.view = inflater.inflate(R.layout.fragment_splash, container, false);

        return this.view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        this.iconLogo = (ImageView)view.findViewById(R.id.iv_logo_image);

        RotateAnimation r;
        r = new RotateAnimation(ROTATE_FROM, ROTATE_TO, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        r.setDuration((long) 9*1500);
        r.setRepeatCount(0);
        iconLogo.startAnimation(r);

        APIClient.getInstance(getActivity()).getApiService().getCategoriesWithGlances(mCallback);
    }

    private Callback<CategoriesWithGlancesResponse> mCallback = new Callback<CategoriesWithGlancesResponse>() {
        @Override
        public void success(CategoriesWithGlancesResponse categoryResponses, Response response) {
            categoryResponses.saveToDatabase(getActivity());
            navigateNextPage();
        }

        @Override
        public void failure(RetrofitError error) {
            Log.d(error.getMessage(), "failed to load glances");
            navigateNextPage();
        }
    };

    private void navigateNextPage() {
        OnboardingActivity delegate = (OnboardingActivity) getActivity();
        delegate.setCustomAnimation(BaseActivity.CUSTOM_ANIMATIONS.SLIDE_FROM_RIGHT);
        VizoUser authUser = LocalStorage.getInstance().loadSavedAuthUserInfo();
        if (authUser != null && authUser.access_token != null) {
            delegate.checkAndNavigate(false);
        } else {
            delegate.showFragment(R.id.fl_fragment_container, WelcomeFragment.newInstance());
        }
    }
}
