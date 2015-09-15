package com.vizo.news.fragments.logo_settings.settings;

import android.animation.Animator;
import android.annotation.TargetApi;
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.DragEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import com.vizo.news.R;
import com.vizo.news.activities.HomeSettingsActivity;
import com.vizo.news.activities.OnboardingActivity;
import com.vizo.news.domain.VizoCategory;
import com.vizo.news.fragments.base.BaseFragment;
import com.vizo.news.service.ProfileSyncService;
import com.vizo.news.ui.Carousel;
import com.vizo.news.ui.CarouselLayoutItem;
import com.vizo.news.ui.CoverFlowCarousel;
import com.vizo.news.utils.CommonUtils;
import com.vizo.news.utils.LocalStorage;
import com.amplitude.api.Amplitude;

/**
 * Created by MarksUser on 4/5/2015.
 *
 * @author nine3_marks
 */
public class PreferencesFragment extends BaseFragment implements
        View.OnClickListener, View.OnDragListener, View.OnTouchListener {

    private View view;
    private CoverFlowCarousel carousel;
    private ListView leftPanel;
    private ListView rightPanel;

    private ArrayList<String> topItems, leftItems, rightItems;
    private CategoryAdapter topAdapter;
    private PanelAdapter leftAdapter, rightAdapter;

    private int draggingIndex;
    private DRAGGING_ITEM currentDragging;
    private float mLastMotionX, mLastMotionY;
    private int mTouchSlop;

    private enum DRAGGING_ITEM {
        TOP_PANEL_ITEM, LEFT_PANEL_ITEM, RIGHT_PANEL_ITEM
    }

    // Category Images
    private static Map<String, Integer> categoryResIds;
    private static ArrayList<VizoCategory> allCategories;

    public static PreferencesFragment newInstance() {
        return new PreferencesFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        this.view = inflater.inflate(R.layout.fragment_preferences, container, false);
        return this.view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // setup fragment
        initViewAndClassMembers();

        /**
         * Check if need to show walkthrough
         * always display walkthrough if Show Tips settings is on
         */
        if (!localStorage.getFlagValue(LocalStorage.WALKED_THROUGH_PREFERENCES)) {
            startWalkthrough();
        }
    }

    /**
     * Initialize View Elements and Class Members
     */
    private void initViewAndClassMembers() {

        final ViewConfiguration configuration = ViewConfiguration.get(getActivity());
        mTouchSlop = configuration.getScaledTouchSlop();

        // Map view elements to class members
        leftPanel = (ListView) this.view.findViewById(R.id.lv_left_panel);
        rightPanel = (ListView) this.view.findViewById(R.id.lv_right_panel);

        // Map view elements to event handlers
        this.view.findViewById(R.id.tv_done_button).setOnClickListener(this);
        this.view.findViewById(R.id.iv_back_button).setOnClickListener(this);
        leftPanel.setOnDragListener(this);
        rightPanel.setOnDragListener(this);

        allCategories = VizoCategory.getAllCategories(getActivity());
        categoryResIds = new HashMap<>();

        categoryResIds.put("5", R.drawable.cat_business);
        Amplitude.getInstance().logEvent("BUSINESS_CATEGORY");

        categoryResIds.put("7", R.drawable.cat_politics);
        Amplitude.getInstance().logEvent("POLITICS_CATEGORY");

        categoryResIds.put("9", R.drawable.cat_sports);
        Amplitude.getInstance().logEvent("SPORTS_CATEGORY");

        categoryResIds.put("10", R.drawable.cat_art_culture);
        Amplitude.getInstance().logEvent("ART_CULTURE_CATEGORY");

        categoryResIds.put("11", R.drawable.cat_technology);
        Amplitude.getInstance().logEvent("TECH_CATEGORY");

        categoryResIds.put("13", R.drawable.cat_usnews);
        Amplitude.getInstance().logEvent("US_NEWS_CATEGORY");

        categoryResIds.put("14", R.drawable.cat_worldnews);
        Amplitude.getInstance().logEvent("WORLD_NEWS_CATEGORY");

        categoryResIds.put("21", R.drawable.cat_celebrities);
        Amplitude.getInstance().logEvent("CELEBRITY_CATEGORY");

        categoryResIds.put("22", R.drawable.cat_vizou);
        Amplitude.getInstance().logEvent("VIZOU_CATEGORY");

        categoryResIds.put("23", R.drawable.cat_science);
        Amplitude.getInstance().logEvent("SCIENCE_CATEGORY");
        // Load saved glance preference settings from Local Storage
        leftItems = localStorage.loadSavedGlancePreferences(LocalStorage.LEFT_PANEL_SETTINGS);
        rightItems = localStorage.loadSavedGlancePreferences(LocalStorage.RIGHT_PANEL_SETTINGS);

        topItems = new ArrayList<>();

        // Populate top category pager items based on left & right preference settings
        for (VizoCategory category : allCategories) {

            // Check if the category is placed on left or right panel settings
            if (!category.isTopNews() && !leftItems.contains(category.categoryId)
                    && !rightItems.contains(category.categoryId)) {
                topItems.add(category.categoryId);
            }

        }

        leftAdapter = new PanelAdapter(getActivity(), leftItems);
        rightAdapter = new PanelAdapter(getActivity(), rightItems);
        leftPanel.setAdapter(leftAdapter);
        rightPanel.setAdapter(rightAdapter);
        adjustPanelHeight();

        carousel = (CoverFlowCarousel) this.view.findViewById(R.id.carousel);
        topAdapter = new CategoryAdapter(getActivity(), topItems);
        carousel.setAdapter(topAdapter);
        carousel.setSpacing(0.6f);

        // disable vertical scrolling when scrolling category carousel
        carousel.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int action = event.getAction();
                switch (action) {
                    case MotionEvent.ACTION_DOWN:
                        // Disallow ScrollView to intercept touch events.
                        v.getParent().requestDisallowInterceptTouchEvent(true);
                        break;

                    case MotionEvent.ACTION_UP:
                        // Allow ScrollView to intercept touch events.
                        v.getParent().requestDisallowInterceptTouchEvent(false);
                        break;
                }

                // Handle ListView touch events.
                v.onTouchEvent(event);
                return true;
            }
        });

        // Check if the count of items which should be displayed on top pager is non-zero
        if (topAdapter.getCount() > 0) {

            // Center the middle item view in coverFlow
            carousel.setSelection(topAdapter.getCount() / 2);
        }
        carousel.setDragListener(new Carousel.CarouselDragListener() {
            @Override
            public void onStartDrag(int position) {
                CarouselLayoutItem itemView = (CarouselLayoutItem) topAdapter.getViewAtIndex(position);
                if (itemView != null) {
                    ClipData data = ClipData.newPlainText("", "");
                    View.DragShadowBuilder mShadow = new View.DragShadowBuilder(itemView);
                    itemView.startDrag(data, mShadow, itemView, 0);
                    draggingIndex = position;
                    currentDragging = DRAGGING_ITEM.TOP_PANEL_ITEM;
                }
            }
        });
        carousel.setOnDragListener(this);

        draggingIndex = -1;
    }

    /**
     * Display custom walkthough screens
     */
    private void startWalkthrough() {
        // Walkthrough View for HOLD & DRAG
        final LinearLayout wtHoldDrag = (LinearLayout) this.view.findViewById(R.id.ll_wt_hold_drag);

        wtHoldDrag.setAlpha(1);
        wtHoldDrag.setVisibility(View.VISIBLE);
        wtHoldDrag.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                wtHoldDrag.animate().setDuration(300).alpha(0).setListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {
                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        // Hide walkthrough for HOLD & DRAG
                        wtHoldDrag.setVisibility(View.GONE);

                        localStorage.setFlagValue(LocalStorage.WALKED_THROUGH_PREFERENCES, true);
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

        if (v.getId() == R.id.tv_done_button) {

            // when user taps on "done" button, update preference
            // and navigate to next page
            localStorage.setFlagValue(LocalStorage.TUTORIAL_VIEWED, true);

            // Save glance preference settings to App Preferences
            localStorage.saveGlancePreferenceSetting(LocalStorage.LEFT_PANEL_SETTINGS, leftItems);
            localStorage.saveGlancePreferenceSetting(LocalStorage.RIGHT_PANEL_SETTINGS, rightItems);

            // Start synchronization service for user profile
            localStorage.setFlagValue(LocalStorage.SYNCHRONIZED_PREFS, false);
            Intent intent = new Intent(getActivity(), ProfileSyncService.class);
            getActivity().startService(intent);

            // check if this page is launched from onboarding
            if (getActivity() instanceof OnboardingActivity) {

                // if this page is displayed after tutorial pages, navigate to home screen
                OnboardingActivity delegate = (OnboardingActivity) getActivity();
                delegate.navigateToHome();

            } else if (getActivity() instanceof HomeSettingsActivity) {

                // if this page is displayed from General Settings page, go back to previous page
                HomeSettingsActivity delegate = (HomeSettingsActivity) getActivity();
                delegate.needHomeScreenUpdate = true;
                delegate.onBackPressed();
            }

        } else if (v.getId() == R.id.iv_back_button) {
            baseActivity.onBackPressed();
        }
    }

    private boolean isDragging = false;

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            mLastMotionX = event.getX();
            mLastMotionY = event.getY();
        } else if (event.getAction() == MotionEvent.ACTION_MOVE) {
            float x = event.getX();
            float y = event.getY();
            final int xDiff = (int) Math.abs(x - mLastMotionX);
            final int yDiff = (int) Math.abs(y - mLastMotionY);

            final int touchSlop = mTouchSlop;
            final boolean xMoved = xDiff > touchSlop;

            if (xMoved && xDiff > yDiff) {

                // Check if can start dragging
                if (!isDragging) {
                    isDragging = true;

                    if (v.getParent() == leftPanel) {
                        currentDragging = DRAGGING_ITEM.LEFT_PANEL_ITEM;
                    } else if (v.getParent() == rightPanel) {
                        currentDragging = DRAGGING_ITEM.RIGHT_PANEL_ITEM;
                    }
                    draggingIndex = (int) v.getTag();
                    ClipData data = ClipData.newPlainText("", "");
                    View.DragShadowBuilder mShadow = new View.DragShadowBuilder(v);
                    v.startDrag(data, mShadow, v, 0);
                }
            }
        } else {
            isDragging = false;
        }
        return true;
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public boolean onDrag(View v, DragEvent event) {

        // Store the action type for the incoming event
        final int action = event.getAction();

        // Handles each of the expected events
        switch (action) {
            case DragEvent.ACTION_DRAG_STARTED:

                // Returns true to indicate that the View can accept the
                // dragged data.
                return true;

            case DragEvent.ACTION_DRAG_ENTERED:

                // Apply a gray tint to the View
                if (v == carousel && currentDragging == DRAGGING_ITEM.TOP_PANEL_ITEM)
                    return true;

                v.setBackground(getResources().getDrawable(R.drawable.panel_drag_entered_bg));

                // Invalidate the view to force a redraw in the new tint
                v.invalidate();

                return true;

            case DragEvent.ACTION_DRAG_LOCATION:

                // Ignore the event
                return true;

            case DragEvent.ACTION_DRAG_EXITED:

                // Re-sets the color tint to yellow
                v.setBackground(getResources().getDrawable(R.drawable.panel_normal_bg));

                // Invalidate the view to force a redraw in the new tint
                v.invalidate();

                return true;

            case DragEvent.ACTION_DROP:

                // Turns off any color tints
                v.setBackground(getResources().getDrawable(R.drawable.panel_normal_bg));

                if (currentDragging == DRAGGING_ITEM.TOP_PANEL_ITEM) {

                    // Get the item by index from top items
                    String dropItem = topItems.get(draggingIndex);

                    // Check if dropped on Left Panel or Right Panel
                    if (v == leftPanel) {

                        // If dropped on left panel
                        leftItems.add(dropItem);
                        leftAdapter.notifyDataSetChanged();

                        topItems.remove(draggingIndex);
                    } else if (v == rightPanel) {

                        // If dropped on right panel
                        rightItems.add(dropItem);
                        rightAdapter.notifyDataSetChanged();

                        topItems.remove(draggingIndex);
                    }

                    topAdapter.notifyDataSetChanged();

                } else if (currentDragging == DRAGGING_ITEM.LEFT_PANEL_ITEM) {

                    // If dragging left panel item view,
                    // check if dropped on left or right panel
                    if (v == rightPanel) {

                        // If dropped on right panel

                        // get dropped item from left items, and remove
                        String droppedItem = leftItems.get(draggingIndex);
                        leftItems.remove(draggingIndex);

                        // calculate item height of left panel in order to get actual insert position
                        int itemHeight = getResources().getDimensionPixelSize(R.dimen.panel_item_height);
                        float y0 = event.getY() - rightPanel.getPaddingTop() + itemHeight / 2;

                        // this drop position can be used as insert position of dragging item
                        int dropPosition = (int) (y0 / getResources().getDimensionPixelSize(R.dimen.panel_item_height));

                        if (dropPosition == 0) {

                            // if drop position is 0, than pop dragging item and insert to top
                            rightItems.add(0, droppedItem);

                        } else if (dropPosition > rightAdapter.getCount() - 1) {

                            // if dropped to the bottom of the table, than pop dragging item and insert to bottom
                            rightItems.add(droppedItem);

                        } else {

                            // if dropped in the middle of the panel, push dragging item between items
                            rightItems.add(dropPosition, droppedItem);

                        }
                        leftAdapter.notifyDataSetChanged();
                        rightAdapter.notifyDataSetChanged();

                    } else if (v == leftPanel) {

                        // -- If dropped on left panel --
                        // get dropped item from left items
                        String droppedItem = leftItems.get(draggingIndex);

                        // calculate item height of left panel in order to get actual insert position
                        int itemHeight = leftAdapter.getViewAtIndex(0).getMeasuredHeight();
                        float y0 = event.getY() - leftPanel.getPaddingTop() + itemHeight / 2;

                        // this drop position can be used as insert position of dragging item
                        int dropPosition = (int) (y0 / getResources().getDimensionPixelSize(R.dimen.panel_item_height));

                        if (dropPosition == 0) {

                            // if drop position is 0, than pop dragging item and insert to top
                            leftItems.remove(draggingIndex);
                            leftItems.add(0, droppedItem);

                        } else if (dropPosition >= leftAdapter.getCount() - 1) {

                            // if dropped to the bottom of the table, than pop dragging item and insert to bottom
                            leftItems.remove(draggingIndex);
                            leftItems.add(droppedItem);

                        } else {

                            // if dropped in the middle of the panel, push dragging item between items
                            leftItems.remove(draggingIndex);
                            int insertPosition = 0;
                            if (draggingIndex > dropPosition) {
                                insertPosition = dropPosition;
                            } else if (draggingIndex < dropPosition) {
                                insertPosition = dropPosition - 1;
                            }
                            leftItems.add(insertPosition, droppedItem);

                        }
                        leftAdapter.notifyDataSetChanged();

                    } else if (v == carousel) {

                        // if dropped on top carousel, remove dragging item and add to top
                        topItems.add(leftItems.get(draggingIndex));
                        leftItems.remove(draggingIndex);
                        leftAdapter.notifyDataSetChanged();
                        topAdapter.notifyDataSetChanged();
                        if (topAdapter.getCount() > 0) {
                            carousel.setSelection(topAdapter.getCount() - 1);
                        }
                    }

                } else if (currentDragging == DRAGGING_ITEM.RIGHT_PANEL_ITEM) {

                    // If dragging right panel item view,
                    // check if dropped on right or left panel
                    if (v == leftPanel) {

                        // If dropped on left panel

                        // get dropped item from left items, and remove
                        String droppedItem = rightItems.get(draggingIndex);
                        rightItems.remove(draggingIndex);

                        // calculate item height of left panel in order to get actual insert position
                        int itemHeight = getResources().getDimensionPixelSize(R.dimen.panel_item_height);
                        float y0 = event.getY() - leftPanel.getPaddingTop() + itemHeight / 2;

                        // this drop position can be used as insert position of dragging item
                        int dropPosition = (int) (y0 / getResources().getDimensionPixelSize(R.dimen.panel_item_height));

                        if (dropPosition == 0) {

                            // if drop position is 0, than pop dragging item and insert to top
                            leftItems.add(0, droppedItem);

                        } else if (dropPosition > leftAdapter.getCount() - 1) {

                            // if dropped to the bottom of the table, than pop dragging item and insert to bottom
                            leftItems.add(droppedItem);

                        } else {

                            // if dropped in the middle of the panel, push dragging item between items
                            leftItems.add(dropPosition, droppedItem);

                        }
                        leftAdapter.notifyDataSetChanged();
                        rightAdapter.notifyDataSetChanged();

                    } else if (v == rightPanel) {

                        // -- If dropped on right panel --
                        // get dropped item from left items
                        String droppedItem = rightItems.get(draggingIndex);

                        // calculate item height of left panel in order to get actual insert position
                        int itemHeight = rightAdapter.getViewAtIndex(0).getMeasuredHeight();
                        float y0 = event.getY() - rightPanel.getPaddingTop() + itemHeight / 2;

                        // this drop position can be used as insert position of dragging item
                        int dropPosition = (int) (y0 / getResources().getDimensionPixelSize(R.dimen.panel_item_height));

                        if (dropPosition == 0) {

                            // if drop position is 0, than pop dragging item and insert to top
                            rightItems.remove(draggingIndex);
                            rightItems.add(0, droppedItem);

                        } else if (dropPosition >= rightAdapter.getCount() - 1) {

                            // if dropped to the bottom of the table, than pop dragging item and insert to bottom
                            rightItems.remove(draggingIndex);
                            rightItems.add(droppedItem);

                        } else {

                            // if dropped in the middle of the panel, push dragging item between items
                            rightItems.remove(draggingIndex);
                            int insertPosition = 0;
                            if (draggingIndex > dropPosition) {
                                insertPosition = dropPosition;
                            } else if (draggingIndex < dropPosition) {
                                insertPosition = dropPosition - 1;
                            }
                            rightItems.add(insertPosition, droppedItem);

                        }
                        rightAdapter.notifyDataSetChanged();

                    } else if (v == carousel) {

                        // if dropped on top carousel, remove dragging item and add to top
                        topItems.add(rightItems.get(draggingIndex));
                        rightItems.remove(draggingIndex);
                        leftAdapter.notifyDataSetChanged();
                        topAdapter.notifyDataSetChanged();
                        if (topAdapter.getCount() > 0) {
                            carousel.setSelection(topAdapter.getCount() - 1);
                        }
                    }
                }

                adjustPanelHeight();

                // Invalidates the view to force a redraw
                v.invalidate();

                return true;

            case DragEvent.ACTION_DRAG_ENDED:

                // Turns off any color tinting
                v.setBackground(getResources().getDrawable(R.drawable.panel_normal_bg));

                // Invalidates the view to force a redraw
                v.invalidate();

                return true;

            default:
                break;
        }

        return false;
    }

    /**
     * Adjust panel height to show all item views
     */
    private void adjustPanelHeight() {
        int minHeight = getResources().getDimensionPixelSize(R.dimen.preference_panel_height);
        CommonUtils.getInstance().setListViewHeightBasedOnItems(leftPanel, minHeight);
        CommonUtils.getInstance().setListViewHeightBasedOnItems(rightPanel, minHeight);
    }

    private class CategoryAdapter extends BaseAdapter {

        private LayoutInflater inflater;
        private Hashtable<Integer, View> itemViews;
        private ArrayList<String> items;
        private Context context;

        public CategoryAdapter(Context context, ArrayList<String> items) {
            this.context = context;
            this.inflater = LayoutInflater.from(context);
            itemViews = new Hashtable<>();
            this.items = items;
            if (this.items == null) {
                this.items = new ArrayList<>();
            }
        }

        @Override
        public int getCount() {
            return items.size();
        }

        @Override
        public Object getItem(int position) {
            return items.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {

            final CarouselLayoutItem itemView;
            if (convertView == null) {
                itemView = (CarouselLayoutItem) inflater.inflate(R.layout.preferences_pager_item, parent, false);
            } else {
                itemView = (CarouselLayoutItem) convertView;
            }
            itemView.setTag(String.valueOf(position));

            VizoCategory category = getCategoryFromId(items.get(position));

            if (category != null) {
                ImageView itemImage = (ImageView) itemView.findViewById(R.id.iv_category_image);
                int resourceId = categoryResIds.get(category.categoryId);
                itemImage.setBackgroundResource(resourceId);

                TextView itemName = (TextView) itemView.findViewById(R.id.tv_category_name);
                itemName.setText(category.getLocalizedName(getActivity()));
            }

            itemViews.put(position, itemView);
            return itemView;
        }

        public View getViewAtIndex(int index) {
            return itemViews.get(index);
        }
    }

    private class PanelAdapter extends BaseAdapter {

        private ArrayList<String> items;
        private LayoutInflater inflater;
        private Hashtable<Integer, View> itemViews;

        public PanelAdapter(Context context, ArrayList<String> items) {
            this.inflater = LayoutInflater.from(context);
            this.items = items;
            if (items == null) {
                this.items = new ArrayList<>();
            }
            itemViews = new Hashtable<>();
        }

        @Override
        public int getCount() {
            return items.size();
        }

        @Override
        public String getItem(int position) {
            return items.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = inflater.inflate(R.layout.preferences_panel_item, parent, false);
            }
            ImageView categoryImage = (ImageView) convertView.findViewById(R.id.iv_item_image);
            TextView categoryName = (TextView) convertView.findViewById(R.id.tv_item_title);

            VizoCategory category = getCategoryFromId(items.get(position));
            if (category != null) {
                categoryImage.setImageResource(categoryResIds.get(category.categoryId));
                categoryName.setText(category.getLocalizedName(getActivity()));
            }

            itemViews.put(position, convertView);
            convertView.setTag(position);
            convertView.setOnTouchListener(PreferencesFragment.this);
            return convertView;
        }

        public View getViewAtIndex(int index) {
            return itemViews.get(index);
        }
    }

    private static VizoCategory getCategoryFromId(String categoryId) {
        for (VizoCategory category : allCategories) {
            if (category.categoryId.equals(categoryId))
                return category;
        }
        return null;
    }

}
