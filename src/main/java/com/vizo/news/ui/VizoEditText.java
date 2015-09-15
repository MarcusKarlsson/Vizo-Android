package com.vizo.news.ui;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.EditText;

/**
 * Custom class for Vizo Style EditText
 * <p/>
 * Created by nine3_marks on 6/9/2015.
 */
public class VizoEditText extends EditText {

    public VizoEditText(Context context) {
        super(context);
        setup();
    }

    public VizoEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        setup();
    }

    public VizoEditText(Context context, AttributeSet attrs, int defStyleAttr) {
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
