package com.vizo.news.fragments.logo_settings.vizou;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.vizo.news.R;
import com.vizo.news.activities.VizoUActivity;
import com.vizo.news.fragments.base.BaseFragment;
import com.vizo.news.ui.VizoButton;
import com.vizo.news.ui.VizoTextView;

/**
 * Custom fragment class for Edit Glance
 * <p/>
 * Created by nine3_marks on 6/23/2015.
 */
public class EditGlanceFragment extends BaseFragment implements View.OnClickListener {

    /**
     * The member which holds the instance of delegate activity
     */
    private VizoUActivity delegate;

    // Members which refer view elements
    private View view;
    private EditText etDescription;
    private VizoTextView tvCounter;
    private VizoButton btnPreview;

    public static EditGlanceFragment newInstance() {
        return new EditGlanceFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        this.view = inflater.inflate(R.layout.fragment_user_glance_edit, container, false);
        return this.view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        delegate = (VizoUActivity) getActivity();

        // setup fragment
        initViewAndClassMembers();
    }

    /**
     * Initialize view elements and class members
     */
    private void initViewAndClassMembers() {

        // Map view elements to class members
        etDescription = (EditText) view.findViewById(R.id.et_glance_description);
        tvCounter = (VizoTextView) view.findViewById(R.id.tv_characters_counter);
        btnPreview = (VizoButton) view.findViewById(R.id.btn_preview);

        // Map view elements to event handlers
        view.findViewById(R.id.iv_back_button).setOnClickListener(this);
        view.findViewById(R.id.tv_done_button).setOnClickListener(this);
        btnPreview.setOnClickListener(this);

        if(localStorage.loadAppLanguage().equalsIgnoreCase("he")) {
            etDescription.setTextDirection(View.TEXT_DIRECTION_RTL);
            etDescription.setGravity(Gravity.RIGHT);
            System.out.println("EDITING THE TEXT HERE ");
            // tvDescription.setEllipsize(TextUtils.TruncateAt.START);
        }

        etDescription.setText(delegate.userGlanceDescription);
        tvCounter.setText(delegate.counterStringForDescription(delegate.userGlanceDescription));
        if (delegate.userGlanceDescription.length() >= 350 ) {
            btnPreview.setEnabled(true);
            btnPreview.setTextColor(getResources().getColor(R.color.white));
            btnPreview.setBackgroundColor(
                    delegate.selectedCategory.getCategoryColor(delegate));
        }
        etDescription.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String text = etDescription.getText().toString();
                tvCounter.setText(delegate.counterStringForDescription(text));
                if (text.length() >= 350 ) {
                    btnPreview.setEnabled(true);
                    btnPreview.setTextColor(getResources().getColor(R.color.white));
                    btnPreview.setBackgroundColor(
                            delegate.selectedCategory.getCategoryColor(delegate));
                } else {
                    btnPreview.setEnabled(false);
                    btnPreview.setTextColor(getResources().getColor(R.color.darker_gray));
                    btnPreview.setBackgroundColor(getResources().getColor(R.color.grey));
                }

            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }
    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.iv_back_button) {

            // Navigate back to previous screen
            baseActivity.onBackPressed();
        } else if (v.getId() == R.id.tv_done_button) {

            // Navigate back to Create Glance page with description entered
            delegate.userGlanceDescription = etDescription.getText().toString();
            baseActivity.onBackPressed();

        } else if (v.getId() == btnPreview.getId()) {

            // Show preview screen
            delegate.userGlanceDescription = etDescription.getText().toString();
            delegate.showFragment(R.id.fl_fragment_container, PreviewFragment.newInstance(), true);
        }
    }
}
