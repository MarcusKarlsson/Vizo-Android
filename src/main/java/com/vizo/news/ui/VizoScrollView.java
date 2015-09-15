package com.vizo.news.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ScrollView;

import com.vizo.news.R;

/**
 * Created by MarksUser on 3/31/2015.
 * <p/>
 * Custom ScrollView with Scroll Changing Listener
 */
public class VizoScrollView extends ScrollView {

    private VizoScrollViewListener scrollViewListener = null;

    public VizoScrollView(Context context) {
        super(context);
    }

    public VizoScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public VizoScrollView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setScrollViewListener(VizoScrollViewListener scrollViewListener) {
        this.scrollViewListener = scrollViewListener;
    }

    @Override
    protected void onScrollChanged(int x, int y, int oldX, int oldY) {
        super.onScrollChanged(x, y, oldX, oldY);

        if (scrollViewListener != null) {
            scrollViewListener.onScrollChanged(this, x, y, oldX, oldY);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {

        if (ev.getAction() == MotionEvent.ACTION_UP) {

            // Check current scroll position to make paging work
            int currentPosition = getScrollY();
            float halfHeight = getMeasuredHeight() / 4;

            // if scrolled above half line of scrollview
            boolean isAboveHalf = currentPosition >= halfHeight;

            if (isAboveHalf) {

                // for paging,
                // if scrolled above the half line, the scrollView should reflect the full state
                // this works but should be dynamic
                smoothScrollTo(0, getResources().getDimensionPixelSize(R.dimen.vizo_scrollview_page_size));
                if (scrollViewListener != null) {
                    scrollViewListener.onScrolledToFull(this);
                }

            } else {

                // for paging,
                // if scrolled below the half line, the scrollView should reflect the initial state
                smoothScrollTo(0, 0);

                if (scrollViewListener != null) {
                    scrollViewListener.onScrolledToPreview(this);
                }
            }
            return true;
        }
        return super.onTouchEvent(ev);
    }

    /**
     * Custom interface which handles scrolling event of VizoScrollView
     */
    public interface VizoScrollViewListener {

        void onScrollChanged(VizoScrollView scrollView, int x, int y, int oldX, int oldY);

        void onScrolledToFull(VizoScrollView scrollView);

        void onScrolledToPreview(VizoScrollView scrollView);
    }

}
