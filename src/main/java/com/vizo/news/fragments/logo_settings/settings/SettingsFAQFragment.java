package com.vizo.news.fragments.logo_settings.settings;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.vizo.news.R;
import com.vizo.news.VizoApplication;
import com.vizo.news.activities.HomeSettingsActivity;
import com.vizo.news.domain.VizoFAQ;
import com.vizo.news.fragments.base.BaseFragment;
import com.vizo.news.ui.AnimatedExpandableListView;

/**
 * Custom fragment class for FAQ Settings Page
 * <p/>
 * Created by fbuibish on 4/17/15.
 * <p/>
 * Modified by nine3_marks
 */
public class SettingsFAQFragment extends BaseFragment implements View.OnClickListener {

    // Members which hold the instance of view elements
    private View view;
    private AnimatedExpandableListView faqList;
    private TextView tvTitle;

    private int categoryIndex;

    public static SettingsFAQFragment newInstance(int categoryIndex) {
        SettingsFAQFragment fragment = new SettingsFAQFragment();
        Bundle bundle = new Bundle();
        bundle.putInt("category_index", categoryIndex);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        categoryIndex = getArguments().getInt("category_index");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        this.view = inflater.inflate(R.layout.fragment_settings_help_faq, container, false);
        return this.view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // setup fragment
        initViewAndClassMembers();
    }

    /**
     * Initialize view elements and class members
     */
    private void initViewAndClassMembers() {
        // map view elements to class members
        faqList = (AnimatedExpandableListView) view.findViewById(R.id.lv_faq_list);
        tvTitle = (TextView) view.findViewById(R.id.tv_title);

        // map view elements to event handlers
        view.findViewById(R.id.iv_back_button).setOnClickListener(this);

        // Get application faq resource
        VizoApplication app = (VizoApplication) baseActivity.getApplication();

        // Check if this page is launched from logo settings
        if (categoryIndex == -1) {

            // When category index is -1, the page will display FAQ categories
            faqList.setAdapter(new FAQCategoryAdapter(app.getVizoFAQs()));

            faqList.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {
                @Override
                public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id) {

                    // Navigate to full questions list
                    // when user taps category item
                    baseActivity.showFragment(R.id.fl_fragment_container,
                            SettingsFAQFragment.newInstance(groupPosition), true);
                    return true;
                }
            });

        } else {

            // If this page is launched after selection of category
            // the page will display questions with answers
            VizoFAQ category = app.getVizoFAQs().get(categoryIndex);
            tvTitle.setText(category.title);

            final FAQAdapter adapter = new FAQAdapter(category);
            faqList.setAdapter(adapter);

            faqList.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {
                @Override
                public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id) {

                    // We call collapseGroupWithAnimation(int) and
                    // expandGroupWithAnimation(int) to animate group
                    // expansion/collapse.
                    if (faqList.isGroupExpanded(groupPosition)) {
                        faqList.collapseGroupWithAnimation(groupPosition);
                    } else {
                        faqList.expandGroupWithAnimation(groupPosition);
                    }
                    return true;
                }
            });

            faqList.setOnGroupCollapseListener(new ExpandableListView.OnGroupCollapseListener() {
                @Override
                public void onGroupCollapse(int groupPosition) {
                    View vi = adapter.getGroupViewAtIndex(groupPosition);

                    TextView tvQuestion = (TextView) vi.findViewById(R.id.tv_question);
                    tvQuestion.setMaxLines(1);
                }
            });

            faqList.setOnGroupExpandListener(new ExpandableListView.OnGroupExpandListener() {
                @Override
                public void onGroupExpand(int groupPosition) {
                    View vi = adapter.getGroupViewAtIndex(groupPosition);

                    TextView tvQuestion = (TextView) vi.findViewById(R.id.tv_question);
                    tvQuestion.setMaxLines(5);
                }
            });

        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.iv_back_button) {

            baseActivity.onBackPressed();
        }
    }

    /**
     * Custom adapter for faq categories
     */
    private class FAQCategoryAdapter extends AnimatedExpandableListView.AnimatedExpandableListAdapter {

        private ArrayList<VizoFAQ> items;

        public FAQCategoryAdapter(ArrayList<VizoFAQ> items) {
            this.items = items;
            if (items == null) {
                this.items = new ArrayList<>();
            }
        }

        @Override
        public int getGroupCount() {
            return items.size();
        }

        @Override
        public VizoFAQ getGroup(int groupPosition) {
            return items.get(groupPosition);
        }

        @Override
        public String getChild(int groupPosition, int childPosition) {
            return null;
        }

        @Override
        public long getGroupId(int groupPosition) {
            return groupPosition;
        }

        @Override
        public long getChildId(int groupPosition, int childPosition) {
            return childPosition;
        }

        @Override
        public boolean hasStableIds() {
            return false;
        }

        @Override
        public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(baseActivity)
                        .inflate(R.layout.faq_group_item, parent, false);
            }

            TextView tvQuestion = (TextView) convertView.findViewById(R.id.tv_question);
            tvQuestion.setText(getGroup(groupPosition).title);

            return convertView;
        }

        @Override
        public View getRealChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
            return null;
        }

        @Override
        public int getRealChildrenCount(int groupPosition) {
            return 0;
        }

        @Override
        public boolean isChildSelectable(int groupPosition, int childPosition) {
            return false;
        }
    }

    /**
     * Custom adapter class for Questions Adapter
     */
    private class FAQAdapter extends AnimatedExpandableListView.AnimatedExpandableListAdapter {

        private ArrayList<VizoFAQ.VizoQuestion> items;
        private Map<Integer, View> groupViews;

        public FAQAdapter(VizoFAQ category) {
            this.items = new ArrayList<>();
            for (VizoFAQ.VizoQuestion item : category.questions) {
                this.items.add(item);
            }
            groupViews = new HashMap<>();
        }

        @Override
        public int getGroupCount() {
            return items.size();
        }

        @Override
        public VizoFAQ.VizoQuestion getGroup(int groupPosition) {
            return items.get(groupPosition);
        }

        @Override
        public String getChild(int groupPosition, int childPosition) {
            return items.get(groupPosition).answer;
        }

        @Override
        public long getGroupId(int groupPosition) {
            return groupPosition;
        }

        @Override
        public long getChildId(int groupPosition, int childPosition) {
            return childPosition;
        }

        @Override
        public boolean hasStableIds() {
            return false;
        }

        @Override
        public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {

            convertView = groupViews.get(groupPosition);

            if (convertView == null) {
                convertView = LayoutInflater.from(baseActivity).inflate(R.layout.faq_group_item, parent, false);

                TextView tvQuestion = (TextView) convertView.findViewById(R.id.tv_question);
                tvQuestion.setText(getGroup(groupPosition).question);

                groupViews.put(groupPosition, convertView);
            }

            return convertView;
        }

        public View getGroupViewAtIndex(int groupPosition) {
            return groupViews.get(groupPosition);
        }

        @Override
        public View getRealChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(baseActivity).inflate(R.layout.faq_child_item, parent, false);
            }

            TextView tvAnswer = (TextView) convertView.findViewById(R.id.tv_answer);
            tvAnswer.setText(getChild(groupPosition, childPosition));

            return convertView;
        }

        @Override
        public int getRealChildrenCount(int groupPosition) {
            return 1;
        }

        @Override
        public boolean isChildSelectable(int groupPosition, int childPosition) {
            return false;
        }
    }

}
