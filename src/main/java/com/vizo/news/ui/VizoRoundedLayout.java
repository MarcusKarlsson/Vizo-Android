package com.vizo.news.ui;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

import com.vizo.news.R;

/**
 * This class rounds subviews.
 * <p/>
 * Define dimension for corner radius in dimens.xml.
 * <dimen name="round_layout_corner_radius">10dp</dimen>
 * <p/>
 * Created by nine3_marks on 5/30/2015.
 */
public class VizoRoundedLayout extends RelativeLayout {

    private int RADIUS_IN_PIXELS = getResources()
            .getDimensionPixelSize(R.dimen.round_layout_corner_radius);

    public VizoRoundedLayout(Context context) {
        super(context);
        init(null);
    }

    public VizoRoundedLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public VizoRoundedLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    /**
     * Initialize Clip Layout
     */
    private void init(AttributeSet attrs) {

        if (attrs != null) {

            TypedArray a = getContext().obtainStyledAttributes(attrs,
                    R.styleable.VizoRoundedLayout);
            RADIUS_IN_PIXELS = (int) a.getDimension(R.styleable.VizoRoundedLayout_corner_radius,
                    RADIUS_IN_PIXELS);

            // Recycle
            a.recycle();
        }

        setWillNotDraw(false);
    }

    public void setRadius(int radius) {
        RADIUS_IN_PIXELS = radius;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        Path path = new Path();
        path.addRoundRect(new RectF(canvas.getClipBounds()),
                RADIUS_IN_PIXELS, RADIUS_IN_PIXELS, Path.Direction.CW);
        canvas.clipPath(path);
    }
}
