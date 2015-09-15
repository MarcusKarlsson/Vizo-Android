package com.vizo.news.fragments.logo_settings.glanced;

import android.animation.Animator;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.vizo.news.R;
import com.vizo.news.activities.HomeSettingsActivity;
import com.vizo.news.domain.VizoCategory;
import com.vizo.news.domain.VizoGlance;
import com.vizo.news.fragments.base.BaseFragment;
import com.vizo.news.service.ProfileSyncService;
import com.vizo.news.ui.Carousel;
import com.vizo.news.ui.CarouselLayoutItem;
import com.vizo.news.ui.CoverFlowCarousel;
import com.vizo.news.ui.VizoDynamicImageView;
import com.vizo.news.utils.LocalStorage;
import com.amplitude.api.Amplitude;


/**
 * Custom fragment class for Glance screen
 *
 * @author nine3_marks
 */
public class GlancedFragment extends BaseFragment implements View.OnClickListener {

    // Holds the instance of delegate Activity
    private HomeSettingsActivity delegate;

    /**
     * Member variable which holds the list of glanced categories
     */
    private ArrayList<String> categoryIds;
    private ArrayList<VizoCategory> glancedCategories;

    private GlancedListAdapter glancedListAdapter;

    // Members variables which refer view elements
    private View view;
    private ListView lvGlancedList;
    private View footerView;
    private CheckBox favoriteButton;

    private LayoutInflater inflater;

    private int mTouchSlop;

    public static GlancedFragment newInstance() {
        return new GlancedFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        this.view = inflater.inflate(R.layout.fragment_glanced, container, false);
        this.inflater = inflater;
        return this.view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        delegate = (HomeSettingsActivity) getActivity();

        // initialize
        initViewAndClassMembers();

        final ViewConfiguration configuration = ViewConfiguration.get(getActivity());
        mTouchSlop = configuration.getScaledTouchSlop();

        /**
         * Check if need to show walkthrough
         * always display walkthrough if Show Tips settings is on
         */
        if (!localStorage.getFlagValue(LocalStorage.WALKED_THROUGH_GLANCED)) {
            startWalkthrough();
        }

        setupGlancedList();
    }

