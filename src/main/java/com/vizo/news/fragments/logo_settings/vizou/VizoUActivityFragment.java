package com.vizo.news.fragments.logo_settings.vizou;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;

import com.facebook.AccessToken;
import com.vizo.news.R;
import com.vizo.news.activities.VizoUActivity;
import com.vizo.news.api.APIVizoUClient;
import com.vizo.news.api.domain.LikeUserGlanceResponse;
import com.vizo.news.api.domain.VizoUGlancesResponse;
import com.vizo.news.domain.VizoCategory;
import com.vizo.news.domain.VizoUGlance;
import com.vizo.news.domain.VizoUser;
import com.vizo.news.fragments.base.BaseFragment;
import com.vizo.news.fragments.logo_settings.account.AccountSettingsFragment;
import com.vizo.news.ui.VizoDynamicImageView;
import com.vizo.news.ui.VizoTextView;
import com.vizo.news.utils.CommonUtils;

import org.json.JSONObject;

import java.util.ArrayList;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

import static com.vizo.news.R.id.iv_unlike_button;

/**
 * Custom fragment class for VizoU activity page
 * <p/>
 * Created by nine3_marks on 6/22/2015.
 */
public class VizoUActivityFragment extends BaseFragment implements View.OnClickListener {

    /**
     * Member which holds the instance of delegate activity
     */
    private VizoUActivity delegate;

    private View view;

    // The view that holds the glances displayed on the Activity page
    private ListView glancesList;
    private LinearLayout facebookBanner;
    private String language;

    private UserGlancesAdapter adapter;

    private ArrayList<VizoUGlance> userGlances;
    private VizoUser authUser;

    private boolean facebookConnected = true;
    private String facebookEmail;

    public static VizoUActivityFragment newInstance() {
        return new VizoUActivityFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        this.view = inflater.inflate(R.layout.fragment_vizou_activity, container, false);
        return this.view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        delegate = (VizoUActivity) getActivity();

        // setup fragment
        initViewAndClassMembers();
    }

