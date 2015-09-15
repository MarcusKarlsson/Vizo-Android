package com.vizo.news.fragments.full_glance;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.ViewAnimator;

import java.util.ArrayList;

import com.vizo.news.R;
import com.vizo.news.activities.GlanceFullActivity;
import com.vizo.news.api.APIClient;
import com.vizo.news.api.domain.ArticleSourcesResponse;
import com.vizo.news.domain.VizoGlance;
import com.vizo.news.fragments.base.BaseFragment;
import com.vizo.news.ui.VizoDynamicImageView;
import com.vizo.news.utils.CommonUtils;
import com.vizo.news.utils.LocalStorage;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Custom Fragment class for Articles Sources View
 *
 * @author nine3_marks
 */
public class ArticleSourcesFragment extends BaseFragment implements View.OnClickListener {

    private GlanceFullActivity delegate;
    private VizoGlance glanceInfo;

    // members referring view elements
    private View view;
    private ListView lvSources;
    private ViewAnimator sourceListAnimator;

    /**
     * custom adapter for sources list
     */
    private SourcesListAdapter adapter;

    public static ArticleSourcesFragment newInstance(VizoGlance glance) {
        ArticleSourcesFragment sourcesFragment = new ArticleSourcesFragment();
        Bundle bundle = new Bundle();
        bundle.putParcelable("glance", glance);
        sourcesFragment.setArguments(bundle);
        return sourcesFragment;
    }

    public ArticleSourcesFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.glanceInfo = getArguments().getParcelable("glance");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        this.view = inflater.inflate(R.layout.fragment_article_sources, container, false);
        return this.view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        delegate = (GlanceFullActivity) getActivity();

        // initialize
        initViewAndClassMembers();

        sourceListAnimator.setDisplayedChild(0);
        APIClient.getInstance(getActivity()).getApiService().getArticleSources(glanceInfo.glanceId, mCallback);
    }

    /**
     * Initialize view elements and class members
     */
    private void initViewAndClassMembers() {

        // map view elements to class members
        lvSources = (ListView) view.findViewById(R.id.lv_article_sources);
        sourceListAnimator = (ViewAnimator) view.findViewById(R.id.sources_list_animator);

        // Map view elements to event handlers
        view.findViewById(R.id.tv_save_image_button).setOnClickListener(this);
        view.findViewById(R.id.tv_done_button).setOnClickListener(this);

        lvSources.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ArticleSourcesResponse.VizoArticleSource source = adapter.getItem(position);
                String address = source.address;
                if (address != null && address.length() > 0) {
                    if (!address.startsWith("http://")) {
                        address = String.format("http://%s", address);
                    }
                    if (source.must_translate == 1
                            && localStorage.getFlagValue(LocalStorage.AUTO_TRANSLATE)) {
                        address = String.format(
                                "http://translate.google.com/translate?js=n&sl=auto&tl=he&u=%s",
                                address);
                    }
                    baseActivity.showFragment(R.id.fragment_container,
                            BrowserFragment.newInstance(address), true);
                } else {
                    CommonUtils.getInstance().showMessage("Incorrect source link");
                }
            }
        });
    }

    private Callback<ArticleSourcesResponse> mCallback = new Callback<ArticleSourcesResponse>() {
        @Override
        public void success(ArticleSourcesResponse vizoArticleSources, Response response) {
            sourceListAnimator.setDisplayedChild(1);
            adapter = new SourcesListAdapter(delegate, vizoArticleSources);
            lvSources.setAdapter(adapter);
        }

        @Override
        public void failure(RetrofitError error) {
            progress.dismiss();
        }
    };

    @Override
    public void onClick(View v) {

        if (v.getId() == R.id.tv_done_button) {

            delegate.onBackPressed();

        } else if (v.getId() == R.id.tv_save_image_button) {

            if (delegate.saveImage()) {
                CommonUtils.getInstance()
                        .showMessage("Glance photo has been saved to the phone");
            } else {
                CommonUtils.getInstance().showMessage("Failed to save a photo");
            }
        }
    }

    /**
     * Custom adapter class for Article Sources List
     */
    private static class SourcesListAdapter extends BaseAdapter {

        private Context context;
        private LayoutInflater inflater;
        private ArrayList<ArticleSourcesResponse.VizoArticleSource> items;

        public SourcesListAdapter(Context context, ArticleSourcesResponse response) {
            this.context = context;
            this.inflater = LayoutInflater.from(context);
            this.items = response;
        }

        @Override
        public int getCount() {
            return items.size();
        }

        @Override
        public ArticleSourcesResponse.VizoArticleSource getItem(int position) {
            return items.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            if (convertView == null) {
                convertView = inflater.inflate(R.layout.listitem_article_sources, parent, false);
            }

            VizoDynamicImageView ivSourceLogo = (VizoDynamicImageView) convertView.findViewById(R.id.iv_source_logo);
            ivSourceLogo.loadImage(items.get(position).logo_url);

            return convertView;
        }
    }
}
