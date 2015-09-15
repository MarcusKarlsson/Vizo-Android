package com.vizo.news.activities;

import android.os.Bundle;
import android.view.View;

import com.vizo.news.R;
import com.vizo.news.activities.base.BaseActivity;
import com.vizo.news.fragments.state_of_day.FragmentStateOfDay;

/**
 * Custom Activity for State Of The Day Module
 * <p/>
 * Created by nine3_marks on 5/19/2015.
 */
public class StateOfDayActivity extends BaseActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.activity_state_of_day);

        showFragment(R.id.fl_fragment_container, FragmentStateOfDay.newInstance());
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.iv_back_button) {

            // Navigate back to Home Screen
            finish();
        }
    }
}
