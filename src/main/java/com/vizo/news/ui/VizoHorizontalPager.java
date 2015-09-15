package com.vizo.news.ui;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;

/**
 * Custom View Pager class for glances pager
 * <p/>
 * Created by MarksUser on 3/27/2015.
 */
public class VizoHorizontalPager extends ViewPager {

    private float mStartDragX;
    private float swipeOutDistance;
    private OnSwipeOutListener listener;
    private GestureDetector mGestureDetector;

    public VizoHorizontalPager(Context context) {
        super(context);
        init();
    }

    public VizoHorizontalPager(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        mGestureDetector = new GestureDetector(getContext(), new XScrollDetector());
        swipeOutDistance = 150;
    }

    public void setOnSwipeOutListener(OnSwipeOutListener listener) {
        this.listener = listener;
    }

    public void setSwipeOutDistance(float swipeOutDistance) {
        this.swipeOutDistance = swipeOutDistance;
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        float x = ev.getX();
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mStartDragX = x;
                break;
            case MotionEvent.ACTION_MOVE:
                if (listener != null) {
                    if (mStartDragX < x - swipeOutDistance && getCurrentItem() == 0) {
                        listener.onSwipeOutAtStart();
                    } else if (mStartDragX > x + swipeOutDistance && getCurrentItem() == getAdapter().getCount() - 1) {
                        listener.onSwipeOutAtEnd();
                    }
                }
                break;
        }
        try {
            return super.onTouchEvent(ev);
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        onTouchEvent(ev);
        return mGestureDetector.onTouchEvent(ev);
    }

    public interface OnSwipeOutListener {

        void onSwipeOutAtStart();

        void onSwipeOutAtEnd();
    }

    public class XScrollDetector extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            return (Math.abs(distanceX) > Math.abs(distanceY));
        }
    }
}
