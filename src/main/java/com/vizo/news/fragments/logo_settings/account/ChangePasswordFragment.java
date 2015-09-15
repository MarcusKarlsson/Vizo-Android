package com.vizo.news.fragments.logo_settings.account;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.vizo.news.R;
import com.vizo.news.activities.base.BaseActivity;
import com.vizo.news.fragments.base.BaseFragment;

/**
 * Custom fragment class for Change Password Page
 *
 * @author nine3_marks
 */
public class ChangePasswordFragment extends BaseFragment implements View.OnClickListener {

    // member variables which hold view elements
    private View view;

    /**
     * member which holds instance of delegate activity
     */
    private BaseActivity delegate;

    public static ChangePasswordFragment newInstance() {
        return new ChangePasswordFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        this.view = inflater.inflate(R.layout.fragment_change_password, container, false);
        return this.view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        delegate = (BaseActivity) getActivity();

        // setup fragment
        initViewAndClassMembers();
    }

    /**
     * initialize view elements and class members
     */
    private void initViewAndClassMembers() {

        // map view elements to event handlers
        view.findViewById(R.id.iv_back_button).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.iv_back_button) {

            // navigate back
            delegate.onBackPressed();
        }
    }
}