    /**
     * Initialize view elements and class members
     */
    private void initViewAndClassMembers() {

        authUser = localStorage.loadSavedAuthUserInfo();

        // Map view elements to class members
        glancesList = (ListView) view.findViewById(R.id.lv_glances_list);
        facebookBanner = (LinearLayout) view.findViewById(R.id.ll_facebook_banner);

        // Map view elements to event handlers
        view.findViewById(R.id.iv_back_button).setOnClickListener(this);
        view.findViewById(R.id.iv_add_button).setOnClickListener(this);
        facebookBanner.setOnClickListener(this);
        glancesList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                VizoUGlance glance = userGlances.get(position);

                VizoTextView tvGlanceDescription = (VizoTextView)
                        view.findViewById(R.id.tv_glance_description);
                ImageView ivDeleteButton = (ImageView)
                        view.findViewById(R.id.iv_delete_button);
                LinearLayout voteArea = (LinearLayout) view.findViewById(R.id.ll_vote_area);
                RelativeLayout bottomContainer = (RelativeLayout)
                        view.findViewById(R.id.rl_bottom_container);

                if (bottomContainer.getVisibility() == View.VISIBLE) {
                    bottomContainer.setVisibility(View.GONE);
                    tvGlanceDescription.setText(glance.getShortDescription());
                } else {
                    bottomContainer.setVisibility(View.VISIBLE);
                    tvGlanceDescription.setText(glance.description);

                    String myEmail = "";
                    if (facebookConnected) {
                        myEmail = facebookEmail;
                    } else if (authUser != null) {
                        myEmail = authUser.email;
                    }

                    if (myEmail.equals(glance.poster_email)) {
                        ivDeleteButton.setVisibility(View.VISIBLE);
                        voteArea.setVisibility(View.GONE);
                    } else {
                        ivDeleteButton.setVisibility(View.GONE);
                        voteArea.setVisibility(View.VISIBLE);
                    }
                }
            }
        });

        // Get language before loading user glances
        language = localStorage.loadAppLanguage();
        System.out.println("language= " + language);

        // Load user glances
        progress.show();
        APIVizoUClient.getInstance(delegate).getApiService().getVizoUGlances(20, 0, 1, mCallback);

        // Check and show facebook banner if needed
        if (AccessToken.getCurrentAccessToken() == null) {
            facebookConnected = false;
            facebookBanner.setVisibility(View.VISIBLE);
        } else {
            facebookConnected = true;
            JSONObject jsonObject = localStorage.getSavedFacebookInfo();
            if (jsonObject != null) {
                facebookEmail = jsonObject.optString("email");
            }
        }
    }

    /**
     * callback closure that returns server response
     * sets glance information to instance variable userGlances
     */
    private Callback<VizoUGlancesResponse> mCallback = new Callback<VizoUGlancesResponse>() {
        @Override
        public void success(VizoUGlancesResponse vizoUGlancesResponse, Response response) {
            progress.dismiss();
            if (vizoUGlancesResponse.totalCount > 0) {
                userGlances = vizoUGlancesResponse.glances;
                adapter = new UserGlancesAdapter(userGlances);
                glancesList.setAdapter(adapter);
            }
        }

        @Override
        public void failure(RetrofitError error) {
            progress.dismiss();
            CommonUtils.getInstance().showMessage("Failed to load user glances");
        }
    };

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.iv_back_button) {

            // Navigate back to previous screen
            baseActivity.onBackPressed();
        } else if (v.getId() == R.id.iv_add_button) {

            // Navigate to Create Glance screen
            if (facebookConnected) {
                delegate.selectedCategory = null;
                delegate.userGlanceDescription = "";
                delegate.glanceImageFile = null;
                delegate.showFragment(R.id.fl_fragment_container,
                        CreateGlanceFragment.newInstance(), true);
            } else {
                CommonUtils.getInstance().showMessage("You should connect your facebook");
            }

        } else if (v.getId() == facebookBanner.getId()) {

            // Navigate to Account Settings Screen
            delegate.showFragment(R.id.fl_fragment_container,
                    AccountSettingsFragment.newInstance(), true);
        }
    }

    /**
     * Custom adapter class for user glances list view
     * Provides VizoU activity with glance content
     */
    private class UserGlancesAdapter extends BaseAdapter {

        private ArrayList<VizoUGlance> glances;

        public UserGlancesAdapter(ArrayList<VizoUGlance> glances) {
            this.glances = glances;
        }

        @Override
        public int getCount() {
            return glances.size();
        }

        @Override
        public VizoUGlance getItem(int position) {
            return glances.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {

            if (convertView == null) {
                convertView = LayoutInflater.from(delegate)
                        .inflate(R.layout.vizou_glance_listitem, parent, false);
            }

            final VizoUGlance glance = glances.get(position);

            // Show glance update time
            VizoTextView tvUpdateTime = (VizoTextView)
                    convertView.findViewById(R.id.tv_update_time);
            String updateTime = CommonUtils.getInstance().getVizoTimeString(glance.modified_date);
            VizoCategory category = VizoCategory.findCategoryById(glance.category_id, delegate);
            if (category != null) {
                updateTime += String.format("  |  %s", category.category_name);
            }
            tvUpdateTime.setText(updateTime);

            // Load glance image
            VizoDynamicImageView glanceImage = (VizoDynamicImageView)
                    convertView.findViewById(R.id.iv_glance_image);
            glanceImage.loadImage(glance.image_url, category.getPlaceholderImage(delegate, true));

            // Apply category color filter
            View categoryFilterView = convertView.findViewById(R.id.view_category_filter);
            if (category != null) {
                categoryFilterView.setBackgroundColor(category.getCategoryColor(delegate));
            }

            // Show poster image under glance image
            VizoDynamicImageView ivPosterImage = (VizoDynamicImageView)
                    convertView.findViewById(R.id.iv_poster_image);
            CommonUtils.getInstance()
                    .loadImage(ivPosterImage, glance.poster_avatar, R.drawable.icon_account);

            // Show poster name on the right of poster image
            VizoTextView tvPosterName = (VizoTextView)
                    convertView.findViewById(R.id.tv_poster_name);
            String posterName = "";
            if (glance.poster_name != null) {
                String[] names = glance.poster_name.split("\\s+");
                posterName = names[0];
                if (names.length > 1) {
                    posterName += " " + names[1].substring(0, 1);
                }
            }
            tvPosterName.setText(posterName);

            // Show glance description under poster image
            VizoTextView tvGlanceDescription = (VizoTextView)
                    convertView.findViewById(R.id.tv_glance_description);
            tvGlanceDescription.setText(glance.getShortDescription());

            // Set right justification for user submitted
            // content that is in Hebrew
            // RTL is only supported on android 11+
            if(language.equalsIgnoreCase("he")) {
                tvGlanceDescription.setTextDirection(View.TEXT_DIRECTION_RTL);
                tvGlanceDescription.setGravity(Gravity.RIGHT);
            }

            // Show favorite numbers
            VizoTextView tvFavorites = (VizoTextView)
                    convertView.findViewById(R.id.tv_favorite_counter);
            tvFavorites.setText(String.valueOf(glance.vote_count));

            ImageView ivDeleteButton = (ImageView)
                    convertView.findViewById(R.id.iv_delete_button);
            convertView.findViewById(R.id.rl_bottom_container).setVisibility(View.GONE);

            // Map event handler to delete button
            ivDeleteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    new AlertDialog.Builder(delegate)
                            .setTitle("")
                            .setMessage(R.string.Delete_glance_confirmation)
                            .setIcon(android.R.drawable.ic_dialog_alert)

                            // If the dialogue button to delete user glance is clicked
                            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {
                                    progress.show();
                                    actionItemIndex = position;
                                    APIVizoUClient.getInstance(delegate)
                                            .getApiService()
                                            .deleteUserGlance(glance.glanceId, deleteCallback);

                                   userGlances.remove(position);

                                    glancesList.destroyDrawingCache();
                                    glancesList.setVisibility(ListView.INVISIBLE);
                                    glancesList.setVisibility(ListView.VISIBLE);
                                }
                            })
                            // If the delete glance module is cancelled
                            .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();

                                }
                            }).show();
                }
            });

            ImageView ivLikeButton = (ImageView) convertView.findViewById(R.id.iv_like_button);
            ImageView ivUnlikeButton = (ImageView) convertView.findViewById(iv_unlike_button);

            ivLikeButton.setAlpha(1.0f);
            ivLikeButton.setEnabled(true);
            ivUnlikeButton.setAlpha(1.0f);
            ivUnlikeButton.setEnabled(true);
            if (localStorage.isLiked(glance.glanceId)) {
                ivLikeButton.setAlpha(0.4f);
                ivLikeButton.setEnabled(false);
            }
            if (localStorage.isDisliked(glance.glanceId)) {
                ivUnlikeButton.setAlpha(0.4f);
                ivUnlikeButton.setEnabled(false);
            }

            final View tapView = convertView;
            ivLikeButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    actionItemIndex = position;
                    like = true;
                    selectedView = tapView;
                    likeUserGlance(glance.glanceId, true);
                }
            });
            ivUnlikeButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    actionItemIndex = position;
                    like = false;
                    selectedView = tapView;
                    likeUserGlance(glance.glanceId, false);
                }
            });

            return convertView;
        }
    }

    private int actionItemIndex = -1;

    private Callback<Response> deleteCallback = new Callback<Response>() {
        @Override
        public void success(Response response, Response response2) {
            progress.dismiss();
            adapter.notifyDataSetChanged();

        }

        @Override
        public void failure(RetrofitError error) {
            progress.dismiss();
        }
    };

    private void likeUserGlance(String glanceId, boolean like) {
        int type = -1;
        if (like) {
            type = 1;
        }

        progress.show();
        APIVizoUClient.getInstance(delegate).getApiService()
                .likeUserGlance(glanceId, type, likeCallback);
    }

    private boolean like = false;
    private View selectedView = null;

    private Callback<LikeUserGlanceResponse> likeCallback = new Callback<LikeUserGlanceResponse>() {
        @Override
        public void success(LikeUserGlanceResponse likeUserGlanceResponse, Response response) {
            progress.dismiss();
            adapter.notifyDataSetChanged();
            VizoUGlance glance = userGlances.get(actionItemIndex);
            glance.vote_count = likeUserGlanceResponse.new_count;
            localStorage.saveGlanceAsLiked(glance.glanceId, like);
            adapter.notifyDataSetChanged();
//            ImageView ivLikeButton = (ImageView) selectedView.findViewById(R.id.iv_like_button);
//            ImageView ivUnlikeButton = (ImageView) selectedView.findViewById(iv_unlike_button);
//            VizoTextView likeCounter = (VizoTextView) selectedView.findViewById(R.id.tv_favorite_counter);
//            if (likeUserGlanceResponse == null || likeCounter == null) {
//                CommonUtils.getInstance().showMessage("Null");
//                return;
//            }
//            likeCounter.setText(likeUserGlanceResponse.new_count);
//
//            VizoUGlance glance = userGlances.get(actionItemIndex);
//            localStorage.saveGlanceAsLiked(glance.glanceId, like);
//            if (localStorage.isLiked(glance.glanceId)) {
//                ivLikeButton.setAlpha(0.4f);
//                ivLikeButton.setEnabled(false);
//                ivUnlikeButton.setAlpha(1.0f);
//                ivUnlikeButton.setEnabled(true);
//            } else {
//                ivLikeButton.setAlpha(1.0f);
//                ivLikeButton.setEnabled(true);
//                ivUnlikeButton.setAlpha(0.4f);
//                ivUnlikeButton.setEnabled(false);
//            }
        }

        @Override
        public void failure(RetrofitError error) {
            progress.dismiss();
            CommonUtils.getInstance().showMessage("Failed");
        }
    };

}
