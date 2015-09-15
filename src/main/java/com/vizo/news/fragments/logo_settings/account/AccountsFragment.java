package com.vizo.news.fragments.logo_settings.account;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;

import java.util.ArrayList;

import com.facebook.AccessToken;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginResult;
import com.twitter.sdk.android.Twitter;
import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.TwitterSession;
import com.vizo.news.R;
import com.vizo.news.activities.base.BaseActivity;
import com.vizo.news.fragments.base.BaseFragment;
import com.vizo.news.ui.VizoAccountItemView;
import com.vizo.news.utils.CommonUtils;
import com.vizo.news.utils.Constants;

import org.json.JSONObject;

/**
 * Custom fragment class for Accounts page
 *
 * @author nine3_marks
 */
public class AccountsFragment extends BaseFragment implements View.OnClickListener {

    // members which hold view elements
    private View view;
    private ListView accountsList;

    /**
     * member which holds the instance of delegate activity
     */
    private BaseActivity delegate;

    private ArrayList<Integer> accounts;

    public static AccountsFragment newInstance() {
        return new AccountsFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        this.view = inflater.inflate(R.layout.fragment_accounts, container, false);
        return this.view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        delegate = (BaseActivity) getActivity();

        // setup fragment
        initViewAndClassMembers();

        // Populate accounts list content
        populateAccountsList();
    }

    /**
     * initialize view elements and class members
     */
    private void initViewAndClassMembers() {

        // map view elements to class members
        accountsList = (ListView) view.findViewById(R.id.lv_accounts_list);

        // map view elements to event handlers
        view.findViewById(R.id.iv_back_button).setOnClickListener(this);

        if (localStorage.loadAppLanguage().equals("he")) {
            buttonPosition = VizoAccountItemView.ACTION_BUTTON_POSITION.LEFT;
        }
    }

    private void populateAccountsList() {
        accounts = new ArrayList<>();

        // Check Facebook connection
        if (AccessToken.getCurrentAccessToken() == null) {
            accounts.add(Constants.FACEBOOK);
        }

        // Check Twitter connection
        if (Twitter.getSessionManager().getActiveSession() == null) {
            accounts.add(Constants.TWITTER);
        }

        accountsList.setAdapter(new AccountsListAdapter());
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.iv_back_button) {

            // navigate back
            delegate.onBackPressed();
        }
    }

    private VizoAccountItemView.ACTION_BUTTON_POSITION buttonPosition =
            VizoAccountItemView.ACTION_BUTTON_POSITION.RIGHT;

    /**
     * Custom class for accounts list
     */
    private class AccountsListAdapter extends BaseAdapter {

        public AccountsListAdapter() {
        }

        @Override
        public int getCount() {
            return accounts.size();
        }

        @Override
        public Integer getItem(int position) {
            return accounts.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            VizoAccountItemView itemView;

            if (convertView == null) {
                itemView = new VizoAccountItemView(getActivity());
            } else {
                itemView = (VizoAccountItemView) convertView;
            }

            itemView.setActionButtonPos(buttonPosition);
            itemView.configureWithAccount(getItem(position), false);
            itemView.setOnActionListener(actionListener);

            return itemView;
        }
    }

    /**
     * Custom action listener for user action on accounts list
     */
    private VizoAccountItemView.OnActionListener actionListener = new VizoAccountItemView.OnActionListener() {
        @Override
        public void onActionClicked(int accountId) {
            if (accountId == Constants.FACEBOOK) {

                // Connect Facebook
                delegate.connectFacebookWithCallback(facebookCallback);

            } else if (accountId == Constants.TWITTER) {

                // Connect Twitter
                delegate.authorizeTwitterWithCallback(twitterSessionCallback);

            }
        }
    };

    private FacebookCallback<LoginResult> facebookCallback = new FacebookCallback<LoginResult>() {
        @Override
        public void onSuccess(LoginResult loginResult) {
            populateAccountsList();

            progress.show();
            GraphRequest request = GraphRequest.newMeRequest(
                    loginResult.getAccessToken(),
                    new GraphRequest.GraphJSONObjectCallback() {

                        @Override
                        public void onCompleted(JSONObject jsonObject,
                                                GraphResponse graphResponse) {
                            localStorage.saveFacebookInfo(jsonObject);
                            progress.dismiss();
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
            CommonUtils.getInstance().showMessage("Failed to connect to Facebook");
        }
    };

    private Callback<TwitterSession> twitterSessionCallback = new Callback<TwitterSession>() {
        @Override
        public void success(Result<TwitterSession> result) {
            populateAccountsList();
        }

        @Override
        public void failure(TwitterException e) {
            CommonUtils.getInstance().showMessage("Failed to connect to Twitter");
        }
    };
}