    /**
     * Initialize view elements and class members
     */
    private void initViewAndClassMembers() {

        View vi = this.view;

        // Map view elements to class members
        lvGlancedList = (ListView) vi.findViewById(R.id.lv_glanced_list);

        // Add footer view which contains "clear history" button
        footerView = this.inflater.inflate(R.layout.glanced_list_footerview, null, false);
        footerView.setVisibility(View.VISIBLE);
        lvGlancedList.addFooterView(footerView);

        // Map view elements to event handlers
        vi.findViewById(R.id.iv_back_button).setOnClickListener(this);
        footerView.findViewById(R.id.tv_clear_history).setOnClickListener(this);

        // when user taps favorite button, change view mode
        favoriteButton = (CheckBox) vi.findViewById(R.id.ch_favorite_button);
        favoriteButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                populateGlancedList(isChecked);
            }
        });
    }

    /**
     * Setup glanced category IDs list to display
     */
    private void setupGlancedList() {

        // load preference settings from local storage in order to arrange like preferences order
        ArrayList<String> leftItems = localStorage.loadSavedGlancePreferences(LocalStorage.LEFT_PANEL_SETTINGS);
        ArrayList<String> rightItems = localStorage.loadSavedGlancePreferences(LocalStorage.RIGHT_PANEL_SETTINGS);

        VizoCategory topCategory = null;
        ArrayList<String> otherCategories = new ArrayList<>();
        for (VizoCategory category : VizoCategory.getAllCategories(getActivity())) {
            if (category.isTopNews()) {
                topCategory = category;
            } else if (!leftItems.contains(category.categoryId) && !rightItems.contains(category.categoryId)) {
                otherCategories.add(category.categoryId);
            }
        }

        categoryIds = new ArrayList<>();
        if (topCategory != null) {
            categoryIds.add(topCategory.categoryId);
        }
        categoryIds.addAll(leftItems);
        categoryIds.addAll(rightItems);
        categoryIds.addAll(otherCategories);

        // Initialize custom glanced list adapter, map glancedCategories as data observer
        glancedListAdapter = new GlancedListAdapter(getActivity());
        lvGlancedList.setAdapter(glancedListAdapter);
    }

    @Override
    public void onResume() {
        super.onResume();

        Amplitude.getInstance().startSession();

        // Populate glanced list
        populateGlancedList(false);
    }

    /**
     * Change view mode
     *
     * @param showFavorite if true show only favorite items, if false, show all items
     */
    private void populateGlancedList(boolean showFavorite) {

        glancedCategories = new ArrayList<>();
        for (String categoryId : categoryIds) {
            VizoCategory category = VizoCategory.findCategoryById(categoryId, getActivity());
            if (category != null) {
                ArrayList<VizoGlance> glances;
                if (showFavorite) {
                    glances = category.getFavoriteItemsInCategory(getActivity());
                } else {
                    glances = category.getGlancedItemsInCategory(getActivity());
                }
                if (glances.size() > 0) {
                    glancedCategories.add(category);
                }
            }
        }

        if (glancedCategories.size() > 0) {

            footerView.findViewById(R.id.tv_clear_history).setVisibility(View.VISIBLE);
            footerView.findViewById(R.id.tv_no_glanced).setVisibility(View.GONE);

        } else {

            // if no glanced items, we don't need to show "clear history" button
            footerView.findViewById(R.id.tv_clear_history).setVisibility(View.GONE);
            footerView.findViewById(R.id.tv_no_glanced).setVisibility(View.VISIBLE);

        }

        glancedListAdapter.setGlancedCategories(glancedCategories);
        glancedListAdapter.notifyDataSetChanged();
    }

    /**
     * Display custom walkthough screens
     */
    private void startWalkthrough() {

        // Walkthrough View for SWIPE LEFT OR RIGHT
        final LinearLayout wtSwipeHor = (LinearLayout) this.view.findViewById(R.id.ll_wt_swipe_hor);

        // Walkthrough View for TAP THE STAR
        final RelativeLayout wtTapStar = (RelativeLayout) this.view.findViewById(R.id.rl_wt_tap_favorite);

        wtSwipeHor.setAlpha(1);
        wtSwipeHor.setVisibility(View.VISIBLE);
        wtSwipeHor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                wtTapStar.animate().setDuration(300).alpha(0).setListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {
                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {

                        // Hide walkthrough for SWIPE LEFT OR RIGHT
                        wtSwipeHor.setVisibility(View.GONE);

                        // Show walkthrough for TAP THE STAR with animation
                        wtTapStar.setVisibility(View.VISIBLE);
                        wtTapStar.animate().setDuration(300).alpha(1);

                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {
                    }

                    @Override
                    public void onAnimationRepeat(Animator animation) {
                    }
                });
            }
        });

        wtTapStar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Dismiss walkthrough for TAP THE STAR with animation when tap favorite indicator
                localStorage.setFlagValue(LocalStorage.WALKED_THROUGH_GLANCED, true);
                wtTapStar.animate().setDuration(300).alpha(0).setListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {
                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        wtTapStar.setVisibility(View.GONE);
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {
                    }

                    @Override
                    public void onAnimationRepeat(Animator animation) {
                    }
                });
            }
        });
    }

    @Override
    public void onClick(View v) {

        if (v.getId() == R.id.iv_back_button) {

            delegate.onBackPressed();

        } else if (v.getId() == R.id.tv_clear_history) {

            // remove all glanced items from database
            VizoGlance.clearGlancedHistory(getActivity());
            populateGlancedList(favoriteButton.isChecked());

            favoriteButton.setChecked(false);

            // Start sync process
            Intent intent = new Intent(getActivity(), ProfileSyncService.class);
            getActivity().startService(intent);
        }
    }

    /**
     * Custom adapter class for Glanced List
     */
    private class GlancedListAdapter extends BaseAdapter {

        private Context context;
        private LayoutInflater inflater;
        private ArrayList<VizoCategory> items;
        private Map<Integer, Integer> lastReadings;

        public GlancedListAdapter(Context context) {
            this.context = context;
            this.inflater = LayoutInflater.from(context);
            this.lastReadings = new HashMap<>();
            this.items = new ArrayList<>();
        }

        public void setGlancedCategories(ArrayList<VizoCategory> items) {
            this.items = items;
        }

        @Override
        public int getCount() {
            return items.size();
        }

        @Override
        public VizoCategory getItem(int position) {
            return items.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = inflater.inflate(R.layout.glanced_category_item, parent, false);
            }

            VizoCategory category = items.get(position);
            final ArrayList<VizoGlance> glances;
            if (favoriteButton.isChecked()) {
                glances = category.getFavoriteItemsInCategory(getActivity());
            } else {
                glances = category.getGlancedItemsInCategory(getActivity());
            }

            // Show category name
            TextView tvCategoryName = (TextView) convertView.findViewById(R.id.tv_category_name);
            tvCategoryName.setText(category.getLocalizedName(getActivity()));

            // TextView for glance preview
            final TextView tvGlancePreview = (TextView) convertView.findViewById(R.id.tv_glance_preview);
//            tvGlancePreview.setText(glances.get(0).getShortDescription());

            // Populate carousel contents
            final CoverFlowCarousel carousel = (CoverFlowCarousel) convertView.findViewById(R.id.carousel_glanced_items);
            carousel.setAdapter(new GlancedCarouselAdapter(context, glances));

            // Backup last reading when navigate back from full glance
            int lastRead = 0;
            if (lastReadings.containsKey(position)) {
                lastRead = lastReadings.get(position);
                if (lastRead >= 0 && lastRead < glances.size()) {
                    carousel.setSelection(lastRead);
                } else {
                    lastRead = 0;
                }
            }
            carousel.setSelection(lastRead);

            carousel.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    int action = event.getAction();
                    switch (action) {
                        case MotionEvent.ACTION_DOWN:
                            // Disallow ScrollView to intercept touch events.
                            //lvGlancedList.requestDisallowInterceptTouchEvent(true);
                            break;

                        case MotionEvent.ACTION_UP:
                            // Allow ScrollView to intercept touch events.
                            //lvGlancedList.requestDisallowInterceptTouchEvent(false);
                            break;
                    }

                    // Handle ListView touch events.
                    v.onTouchEvent(event);
                    return true;
                }
            });

            // Change glance preview when scroll carousel
            carousel.setOnItemSelectedListener(new Carousel.OnItemSelectedListener() {
                @Override
                public void onItemSelected(View child, int itemPos) {
                    // Show glance preview under carousel
                    tvGlancePreview.setText(glances.get(itemPos).getShortDescription());
                    lastReadings.put(position, itemPos);
                }
            });

            // Navigate to full glance when tap glance or "read glance" button
            convertView.findViewById(R.id.tv_read_glance).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    delegate.navigateToFullGlance(glances.get(carousel.getSelection()));
                }
            });

            return convertView;
        }
    }

    /**
     * Custom adapter class for carousel of glanced items
     */
    private class GlancedCarouselAdapter extends BaseAdapter {

        private LayoutInflater inflater;
        private ArrayList<VizoGlance> glancedItems;

        public GlancedCarouselAdapter(Context context, ArrayList<VizoGlance> glancedItems) {
            this.inflater = LayoutInflater.from(context);
            this.glancedItems = glancedItems;
        }

        @Override
        public int getCount() {
            return glancedItems.size();
        }

        @Override
        public VizoGlance getItem(int position) {
            return glancedItems.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            CarouselLayoutItem view;
            if (convertView == null) {
                view = (CarouselLayoutItem) inflater.inflate(R.layout.glanced_carousel_item, parent, false);
            } else {
                view = (CarouselLayoutItem) convertView;
            }

            final VizoGlance glance = glancedItems.get(position);

            // Load glance image
            VizoDynamicImageView itemImage = (VizoDynamicImageView) view.findViewById(R.id.iv_item_image);
            itemImage.loadImage(glance.image_url);

            // Show favorite icon if the glance is favorited
            int visibility = glance.isFavorite == 1 ? View.VISIBLE : View.INVISIBLE;
            view.findViewById(R.id.iv_favorite_icon).setVisibility(visibility);

            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    delegate.navigateToFullGlance(glance);
                }
            });

            return view;
        }
    }

}
