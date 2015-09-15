package com.vizo.news.fragments.full_glance;

import android.animation.Animator;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.facebook.AccessToken;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.share.Sharer;
import com.facebook.share.model.ShareLinkContent;
import com.facebook.share.ShareApi;
import com.facebook.share.widget.ShareDialog;
import com.twitter.sdk.android.Twitter;
import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.TwitterSession;
import com.twitter.sdk.android.core.models.Tweet;
import com.vizo.news.R;
import com.vizo.news.activities.GlanceFullActivity;
import com.vizo.news.domain.VizoGlance;
import com.vizo.news.fragments.base.BaseFragment;
import com.vizo.news.utils.CommonUtils;

import org.json.JSONObject;

import java.util.Arrays;
import java.util.Set;


/**
 * Created by MarksUser on 4/18/2015.
 *
 * @author nine3_marks
 */
public class GlanceShareFragment extends BaseFragment implements View.OnClickListener {

    // member variables referring view elements
    private View view;
    private TextView doneButton;
    private LinearLayout shareArea;

    /**
     * member variable which holds delegate activity
     */
    private GlanceFullActivity delegate;

    public static GlanceShareFragment newInstance() {
        return new GlanceShareFragment();
    }

    public GlanceShareFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        this.view = inflater.inflate(R.layout.fragment_share_glance, container, false);
        this.view.findViewById(R.id.tv_done_button).setOnClickListener(this);
        return this.view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        delegate = (GlanceFullActivity) getActivity();

        // setup fragment
        initViewAndClassMembers();

