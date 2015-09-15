package com.vizo.news.activities.base;

import android.content.Intent;
import android.support.v4.app.FragmentActivity;

/**
 * Created by MarksUser on 3/20/2015.
 *
 * @author nine3_marks
 */
import java.util.Arrays;
import java.util.Set;
import java.util.UUID;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.TwitterSession;
import com.twitter.sdk.android.core.identity.TwitterAuthClient;
import com.vizo.news.R;
import com.vizo.news.fragments.base.BaseFragment;
import com.vizo.news.ui.VizoProgressDialog;
import com.vizo.news.utils.LocalStorage;
import com.amplitude.api.Amplitude;


public class BaseActivity extends FragmentActivity {

    protected BaseFragment rootFragment;
    protected BaseFragment activeFragment;
    protected LocalStorage localStorage;
    protected VizoProgressDialog progress;

    /**
     * Twitter auth client
     */
    private TwitterAuthClient mAuthClient;

    /**
     * Facebook callback manager
     */
    private CallbackManager callbackManager;

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);

        Amplitude.getInstance().initialize(this, "7de56aaf3639ad3c763d4a01f9275acb");

        localStorage = LocalStorage.getInstance();
        progress = new VizoProgressDialog(this);

        mAuthClient = new TwitterAuthClient();

        callbackManager = CallbackManager.Factory.create();
    }

    private CUSTOM_ANIMATIONS custom_animation = CUSTOM_ANIMATIONS.FADE_IN;

    public enum CUSTOM_ANIMATIONS {
        FADE_IN, SLIDE_FROM_LEFT, SLIDE_FROM_RIGHT, SLIDE_FROM_TOP, SLIDE_FROM_BOTTOM
    }

    public void showFragment(int contentFrame, BaseFragment fragment) {
        showFragment(contentFrame, fragment, false);
    }

    public void showFragment(int contentFrame, BaseFragment fragment,
                             boolean addToBackStack) {
        this.activeFragment = fragment;
        String tag = UUID.randomUUID().toString();
        FragmentTransaction transaction = getSupportFragmentManager()
                .beginTransaction();

        switch (custom_animation) {
            case FADE_IN:
                transaction.setCustomAnimations(R.anim.abc_fade_in,
                        R.anim.abc_fade_out, R.anim.abc_fade_in,
                        R.anim.abc_fade_out);
                break;
            case SLIDE_FROM_LEFT:
                transaction.setCustomAnimations(R.anim.abc_slide_in_left,
                        R.anim.abc_slide_out_right, R.anim.abc_fade_in,
                        R.anim.abc_fade_out);
                break;
            case SLIDE_FROM_RIGHT:
                transaction.setCustomAnimations(R.anim.abc_slide_in_right,
                        R.anim.abc_slide_out_left, R.anim.abc_fade_in,
                        R.anim.abc_fade_out);
                break;
            case SLIDE_FROM_BOTTOM:
                transaction.setCustomAnimations(R.anim.abc_slide_in_bottom,
                        R.anim.abc_slide_out_top, R.anim.abc_fade_in,
                        R.anim.abc_fade_out);
                break;
            case SLIDE_FROM_TOP:
                transaction.setCustomAnimations(R.anim.abc_slide_in_top,
                        R.anim.abc_slide_out_bottom, R.anim.abc_fade_in,
                        R.anim.abc_fade_out);
                break;
            default:
                transaction.setCustomAnimations(R.anim.abc_fade_in,
                        R.anim.abc_fade_out, R.anim.abc_fade_in,
                        R.anim.abc_fade_out);
                break;
        }

        transaction.replace(contentFrame, fragment, tag);
        if (addToBackStack) {
            transaction.addToBackStack(tag);
        }
        transaction.commit();
        getSupportFragmentManager().executePendingTransactions();
        if (getSupportFragmentManager().getBackStackEntryCount() == 0)
            rootFragment = fragment;
    }

    /**
     * Set custom_animation for fragment transaction *
     */
    public void setCustomAnimation(CUSTOM_ANIMATIONS custom_animation) {
        this.custom_animation = custom_animation;
    }

    /**
     * Enables the cleanup of all stack before adding this fragment. Can be
     * useful to make the Dash-board or other fragment the base fragment in
     * terms of order
     *
     * @param contentFrame   Layout Resource ID to replace fragment
     * @param fragment       BaseFragment object to replace
     * @param addToBackStack Determines to add to fragment back-stack
     * @param remove         If true, cleanup all stack
     */
    public void showFragment(int contentFrame, BaseFragment fragment,
                             boolean addToBackStack, boolean remove) {

        if (remove) {
            this.popToRoot();
        }
        showFragment(contentFrame, fragment, addToBackStack);
    }

    public void popToRoot() {
        int backStackCount = getSupportFragmentManager()
                .getBackStackEntryCount();
        for (int i = 0; i < backStackCount; i++) {
            // Get the back stack fragment id.
            int backStackId = getSupportFragmentManager()
                    .getBackStackEntryAt(i).getId();
            getSupportFragmentManager().popBackStack(backStackId,
                    FragmentManager.POP_BACK_STACK_INCLUSIVE);
        }
        activeFragment = null;
    }

    @Override
    public void onBackPressed() {
        if (activeFragment != null) {
            if (activeFragment.backButtonPressed()) {
                super.onBackPressed();
                int backStackCount = getSupportFragmentManager()
                        .getBackStackEntryCount();
                if (backStackCount > 0) {
                    String tag = getSupportFragmentManager()
                            .getBackStackEntryAt(backStackCount - 1).getName();
                    activeFragment = (BaseFragment) getSupportFragmentManager()
                            .findFragmentByTag(tag);
                } else {
                    if (activeFragment.equals(rootFragment))
                        activeFragment = null;
                    else
                        activeFragment = rootFragment;
                }
            }
        } else {
            super.onBackPressed();
        }
    }

    /**
     * * Hide soft keyboard ***
     */
    public void hideKeyboard() {
        InputMethodManager inputManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

        // check if no view has focus:
        View view = getCurrentFocus();
        if (view == null)
            return;

        inputManager.hideSoftInputFromWindow(view.getWindowToken(),
                InputMethodManager.HIDE_NOT_ALWAYS);
    }

    /**
     * Authorize twitter with callback
     *
     * @param callback Twitter session callback
     */
    public void authorizeTwitterWithCallback(Callback<TwitterSession> callback) {
        mAuthClient.authorize(this, callback);
    }

    /**
     * Connect to facebook with callback
     *
     * @param mCallback The callback
     */
    public void connectFacebookWithCallback(FacebookCallback<LoginResult> mCallback) {
        LoginManager.getInstance().registerCallback(callbackManager, mCallback);
//        LoginManager.getInstance().logInWithReadPermissions(this, Arrays.asList("public_profile", "user_friends", "email"));
        LoginManager.getInstance().logInWithPublishPermissions(this, Arrays.asList("publish_actions"));

        //Set<String> permissions = AccessToken.getCurrentAccessToken().getPermissions();
        //Set<String> declinedpermissions = AccessToken.getCurrentAccessToken().getDeclinedPermissions();

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mAuthClient.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }
}

