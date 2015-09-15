package com.vizo.news.fragments.full_glance;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.vizo.news.R;
import com.vizo.news.fragments.base.BaseFragment;

/**
 * Created by MarksUser on 3/31/2015.
 *
 * @author nin3_marks
 */
public class BrowserFragment extends BaseFragment implements View.OnClickListener {

    private String url;

    private View view;
    private WebView webView;

    public static BrowserFragment newInstance(String url) {
        BrowserFragment fragment = new BrowserFragment();
        Bundle bundle = new Bundle();
        bundle.putString("url", url);
        fragment.setArguments(bundle);
        return fragment;
    }

    public BrowserFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.url = getArguments().getString("url");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        this.view = inflater.inflate(R.layout.fragment_app_browser, container, false);
        webView = (WebView) this.view.findViewById(R.id.wv_app_browser);
        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                if (newProgress == 100) {
                    progress.dismiss();
                }
            }
        });
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return true;
            }
        });
        webView.getSettings().setJavaScriptEnabled(true);
        webView.loadUrl(url);
        progress.show();

        this.view.findViewById(R.id.tv_done_button).setOnClickListener(this);
        return this.view;
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.tv_done_button) {
            baseActivity.onBackPressed();
        }
    }
}
