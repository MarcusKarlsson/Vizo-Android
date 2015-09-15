package com.vizo.news.activities;

import android.animation.Animator;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Locale;

import com.vizo.news.R;
import com.vizo.news.activities.base.BaseActivity;
import com.vizo.news.api.APIVizoUClient;
import com.vizo.news.api.domain.LikeUserGlanceResponse;
import com.vizo.news.database.DatabaseConstants;
import com.vizo.news.domain.VizoCategory;
import com.vizo.news.domain.VizoGlance;
import com.vizo.news.domain.VizoUGlance;
import com.vizo.news.fragments.full_glance.ArticleSourcesFragment;
import com.vizo.news.fragments.full_glance.GalleryFragment;
import com.vizo.news.fragments.full_glance.GlanceFullFragment;
import com.vizo.news.fragments.full_glance.GlanceShareFragment;
import com.vizo.news.service.ProfileSyncService;
import com.vizo.news.ui.VizoFavoriteButton;
import com.vizo.news.ui.VizoHorizontalPager;
import com.vizo.news.utils.CommonUtils;
import com.vizo.news.utils.Constants;
import com.vizo.news.utils.LocalStorage;
import com.amplitude.api.Amplitude;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Created by andrewstukey on 1/23/15.
 * <p/>
 * Modified by nine3_marks on 3/31/15
 */
public class GlanceFullActivity extends BaseActivity implements View.OnClickListener, TextToSpeech.OnInitListener {

    private VizoGlance glance;

    // Class members which refer view elements
    private VizoHorizontalPager glancePager;
    private LinearLayout noGlanceIndicator;
    private VizoFavoriteButton favoriteButton;
    public LinearLayout layoutButtonArea;

    private FullGlanceAdapter adapter;
    private VizoCategory currentCategory;
    private ArrayList<VizoGlance> categoryGlances;

    private float buttonAreaPosY = 0;

    /**
     * Full Glance show mode
     */
    private int showMode;


