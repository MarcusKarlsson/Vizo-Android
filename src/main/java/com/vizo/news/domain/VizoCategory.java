package com.vizo.news.domain;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.os.Handler;
import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

import com.vizo.news.R;
import com.vizo.news.api.APIVizoUClient;
import com.vizo.news.api.domain.VizoUGlancesResponse;
import com.vizo.news.database.DatabaseConstants;
import com.vizo.news.database.DatabaseProvider;
import com.vizo.news.ui.VizoVerticalCategoryPager;
import com.vizo.news.utils.CommonUtils;
import com.vizo.news.utils.LocalStorage;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Created by MarksUser on 3/26/2015.
 *
 * @author nine3_marks
 */
public class VizoCategory implements Parcelable {

    @SerializedName("id")
    public String categoryId;

    @SerializedName("name")
    public String category_name;

    @SerializedName("image_url")
    public String imageUrl;

    /**
     * using top news id as static constant identifier
     * this needs further work on endpoint
     */
    public static final String VIZO_TOP_NEWS = "12";
    public static final String VIZO_VIZOU = "22";

    private ArrayList<VizoUGlance> userGlances;
    private boolean bGotUserGlances = false;
    private Context gContext;

    /**
     * Required function for implementing Parceable. (We will not need to use
     * this)
     */

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(categoryId);
        dest.writeString(category_name);
        dest.writeString(imageUrl);
    }

    private VizoCategory(Parcel in) {
        this.categoryId = in.readString();
        this.category_name = in.readString();
        this.imageUrl = in.readString();
    }

    public VizoCategory() {
        userGlances = new ArrayList<VizoUGlance>();
    }

    /**
     * Creator that regenerates the object.
     */
    public static final Parcelable.Creator<VizoCategory> CREATOR = new Parcelable.Creator<VizoCategory>() {
        public VizoCategory createFromParcel(Parcel in) {
            return new VizoCategory(in);
        }

        public VizoCategory[] newArray(int size) {
            return new VizoCategory[size];
        }
    };

    /**
     * Get glanced items in category
     *
     * @param context Context object
     * @return The array list of glanced items
     */
    public ArrayList<VizoGlance> getGlancedItemsInCategory(Context context) {
        ArrayList<VizoGlance> glancedItems = new ArrayList<>();
        ContentResolver contentResolver = context.getContentResolver();

        // Filter results
        // we should fetch the glances for current edition
        String selection = DatabaseConstants.CATEGORY_ID + " =?"
                + " AND " + DatabaseConstants.LANG + " =?"
                + " AND " + DatabaseConstants.SYNC_STATE + " IS NOT ?";
        String args[] = {categoryId,
                LocalStorage.getInstance().loadAppLanguage(),
                DatabaseConstants.DELETE_REQUIRED};

        Cursor cursor = contentResolver.query(DatabaseProvider.GLANCED_URI,
                null, selection, args, null);
        if (cursor != null && cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                glancedItems.add(VizoGlance.fromCursor(cursor));
            }
        }

        if (cursor != null) {
            cursor.close();
        }

        return glancedItems;
    }

    /**
     * Get favorite items in category
     *
     * @param context Context object
     * @return Array of favorite items
     */
    public ArrayList<VizoGlance> getFavoriteItemsInCategory(Context context) {
        ArrayList<VizoGlance> favoriteItems = new ArrayList<>();
        ContentResolver contentResolver = context.getContentResolver();

        // Filter results
        // we should fetch of the current edition
        String selection = DatabaseConstants.CATEGORY_ID + " =?"
                + " AND " + DatabaseConstants.LANG + " =?"
                + " AND " + DatabaseConstants.IS_FAVORITE + " =1"
                + " AND " + DatabaseConstants.SYNC_STATE + " IS NOT ?";
        String args[] = {categoryId,
                LocalStorage.getInstance().loadAppLanguage(),
                DatabaseConstants.DELETE_REQUIRED};

        Cursor cursor = contentResolver.query(DatabaseProvider.GLANCED_URI,
                null, selection, args, null);
        if (cursor != null && cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                favoriteItems.add(VizoGlance.fromCursor(cursor));
            }
        }

        if (cursor != null) {
            cursor.close();
        }

        return favoriteItems;
    }

    /**
     * Check if category is Top News Category
     *
     * @return true if Top News, false otherwise
     */
    public boolean isTopNews() {
        return categoryId.equals(VIZO_TOP_NEWS);
    }

    /**
     * Get localized category name
     *
     * @param context Context object
     * @return Localized category name
     */
    public String getLocalizedName(Context context) {

        if (context == null)
            return category_name;

        // get names and ids array
        String[] categoryNames = context.getResources().getStringArray(R.array.category_names);
        int[] categoryIds = context.getResources().getIntArray(R.array.category_ids);

        int index = -1;
        for (int i = 0; i < categoryIds.length; i++) {
            if (categoryIds[i] == Integer.parseInt(categoryId)) {
                index = i;
                break;
            }
        }

        String localizedName = "";
        if (index != -1) {
            localizedName = categoryNames[index];
        }

        return localizedName;
    }

    /**
     * Insert or update category to database
     *
     * @param context Context object
     */
    public void insertOrUpdate(Context context) {

        ContentValues values = this.toContentValues();
        ContentResolver contentResolver = context.getContentResolver();

        String selection = DatabaseConstants.CATEGORY_TABLE_ID + " =?";
        String args[] = {this.categoryId};
        Cursor cursor = contentResolver.query(
                DatabaseProvider.CATEGORIES_URI, null, selection, args, null);

        // Check if the glance already exist in the db
        if (cursor != null && cursor.getCount() > 0) {

            // Update glance
            contentResolver.update(DatabaseProvider.CATEGORIES_URI, values, selection, args);
        } else {

            // If not exist, add a new one
            contentResolver.insert(DatabaseProvider.CATEGORIES_URI, values);
        }

        if (cursor != null) {
            cursor.close();
        }
    }

    /**
     * Get category object from cursor
     *
     * @param cursor cursor object
     * @return category object
     */
    public static VizoCategory fromCursor(Cursor cursor) {
        VizoCategory category = new VizoCategory();
        category.categoryId = cursor.getString(cursor.getColumnIndex(DatabaseConstants.CATEGORY_TABLE_ID));
        category.category_name = cursor.getString(cursor.getColumnIndex(DatabaseConstants.CATEGORY_NAME));
        category.imageUrl = cursor.getString(cursor.getColumnIndex(DatabaseConstants.CATEGORY_IMAGE_URL));
        return category;
    }

    /**
     * Convert category object instance to content values
     *
     * @return content values
     */
    public ContentValues toContentValues() {
        ContentValues values = new ContentValues();
        values.put(DatabaseConstants.CATEGORY_TABLE_ID, this.categoryId);
        values.put(DatabaseConstants.CATEGORY_NAME, this.category_name);
        values.put(DatabaseConstants.CATEGORY_IMAGE_URL, this.imageUrl);
        return values;
    }

    /**
     * Get all glances of the category
     *
     * @param context Context object
     * @return Array of glances
     */
    public ArrayList<VizoGlance> getCategoryGlances(Context context) {
        gContext = context;

        ArrayList<VizoGlance> items = new ArrayList<>();

        /*if(categoryId.equals(VIZO_VIZOU)){
            bGotUserGlances = false;

            Thread t = new Thread(new Runnable() {
                public void run() {

                    APIVizoUClient.getInstance(gContext).getApiService().getVizoUGlances(20, 0, mCallback);

                }
            });

            t.start();


            int nRetryCount = 1000;
            while(nRetryCount > 0){

                if(bGotUserGlances == true){
                    items = VizoVerticalCategoryPager.VizoGlanceViewPagerFragment.getVizoGlances(userGlances);
                }

                nRetryCount--;
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }*/
        if(categoryId.equals(VIZO_VIZOU)){
            //returning vizoUGlances for VizoU category
            items= VizoVerticalCategoryPager.VizoGlanceViewPagerFragment.getVizoGlances(VizoVerticalCategoryPager.VizoGlanceViewPagerFragment.userGlances);
        }else
        {
            ContentResolver contentResolver = context.getContentResolver();

            // Filter results
            // we should fetch the glances for current edition
            String selection = DatabaseConstants.CATEGORY_ID + " =?"
                    + " AND " + DatabaseConstants.LANG + " =?";
            String args[] = {categoryId,
                    LocalStorage.getInstance().loadAppLanguage()};

            Cursor cursor = contentResolver.query(DatabaseProvider.GLANCES_URI,
                    null, selection, args, null);
            if (cursor != null && cursor.getCount() > 0) {
                while (cursor.moveToNext()) {
                    items.add(VizoGlance.fromCursor(cursor));
                }
            }

            if (cursor != null) {
                cursor.close();
            }
        }


        return items;
    }

    private Callback<VizoUGlancesResponse> mCallback = new Callback<VizoUGlancesResponse>() {
        @Override
        public void success(VizoUGlancesResponse vizoUGlancesResponse, Response response) {
            if (vizoUGlancesResponse.totalCount > 0) {
                userGlances = vizoUGlancesResponse.glances;
            }
            bGotUserGlances = true;
        }

        @Override
        public void failure(RetrofitError error) {
            CommonUtils.getInstance().showMessage("Failed to load user glances");
            bGotUserGlances = true;
        }
    };

    /**
     * Get all categories from database
     *
     * @param context Context obejct
     * @return Array of categories
     */
    public static ArrayList<VizoCategory> getAllCategories(Context context) {

        ArrayList<VizoCategory> categories = new ArrayList<>();
        ContentResolver contentResolver = context.getContentResolver();

        Cursor cursor = contentResolver.query(DatabaseProvider.CATEGORIES_URI,
                null, null, null, null);
        if (cursor != null && cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                VizoCategory category = VizoCategory.fromCursor(cursor);
                if(category.categoryId.equals(VIZO_VIZOU)) continue;

                categories.add(category);
            }
        }

        if (cursor != null) {
            cursor.close();
        }

        return categories;
    }

    /**
     * Find category by ID
     *
     * @param categoryId The Category ID in string
     * @param context    Context object
     * @return Category object
     */
    public static VizoCategory findCategoryById(String categoryId, Context context) {

        ContentResolver contentResolver = context.getContentResolver();

        // Filter results
        // we should fetch the glances for current edition
        String selection = DatabaseConstants.CATEGORY_TABLE_ID + " =?";
        String args[] = {categoryId};

        Cursor cursor = contentResolver.query(DatabaseProvider.CATEGORIES_URI,
                null, selection, args, null);

        VizoCategory category = null;
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                category = VizoCategory.fromCursor(cursor);
            }
        }

        if (cursor != null) {
            cursor.close();
        }

        return category;
    }

    /**
     * Get category color for the category
     *
     * @param context Context object
     */
    /**
     * Get category color for the category
     *
     * @param context Context object
     * @return Category color
     */
    public int getCategoryColor(Context context) {
        int[] categoryIds = context.getResources().getIntArray(R.array.category_ids);
        int[] categoryColors = context.getResources().getIntArray(R.array.category_colors);

        int index = -1;
        for (int i = 0; i < categoryIds.length; i++) {
            if (Integer.valueOf(this.categoryId) == categoryIds[i]) {
                index = i;
                break;
            }
        }

        //add max index check
        if (index != -1 && index < categoryColors.length) {
            return categoryColors[index];
        } else {
            //returning first color if it has not found correct color for category
            return categoryColors[0];
        }
    }

    /**
     * Get placeholder image for the category
     *
     * @param context             Context object
     * @param landscapeForTopNews if true, get landscape image for Top News
     * @return Resource ID for the category placeholder image
     */
    public int getPlaceholderImage(Context context, boolean landscapeForTopNews) {
        int[] categoryIds = context.getResources().getIntArray(R.array.category_ids);
        TypedArray placeholders = context.getResources()
                .obtainTypedArray(R.array.category_placeholders);

        int index = -1;
        for (int i = 0; i < categoryIds.length; i++) {
            if (Integer.valueOf(this.categoryId) == categoryIds[i]) {
                index = i;
                break;
            }
        }

        int resourceId = -1;
        if (isTopNews()) {
            if (landscapeForTopNews) {
                resourceId = R.drawable.placeholder_top_news_land;
            } else {
                resourceId = R.drawable.placeholder_top_news_port;
            }
        } else {
            if (index != -1) {
                resourceId = placeholders.getResourceId(index, -1);
            }
        }

        placeholders.recycle();

        return resourceId;
    }

}
