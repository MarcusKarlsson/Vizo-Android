package com.vizo.news.fragments.logo_settings.vizou;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;

import com.vizo.news.R;
import com.vizo.news.activities.HomeSettingsActivity;
import com.vizo.news.activities.VizoUActivity;
import com.vizo.news.activities.base.BaseActivity;
import com.vizo.news.domain.VizoCategory;
import com.vizo.news.fragments.base.BaseFragment;
import com.vizo.news.ui.VizoTextView;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Custom fragment to capture glance image
 * <p/>
 * Created by nine3_marks on 6/23/2015.
 */
public class CameraFragment extends BaseFragment implements
        View.OnClickListener, SurfaceHolder.Callback {

    /**
     * The member which holds the instance of delegate activity
     */
    private VizoUActivity delegate;

    private View view;
    private Camera mCamera;
    private SurfaceView mPreview;
    private VizoTextView tvCaptureButton;
    private VizoTextView tvUseButton;
    private VizoTextView tvUploadButton;
    private VizoTextView tvReshootButton;
    private VizoTextView tvNextButton;

    private ListView lvCategoryList;
    private RelativeLayout cameraFrame;
    private RelativeLayout categoryFrame;
    private View categoryFilterView;
    private ImageView ivGlanceImage;

    private File imageFile;
    private ArrayList<VizoCategory> userCategories;

    public static CameraFragment newInstance() {
        return new CameraFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        this.view = inflater.inflate(R.layout.fragment_camera, container, false);
        return this.view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        delegate = (VizoUActivity) getActivity();

        // setup fragment
        initViewAndClassMembers();

        populateCategoryList();
    }

    /**
     * Initialize view elements and class members
     */
    private void initViewAndClassMembers() {

        mPreview = (SurfaceView) view.findViewById(R.id.surCaptureView);
        mPreview.getHolder().addCallback(this);
        mPreview.getHolder().setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

        mCamera = Camera.open();
        mCamera.setDisplayOrientation(90);

        userCategories = VizoCategory.getAllCategories(delegate);
        int index = -1;
        for (int i = 0; i < userCategories.size(); i++) {
            if (userCategories.get(i).isTopNews()) {
                index = i;
            }
        }
        if (index != -1) {
            userCategories.remove(index);
        }

        // Map view elements to class members
        tvCaptureButton = (VizoTextView) view.findViewById(R.id.tv_capture_button);
        tvUseButton = (VizoTextView) view.findViewById(R.id.tv_use_button);
        tvUploadButton = (VizoTextView) view.findViewById(R.id.tv_upload_button);
        tvReshootButton = (VizoTextView) view.findViewById(R.id.tv_reshoot_button);
        tvNextButton = (VizoTextView) view.findViewById(R.id.tv_next_button);
        lvCategoryList = (ListView) view.findViewById(R.id.lv_category_list);
        cameraFrame = (RelativeLayout) view.findViewById(R.id.rl_camera_frame);
        categoryFrame = (RelativeLayout) view.findViewById(R.id.rl_category_frame);
        categoryFilterView = view.findViewById(R.id.view_category_filter);
        ivGlanceImage = (ImageView) view.findViewById(R.id.iv_glance_image);

        // Map view elements to event handlers
        view.findViewById(R.id.iv_back_button).setOnClickListener(this);
        tvCaptureButton.setOnClickListener(this);
        tvUseButton.setOnClickListener(this);
        tvUploadButton.setOnClickListener(this);
        tvReshootButton.setOnClickListener(this);
        tvNextButton.setOnClickListener(this);
        view.findViewById(R.id.tv_next_button).setOnClickListener(this);
        view.findViewById(R.id.iv_back_in_category).setOnClickListener(this);
        lvCategoryList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (selectedIndex != position) {
                    selectedIndex = position;

                    VizoCategory category = userCategories.get(position);
                    categoryFilterView.setBackgroundColor(category.getCategoryColor(delegate));
                    populateCategoryList();

                    tvNextButton.setTextColor(delegate.getResources()
                            .getColor(R.color.vizo_yellow));
                    tvNextButton.setEnabled(true);
                }
            }
        });
    }

    /**
     * Populate category list in order to let the user select category
     * for the publishing glance
     */
    private void populateCategoryList() {
        lvCategoryList.setAdapter(new CategoryAdapter());
    }

    @Override
    public void onPause() {
        super.onPause();
        mCamera.stopPreview();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mCamera.release();
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.iv_back_button) {

            // Navigate back to previous screen
            delegate.onBackPressed();
        } else if (v.getId() == tvCaptureButton.getId()) {

            // Capture screen
            mCamera.takePicture(null, null, mPicture);

            tvCaptureButton.setVisibility(View.GONE);
            tvUseButton.setVisibility(View.VISIBLE);
            tvUploadButton.setVisibility(View.GONE);
            tvReshootButton.setVisibility(View.VISIBLE);

        } else if (v.getId() == tvReshootButton.getId()) {

            // Start camera preview again
            try {
                mCamera.startPreview();

                mPreview.setVisibility(View.VISIBLE);
                ivGlanceImage.setVisibility(View.GONE);
                tvCaptureButton.setVisibility(View.VISIBLE);
                tvUseButton.setVisibility(View.GONE);
                tvUploadButton.setVisibility(View.VISIBLE);
                tvReshootButton.setVisibility(View.GONE);
            } catch (Exception e) {
            }

        } else if (v.getId() == tvUseButton.getId()) {

            cameraFrame.setVisibility(View.GONE);
            categoryFrame.setVisibility(View.VISIBLE);

        } else if (v.getId() == R.id.iv_back_in_category) {

            cameraFrame.setVisibility(View.VISIBLE);
            categoryFrame.setVisibility(View.GONE);

        } else if (v.getId() == R.id.tv_next_button) {

            delegate.glanceImageFile = imageFile;
            delegate.selectedCategory = userCategories.get(selectedIndex);
            delegate.onBackPressed();
        } else if (v.getId() == tvUploadButton.getId()) {

            // Open gallery in order to let the user select photo from file
            Intent intent = new Intent(
                    Intent.ACTION_PICK,
                    android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            intent.setType("image/*");
            startActivityForResult(Intent.createChooser(intent, "Select File"), 120);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == BaseActivity.RESULT_OK && requestCode == 120) {
            Uri selectedImageUri = data.getData();
            String tempPath = getPath(selectedImageUri, delegate);
            imageFile = new File(tempPath);
            BitmapFactory.Options bitmapOptions = new BitmapFactory.Options();
            Bitmap selectedBitmap = BitmapFactory.decodeFile(
                    imageFile.getAbsolutePath(), bitmapOptions);
            ivGlanceImage.setImageBitmap(selectedBitmap);

            mPreview.setVisibility(View.GONE);
            ivGlanceImage.setVisibility(View.VISIBLE);
            tvCaptureButton.setVisibility(View.GONE);
            tvUseButton.setVisibility(View.VISIBLE);
            tvUploadButton.setVisibility(View.GONE);
            tvReshootButton.setVisibility(View.VISIBLE);
        }
    }

    private String getPath(Uri uri, BaseActivity activity) {
        String[] projection = {MediaStore.MediaColumns.DATA};
        Cursor cursor = activity.getContentResolver().query(uri, projection, null, null, null);
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);
        cursor.moveToFirst();
        return cursor.getString(column_index);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        try {
            mCamera.setPreviewDisplay(mPreview.getHolder());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

        if (holder.getSurface() == null) {
            return;
        }
        Camera.Parameters params = mCamera.getParameters();

        Camera.Size selected = getBestPreviewSize(params);
        params.setPreviewSize(selected.width, selected.height);

        System.out.println(params.getPictureSize().width + "-" + params.getPictureSize().width);
        selected = getBestPictureSize(params);
        params.setPictureSize(selected.width, selected.height);
        System.out.println(params.getPictureSize().width + "-" + params.getPictureSize().width);

        params.setFocusMode("continuous-picture");

        Camera.CameraInfo info = new Camera.CameraInfo();
        Camera.getCameraInfo(Camera.CameraInfo.CAMERA_FACING_BACK, info);
        int rotation = delegate.getWindowManager().getDefaultDisplay().getRotation();
        int degrees = 0;
        switch (rotation) {
            case Surface.ROTATION_0:
                degrees = 0;
                break;
            case Surface.ROTATION_90:
                degrees = 90;
                break;
            case Surface.ROTATION_180:
                degrees = 180;
                break;
            case Surface.ROTATION_270:
                degrees = 270;
                break;
        }
        int rotate = (info.orientation - degrees + 360) % 360;

        params.setRotation(rotate);

        mCamera.setParameters(params);

        // stop preview before making changes
        try {
            mCamera.stopPreview();
        } catch (Exception e) {
            // ignore: tried to stop a non-existent preview
        }

        // set preview size and make any resize, rotate or
        // reformatting changes here


        // start preview with new settings
        try {
            mCamera.startPreview();
        } catch (Exception e) {
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
    }

    private Camera.Size getBestPreviewSize(Camera.Parameters parameters) {
        Camera.Size bestSize;
        List<Camera.Size> sizeList = parameters.getSupportedPreviewSizes();

        bestSize = sizeList.get(0);

        for (int i = 1; i < sizeList.size(); i++) {
            if ((sizeList.get(i).width * sizeList.get(i).height) > (bestSize.width * bestSize.height)) {
                bestSize = sizeList.get(i);
            }
        }

        return bestSize;
    }

    private Camera.Size getBestPictureSize(Camera.Parameters parameters) {
        Camera.Size bestSize;
        List<Camera.Size> sizeList = parameters.getSupportedPictureSizes();

        bestSize = sizeList.get(0);

        for (int i = 1; i < sizeList.size(); i++) {
            if ((sizeList.get(i).width * sizeList.get(i).height) > (bestSize.width * bestSize.height)) {
                bestSize = sizeList.get(i);
            }
        }

        return bestSize;
    }

    Camera.PictureCallback mPicture = new Camera.PictureCallback() {
        @Override
        public void onPictureTaken(byte[] data, Camera camera) {

            // This file should temporally save the bytes
            imageFile = new File(Environment.getExternalStorageDirectory(),
                    "/vizo_cache/glance_photo.jpg");
            imageFile.getParentFile().mkdirs();
            try {
                FileOutputStream fos = new FileOutputStream(imageFile);
                fos.write(data);
                fos.close();
            } catch (Exception e) {
                imageFile = null;
            }
            mCamera.stopPreview();
        }
    };

    private int selectedIndex = -1;

    private class CategoryAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return userCategories.size();
        }

        @Override
        public VizoCategory getItem(int position) {
            return userCategories.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(delegate).inflate(
                        R.layout.user_category_listitem, parent, false);
            }

            // Present user selection with check icon
            ImageView checkIcon = (ImageView) convertView.findViewById(R.id.iv_check_icon);
            if (position == selectedIndex) {
                checkIcon.setVisibility(View.VISIBLE);
            } else {
                checkIcon.setVisibility(View.GONE);
            }

            VizoCategory category = userCategories.get(position);
            VizoTextView tvTitle = (VizoTextView) convertView.findViewById(R.id.tv_item_title);
            tvTitle.setText(category.category_name);

            return convertView;
        }
    }

}
