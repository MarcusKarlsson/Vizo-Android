package com.vizo.news.ui;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.vizo.news.R;
import com.vizo.news.api.APIVizoUClient;
import com.vizo.news.api.domain.VizoUGlancesResponse;
import com.vizo.news.domain.VizoCategory;
import com.vizo.news.domain.VizoGlance;
import com.vizo.news.domain.VizoUGlance;
import com.vizo.news.utils.CommonUtils;
import com.vizo.news.utils.Constants;

import fr.castorflex.android.verticalviewpager.VerticalViewPager;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Created by MarksUser on 3/26/2015.
 *
 * @author nine3_marks
 */
public class VizoVerticalCategoryPager extends VerticalViewPager {

    private ArrayList<VizoCategory> categories;
    private VizoCategoryPagerAdapter adapter;

    private static final float MIN_SCALE = 0.75f;
    private static final float MIN_ALPHA = 0.75f;

    public VizoVerticalCategoryPager(Context context) {
        super(context);
        init();
    }

    public VizoVerticalCategoryPager(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        setPageTransformer(true, new ViewPager.PageTransformer() {
            @Override
            public void transformPage(View view, float position) {
                int pageWidth = view.getWidth();
                int pageHeight = view.getHeight();

                if (position < -1) { // [-Infinity,-1)
                    // This page is way off-screen to the left.
                    view.setAlpha(0);

                } else if (position <= 1) { // [-1,1]
                    // Modify the default slide transition to shrink the page as well
                    float scaleFactor = Math.max(MIN_SCALE, 1 - Math.abs(position));
                    float vertMargin = pageHeight * (1 - scaleFactor) / 2;
                    float horzMargin = pageWidth * (1 - scaleFactor) / 2;
                    if (position < 0) {
                        view.setTranslationY(vertMargin - horzMargin / 2);
                    } else {
                        view.setTranslationY(-vertMargin + horzMargin / 2);
                    }

                    // Scale the page down (between MIN_SCALE and 1)
                    view.setScaleX(scaleFactor);
                    view.setScaleY(scaleFactor);

                    // Fade the page relative to its size.
                    view.setAlpha(MIN_ALPHA +
                            (scaleFactor - MIN_SCALE) /
                                    (1 - MIN_SCALE) * (1 - MIN_ALPHA));

                } else { // (1,+Infinity]
                    // This page is way off-screen to the right.
                    view.setAlpha(0);
                }
            }
        });
    }

    /**
     * Populate categories to be shown in pager
     *
     * @param categoryIds Array of category IDs
     */
    public void setCategories(ArrayList<String> categoryIds) {
        categories = new ArrayList<>();
        for (String categoryId : categoryIds) {
            VizoCategory category = VizoCategory.findCategoryById(categoryId, getContext());
            if (category != null) {
                categories.add(category);
            }
        }
    }

