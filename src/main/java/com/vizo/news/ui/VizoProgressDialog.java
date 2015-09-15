package com.vizo.news.ui;

import android.app.Dialog;
import android.content.Context;
import android.view.Gravity;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import com.vizo.news.R;

/**
 * Custom Progress Dialog
 * <p/>
 * Created by nine3_marks on 3/31/2015.
 */
public class VizoProgressDialog extends Dialog {

    public VizoProgressDialog(Context context) {
        super(context, R.style.TransparentProgressDialog);

        WindowManager.LayoutParams wlmp = getWindow().getAttributes();
        wlmp.gravity = Gravity.CENTER_HORIZONTAL;
        getWindow().setAttributes(wlmp);
        getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        setCanceledOnTouchOutside(false);
        setOnCancelListener(null);
        LinearLayout layout = new LinearLayout(context);
        layout.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                context.getResources().getDimensionPixelSize(
                        R.dimen.progress_dialog_width), context.getResources()
                .getDimensionPixelSize(R.dimen.progress_dialog_height));

        ProgressBar progress = new ProgressBar(context);
        progress.setIndeterminate(true);
        layout.addView(progress, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        layout.setGravity(Gravity.CENTER);
        addContentView(layout, params);
    }

    @Override
    public void show() {
        super.show();
    }
}
