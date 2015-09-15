package com.vizo.news.fragments.onboarding;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.viewpagerindicator.CirclePageIndicator;

import com.vizo.news.R;
import com.vizo.news.activities.OnboardingActivity;
import com.vizo.news.fragments.base.BaseFragment;
import com.vizo.news.fragments.logo_settings.settings.PreferencesFragment;
import com.vizo.news.ui.VizoHorizontalPager;

/**
 * Custom Fragment class for Tutorials
 *
 * @author nine3_marks
 */
public class TutorialsFragment extends BaseFragment {

    private View view;

    public static TutorialsFragment newInstance() {
        return new TutorialsFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        this.view = inflater.inflate(R.layout.fragment_tutorials, container, false);
        return this.view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // Initialize view elements and class members
        initViewAndClassMembers();
    }

    private void initViewAndClassMembers() {
        View vi = this.view;
        VizoHorizontalPager tutorialsPager = (VizoHorizontalPager) vi.findViewById(R.id.vp_tutorials_pager);
        tutorialsPager.setAdapter(new TutorialsPagerAdapter(getChildFragmentManager()));
        CirclePageIndicator pageIndicator = (CirclePageIndicator) vi.findViewById(R.id.page_indicator);
        pageIndicator.setViewPager(tutorialsPager);

        tutorialsPager.setOnSwipeOutListener(new VizoHorizontalPager.OnSwipeOutListener() {
            @Override
            public void onSwipeOutAtStart() {
                if (baseActivity.getSupportFragmentManager().getBackStackEntryCount() != 0) {
                    baseActivity.onBackPressed();
                }
            }

            @Override
            public void onSwipeOutAtEnd() {
                OnboardingActivity delegate = (OnboardingActivity) getActivity();
                delegate.showFragment(R.id.fl_fragment_container, PreferencesFragment.newInstance(), true);
            }
        });
    }

    private static class TutorialsPagerAdapter extends FragmentPagerAdapter {

        private int tutorialResIds[];

        public TutorialsPagerAdapter(FragmentManager fm) {
            super(fm);

            tutorialResIds = new int[]{
                    R.layout.fragment_tutorial_1,
                    R.layout.fragment_tutorial_2,
                    R.layout.fragment_tutorial_3,
//                    R.layout.fragment_tutorial_4,
                    R.layout.fragment_tutorial_5};
        }

        @Override
        public Fragment getItem(int position) {
            return TutorialItemFragment.newInstance(tutorialResIds[position]);
        }

        @Override
        public int getCount() {
            return tutorialResIds.length;
        }
    }

    public static class TutorialItemFragment extends BaseFragment implements View.OnClickListener {

        private View view;
        private int resId;

        public static TutorialItemFragment newInstance(int resId) {
            TutorialItemFragment fragment = new TutorialItemFragment();
            Bundle bundle = new Bundle();
            bundle.putInt("res_id", resId);
            fragment.setArguments(bundle);
            return fragment;
        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            resId = getArguments().getInt("res_id");
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            this.view = inflater.inflate(resId, container, false);
            TextView nextButton = (TextView) this.view.findViewById(R.id.tv_next_button);
            if (nextButton != null)
                nextButton.setOnClickListener(this);
            return this.view;
        }

        @Override
        public void onClick(View v) {
            if (v.getId() == R.id.tv_next_button) {
                OnboardingActivity delegate = (OnboardingActivity) getActivity();
                delegate.showFragment(R.id.fl_fragment_container, PreferencesFragment.newInstance(), true);
            }
        }
    }
}