    public void loadGlances(FragmentManager fm) {
        adapter = new VizoCategoryPagerAdapter(fm, categories);
        setAdapter(adapter);
//        setPageMargin(getResources().getDimensionPixelSize(R.dimen.pagemargin));
//        setPageMarginDrawable(new ColorDrawable(getResources().getColor(android.R.color.holo_green_dark)));
        setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                VizoGlanceViewPagerFragment fragment = (VizoGlanceViewPagerFragment) adapter.getItem(getCurrentItem());
                if (null != fragment) {
                    fragment.animateCategoryPreviwer();
                    fragment.showFirstGlance();
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });
    }

    public void updateGlance() {
        VizoGlanceViewPagerFragment fragment = (VizoGlanceViewPagerFragment) adapter.getItem(getCurrentItem());
        if (fragment != null) {
            fragment.slideGlancePager();
        }
    }

    /**
     * Get current visible glance
     *
     * @return VizoGlance object
     */
    public VizoGlance getCurrentGlance() {
        VizoGlanceViewPagerFragment mFragment = (VizoGlanceViewPagerFragment) adapter.getItem(getCurrentItem());
        if (mFragment != null) {
            int glanceIndex = mFragment.glancePager.getCurrentItem();
            if (mFragment.lastGlanceIndicator.getVisibility() == View.VISIBLE) {
                return null;
            }
            if(mFragment.category.categoryId.equals(VizoCategory.VIZO_VIZOU)){
                ArrayList<VizoUGlance> vizoUGlances = VizoGlanceViewPagerFragment.userGlances;
                if(vizoUGlances.size() > glanceIndex)
                    return VizoGlanceViewPagerFragment.getVizoGlance(vizoUGlances.get(glanceIndex));
                else
                    return null;

            }else{
                ArrayList<VizoGlance> categoryGlances = mFragment.category.getCategoryGlances(getContext());
                if (categoryGlances.size() > 0) {
                    return categoryGlances.get(glanceIndex);
                }
            }
        }
        return null;
    }

    private class VizoCategoryPagerAdapter extends FragmentStatePagerAdapter {

        private ArrayList<VizoCategory> categories = new ArrayList<>();
        private Hashtable<Integer, VizoGlanceViewPagerFragment> pagerFragments;

        public VizoCategoryPagerAdapter(FragmentManager fm, ArrayList<VizoCategory> categories) {
            super(fm);
            this.categories.addAll(categories);
            this.pagerFragments = new Hashtable<>();
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            VizoGlanceViewPagerFragment fragment = this.pagerFragments.get(position);
            if (fragment == null) {
                fragment = VizoGlanceViewPagerFragment.newInstance(this.categories.get(position));
                this.pagerFragments.put(position, fragment);
            }
            return fragment;
        }

        @Override
        public int getCount() {
            return categories.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            VizoCategory category = categories.get(position);
            return category.category_name;
        }

        @Override
        public int getItemPosition(Object object) {
            return POSITION_NONE;
        }
    }

    /**
     * Custom Fragment class corresponding to glance panel in home screen
     */
    public static class VizoGlanceViewPagerFragment extends Fragment implements VizoHorizontalPager.OnSwipeOutListener, OnClickListener {

        /**
         * The category of which glances for view pager
         */
        public VizoCategory category;
        private static final String PAGER_CATEGORY = "pager_category";
        private View view;

        private VizoHorizontalPager glancePager;
        private LinearLayout lastGlanceIndicator;
        private TextView tvCategoryIndicator1;
        private TextView tvCategoryIndicator2;
        private TextView tvCategoryIndicator3;
        private TextView tvCategoryIndicator4;

        // Category Preview Screen Elements
        private RelativeLayout categoryPreviewIndicator;
        private TextView tvCategoryPreviewIndicator;

        public static ArrayList<VizoUGlance> userGlances  = new ArrayList<VizoUGlance>();
        /**
         * Returns a new instance of this fragment for the category
         * number.
         */
        public static VizoGlanceViewPagerFragment newInstance(VizoCategory category) {
            VizoGlanceViewPagerFragment fragment = new VizoGlanceViewPagerFragment();
            Bundle args = new Bundle();
            args.putParcelable(PAGER_CATEGORY, category);
            fragment.setArguments(args);
            return fragment;
        }

        public VizoGlanceViewPagerFragment() {
        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            this.category = getArguments().getParcelable(PAGER_CATEGORY);
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            this.view = inflater.inflate(R.layout.vertical_category_pager_item, container, false);

            // setup horizontal glance pager
            glancePager = (VizoHorizontalPager) this.view.findViewById(R.id.vp_glance_pager);
            glancePager.setOnSwipeOutListener(this);

            if(category.categoryId.equals(VizoCategory.VIZO_VIZOU))
                // load VizoU glances
                loadGlancePageData();
            else {
                glancePager.setAdapter(new VizoGlanceViewPagerAdapter(
                        getChildFragmentManager(), category.getCategoryGlances(getActivity())));
                glancePager.setOffscreenPageLimit(3);
                //loadGlances();
            }

            // Category previewer which is used for Category Transition Animation
            categoryPreviewIndicator = (RelativeLayout) this.view.findViewById(R.id.rl_category_preview);
            tvCategoryPreviewIndicator = (TextView) this.view.findViewById(R.id.tv_category_preview_indicator);
            tvCategoryPreviewIndicator.setText(category.getLocalizedName(getActivity()));

            // Populate no-glance view
            if (category.isTopNews()) {

                // If this is shown on top panel, no need to show "change category" buttons
                lastGlanceIndicator = (LinearLayout) this.view.findViewById(R.id.ll_last_glance_indicator_1);
            } else {

                // If this is shown on left or right panel, show "change category" buttons
                lastGlanceIndicator = (LinearLayout) this.view.findViewById(R.id.ll_last_glance_indicator_2);
            }

            this.view.findViewById(R.id.iv_button_read_previous_1).setOnClickListener(this);
            this.view.findViewById(R.id.iv_button_read_previous_2).setOnClickListener(this);

            tvCategoryIndicator1 = (TextView) this.view.findViewById(R.id.tv_category_indicator_1);
            tvCategoryIndicator2 = (TextView) this.view.findViewById(R.id.tv_category_indicator_2);
            tvCategoryIndicator3 = (TextView) this.view.findViewById(R.id.tv_category_indicator_3);
            tvCategoryIndicator4 = (TextView) this.view.findViewById(R.id.tv_category_indicator_4);

            String strCategory = String.format("%s %s",
                    category.getLocalizedName(getActivity()),
                    getActivity().getResources().getString(R.string.Glances));
            tvCategoryIndicator1.setText(strCategory);
            tvCategoryIndicator2.setText(strCategory);
            tvCategoryIndicator3.setText(strCategory);
            tvCategoryIndicator4.setText(strCategory);
            return this.view;
        }

        ArrayList<VizoGlance> mVizoGlances;

        private void loadGlances(){
            new AsyncTask<Void, Void, String>() {

                @Override
                protected void onPreExecute() {
                    mVizoGlances = new ArrayList<VizoGlance>();
                }

                @Override
                protected String doInBackground(Void... params) {
                    mVizoGlances = category.getCategoryGlances(getActivity());
                    return null;

                }

                @Override
                protected void onPostExecute(String result) {
                    glancePager.setAdapter(new VizoGlanceViewPagerAdapter(
                            getChildFragmentManager(), mVizoGlances ));
                    glancePager.setOffscreenPageLimit(3);
                }
            }.execute(null, null, null);
        }

        private void loadGlancePageData(){
            APIVizoUClient.getInstance(getActivity()).getApiService().getVizoUGlances(20, 0, 0, mCallback);
        }
        private Callback<VizoUGlancesResponse> mCallback = new Callback<VizoUGlancesResponse>() {
            @Override
            public void success(VizoUGlancesResponse vizoUGlancesResponse, Response response) {
                if (vizoUGlancesResponse.totalCount > 0) {
                    userGlances = vizoUGlancesResponse.glances;
                    glancePager.setAdapter(new VizoGlanceViewPagerAdapter(
                            getChildFragmentManager(), getVizoGlances(userGlances), true));
                    glancePager.setOffscreenPageLimit(3);
                }
            }

            @Override
            public void failure(RetrofitError error) {
                CommonUtils.getInstance().showMessage("Failed to load user glances");
            }
        };

        // api for converting VizoUGlances to VizoGlances
        public static ArrayList<VizoGlance> getVizoGlances(ArrayList<VizoUGlance> uGlances){
            ArrayList<VizoGlance>  arrayGlances = new ArrayList<VizoGlance>();
            for(int i = 0; i < uGlances.size(); i++){
                VizoUGlance uGlance = uGlances.get(i);

                arrayGlances.add(getVizoGlance(uGlance));
            }
            return arrayGlances;
        }

        // api for converting VizoUGlance to VizoGlance
        public static VizoGlance getVizoGlance(VizoUGlance uGlance){
            VizoGlance vizoGlance = new VizoGlance();
            vizoGlance.glanceId = uGlance.glanceId;
            vizoGlance.category_id = /*uGlance.category_id*/"22";
            vizoGlance.title = uGlance.poster_name;
            vizoGlance.description = uGlance.description;
            vizoGlance.image_url = uGlance.image_url;
            vizoGlance.image_sub_url = uGlance.image_sub_url;
            vizoGlance.image_title = uGlance.image_title;
            vizoGlance.image_credit = uGlance.image_credit;
            vizoGlance.image_caption = uGlance.image_caption;
            vizoGlance.language = uGlance.language;
            vizoGlance.syncState = uGlance.syncState;
            vizoGlance.isFavorite = uGlance.isFavorite;
            vizoGlance.state_of_day = uGlance.state_of_day;
            vizoGlance.poster_name = uGlance.poster_name;
            vizoGlance.poster_avatar = uGlance.poster_avatar;
            vizoGlance.poster_email = uGlance.poster_email;
            vizoGlance.modified_date = uGlance.modified_date;
            vizoGlance.vote_count = uGlance.vote_count;

            return vizoGlance;
        }
        @Override
        public void onSwipeOutAtStart() {

        }

        @Override
        public void onSwipeOutAtEnd() {
            if (lastGlanceIndicator.getVisibility() != View.VISIBLE) {
                Animation fadeIn = new AlphaAnimation(0, 1);
                fadeIn.setInterpolator(new DecelerateInterpolator()); //add this
                fadeIn.setDuration(300);
                lastGlanceIndicator.setAnimation(fadeIn);
                lastGlanceIndicator.setVisibility(View.VISIBLE);
            }
        }

        @Override
        public void onClick(View v) {
            if (v.getId() == R.id.iv_button_read_previous_1 || v.getId() == R.id.iv_button_read_previous_2) {
                lastGlanceIndicator.setVisibility(View.GONE);
                glancePager.setCurrentItem(0);
            }
        }


        /**
         * Slide glance pager with animation
         */
        public void slideGlancePager() {
            if (glancePager != null) {
                int index = glancePager.getCurrentItem() + 1;
                if (index > Constants.MAX_VISIBLE_GLANCES)
                    index = 0;
                if (lastGlanceIndicator.getVisibility() != View.VISIBLE)
                    glancePager.setCurrentItem(index);
            }
        }

        public void animateCategoryPreviwer() {
            categoryPreviewIndicator.setAlpha(1);
            categoryPreviewIndicator.animate().setStartDelay(1000).setDuration(500).alpha(0);
        }

        public void showFirstGlance() {
            glancePager.setCurrentItem(0);
        }

    }

    public static class VizoGlanceViewPagerAdapter extends FragmentPagerAdapter {

        private ArrayList<VizoGlance> glances;
        private Hashtable<Integer, VizoGlanceItemView> pagerFragments;
        private boolean m_bVizou = false;

        public VizoGlanceViewPagerAdapter(FragmentManager fm, ArrayList<VizoGlance> glances) {
            super(fm);
            this.glances = glances;
            pagerFragments = new Hashtable<>();
        }

        public VizoGlanceViewPagerAdapter(FragmentManager fm, ArrayList<VizoGlance> glances, boolean bVizoU) {
            super(fm);
            this.glances = glances;
            pagerFragments = new Hashtable<>();
            m_bVizou = bVizoU;
        }
        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            VizoGlanceItemView fragment = pagerFragments.get(position);
            if (fragment == null) {
                fragment = VizoGlanceItemView.newInstance(glances.get(position), m_bVizou);
                pagerFragments.put(position, fragment);
            }
            return fragment;
        }

        @Override
        public int getCount() {
            return glances.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            VizoGlance glance = glances.get(position);
            return glance.title;
        }

    }

    /**
     * Custom fragment class for each Glance Item
     */
    public static class VizoGlanceItemView extends Fragment {

        private VizoGlance glance;

        private TextView tvGlanceTitle;

        private Boolean m_bVizoU = false;
        public static VizoGlanceItemView newInstance(VizoGlance glance, boolean bVizoU) {
            VizoGlanceItemView itemView = new VizoGlanceItemView(bVizoU);
            Bundle args = new Bundle();
            args.putParcelable("glance", glance);
            itemView.setArguments(args);
            return itemView;
        }

        public VizoGlanceItemView() {
        }

        public VizoGlanceItemView(boolean bVizoU) {
            m_bVizoU = bVizoU;
        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            glance = getArguments().getParcelable("glance");
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

            View rootView = inflater.inflate(R.layout.horizontal_glance_pager_item, container, false);

            // Load glance image
            VizoDynamicImageView ivGlanceImage = (VizoDynamicImageView) rootView.findViewById(R.id.iv_glance_image);

            VizoCategory category = VizoCategory.findCategoryById(
                    glance.category_id, getActivity());
            ivGlanceImage.loadImage(this.glance.image_url,
                    category.getPlaceholderImage(getActivity(), true));
            ivGlanceImage.setEnabled(false);

            // show glance date-time
            TextView tvGlanceDateTime = (TextView) rootView.findViewById(R.id.tv_glance_date_time);
            tvGlanceDateTime.setText(CommonUtils
                    .getInstance().getVizoTimeString(glance.modified_date));

            // tag glance title on bottom-right
            tvGlanceTitle = (TextView) rootView.findViewById(R.id.tv_glance_title);

            return rootView;
        }

        @Override
        public void onResume() {
            super.onResume();

            // tag glance title on bottom-right
            if (tvGlanceTitle == null)
                return;

            if(m_bVizoU)
            {
                // if category is VizoU, use poseter_name as a title
                if (glance.isGlanced(getActivity())) {
                    tvGlanceTitle.setText(generateSpannable(this.glance.poster_name));
                } else {
                    tvGlanceTitle.setText(this.glance.poster_name);
                }
            }else{
                if (glance.isGlanced(getActivity())) {
                    tvGlanceTitle.setText(generateSpannable(this.glance.title));
                } else {
                    tvGlanceTitle.setText(this.glance.title);
                }
            }

        }

        private Spannable generateSpannable(String word) {
            if (word == null)
                word = "";
            Spannable indicator = new SpannableString(" ● " + word);
            indicator.setSpan(
                    new ForegroundColorSpan(getResources().getColor(R.color.vizo_yellow)),
                    0, 3, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            );
            indicator.setSpan(
                    new ForegroundColorSpan(getResources().getColor(R.color.white)),
                    3, word.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            );
            return indicator;
        }

    }
}
