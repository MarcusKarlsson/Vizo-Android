package com.vizo.news.fragments.logo_settings.vizou;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;

import com.vizo.news.R;
import com.vizo.news.activities.VizoUActivity;
import com.vizo.news.api.APIVizoUClient;
import com.vizo.news.fragments.base.BaseFragment;
import com.vizo.news.ui.VizoTextView;
import com.vizo.news.utils.BitmapUtils;
import com.vizo.news.utils.CommonUtils;
import com.vizo.news.utils.DateUtils;

import org.json.JSONObject;

import java.util.Calendar;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Custom fragment class for preview screen
 * <p/>
 * Created by nine3_marks on 6/24/2015.
 */
public class PreviewFragment extends BaseFragment implements View.OnClickListener {

    private VizoUActivity delegate;

    private View view;
    private ImageView ivGlanceImage;
    private VizoTextView tvUpdateTime;
    private VizoTextView tvGlanceDescription;
    private View categoryFilterView;
    private Bitmap selectedBitmap;
    private CheckBox chkBoxPublicly;

    // Facebook Info
    private String facebookEmail;
    private String facebookName;
    private String facebookPicture;

    public static PreviewFragment newInstance() {
        return new PreviewFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        this.view = inflater.inflate(R.layout.fragment_preview, container, false);
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
        ivGlanceImage = (ImageView) view.findViewById(R.id.iv_glance_image);
        tvGlanceDescription = (VizoTextView) view.findViewById(R.id.tv_glance_description);
        tvUpdateTime = (VizoTextView) view.findViewById(R.id.tv_update_time);
        categoryFilterView = view.findViewById(R.id.view_category_filter);
        chkBoxPublicly = (CheckBox)view.findViewById(R.id.chkPublicly);

        // Map view elements to event handlers
        view.findViewById(R.id.tv_edit_button).setOnClickListener(this);
        view.findViewById(R.id.tv_publish_button).setOnClickListener(this);

        // Populate preview content
        BitmapFactory.Options bitmapOptions = new BitmapFactory.Options();

        //subsmalling for small iamge
        bitmapOptions.inSampleSize = 2;

        selectedBitmap = BitmapFactory.decodeFile(
                delegate.glanceImageFile.getAbsolutePath(),
                bitmapOptions);
        ivGlanceImage.setImageBitmap(selectedBitmap);

        tvGlanceDescription.setText(delegate.userGlanceDescription);
        String currentTime = DateUtils.getInstance().convertTimeToStringWithFormat(
                Calendar.getInstance(), "yyyy-MM-dd HH:mm:ss");
        String updateTime = CommonUtils.getInstance().getVizoTimeString(currentTime);
        tvUpdateTime.setText(String.format("%s  |  %s", updateTime,
                delegate.selectedCategory.category_name));

        categoryFilterView.setBackgroundColor(delegate.selectedCategory.getCategoryColor(delegate));

        // Fetch facebook account info
        JSONObject jsonObject = localStorage.getSavedFacebookInfo();
        if (jsonObject != null) {
            String facebookId = jsonObject.optString("id");
            facebookEmail = jsonObject.optString("email");
            String firstName = jsonObject.optString("first_name");
            String lastName = jsonObject.optString("last_name");
            facebookName = firstName + " " + lastName;
            facebookPicture = "https://graph.facebook.com/" +
                    facebookId + "/picture?type=large";
        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.tv_edit_button) {

            // Navigate back to create glance page
            delegate.onBackPressed();
        } else if (v.getId() == R.id.tv_publish_button) {

            // Publish user created glance

            boolean bPublicly = chkBoxPublicly.isChecked();
            int isGlobal = 0;
            if(bPublicly == true){
                isGlobal = 1;
            }

            progress.show();
            String imageFile = BitmapUtils.convertBitmapToString(selectedBitmap);
            APIVizoUClient.getInstance(delegate).getApiService().publishUserGlance(
                    delegate.userGlanceDescription,
                    imageFile,
                    delegate.selectedCategory.categoryId,
                    facebookEmail,
                    facebookName,
                    facebookPicture,
                    isGlobal,
                    mCallback);
        }
    }

    private Callback<Response> mCallback = new Callback<Response>() {
        @Override
        public void success(Response response, Response response2) {
            progress.dismiss();
            CommonUtils.getInstance().showMessage("You have published the glance");
            delegate.popToRoot();
        }

        @Override
        public void failure(RetrofitError error) {
            progress.dismiss();
            CommonUtils.getInstance().showMessage("Failed to publish the glance, try again later");
        }
    };

}
