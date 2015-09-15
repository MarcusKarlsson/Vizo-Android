package com.vizo.news.fragments.logo_settings.account;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.TypedArray;
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
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;

import java.io.File;
import java.util.ArrayList;

import com.facebook.AccessToken;
import com.facebook.login.LoginManager;
import com.twitter.sdk.android.Twitter;
import com.vizo.news.R;
import com.vizo.news.activities.base.BaseActivity;
import com.vizo.news.domain.VizoUser;
import com.vizo.news.fragments.base.BaseFragment;
import com.vizo.news.ui.VizoAccountItemView;
import com.vizo.news.utils.CommonUtils;
import com.vizo.news.utils.Constants;

/**
 * Custom fragment class for My Account page
 *
 * @author nine3_marks
 */
public class AccountSettingsFragment extends BaseFragment implements View.OnClickListener {

    // Request code for Gallery and Camera
    private final int REQUEST_CAMERA = 120;
    private final int SELECT_FILE = 121;
    private Bitmap selectedBitmap;

    // member variables referring view elements
    private View view;
    private ImageView btnAddPhoto;
    private ListView lvAccountsList;
    private EditText etFirstName;
    private EditText etLastName;
    private EditText etEmail;

    // arrays of accounts info
    private String[] accountNames;
    private int[] accountImageIds;

    /**
     * connected accounts list
     */
    private ArrayList<Integer> connectedAccounts;

    /**
     * member variable which holds delegate activity
     */
    private BaseActivity delegate;

    public static AccountSettingsFragment newInstance() {
        return new AccountSettingsFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        this.view = inflater.inflate(R.layout.fragment_account_settings, container, false);
        return this.view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        delegate = (BaseActivity) getActivity();

        // fetch account info from resources
        accountNames = delegate.getResources().getStringArray(R.array.account_names);
        accountImageIds = new int[accountNames.length];
        TypedArray images = delegate.getResources().obtainTypedArray(R.array.account_image_ids);
        for (int i = 0; i < images.length(); i++) {
            accountImageIds[i] = images.getResourceId(i, -1);
        }
        images.recycle();

        // setup fragment
        initViewAndClassMembers();
    }

    /**
     * initialize view elements and class members
     */
    private void initViewAndClassMembers() {

        // map view elements to class members
        btnAddPhoto = (ImageView) view.findViewById(R.id.btn_add_photo);
        lvAccountsList = (ListView) view.findViewById(R.id.lv_accounts_list);
        etFirstName = (EditText) view.findViewById(R.id.et_first_name);
        etLastName = (EditText) view.findViewById(R.id.et_last_name);
        etEmail = (EditText) view.findViewById(R.id.et_email_address);

        // map view elements to event handlers
        view.findViewById(R.id.iv_back_button).setOnClickListener(this);
        view.findViewById(R.id.rl_change_password).setOnClickListener(this);
        view.findViewById(R.id.tv_logout_button).setOnClickListener(this);
        btnAddPhoto.setOnClickListener(this);

        // Setup user info
        VizoUser user = localStorage.loadSavedAuthUserInfo();
        if (user != null) {
            etFirstName.setText(user.first_name);
            etLastName.setText(user.last_name);
            etEmail.setText(user.email);
        }

        if (localStorage.loadAppLanguage().equals("he")) {
            buttonPosition = VizoAccountItemView.ACTION_BUTTON_POSITION.LEFT;
        }

        populateAccountsList();
    }

