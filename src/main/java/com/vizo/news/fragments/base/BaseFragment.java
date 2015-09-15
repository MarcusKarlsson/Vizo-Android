package com.vizo.news.fragments.base;

import android.app.Activity;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.vizo.news.activities.base.BaseActivity;
import com.vizo.news.ui.VizoProgressDialog;
import com.vizo.news.utils.LocalStorage;

/**
 * Created by MarksUser on 3/20/2015.
 *
 * @author nine3_marks
 */
public class BaseFragment extends Fragment {

    protected BaseActivity baseActivity;
    protected LocalStorage localStorage;
    protected View rootView;
    protected VizoProgressDialog progress;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            baseActivity = (BaseActivity) activity;
            localStorage = LocalStorage.getInstance();
            progress = new VizoProgressDialog(activity);
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must extend BaseActivity");
        }
    }

    @Override
    public void onDestroyView() {
        baseActivity.hideKeyboard();

        super.onDestroyView();
    }

    public boolean backButtonPressed() {
        return true;
    }

    /**
     * @param inflater
     * @param container
     * @param resource
     * @return true if view was inflated
     */
    protected boolean inflateViewIfNull(LayoutInflater inflater,
                                        ViewGroup container, int resource) {
        if (rootView == null) {
            rootView = inflater.inflate(resource, container, false);
            return true;
        } else {
            ((ViewGroup) rootView.getParent()).removeView(rootView);
            return false;
        }
    }
}

