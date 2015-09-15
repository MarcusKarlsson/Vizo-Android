package com.vizo.news.activities;

import android.animation.Animator;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.vizo.news.R;
import com.vizo.news.activities.base.BaseActivity;
import com.vizo.news.api.APIClient;
import com.vizo.news.api.domain.CategoriesWithGlancesResponse;
import com.vizo.news.domain.VizoCategory;
import com.vizo.news.domain.VizoGlance;
import com.vizo.news.service.ProfileSyncService;
import com.vizo.news.ui.VizoVerticalCategoryPager;
import com.vizo.news.utils.BitmapUtils;
import com.vizo.news.utils.CommonUtils;
import com.vizo.news.utils.Constants;
import com.vizo.news.utils.LocalStorage;
import com.amplitude.api.Amplitude;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Created by nine3 on 3/26/15.
 *
 * @author nine3_marks
 */
public class HomeScreenActivity extends BaseActivity implements View.OnTouchListener {

    private Timer transitionTimer;
    private int ticker = 0;

    // Members which hold view elements
    private VizoVerticalCategoryPager topPager, leftPager, rightPager;
    private LinearLayout previewContainer;
    private TextView previewIndicator;
    private ImageView refreshButton;

    public static HomeScreenActivity instance;

    private FrameLayout frameProgress;