    /**
     * update connected accounts list
     */
    private void populateAccountsList() {

        connectedAccounts = new ArrayList<>();

        // Check Facebook connection
        if (AccessToken.getCurrentAccessToken() != null) {
            connectedAccounts.add(Constants.FACEBOOK);
        }

        // Check Twitter connection
        if (Twitter.getSessionManager().getActiveSession() != null) {
            connectedAccounts.add(Constants.TWITTER);
        }

        // Show Add Account button if needed
        if (connectedAccounts.size() < 2) {
            connectedAccounts.add(Constants.ADD_ACCOUNT);
        }

        lvAccountsList.setAdapter(new AccountsListAdapter());
        CommonUtils.getInstance().setListViewHeightBasedOnItems(lvAccountsList, 0);
    }

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
    public void onClick(View v) {
        if (v.getId() == R.id.iv_back_button) {

            delegate.onBackPressed();

        } else if (v.getId() == btnAddPhoto.getId()) {

            // Show image picker
            showImagePicker();
        } else if (v.getId() == R.id.rl_change_password) {

            // show change password screen
            delegate.showFragment(R.id.fl_fragment_container,
                    ChangePasswordFragment.newInstance(), true);

        } else if (v.getId() == R.id.tv_logout_button) {

            // Disconnect Facebook
            LoginManager.getInstance().logOut();

            // Disconnect Twitter
            Twitter.getSessionManager().clearActiveSession();
            Twitter.logOut();

            populateAccountsList();
        }
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
                    BitmapFactory.Options bitmapOptions = new BitmapFactory.Options();
//                    bitmapOptions.inJustDecodeBounds = false;
//                    bitmapOptions.inPreferredConfig = Bitmap.Config.RGB_565;
//                    bitmapOptions.inDither = true;

                    selectedBitmap = BitmapFactory.decodeFile(f.getAbsolutePath(),
                            bitmapOptions);

                    // bm = Bitmap.createScaledBitmap(bm, 70, 70, true);
                    btnAddPhoto.setImageBitmap(selectedBitmap);
                    f.delete();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else if (requestCode == SELECT_FILE) {
                Uri selectedImageUri = data.getData();
                String tempPath = getPath(selectedImageUri, delegate);
                BitmapFactory.Options bitmapOptions = new BitmapFactory.Options();
                selectedBitmap = BitmapFactory.decodeFile(tempPath, bitmapOptions);
                btnAddPhoto.setImageBitmap(selectedBitmap);
            }
        }
    }

    private String getPath(Uri uri, BaseActivity activity) {
        String[] projection = {MediaStore.MediaColumns.DATA};
        Cursor cursor = activity.getContentResolver().query(uri, projection, null, null, null);
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);
        cursor.moveToFirst();
        return cursor.getString(column_index);
    }

    private VizoAccountItemView.ACTION_BUTTON_POSITION buttonPosition =
            VizoAccountItemView.ACTION_BUTTON_POSITION.RIGHT;

    /**
     * Custom list adapter for connected accounts
     */
    private class AccountsListAdapter extends BaseAdapter {

        public AccountsListAdapter() {
        }

        @Override
        public int getCount() {
            return connectedAccounts.size();
        }

        @Override
        public Integer getItem(int position) {
            return connectedAccounts.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            VizoAccountItemView itemView;
            if (convertView == null) {
                itemView = new VizoAccountItemView(getActivity());
            } else {
                itemView = (VizoAccountItemView) convertView;
            }

            itemView.setActionButtonPos(buttonPosition);
            itemView.configureWithAccount(getItem(position), true);
            itemView.setOnActionListener(actionListener);

            return itemView;
        }
    }

    /**
     * Custom action listener for user action on accounts list
     */
    private VizoAccountItemView.OnActionListener actionListener = new VizoAccountItemView.OnActionListener() {
        @Override
        public void onActionClicked(int accountId) {
            if (accountId == Constants.ADD_ACCOUNT) {

                // Show Accounts Page
                baseActivity.showFragment(R.id.fl_fragment_container,
                        AccountsFragment.newInstance(), true);

            } else if (accountId == Constants.FACEBOOK) {

                // Disconnect Facebook
                LoginManager.getInstance().logOut();
                populateAccountsList();

            } else if (accountId == Constants.TWITTER) {

                // Disconnect Twitter
                Twitter.getSessionManager().clearActiveSession();
                Twitter.logOut();
                populateAccountsList();

            }
        }
    };
}