        // animate buttons
        animateButtons();
    }

    /**
     * Initialize view elements and class members
     */
    private void initViewAndClassMembers() {

        // Map view elements to class members
        doneButton = (TextView) view.findViewById(R.id.tv_done_button);
        shareArea = (LinearLayout) view.findViewById(R.id.ll_share_area);

        // Map view elements to event handlers
        view.findViewById(R.id.ll_post_to_facebook).setOnClickListener(this);
        view.findViewById(R.id.ll_post_to_twitter).setOnClickListener(this);
        view.findViewById(R.id.ll_share_with_text).setOnClickListener(this);
        view.findViewById(R.id.ll_share_with_email).setOnClickListener(this);
    }

    /**
     * Perform animations of buttons
     */
    private void animateButtons() {
        view.post(new Runnable() {
            @Override
            public void run() {
                doneButton.setTranslationY(doneButton.getHeight());
                doneButton.animate().translationYBy(0 - doneButton.getHeight());

                // animate share buttons
                for (int i = 0; i < shareArea.getChildCount(); i++) {
                    LinearLayout itemView = (LinearLayout) shareArea.getChildAt(i);
                    itemView.setTranslationX(-200);
                    itemView.setAlpha(0);
                    itemView.animate().translationXBy(200).alpha(1).setStartDelay(100 * i).setDuration(400);
                }
            }
        });
    }

    /**
     * dismiss buttons with animation
     */
    private void dismissShareView() {

        int i;
        // animate share buttons
        for (i = 0; i < shareArea.getChildCount(); i++) {
            LinearLayout itemView = (LinearLayout) shareArea.getChildAt(i);
            itemView.animate().translationXBy(-200).alpha(0).setStartDelay(100 * i).setDuration(400);
        }

        doneButton.animate()
                .translationYBy(doneButton.getHeight())
                .setStartDelay(100 * i)
                .setListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {
                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        delegate.onBackPressed();
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {
                    }

                    @Override
                    public void onAnimationRepeat(Animator animation) {
                    }
                });
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.tv_done_button) {

            dismissShareView();

        } else if (v.getId() == R.id.ll_post_to_facebook) {

            // Post glance to facebook
            postToFacebook(delegate.getCurrentGlance());

        } else if (v.getId() == R.id.ll_post_to_twitter) {

            // Tweet glance
            if (Twitter.getSessionManager().getActiveSession() != null) {
                tweetGlance(delegate.getCurrentGlance());
            } else {
                delegate.authorizeTwitterWithCallback(new Callback<TwitterSession>() {
                    @Override
                    public void success(Result<TwitterSession> result) {
                        tweetGlance(delegate.getCurrentGlance());
                    }

                    @Override
                    public void failure(TwitterException e) {
                        CommonUtils.getInstance().showMessage("Failed, try again later");
                    }
                });
            }
        } else if (v.getId() == R.id.ll_share_with_text) {

            // Sharing via Text
            shareGlanceViaText(delegate.getCurrentGlance());

        } else if (v.getId() == R.id.ll_share_with_email) {

            // Share glance via Email
            shareGlanceViaEmail(delegate.getCurrentGlance());

        }
    }

    /**
     * Post vizo glance to facebook wall
     *
     * @param glance VizoGlance object
     */
    private void postToFacebook(VizoGlance glance) {
        String contentUrl = String.format("http://vizonews.com/#/glance/%s", glance.glanceId);

        //String description = glance.getFacebookDescription();
        String description = glance.getFacebookFullDescription();
        description += "\n" + contentUrl;
        description += "\n" + getResources().getString(R.string.Want_to_read_more);
        description += "\n" + "http://vizonews.com";

        ShareLinkContent content = new ShareLinkContent.Builder()
                .setContentTitle(String.format("%s Vizo News", glance.title))
                .setContentUrl(Uri.parse(contentUrl))
                .setContentDescription(description)
                .setImageUrl(Uri.parse(glance.image_url))
                .build();
      //  ShareDialog.show(this, content);

        Set<String> permissions = AccessToken.getCurrentAccessToken().getPermissions();
        LoginManager.getInstance().logInWithPublishPermissions(this, Arrays.asList("publish_actions"));
        ShareApi.share(content, facebookCallback);
    
    }

    private FacebookCallback<Sharer.Result> facebookCallback = new FacebookCallback<Sharer.Result>() {
        @Override
        public void onSuccess(Sharer.Result loginResult) {

        }

        @Override
        public void onCancel() {

        }

        @Override
        public void onError(FacebookException e) {
            CommonUtils.getInstance().showMessage("Failed to connect to Facebook");
        }
    };
    /**
     * Tweet glance info
     *
     * @param glance VizoGlance object
     */
    private void tweetGlance(VizoGlance glance) {
        progress.show();
        String content = glance.title;
        content += "\n" + String.format("http://vizonews.com/#/glance/%s", glance.glanceId);
        content += "\n" + getResources().getString(R.string.Get_the_app_today);
        content += "\n" + "http://vizonews.com";
        Twitter.getApiClient().getStatusesService().update(content,
                null, null, null, null, null, null, null, new Callback<Tweet>() {
                    @Override
                    public void success(Result<Tweet> result) {
                        progress.dismiss();
                        CommonUtils.getInstance().showMessage("You have posted the glance");
                    }

                    @Override
                    public void failure(TwitterException e) {
                        progress.dismiss();
                        CommonUtils.getInstance().showMessage("Failed, try again later");
                    }
                });
    }

    /**
     * Share glance object via Text
     *
     * @param glance VizoGlance object
     */
    private void shareGlanceViaText(VizoGlance glance) {

        // Sharing via Text
        Intent smsIntent = new Intent(Intent.ACTION_VIEW);
        smsIntent.setType("vnd.android-dir/mms-sms");
        smsIntent.addCategory(Intent.CATEGORY_DEFAULT);
        smsIntent.setData(Uri.parse("sms:"));

        String contentUrl = String.format("http://vizonews.com/#/glance/%s", glance.glanceId);

        String description = glance.getShortDescription();
        description += "\n" + contentUrl;
        description += "\n" + getResources().getString(R.string.Want_to_read_more);
        description += "\n" + "http://vizonews.com";

        smsIntent.putExtra("sms_body", description);
        startActivity(smsIntent);
    }

    /**
     * Share vizo glance via Email
     *
     * @param glance VizoGlance object
     */
    private void shareGlanceViaEmail(VizoGlance glance) {
        String contentUrl = String.format("http://vizonews.com/#/glance/%s", glance.glanceId);

        String description = glance.getShortDescription();
        description += "\n" + contentUrl;
        description += "\n" + getResources().getString(R.string.Want_to_read_more);
        description += "\n" + "http://vizonews.com";

        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_SUBJECT, String.format("%s Vizo News", glance.title));
        intent.putExtra(Intent.EXTRA_TEXT, description);
        intent.setData(Uri.parse("mailto:"));
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//        startActivity(intent);
        startActivity(Intent.createChooser(intent, "Send email using: "));
    }
}