    /**
     * The static constant which holds the request code for Home Settings Page
     */
    private static final int HOME_SETTINGS = 120;

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.activity_home_screen);

        /**
         * Check if need to show walkthrough
         * always display walkthrough if Show Tips settings is on
         */
        if (!localStorage.getFlagValue(LocalStorage.WALKED_THROUGH_HOME)) {
            startWalkthrough();
        }

        // Map view elements to class members
        previewContainer = (LinearLayout) findViewById(R.id.rl_preview_container);
        previewIndicator = (TextView) findViewById(R.id.tv_preview_indicator);
        refreshButton = (ImageView) findViewById(R.id.iv_refresh_icon);

        frameProgress = (FrameLayout) findViewById(R.id.frameProgress);
        frameProgress.setVisibility(View.VISIBLE);
        /**
         * Display home settings when long press logo button
         * on home screen
         */
        findViewById(R.id.iv_vizo_button).setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                Intent intent = new Intent(HomeScreenActivity.this, HomeSettingsActivity.class);
                startActivityForResult(intent, HOME_SETTINGS);
                return true;
            }
        });

        /**
         * We should show State Of The Day when tap on logo button
         * on Home Screen
         */
        findViewById(R.id.iv_vizo_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (VizoGlance.getStateOfDayGlances(HomeScreenActivity.this).size() != 0) {
                    Intent intent = new Intent(HomeScreenActivity.this, StateOfDayActivity.class);
                    startActivity(intent);
                } else {
                    CommonUtils.getInstance().showMessage("There is no glance in State Of The Day");
                }
            }
        });

        // Refresh app when user taps bolt icon
        refreshButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progress.show();
                APIClient.getInstance(HomeScreenActivity.this)
                        .getApiService().getCategoriesWithGlances(refreshCallback);
            }
        });

        findViewById(R.id.rl_top_pager_container).setOnTouchListener(this);
        findViewById(R.id.rl_left_pager_container).setOnTouchListener(this);
        findViewById(R.id.rl_right_pager_container).setOnTouchListener(this);

        // load glances to home screen

         loadGlancesToView();
    }

    private Callback<CategoriesWithGlancesResponse> refreshCallback = new Callback<CategoriesWithGlancesResponse>() {
        @Override
        public void success(CategoriesWithGlancesResponse categoryResponses, Response response) {
            progress.dismiss();
            categoryResponses.saveToDatabase(getApplicationContext());
            loadGlancesToView();
            refreshButton.setVisibility(View.GONE);
        }

        @Override
        public void failure(RetrofitError error) {
            progress.dismiss();
        }
    };

    @Override
    protected void onStart() {

        super.onStart();

    }

    private void funcResume()
    {
        new AsyncTask<Void, Void, String>() {

            @Override
            protected void onPreExecute() {
            }

            @Override
            protected String doInBackground(Void... params) {

                Amplitude.getInstance().startSession();

                // Start glance transition animation
                startTransitionTask();

                // Start synchronization service
                Intent intent = new Intent(HomeScreenActivity.this, ProfileSyncService.class);
                startService(intent);

                return null;
            }

            @Override
            protected void onPostExecute(String result) {
                showRefreshIcon();
            }
        }.execute(null, null, null);
    }

    @Override
    protected void onResume() {
        super.onResume();
        instance = this;

        funcResume();
    }

    @Override
    protected void onPause() {
        super.onPause();

        Amplitude.getInstance().endSession();

        // stop glance transition animation
        transitionTimer.cancel();

        instance = null;
    }

    /**
     * Display custom walkthough screens which indicate
     * hold vizo and hold glance
     */
    private void startWalkthrough() {
        // Walkthrough View which indicates Hold Vizo
        final LinearLayout holdVizoWalk = (LinearLayout) findViewById(R.id.ll_walkthrough_hold_vizo);
        // Walkthrough View which indicates Hold Glance
        final LinearLayout swipeUpDownWalk = (LinearLayout) findViewById(R.id.ll_walkthrough_swipe_updown);

        holdVizoWalk.setAlpha(1);
        holdVizoWalk.setVisibility(View.VISIBLE);
        holdVizoWalk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                holdVizoWalk.animate().setDuration(500).alpha(0).setListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {
                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        holdVizoWalk.setVisibility(View.GONE);
                        swipeUpDownWalk.animate().setDuration(500).alpha(1);
                        swipeUpDownWalk.setVisibility(View.VISIBLE);
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

        swipeUpDownWalk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                swipeUpDownWalk.animate().setDuration(500).alpha(0).setListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {
                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        swipeUpDownWalk.setVisibility(View.GONE);
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {
                    }

                    @Override
                    public void onAnimationRepeat(Animator animation) {
                    }
                });
                localStorage.setFlagValue(LocalStorage.WALKED_THROUGH_HOME, true);
            }
        });
    }

    /**
     * Display walkthrough screen which shows tips that refresh app with
     * the lightning bolt icon
     */
    private void showRefreshWalkthrough() {

        // Walkthrough view for refresh
        final LinearLayout wtRefresh = (LinearLayout) findViewById(R.id.ll_walkthrough_tap_bolt);

        wtRefresh.setAlpha(1);
        wtRefresh.setVisibility(View.VISIBLE);
        wtRefresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                wtRefresh.animate().setDuration(500).alpha(0).setListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {
                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        wtRefresh.setVisibility(View.GONE);
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {
                    }

                    @Override
                    public void onAnimationRepeat(Animator animation) {
                    }
                });
                localStorage.setFlagValue(LocalStorage.WALKED_THROUGH_REFRESH, true);
            }
        });
    }

    /**
     * Fetch glances from preferences (for offline feature)
     * and display on home screen
     */
    ArrayList<String> topCategories;
    ArrayList<String> leftCategories;
    ArrayList<String> rightCategories;
    ArrayList<String> otherCategories;
    private void loadGlancesToView() {

        final Handler handler = new Handler();
        Runnable runable = new Runnable() {

            @Override
            public void run() {
                try{
                    ArrayList<VizoCategory> allCategories = VizoCategory.getAllCategories(HomeScreenActivity.this);
                    topCategories = new ArrayList<>();
                    leftCategories = localStorage.loadSavedGlancePreferences(LocalStorage.LEFT_PANEL_SETTINGS);
                    rightCategories = localStorage.loadSavedGlancePreferences(LocalStorage.RIGHT_PANEL_SETTINGS);
                    otherCategories = new ArrayList<>();

                    // classify panel items
                    for (int i = 0; i < allCategories.size(); i++) {
                        VizoCategory category = allCategories.get(i);
                        if (category.isTopNews()) {
                            topCategories.add(category.categoryId);
                        } else {
                            ArrayList<VizoGlance> glances = category.getCategoryGlances(getApplicationContext());
                            if (glances.size() > 0
                                    && !leftCategories.contains(category.categoryId)
                                    && !rightCategories.contains(category.categoryId)) {
                                otherCategories.add(category.categoryId);
                            }
                        }
                    }

                    // Determine panel categories
                    if (leftCategories.size() == 0 && rightCategories.size() == 0) {

                        // Setup left panel with default categories
                        // : Sports, U.S, Celebrities, World
                        leftCategories.add("9");
                        leftCategories.add("13");
                        leftCategories.add("21");
                        leftCategories.add("14");
                        //leftCategories.add("22");

                        // Setup right panel with default categories
                        // : Technology, Business, Politics, Arts & Culture
                        rightCategories.add("11");
                        rightCategories.add("5");
                        rightCategories.add("7");
                        rightCategories.add("10");
                        rightCategories.add("23");

                    } else {
                        if (leftCategories.size() == 0) {
                            if (otherCategories.size() == 0) {
                                leftCategories = rightCategories;
                            } else {
                                leftCategories = otherCategories;
                            }
                        } else if (rightCategories.size() == 0) {
                            if (otherCategories.size() == 0) {
                                rightCategories = leftCategories;
                            } else {
                                rightCategories = otherCategories;
                            }
                        } else {
                            if (leftCategories.size() + rightCategories.size() < 3) {
                                for (String categoryId : otherCategories) {
                                    if (leftCategories.size() > rightCategories.size()) {
                                        rightCategories.add(categoryId);
                                    } else {
                                        leftCategories.add(categoryId);
                                    }
                                }
                            }
                        }
                    }

                    topPager = (VizoVerticalCategoryPager) findViewById(R.id.vp_glance_pager_top);
                    leftPager = (VizoVerticalCategoryPager) findViewById(R.id.vp_glance_pager_left);
                    rightPager = (VizoVerticalCategoryPager) findViewById(R.id.vp_glance_pager_right);

                    topPager.setCategories(topCategories);
                    leftPager.setCategories(leftCategories);
                    rightPager.setCategories(rightCategories);
                    //do your code here
                    topPager.loadGlances(getSupportFragmentManager());
                    leftPager.loadGlances(getSupportFragmentManager());
                    rightPager.loadGlances(getSupportFragmentManager());

                    frameProgress.setVisibility(View.GONE);
                    //also call the same runnable
                    //  handler.postDelayed(this, 1000);
                }
                catch (Exception e) {
                    // TODO: handle exception
                }
                finally{
                    //also call the same runnable
                    //   handler.postDelayed(this, 1000);
                }
            }
        };
        handler.postDelayed(runable, 100);
        return;

        /*new AsyncTask<Void, Void, String>() {

            @Override
            protected void onPreExecute() {
            }

            @Override
            protected String doInBackground(Void... params) {
                ArrayList<VizoCategory> allCategories = VizoCategory.getAllCategories(HomeScreenActivity.this);
                topCategories = new ArrayList<>();
                leftCategories = localStorage.loadSavedGlancePreferences(LocalStorage.LEFT_PANEL_SETTINGS);
                rightCategories = localStorage.loadSavedGlancePreferences(LocalStorage.RIGHT_PANEL_SETTINGS);
                otherCategories = new ArrayList<>();

                // classify panel items
                for (int i = 0; i < allCategories.size(); i++) {
                    VizoCategory category = allCategories.get(i);
                    if (category.isTopNews()) {
                        topCategories.add(category.categoryId);
                    } else {
                        ArrayList<VizoGlance> glances = category.getCategoryGlances(getApplicationContext());
                        if (glances.size() > 0
                                && !leftCategories.contains(category.categoryId)
                                && !rightCategories.contains(category.categoryId)) {
                            otherCategories.add(category.categoryId);
                        }
                    }
                }

                // Determine panel categories
                if (leftCategories.size() == 0 && rightCategories.size() == 0) {

                    // Setup left panel with default categories
                    // : Sports, U.S, Celebrities, World
                    leftCategories.add("9");
                    leftCategories.add("13");
                    leftCategories.add("21");
                    leftCategories.add("14");
                    //leftCategories.add("22");

                    // Setup right panel with default categories
                    // : Technology, Business, Politics, Arts & Culture
                    rightCategories.add("11");
                    rightCategories.add("5");
                    rightCategories.add("7");
                    rightCategories.add("10");
                    rightCategories.add("23");

                } else {
                    if (leftCategories.size() == 0) {
                        if (otherCategories.size() == 0) {
                            leftCategories = rightCategories;
                        } else {
                            leftCategories = otherCategories;
                        }
                    } else if (rightCategories.size() == 0) {
                        if (otherCategories.size() == 0) {
                            rightCategories = leftCategories;
                        } else {
                            rightCategories = otherCategories;
                        }
                    } else {
                        if (leftCategories.size() + rightCategories.size() < 3) {
                            for (String categoryId : otherCategories) {
                                if (leftCategories.size() > rightCategories.size()) {
                                    rightCategories.add(categoryId);
                                } else {
                                    leftCategories.add(categoryId);
                                }
                            }
                        }
                    }
                }

                topPager = (VizoVerticalCategoryPager) findViewById(R.id.vp_glance_pager_top);
                leftPager = (VizoVerticalCategoryPager) findViewById(R.id.vp_glance_pager_left);
                rightPager = (VizoVerticalCategoryPager) findViewById(R.id.vp_glance_pager_right);


                topPager.setCategories(topCategories);
                leftPager.setCategories(leftCategories);
                rightPager.setCategories(rightCategories);
                return null;
            }

            @Override
            protected void onPostExecute(String result) {
                loadGlances();
            }
        }.execute(null, null, null);*/
    }

    private void loadGlances()
    {
        final Handler handler = new Handler();
        Runnable runable = new Runnable() {

            @Override
            public void run() {
                try{
                    //do your code here
                    topPager.loadGlances(getSupportFragmentManager());
                    leftPager.loadGlances(getSupportFragmentManager());
                    rightPager.loadGlances(getSupportFragmentManager());

                    frameProgress.setVisibility(View.GONE);
                    //also call the same runnable
                  //  handler.postDelayed(this, 1000);
                }
                catch (Exception e) {
                    // TODO: handle exception
                }
                finally{
                    //also call the same runnable
                 //   handler.postDelayed(this, 1000);
                }
            }
        };
        handler.postDelayed(runable, 100);

        /*runOnUiThread(new Runnable() {
            @Override
            public void run() {
                topPager.loadGlances(getSupportFragmentManager());
                leftPager.loadGlances(getSupportFragmentManager());
                rightPager.loadGlances(getSupportFragmentManager());

                frameProgress.setVisibility(View.GONE);
            }
        });*/
    }

    /**
     * Change glances with transition animation
     */
    private void updateGlances() {
        if (ticker == 3) {
            topPager.updateGlance();
        } else if (ticker == 6) {
            leftPager.updateGlance();
        } else if (ticker >= 9) {
            rightPager.updateGlance();
            ticker = 0;
        }
    }

    private void startTransitionTask() {
        ticker = ticker - ticker % 3;
        if (transitionTimer != null) {
            transitionTimer.cancel();
        }
        transitionTimer = new Timer();
        transitionTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ticker++;
                        updateGlances();
                    }
                });
            }
        }, 0, 1000);
    }

    private void showPreviewContainer(boolean visible) {
        if (visible) {
            setPreviewContent(touchGlance);
            previewContainer.animate().setDuration(300).alpha(1).setListener(null);
            previewContainer.setVisibility(View.VISIBLE);
        } else {
            previewContainer.animate().setDuration(300).alpha(0).setListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    previewContainer.setVisibility(View.GONE);
                }

                @Override
                public void onAnimationCancel(Animator animation) {
                }

                @Override
                public void onAnimationRepeat(Animator animation) {
                }
            });
        }
    }

    private void startFullGlance(VizoGlance glance) {
        transitionTimer.cancel();
        Intent intent = new Intent(HomeScreenActivity.this, GlanceFullActivity.class);
        intent.putExtra("glance", glance);
        intent.putExtra("show_mode", Constants.SHOW_ALL);
        startActivity(intent);
    }

    /**
     * Generate blur overlay over activity
     */
    private void populateBlurOverlay() {
        RelativeLayout view = (RelativeLayout) findViewById(R.id.rl_parent_layout);
        view.setDrawingCacheEnabled(true);
        view.buildDrawingCache();
        Bitmap bm = view.getDrawingCache();

        ImageView blurOverlay = (ImageView) findViewById(R.id.iv_blur_overlay);
        blurOverlay.setImageBitmap(BitmapUtils.getBlur(bm, 20));
        blurOverlay.setVisibility(View.VISIBLE);
    }

    /**
     * populate glance preview content
     *
     * @param glance glance info
     */
    private void setPreviewContent(VizoGlance glance) {
        TextView tvPreview = (TextView) findViewById(R.id.tv_preview_description);
        tvPreview.setText(glance.getShortDescription());

        TextView tvRelease = (TextView) findViewById(R.id.tv_release_label);
        tvRelease.setText(R.string.release_to_cancel);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Check if user close Home Settings
        if (requestCode == HOME_SETTINGS) {
            if (resultCode == RESULT_OK && data.getBooleanExtra("need_update", false)) {
                loadGlancesToView();
            }
        }
    }

    /**
     * Show refresh icon if needed
     */
    public void showRefreshIcon() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (localStorage.getFlagValue(LocalStorage.NEW_GLANCES_POSTED)) {
                    refreshButton.setVisibility(View.VISIBLE);

                    if (!localStorage.getFlagValue(LocalStorage.WALKED_THROUGH_REFRESH)) {
                        showRefreshWalkthrough();
                    }
                }
            }
        });
    }

    private int glanceTouchCounter = 0;
    private Timer previewTimer;
    private VizoGlance touchGlance;

    private float pressedX;
    private float pressedY;
    private final int MAX_CLICK_DISTANCE = 10;
    private final int MAX_CLICK_DURATION = 200;
    private long startClickTime;

    @Override
    public boolean onTouch(View v, MotionEvent event) {

        // Determine the glance which handling touch event
        VizoGlance glance = null;
        if (v.getId() == R.id.rl_top_pager_container) {
            glance = topPager.getCurrentGlance();
            if (previewContainer.getVisibility() != View.VISIBLE) {
                topPager.dispatchTouchEvent(event);
            }
        } else if (v.getId() == R.id.rl_left_pager_container) {
            glance = leftPager.getCurrentGlance();
            if (previewContainer.getVisibility() != View.VISIBLE) {
                leftPager.dispatchTouchEvent(event);
            }
        } else if (v.getId() == R.id.rl_right_pager_container) {
            glance = rightPager.getCurrentGlance();
            if (previewContainer.getVisibility() != View.VISIBLE) {
                rightPager.dispatchTouchEvent(event);
            }
        }

        // If glance is null, return
        if (glance == null)
            return true;

        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            pressedX = event.getX();
            pressedY = event.getY();
            startClickTime = Calendar.getInstance().getTimeInMillis();

            transitionTimer.cancel();
            touchGlance = glance;
            startPreviewCounter();
        } else if (event.getAction() == MotionEvent.ACTION_UP) {
            long clickDuration = Calendar.getInstance().getTimeInMillis() - startClickTime;
            if (distance(pressedX, pressedY, event.getX(), event.getY()) < MAX_CLICK_DISTANCE
                    && clickDuration < MAX_CLICK_DURATION) {
                previewTimer.cancel();
                startFullGlance(glance);
            } else {
                previewTimer.cancel();
                showPreviewContainer(false);
                startTransitionTask();
            }
        } else if (event.getAction() == MotionEvent.ACTION_CANCEL
                || event.getAction() == MotionEvent.ACTION_OUTSIDE) {
            previewTimer.cancel();
            showPreviewContainer(false);
            startTransitionTask();
        } else if (event.getAction() == MotionEvent.ACTION_MOVE) {
            if (distance(pressedX, pressedY, event.getX(), event.getY()) > MAX_CLICK_DISTANCE
                    && glanceTouchCounter < 5) {
                previewTimer.cancel();
            }
        }
        return true;
    }

    private float distance(float x1, float y1, float x2, float y2) {
        float dx = x1 - x2;
        float dy = y1 - y2;
        float distanceInPx = (float) Math.sqrt(dx * dx + dy * dy);
        return pxToDp(distanceInPx);
    }

    private float pxToDp(float px) {
        return px / getResources().getDisplayMetrics().density;
    }

    /**
     * Start touch counter
     */
    private void startPreviewCounter() {

        glanceTouchCounter = 0;

        // Cancel previous timer task
        if (previewTimer != null) {
            previewTimer.cancel();
        }
        previewTimer = new Timer();
        previewTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        glanceTouchCounter++;
                        if (glanceTouchCounter == 5) {
                            showPreviewContainer(true);
                        }
                        if (glanceTouchCounter > 5) {
                            String strIndicator = getString(R.string.continue_hold);
                            int cursorPosition = glanceTouchCounter - 5;
                            if (cursorPosition > strIndicator.length()) {
                                previewTimer.cancel();
                                showPreviewContainer(false);
                                startFullGlance(touchGlance);
                            } else {
                                Spannable word = new SpannableString(strIndicator.substring(0, cursorPosition));
                                word.setSpan(
                                        new ForegroundColorSpan(getResources().getColor(R.color.vizo_yellow)),
                                        0, word.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                                previewIndicator.setText(word);

                                Spannable wordTwo = new SpannableString(
                                        strIndicator.substring(cursorPosition, strIndicator.length()));
                                wordTwo.setSpan(
                                        new ForegroundColorSpan(getResources().getColor(R.color.white)),
                                        0, wordTwo.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                                previewIndicator.append(wordTwo);
                            }
                        }
                    }
                });
            }
        }, 0, 200);
    }

    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
    }
}
