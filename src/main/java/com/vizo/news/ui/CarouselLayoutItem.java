package com.vizo.news.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

/**
 * Created by MarksUser on 4/9/2015.
 *
 * @author nine3_marks
 */
public class CarouselLayoutItem extends RelativeLayout {

    public CarouselLayoutItem(Context context) {
        super(context);
        setSelected(false);
    }

    public CarouselLayoutItem(Context context, AttributeSet attrs) {
        super(context, attrs);
        setSelected(false);
    }

    public CarouselLayoutItem(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setSelected(false);
    }

    @Override
    public void setSelected(boolean selected) {
        super.setSelected(selected);

        if (selected) {
            setAlpha(1.0f);
        } else {
            setAlpha(0.3f);
        }
    }
}