    /**
     * Member which holds the instance of TextToSpeech Engine
     */
    private TextToSpeech tts;

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.activity_glance_full);

        // Map view elements to class members
        noGlanceIndicator = (LinearLayout) findViewById(R.id.ll_no_glance_indicator);
        favoriteButton = (VizoFavoriteButton) findViewById(R.id.ch_favorite_button);
        layoutButtonArea = (LinearLayout) findViewById(R.id.ll_button_area);

        /**
         * Check if need to show walkthrough
         * always display walkthrough if Show Tips settings is on
         */
        if (!localStorage.getFlagValue(LocalStorage.WALKED_THROUGH_FULL)) {
            startWalkthrough();
        }

        // Get glance selected glance object from Intent
        glance = getIntent().getParcelableExtra("glance");

        // Get show mode
        showMode = getIntent().getIntExtra("show_mode", Constants.SHOW_ALL);
        currentCategory = VizoCategory.findCategoryById(glance.category_id, this);
        if (showMode == Constants.SHOW_ALL) {
            categoryGlances = currentCategory.getCategoryGlances(this);
        } else if (showMode == Constants.SHOW_GLANCED) {
            categoryGlances = currentCategory.getGlancedItemsInCategory(this);
        } else if (showMode == Constants.SHOW_STATE_OF_DAY) {
            categoryGlances = VizoGlance.getStateOfDayGlances(this);
        }

        glancePager = (VizoHorizontalPager) findViewById(R.id.vp_glance_pager);
        adapter = new FullGlanceAdapter(getSupportFragmentManager());
        glancePager.setAdapter(adapter);
        glancePager.setSwipeOutDistance(320);
        glancePager.setOnSwipeOutListener(swipeOutListener);

        int currentIndex = 0;
        for (int i = 0; i < categoryGlances.size(); i++) {
            if (categoryGlances.get(i).glanceId.equals(glance.glanceId)) {
                currentIndex = i;
                break;
            }
        }

        glancePager.setCurrentItem(currentIndex);
        ((GlanceFullFragment) adapter.getItem(currentIndex)).showFull();
        favoriteButton.setChecked(categoryGlances.get(currentIndex).isFavorite(this));

        // Map event handlers to view elements
        findViewById(R.id.iv_home_button).setOnClickListener(this);
        findViewById(R.id.iv_share_button).setOnClickListener(this);
        favoriteButton.setOnClickListener(favoriteToggleListener);
        findViewById(R.id.iv_source_button).setOnClickListener(this);
        findViewById(R.id.iv_votedown_button).setOnClickListener(this);
        findViewById(R.id.iv_voteup_button).setOnClickListener(this);

        findViewById(R.id.tv_read_previous_button).setOnClickListener(this);
        findViewById(R.id.tv_change_category_button).setOnClickListener(this);
        glancePager.setOnPageChangeListener(pageChangeListener);

        // Initialize bar button
        if(glance.category_id.compareTo(VizoCategory.VIZO_VIZOU) == 0)
        {
            intializeView(true);
        }else{
            intializeView(false);
        }
        // Initialize instance of TextToSpeech engine
        tts = new TextToSpeech(this, this);
    }

    /**
     * Display custom walkthough screens
     */
    private void startWalkthrough() {
        // Walkthrough View for SWIPE DOWN
        final LinearLayout wtSwipeDown = (LinearLayout) findViewById(R.id.ll_walkthrough_swipe_down);
        // Walkthrough View for SWIPE LEFT OR RIGHT
        final LinearLayout wtSwipeHor = (LinearLayout) findViewById(R.id.ll_walkthrough_swipe_hor);

        wtSwipeDown.setAlpha(1);
        wtSwipeDown.setVisibility(View.VISIBLE);
        wtSwipeDown.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                wtSwipeDown.animate().setDuration(300).alpha(0).setListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {
                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        wtSwipeDown.setVisibility(View.GONE);
                        wtSwipeHor.setVisibility(View.VISIBLE);
                        wtSwipeHor.animate().setDuration(300).alpha(1);
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {
                    }

                    @Override
                    public void onAnimationRepeat(Animator animation) {
                    }
                });
            }
        });

        wtSwipeHor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                localStorage.setFlagValue(LocalStorage.WALKED_THROUGH_FULL, true);
                wtSwipeHor.animate().setDuration(300).alpha(0).setListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {
                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        wtSwipeHor.setVisibility(View.GONE);
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {
                    }

                    @Override
                    public void onAnimationRepeat(Animator animation) {
                    }
                });
            }
        });
    }

    /**
     * Custom handler which handles swipe out event of horizontal glance pager
     */
    private VizoHorizontalPager.OnSwipeOutListener swipeOutListener = new VizoHorizontalPager.OnSwipeOutListener() {
        @Override
        public void onSwipeOutAtStart() {
            GlanceFullActivity.this.finish();
        }

        @Override
        public void onSwipeOutAtEnd() {
            if (showMode != Constants.SHOW_STATE_OF_DAY) {
                showNoGlanceIndicator(true);
            }
        }
    };

    /**
     * listener object which handle page change event of glance pager
     */
    private ViewPager.OnPageChangeListener pageChangeListener = new ViewPager.OnPageChangeListener() {
        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        }

        @Override
        public void onPageSelected(int position) {
            glance = categoryGlances.get(position);
            favoriteButton.setChecked(glance.isFavorite(GlanceFullActivity.this));

            GlanceFullFragment fragment = (GlanceFullFragment) adapter.getItem(position);
            fragment.refresh();

            if (tts != null && tts.isSpeaking()) {
                tts.stop();
            }

            refreshVoteButton();
        }

        @Override
        public void onPageScrollStateChanged(int state) {
        }
    };

    /**
     * Custom handler which handles check change event of favorite button
     */
    private View.OnClickListener favoriteToggleListener = new View.OnClickListener() {

        /**
         * Flag variable which hold the status whether favorite button is animating or not
         */
        private boolean isAnimating = false;

        @Override
        public void onClick(View v) {

            // If favorite button is rotating, we should avoid duplicate animation
            if (isAnimating) {
                return;
            }

            favoriteButton.setChecked(!favoriteButton.isChecked());
            favoriteButton.animate().rotationBy(720).scaleX(1.5f).scaleY(1.5f)
                    .setInterpolator(new OvershootInterpolator(2.0f))
                    .setListener(new Animator.AnimatorListener() {
                        @Override
                        public void onAnimationStart(Animator animation) {
                            isAnimating = true;
                        }

                        @Override
                        public void onAnimationEnd(Animator animation) {
                            favoriteButton.setScaleX(1.0f);
                            favoriteButton.setScaleY(1.0f);
                            if (currentCategory != null && categoryGlances.size() > 0) {
                                int currentIndex = glancePager.getCurrentItem();
                                VizoGlance glance = categoryGlances.get(currentIndex);
                                glance.isFavorite = favoriteButton.isChecked() ? 1 : 0;
                                glance.syncState = DatabaseConstants.UPLOAD_REQUIRED;
                                glance.addAsGlanced(GlanceFullActivity.this);

                                // Start sync process
                                Intent intent = new Intent(GlanceFullActivity.this, ProfileSyncService.class);
                                startService(intent);
                            }

                            isAnimating = false;
                        }

                        @Override
                        public void onAnimationCancel(Animator animation) {
                            isAnimating = false;
                        }

                        @Override
                        public void onAnimationRepeat(Animator animation) {
                        }
                    });
        }
    };

    /**
     * Show no-glance view
     *
     * @param visible if true, show no-glance view
     */
    private void showNoGlanceIndicator(boolean visible) {

        // Category Name : e.g : Politics Glances
        TextView categoryName = (TextView) findViewById(R.id.tv_category_name);
        categoryName.setText(String.format("%s %s",
                this.currentCategory.getLocalizedName(this),
                getResources().getString(R.string.Glances)));

        // Category Indicator
        TextView categoryIndicator = (TextView) findViewById(R.id.tv_category_indicator);
        categoryIndicator.setText(
                String.format("%s %s %s", getResources().getString(R.string.all_of_the),
                        currentCategory.getLocalizedName(this),
                        getResources().getString(R.string.Glances)));

        Amplitude.getInstance().logEvent("READ_ALL_GLANCES");

        if (visible) {
            Animation fadeIn = new AlphaAnimation(0, 1);
            fadeIn.setInterpolator(new DecelerateInterpolator()); //add this
            fadeIn.setDuration(400);
            noGlanceIndicator.setAnimation(fadeIn);
            noGlanceIndicator.setVisibility(View.VISIBLE);
        } else {
            noGlanceIndicator.setVisibility(View.GONE);
        }
    }

    private void intializeView(boolean bVizoU){
        if(bVizoU){
            findViewById(R.id.iv_share_button).setVisibility(View.GONE);
            findViewById(R.id.iv_source_button).setVisibility(View.GONE);
            findViewById(R.id.iv_votedown_button).setVisibility(View.VISIBLE);
            findViewById(R.id.iv_voteup_button).setVisibility(View.VISIBLE);

            refreshVoteButton();

        }else{
            findViewById(R.id.iv_share_button).setVisibility(View.VISIBLE);
            findViewById(R.id.iv_source_button).setVisibility(View.VISIBLE);
            findViewById(R.id.iv_votedown_button).setVisibility(View.GONE);
            findViewById(R.id.iv_voteup_button).setVisibility(View.GONE);
        }
    }

    private void refreshVoteButton()
    {
        findViewById(R.id.iv_voteup_button).setAlpha(1.0f);
        findViewById(R.id.iv_voteup_button).setEnabled(true);
        findViewById(R.id.iv_votedown_button).setAlpha(1.0f);
        findViewById(R.id.iv_votedown_button).setEnabled(true);

        if (localStorage.isLiked(glance.glanceId)) {
            findViewById(R.id.iv_voteup_button).setAlpha(0.4f);
            findViewById(R.id.iv_voteup_button).setEnabled(false);
        }
        if (localStorage.isDisliked(glance.glanceId)) {
            findViewById(R.id.iv_votedown_button).setAlpha(0.4f);
            findViewById(R.id.iv_votedown_button).setEnabled(false);
        }
    }

    private void likeUserGlance(String glanceId, boolean like) {
        int type = -1;
        if (like) {
            type = 1;
        }

        progress.show();
        APIVizoUClient.getInstance(this).getApiService()
                .likeUserGlance(glanceId, type, likeCallback);
    }

    private boolean like = false;

    private Callback<LikeUserGlanceResponse> likeCallback = new Callback<LikeUserGlanceResponse>() {
        @Override
        public void success(LikeUserGlanceResponse likeUserGlanceResponse, Response response) {
            progress.dismiss();
            glance.vote_count = likeUserGlanceResponse.new_count;
            localStorage.saveGlanceAsLiked(glance.glanceId, like);

            refreshVoteButton();
        }

        @Override
        public void failure(RetrofitError error) {
            progress.dismiss();
            CommonUtils.getInstance().showMessage("Failed");
        }
    };

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.iv_home_button) {

            finish();

        } else if (v.getId() == R.id.iv_share_button) {

            buttonAreaPosY = layoutButtonArea.getY();
            layoutButtonArea.animate().y(buttonAreaPosY + layoutButtonArea.getHeight());
            showFragment(R.id.fragment_container, GlanceShareFragment.newInstance(), true);

        } else if (v.getId() == R.id.iv_source_button) {

            buttonAreaPosY = layoutButtonArea.getY();
            layoutButtonArea.animate().y(buttonAreaPosY + layoutButtonArea.getHeight());
            int currentIndex = glancePager.getCurrentItem();
            showFragment(R.id.fragment_container,
                    ArticleSourcesFragment.newInstance(categoryGlances.get(currentIndex)), true);

        } else if (v.getId() == R.id.tv_read_previous_button) {

            showNoGlanceIndicator(false);
            glancePager.setCurrentItem(0);

        } else if (v.getId() == R.id.tv_change_category_button) {

            changeCategory();

        } else if(v.getId() == R.id.iv_voteup_button){
            like = true;
            likeUserGlance(glance.glanceId, true);
        } else if (v.getId() == R.id.iv_votedown_button){
            like = false;
            likeUserGlance(glance.glanceId, false);
        }
    }

    @Override
    public void onBackPressed() {

        // When user tap back button
        if (activeFragment != null) {
            if (activeFragment instanceof GlanceShareFragment) {

                // If glance sharing view was showing
                onDismissSharingView();

            } else if (activeFragment instanceof ArticleSourcesFragment) {

                // If article sources view was showing
                onDismissSourcesView();

            } else if (activeFragment instanceof GalleryFragment) {

                // If gallery view was showing
                if (layoutButtonArea.getAlpha() < 1) {
                    layoutButtonArea.animate().alpha(1).setDuration(160);
                }
            }
        }
        super.onBackPressed();
    }

    /**
     * callback which should be called when user dismiss sharing view
     */
    public void onDismissSharingView() {
        layoutButtonArea.animate().y(buttonAreaPosY)
                .setInterpolator(new AccelerateDecelerateInterpolator());
    }

    /**
     * callback which should be called when user dismiss sources view
     */
    public void onDismissSourcesView() {
        layoutButtonArea.animate().y(buttonAreaPosY)
                .setInterpolator(new AccelerateDecelerateInterpolator());
    }

    /**
     * Change category of glance pager
     */
    private void changeCategory() {
        int currentIndex = -1;
        ArrayList<VizoCategory> allCategories = VizoCategory.getAllCategories(this);
        for (int i = 0; i < allCategories.size(); i++) {
            if (currentCategory.categoryId.equals(allCategories.get(i).categoryId)) {
                currentIndex = i;
                break;
            }
        }

        for (int i = 0; i < allCategories.size(); i++) {
            VizoCategory category = allCategories.get((i + currentIndex + 1) % allCategories.size());

            if (showMode == Constants.SHOW_ALL) {
                categoryGlances = category.getCategoryGlances(getApplicationContext());
            } else if (showMode == Constants.SHOW_GLANCED) {
                categoryGlances = category.getGlancedItemsInCategory(getApplicationContext());
            }

            if (categoryGlances != null && categoryGlances.size() > 0) {
                currentCategory = category;
                showNoGlanceIndicator(false);
                adapter = new FullGlanceAdapter(getSupportFragmentManager());
                glancePager.setAdapter(adapter);
                favoriteButton.setChecked(categoryGlances.get(0).isFavorite(getApplicationContext()));
                break;
            }
        }
    }

    /**
     * Save current glance image to phone storage
     *
     * @return true if success, otherwise false
     */
    public boolean saveImage() {
        return getCurrentFragment().saveGlanceImageToStorage();
    }

    /**
     * Get current visible glance image as Bitmap object
     *
     * @return Bitmap object for glance image
     */
    public Bitmap getCurrentGlanceImage() {
        return getCurrentFragment().getGlanceImage();
    }

    /**
     * Get current visible glance
     *
     * @return VizoGlance object
     */
    public VizoGlance getCurrentGlance() {
        return getCurrentFragment().getGlance();
    }

    /**
     * Get current fragment
     *
     * @return Current visible glance fragment
     */
    private GlanceFullFragment getCurrentFragment() {
        return (GlanceFullFragment) adapter.getItem(glancePager.getCurrentItem());
    }

    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            tts.setLanguage(Locale.getDefault());
        } else {
            tts = null;

            Log.d("GlanceFullActivity", "Failed to instantiate TextToSpeech Engine");
        }
    }

    /**
     * Get the instance of TextToSpeech Engine
     *
     * @return TextToSpeech Engine instance object
     */
    public TextToSpeech getTTSEngine() {
        return this.tts;
    }

    @Override
    protected void onDestroy() {

        // Shut down TextToSpeech engine if needed
        if (tts != null) {
            tts.stop();

            tts.shutdown();
        }

        super.onDestroy();
    }

    /**
     * Custom adapter class for Horizontal Glance Pager
     */
    private class FullGlanceAdapter extends FragmentStatePagerAdapter {

        private Hashtable<Integer, GlanceFullFragment> pagerFragments;

        public FullGlanceAdapter(FragmentManager fm) {
            super(fm);
            this.pagerFragments = new Hashtable<>();
        }

        @Override
        public Fragment getItem(int position) {
            GlanceFullFragment fragment = pagerFragments.get(position);
            if (fragment == null) {
                fragment = GlanceFullFragment.newInstance(categoryGlances.get(position));
                pagerFragments.put(position, fragment);
            }
            fragment.setNeedGlancedMark(showMode != Constants.SHOW_GLANCED);
            fragment.setOnClickListener(mItemClickListener);
            return fragment;
        }

        @Override
        public int getCount() {
            return categoryGlances.size();
        }
    }

    /**
     * Custom OnClickListener object which intercepts click event on full glance item
     */
    private View.OnClickListener mItemClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            showFragment(R.id.fragment_container, GalleryFragment.newInstance(
                    categoryGlances.get(glancePager.getCurrentItem())), true);
        }
    };
}
