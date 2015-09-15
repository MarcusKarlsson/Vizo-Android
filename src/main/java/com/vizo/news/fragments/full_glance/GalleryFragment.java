package com.vizo.news.fragments.full_glance;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.vizo.news.R;
import com.vizo.news.activities.GlanceFullActivity;
import com.vizo.news.domain.VizoCategory;
import com.vizo.news.domain.VizoGlance;
import com.vizo.news.fragments.base.BaseFragment;
import com.vizo.news.utils.CommonUtils;

/**
 * Custom gallery fragment class which implement full glance image feature
 *
 * @author nine3_marks
 */
public class GalleryFragment extends BaseFragment implements View.OnClickListener {

    // Members which refer view elements
    private View view;
    private TextView tvDoneButton;

    /**
     * Member variable which holds the instance of delegate activity
     */
    private GlanceFullActivity delegate;

    private VizoGlance glance;

    private boolean fullMode = true;
    private int doneButtonBasePos;

    public static GalleryFragment newInstance(VizoGlance glance) {
        GalleryFragment fragment = new GalleryFragment();
        Bundle args = new Bundle();
        args.putParcelable("glance", glance);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.glance = getArguments().getParcelable("glance");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        this.view = inflater.inflate(R.layout.fragment_gallery, container, false);
        return this.view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        delegate = (GlanceFullActivity) getActivity();

        // setup fragment
        initViewAndClassMembers();
    }

    @Override
    public void onStart() {
        super.onStart();

        delegate.layoutButtonArea.setAlpha(0);
        tvDoneButton.post(new Runnable() {
            @Override
            public void run() {
                doneButtonBasePos = tvDoneButton.getTop();
                tvDoneButton.setY(0 - tvDoneButton.getBottom());
            }
        });
    }

    /**
     * Initialize view elements and class members
     */
    private void initViewAndClassMembers() {

        // Map view elements to class members
        tvDoneButton = (TextView) view.findViewById(R.id.tv_done_button);

        // Map view elements to event handlers
        tvDoneButton.setOnClickListener(this);

        // Populate gallery pager content
        ViewPager galleryPager = (ViewPager) view.findViewById(R.id.vp_gallery_pager);
        galleryPager.setAdapter(new GalleryPagerAdapter(getActivity()));
    }

    @Override
    public void onClick(View v) {

        if (v.getId() == tvDoneButton.getId()) {
            delegate.onBackPressed();
        } else if (v.getId() == R.id.iv_gallery_image) {
            fullMode = !fullMode;
            changeViewMode();
        }
    }

    /**
     * Change view mode according to the value of full mode variable
     */
    private void changeViewMode() {
        int duration = 160;
        if (fullMode) {
            delegate.layoutButtonArea.animate().alpha(0).setDuration(duration);
            tvDoneButton.animate().y(0 - tvDoneButton.getHeight()).setDuration(duration);
        } else {
            delegate.layoutButtonArea.animate().alpha(1).setDuration(duration);
            tvDoneButton.animate().y(doneButtonBasePos).setDuration(duration);
        }
    }

    /**
     * Custom adapter class for Gallery Pager
     */
    private class GalleryPagerAdapter extends PagerAdapter {

        private LayoutInflater inflater;
        private String[] imageUrls;

        public GalleryPagerAdapter(Context context) {
            this.inflater = LayoutInflater.from(context);
            imageUrls = new String[]{glance.image_url, glance.image_sub_url};
        }

        @Override
        public int getCount() {
            return 2;
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            View itemView = inflater.inflate(R.layout.full_glance_gallery_item, container, false);

            ImageView imageView = (ImageView) itemView.findViewById(R.id.iv_gallery_image);
//            Picasso.with(baseActivity.getApplicationContext())
//                    .load(imageUrls[position])
//                    .placeholder(R.drawable.news_placeholder)
//                    .into(imageView);
            VizoCategory category = VizoCategory.findCategoryById(glance.category_id, baseActivity);
            CommonUtils.getInstance().loadImage(
                    imageView,
                    imageUrls[position],
                    category.getPlaceholderImage(baseActivity, false));
            imageView.setOnClickListener(GalleryFragment.this);

            container.addView(itemView);
            return itemView;
        }
    }
}
