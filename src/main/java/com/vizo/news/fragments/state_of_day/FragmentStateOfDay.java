package com.vizo.news.fragments.state_of_day;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.droidux.ui.widgets.gallery.DuxAdapterView;
import com.droidux.ui.widgets.gallery.GalleryFlowZoom;
import com.vizo.news.R;
import com.vizo.news.activities.GlanceFullActivity;
import com.vizo.news.activities.StateOfDayActivity;
import com.vizo.news.domain.VizoGlance;
import com.vizo.news.fragments.base.BaseFragment;
import com.vizo.news.fragments.logo_settings.settings.SettingsFAQFragment;
import com.vizo.news.ui.VizoDynamicImageView;
import com.vizo.news.utils.Constants;

import java.util.ArrayList;

/**
 * Custom fragment for State Of The Day Page
 * <p/>
 * Created by nine3_marks on 5/19/2015.
 */
public class FragmentStateOfDay extends BaseFragment implements View.OnClickListener {

    // Members which hold view elements
    private View view;
    private GalleryFlowZoom glancesCarousel;
    private TextView textPreview;

    private ArrayList<VizoGlance> glances;

    // Category index of State Of The Day
    private final int STATE_OF_DAY = 4;

    public static FragmentStateOfDay newInstance() {
        return new FragmentStateOfDay();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        this.view = inflater.inflate(R.layout.fragment_state_of_day, container, false);
        return this.view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // Setup fragment
        initViewAndMembers();
    }

    /**
     * Initialize view elements and class members
     */
    private void initViewAndMembers() {
        // Map view elements to class members
        glancesCarousel = (GalleryFlowZoom) view.findViewById(R.id.glances_carousel);
        textPreview = (TextView) view.findViewById(R.id.tv_glance_preview);

        // Map view elements to event handlers
        view.findViewById(R.id.iv_back_button).setOnClickListener(this);
        view.findViewById(R.id.tv_read_glance).setOnClickListener(this);
        view.findViewById(R.id.tv_help_button).setOnClickListener(this);

        // Setup Glances carousel
        glances = VizoGlance.getStateOfDayGlances(getActivity());
        glancesCarousel.setAdapter(new GlancesCarouselAdapter());
        glancesCarousel.setOnItemClickListener(new DuxAdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(DuxAdapterView<?> duxAdapterView, View view, int i, long l) {
                if (i == glancesCarousel.getSelectedItemPosition()) {
                    VizoGlance glance = glances.get(i);
                    if (glance != null) {
                        navigateToFullGlance(glance);
                    }
                }
            }
        });
        glancesCarousel.setOnItemSelectedListener(new DuxAdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(DuxAdapterView<?> duxAdapterView, View view, int i, long l) {
                VizoGlance glance = glances.get(i);
                if (glance != null) {
                    textPreview.setText(glance.getShortDescription());
                }
            }

            @Override
            public void onNothingSelected(DuxAdapterView<?> duxAdapterView) {
            }
        });
        textPreview.setText(glances.get(0).getShortDescription());
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.iv_back_button) {

            // Navigate back to Home Screen
            StateOfDayActivity delegate = (StateOfDayActivity) getActivity();
            if (delegate != null) {
                delegate.finish();
            }

        } else if (v.getId() == R.id.tv_read_glance) {

            // Navigate to Full Glance Page
            VizoGlance glance = glances.get(glancesCarousel.getSelectedItemPosition());
            if (glance != null) {
                navigateToFullGlance(glance);
            }

        } else if (v.getId() == R.id.tv_help_button) {

            // Navigate to FAQ page
            StateOfDayActivity delegate = (StateOfDayActivity) getActivity();
            delegate.showFragment(R.id.fl_fragment_container,
                    SettingsFAQFragment.newInstance(STATE_OF_DAY), true);

        }
    }

    /**
     * Navigate to full glance page with selected glance
     *
     * @param glance Selected glance object
     */
    public void navigateToFullGlance(VizoGlance glance) {
        Intent intent = new Intent(getActivity(), GlanceFullActivity.class);
        intent.putExtra("glance", glance);
        intent.putExtra("show_mode", Constants.SHOW_STATE_OF_DAY);
        startActivity(intent);
    }

    /**
     * Custom adapter class for Glances Carousel
     */
    private class GlancesCarouselAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return glances.size();
        }

        @Override
        public VizoGlance getItem(int position) {
            return glances.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            final VizoGlance glance = getItem(position);
            if (convertView == null) {
                convertView = LayoutInflater
                        .from(baseActivity)
                        .inflate(R.layout.glance_carousel_item, parent, false);
            }

            VizoDynamicImageView iv = (VizoDynamicImageView)
                    convertView.findViewById(R.id.iv_glance_image);

            iv.loadImage(glance.image_url);

            return convertView;
        }
    }
}
