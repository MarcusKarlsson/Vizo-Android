package com.vizo.news.ui;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.TextView;

/**
 * Custom class for Vizo style TextView
 * <p/>
 * Created by nine3_marks on 6/8/2015.
 */
public class VizoTextView extends TextView {

    public VizoTextView(Context context) {
        super(context);
        setup();
    }

    public VizoTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setup();
    }

    public VizoTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setup();
    }

    /**
     * Setup Vizo TextView
     */
    private void setup() {
        Typeface globerFont = Typeface
                .createFromAsset(getContext().getResources().getAssets(), "fonts/GloberSemiBold.otf");
        setTypeface(globerFont);
    }

}
