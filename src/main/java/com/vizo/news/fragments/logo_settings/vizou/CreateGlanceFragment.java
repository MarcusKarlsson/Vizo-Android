package com.vizo.news.fragments.logo_settings.vizou;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.vizo.news.R;
import com.vizo.news.activities.VizoUActivity;
import com.vizo.news.fragments.base.BaseFragment;
import com.vizo.news.fragments.logo_settings.settings.SettingsFAQFragment;
import com.vizo.news.ui.VizoButton;
import com.vizo.news.ui.VizoTextView;

/**
 * Custom fragment class for Create Glance page
 * <p/>
 * Created by nine3_marks on 6/22/2015.
 */
public class CreateGlanceFragment extends BaseFragment implements View.OnClickListener {

    /**
     * The member which holds the instance of delegate activity
     */
    private VizoUActivity delegate;

    // Members which refer view elements
    private View view;
    private VizoTextView tvAddPhoto;
    private ImageView ivGlanceImage;
    private VizoTextView tvDescription;
    private VizoTextView tvCounter;
    private VizoButton btnPreview;
    private View categoryFilterView;

    private String language;

    // FAQ category index for VizoU
    private final int CATEGORY_VIZOU = 3;

    public static CreateGlanceFragment newInstance() {
        return new CreateGlanceFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        this.view = inflater.inflate(R.layout.fragment_user_glance_create, container, false);
        return this.view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        language = localStorage.loadAppLanguage();

        delegate = (VizoUActivity) getActivity();

        // setup fragment
        initViewAndClassMembers();
    }

    /**
     * Initialize view elements and class members
     */
    private void initViewAndClassMembers() {

        // Map view elements to class members
        ivGlanceImage = (ImageView) view.findViewById(R.id.iv_glance_image);
        tvAddPhoto = (VizoTextView) view.findViewById(R.id.tv_add_photo);
        tvDescription = (VizoTextView) view.findViewById(R.id.tv_glance_description);
        tvCounter = (VizoTextView) view.findViewById(R.id.tv_characters_counter);
        btnPreview = (VizoButton) view.findViewById(R.id.btn_preview);
        categoryFilterView = view.findViewById(R.id.view_category_filter);

        // Map view elements to event handlers
        view.findViewById(R.id.iv_back_button).setOnClickListener(this);
        view.findViewById(R.id.tv_help_button).setOnClickListener(this);
        ivGlanceImage.setOnClickListener(this);
        btnPreview.setOnClickListener(this);
        tvDescription.setOnClickListener(this);
        // Check local language for Hebrew
        // If Hebrew make text-view right to left justified
        // If not hebrew leave default as English
        // RTL is only supported on android 11+
        if(language.equalsIgnoreCase("he")) {
            tvDescription.setTextDirection(View.TEXT_DIRECTION_RTL);
            tvDescription.setGravity(Gravity.RIGHT);
           // tvDescription.setEllipsize(TextUtils.TruncateAt.START);
        }

        // Show glance description which is entered by user
        if (delegate.userGlanceDescription.length() == 0) {
            tvDescription.setText(R.string.Write_something_novel);
        } else {
            tvDescription.setText(delegate.userGlanceDescription);
        }




        tvCounter.setText(delegate.counterStringForDescription(delegate.userGlanceDescription));

        if (delegate.glanceImageFile != null && delegate.selectedCategory != null) {
            BitmapFactory.Options bitmapOptions = new BitmapFactory.Options();
            //subsmalling for small iamge
            bitmapOptions.inSampleSize = 2;

            Bitmap selectedBitmap = BitmapFactory.decodeFile(
                    delegate.glanceImageFile.getAbsolutePath(),
                    bitmapOptions);
            ivGlanceImage.setImageBitmap(selectedBitmap);

            categoryFilterView.setBackgroundColor(
                    delegate.selectedCategory.getCategoryColor(delegate));

            tvAddPhoto.setText(R.string.Tap_to_change_photo);
            tvDescription.setEnabled(true);
            tvDescription.setTextColor(getResources().getColor(R.color.white));

            if (delegate.userGlanceDescription.length() >= 350) {
                btnPreview.setBackgroundColor(delegate.selectedCategory.getCategoryColor(delegate));
                btnPreview.setEnabled(true);
                btnPreview.setTextColor(getResources().getColor(R.color.white));
            }
        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.iv_back_button) {

            // Navigate back to previous screen
            baseActivity.onBackPressed();
        } else if (v.getId() == ivGlanceImage.getId()) {

            // Navigate to camera screen
            delegate.showFragment(R.id.fl_fragment_container,
                    CameraFragment.newInstance(), true);
        } else if (v.getId() == btnPreview.getId()) {

            // Show preview
            delegate.showFragment(R.id.fl_fragment_container,
                    PreviewFragment.newInstance(), true);
        } else if (v.getId() == tvDescription.getId()) {

            // Navigate to edit page
            delegate.showFragment(R.id.fl_fragment_container,
                    EditGlanceFragment.newInstance(), true);
        } else if (v.getId() == R.id.tv_help_button) {

            // Navigate to FAQs screen for the VizoU category
            delegate.showFragment(R.id.fl_fragment_container,
                    SettingsFAQFragment.newInstance(CATEGORY_VIZOU), true);
        }
    }

}
