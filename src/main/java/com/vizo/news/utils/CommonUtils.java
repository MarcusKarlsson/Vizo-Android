package com.vizo.news.utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Calendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;

/**
 * Custom class which defines common util functions
 *
 * @author nine3_marks
 */
public class CommonUtils {

    private Context context;
    private static CommonUtils instance = null;

    public static synchronized CommonUtils init(Context context) {
        if (instance == null) {
            instance = new CommonUtils(context);
        }
        return instance;
    }

    public static CommonUtils getInstance() {
        return instance;
    }

    private CommonUtils(Context context) {
        this.context = context;
    }

    public static void CopyStream(InputStream is, OutputStream os) {
        final int buffer_size = 1024;
        try {
            byte[] bytes = new byte[buffer_size];
            for (; ; ) {
                int count = is.read(bytes, 0, buffer_size);
                if (count == -1)
                    break;
                os.write(bytes, 0, count);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String trimString(String val) {
        return val.substring(val.indexOf('['));
    }

    public void loadImage(ImageView imageView, String imageUrl, int blankResId) {
        loadImage(imageView, imageUrl, blankResId, null);
    }

    public void loadImage(ImageView imageView, String imageUrl,
                          int blankResId, ImageLoadingListener listener) {
        DisplayImageOptions options = new DisplayImageOptions.Builder()
                .showImageForEmptyUri(blankResId)
                .showImageOnFail(blankResId)
                .showImageForEmptyUri(blankResId)
                .showImageOnLoading(blankResId)
                .imageScaleType(ImageScaleType.EXACTLY)
                .cacheOnDisk(true)
                .cacheInMemory(true)
                .resetViewBeforeLoading(false)
                .build();
        if (listener != null)
            ImageLoader.getInstance().displayImage(imageUrl, imageView, options, listener);
        else
            ImageLoader.getInstance().displayImage(imageUrl, imageView, options);
    }

    public void showMessage(int resId) {
        showMessage(context.getString(resId));
    }

    public void showMessage(String message) {
        Toast toast = Toast.makeText(context, message, Toast.LENGTH_LONG);
        toast.show();
    }

    /**
     * Validate email address
     *
     * @return true if @param email is valid email address,
     * otherwise false
     */
    public boolean validateEmail(String email) {
        Pattern p = Pattern.compile("[A-Z0-9._%+-]+@[A-Z0-9.-]+.[A-Z]{2,4}",
                Pattern.CASE_INSENSITIVE);
        Matcher m = p.matcher(email);
        return m.matches();
    }

    /**
     * Change ListView height to fit item views
     *
     * @param listView ListView instance
     * @return true if adapter is available, false otherwise
     */
    public boolean setListViewHeightBasedOnItems(ListView listView, int limitHeight) {

        ListAdapter listAdapter = listView.getAdapter();
        if (listAdapter != null) {

            int numberOfItems = listAdapter.getCount();

            // Get total height of all items.
            int totalItemsHeight = 0;
            for (int itemPos = 0; itemPos < numberOfItems; itemPos++) {
                View item = listAdapter.getView(itemPos, null, listView);
                ViewGroup.LayoutParams params = item.getLayoutParams();
                if (params == null) {
                    params = new ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT);
                    item.setLayoutParams(params);
                }
                item.measure(0, 0);
                totalItemsHeight += item.getMeasuredHeight();
            }

            // Get total height of all item dividers.
            int totalDividersHeight = listView.getDividerHeight() * (numberOfItems - 1)
                    + listView.getPaddingTop() + listView.getPaddingBottom();

            // Set list height.
            ViewGroup.LayoutParams params = listView.getLayoutParams();
            params.height = totalItemsHeight + totalDividersHeight;
            if (limitHeight != 0) {
                params.height = Math.max(totalItemsHeight + totalDividersHeight, limitHeight);
            }
            // Add extra space at the bottom
            params.height += listView.getDividerHeight();
            listView.setLayoutParams(params);
            listView.requestLayout();

            return true;

        } else {
            return false;
        }
    }

    /**
     * Generate formatted date string from glance date
     *
     * @param glance_modifiedDate Modified date of glance
     * @return generated formatted string
     */
    public String getVizoTimeString(String glance_modifiedDate) {

        if (glance_modifiedDate == null || TextUtils.isEmpty(glance_modifiedDate))
            return "";

        String format = "yyyy-MM-dd HH:mm:ss";
        DateUtils dateUtils = DateUtils.getInstance();
        Calendar modifiedTime = dateUtils.convertStringToTimeWithFormat(glance_modifiedDate, format);
        if (modifiedTime == null) {
            return "";
        }

        // Generate formatted string to show in Full Glance Screen
        if (dateUtils.isToday(modifiedTime)) {
            return dateUtils.timePeriod(modifiedTime.get(Calendar.HOUR_OF_DAY));
        } else {
            String timeFormat = "MM-dd";
            if (LocalStorage.getInstance().loadAppLanguage().equals("he")) {
                timeFormat = "dd-MM";
            }
            return dateUtils.convertTimeToStringWithFormat(modifiedTime, timeFormat);
        }
    }

    /**
     * Get json string from assets
     *
     * @param assetName Assets file name
     * @return JSON string
     */
    public String loadJSONFromAssets(String assetName) {
        String json;
        try {

            InputStream is = context.getAssets().open(assetName);

            int size = is.available();

            byte[] buffer = new byte[size];

            is.read(buffer);

            is.close();

            json = new String(buffer, "UTF-8");

        } catch (IOException e) {
            e.printStackTrace();
            json = null;
        }

        return json;
    }

    /**
     * Convert Android's density independent pixels to actual pixels
     *
     * @param dp Density independent pixels
     * @return Actual pixels
     */
    public int dpToPx(int dp) {
        float density = context.getResources().getDisplayMetrics().density;
        return Math.round((float) dp * density);
    }
}
