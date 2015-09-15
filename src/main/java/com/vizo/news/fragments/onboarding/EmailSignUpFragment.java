package com.vizo.news.fragments.onboarding;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;

import java.io.File;

import com.vizo.news.R;
import com.vizo.news.activities.OnboardingActivity;
import com.vizo.news.activities.base.BaseActivity;
import com.vizo.news.api.APIClient;
import com.vizo.news.api.domain.PostLoginResponse;
import com.vizo.news.fragments.base.BaseFragment;
import com.vizo.news.utils.BitmapUtils;
import com.vizo.news.utils.CommonUtils;
import com.vizo.news.utils.LocalStorage;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Created by MarksUser on 4/6/2015.
 *
 * @author nine3_marks
 */
public class EmailSignUpFragment extends BaseFragment implements View.OnClickListener {

    // Request code for Gallery and Camera
    private final int REQUEST_CAMERA = 120;
    private final int SELECT_FILE = 121;
    private Bitmap selectedBitmap;

    private OnboardingActivity delegate;

    private View view;
    private ImageView btnAddPhoto;

    public static EmailSignUpFragment newInstance() {
        return new EmailSignUpFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        this.view = inflater.inflate(R.layout.fragment_email_signup, container, false);
        return this.view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        delegate = (OnboardingActivity) getActivity();

        // Initialize view elements and class members
        initViewAndClassMembers();
    }

    private void initViewAndClassMembers() {
        View vi = this.view;

        // Map view elements to class members
        btnAddPhoto = (ImageView) vi.findViewById(R.id.btn_add_photo);

        // Map view elements to event handlers
        vi.findViewById(R.id.tv_back_button).setOnClickListener(this);
        vi.findViewById(R.id.tv_next_button).setOnClickListener(this);
        btnAddPhoto.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.tv_back_button) {
            baseActivity.onBackPressed();
        } else if (v.getId() == R.id.tv_next_button) {
            validateAndPostSignUp();
        } else if (v.getId() == R.id.btn_add_photo) {
            showImagePicker();
        }
    }

    /**
     * Check user input and post user register request
     */
    private void validateAndPostSignUp() {
        EditText etFirstName = (EditText) this.view.findViewById(R.id.et_first_name);
        EditText etLastName = (EditText) this.view.findViewById(R.id.et_last_name);
        EditText etEmail = (EditText) this.view.findViewById(R.id.et_email_address);
        EditText etPassword = (EditText) this.view.findViewById(R.id.et_password);

        String firstName = etFirstName.getText().toString();
        String lastName = etLastName.getText().toString();
        String emailAddress = etEmail.getText().toString();
        String password = etPassword.getText().toString();

        CommonUtils helperUtils = CommonUtils.getInstance();
        if (firstName.length() == 0
                || lastName.length() == 0
                || emailAddress.length() == 0
                || password.length() == 0) {
            helperUtils.showMessage("Please input all the fields");
            return;
        }

        if(firstName.contains(" ")){
            helperUtils.showMessage("Please remove spaces in the first name. Name can not have spaces.");
            return;
        }

        if(lastName.contains(" ")){
            helperUtils.showMessage("Please remove spaces in the last name. Name can not have spaces.");
            return;
        }

        if (!helperUtils.validateEmail(emailAddress)) {
            helperUtils.showMessage("Invalid Email");
            return;
        }
        if (password.length() < 8) {
            helperUtils.showMessage("Password length should not be smaller than 8");
            return;
        }

        progress.show();
        String avatar_image = null;
        if (selectedBitmap != null) {
            avatar_image = BitmapUtils.convertBitmapToString(selectedBitmap);
        }
        APIClient.getInstance(getActivity()).getApiService().postCreateUserRequest(
                emailAddress, null, null, password, firstName, lastName, avatar_image, mCallback);
    }

    private Callback<PostLoginResponse> mCallback = new Callback<PostLoginResponse>() {
        @Override
        public void success(PostLoginResponse postLoginResponse, Response response) {
            progress.dismiss();
            if (postLoginResponse.result) {
                LocalStorage.getInstance().saveAuthUserInfo(postLoginResponse.user);
                delegate.checkAndNavigate(false);
            } else {
                CommonUtils.getInstance().showMessage(postLoginResponse.errorMessage);
            }
        }

        @Override
        public void failure(RetrofitError error) {
            progress.dismiss();
            CommonUtils.getInstance().showMessage("Failure");
        }
    };

    /**
     * Display popup which lets the user to take
     * image from gallery or camera
     */
    private void showImagePicker() {
        final CharSequence[] items = {"Take Photo", "Choose from Library",
                "Cancel"};

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Add Photo!");
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                if (items[item].equals("Take Photo")) {
                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    File f = new File(android.os.Environment
                            .getExternalStorageDirectory(), "temp.jpg");
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(f));
                    startActivityForResult(intent, REQUEST_CAMERA);
                } else if (items[item].equals("Choose from Library")) {
                    Intent intent = new Intent(
                            Intent.ACTION_PICK,
                            android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    intent.setType("image/*");
                    startActivityForResult(
                            Intent.createChooser(intent, "Select File"),
                            SELECT_FILE);
                } else if (items[item].equals("Cancel")) {
                    dialog.dismiss();
                }
            }
        });
        builder.show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == BaseActivity.RESULT_OK) {
            if (requestCode == REQUEST_CAMERA) {
                File f = new File(Environment.getExternalStorageDirectory()
                        .toString());
                for (File temp : f.listFiles()) {
                    if (temp.getName().equals("temp.jpg")) {
                        f = temp;
                        break;
                    }
                }
                try {
                    BitmapFactory.Options btmapOptions = new BitmapFactory.Options();

                    selectedBitmap = BitmapFactory.decodeFile(f.getAbsolutePath(),
                            btmapOptions);

                    // bm = Bitmap.createScaledBitmap(bm, 70, 70, true);
                    btnAddPhoto.setImageBitmap(selectedBitmap);
                    f.delete();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else if (requestCode == SELECT_FILE) {
                Uri selectedImageUri = data.getData();
                String tempPath = getPath(selectedImageUri, delegate);
                BitmapFactory.Options btmapOptions = new BitmapFactory.Options();
                selectedBitmap = BitmapFactory.decodeFile(tempPath, btmapOptions);
                btnAddPhoto.setImageBitmap(selectedBitmap);
            }
        }
    }

    public String getPath(Uri uri, BaseActivity activity) {
        String[] projection = {MediaStore.MediaColumns.DATA};
        Cursor cursor = activity.getContentResolver().query(uri, projection, null, null, null);
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);
        cursor.moveToFirst();
        return cursor.getString(column_index);
    }
}
