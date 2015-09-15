package com.vizo.news.ui;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.Button;

/**
 * Custom class for Vizo Style Button
 * <p/>
 * Created by nine3_marks on 6/9/2015.
 */
public class VizoButton extends Button {

    public VizoButton(Context context) {
        super(context);
        setup();
    }

    public VizoButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        setup();
    }

    public VizoButton(Context context, AttributeSet attrs, int defStyleAttr) {
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
