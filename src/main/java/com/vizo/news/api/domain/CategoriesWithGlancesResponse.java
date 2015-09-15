package com.vizo.news.api.domain;

import android.content.Context;

import com.vizo.news.domain.VizoCategory;
import com.vizo.news.domain.VizoGlance;
import com.vizo.news.utils.LocalStorage;

import java.util.ArrayList;

/**
 * Response class for categoriesWithGlances endpoint
 * <p/>
 * Created by nine3_marks on 3/26/2015.
 */
public class CategoriesWithGlancesResponse extends ArrayList<VizoCategoryResponse> {

    /**
     * Save the response object to database
     *
     * @param context Context object
     */
    public void saveToDatabase(Context context) {
        VizoGlance.clearTable(context);

        for (VizoCategoryResponse item : this) {
            VizoCategory category = new VizoCategory();
            category.categoryId = item.categoryId;
            category.category_name = item.category_name;
            category.imageUrl = item.imageUrl;
            category.insertOrUpdate(context);

            for (VizoGlance glance : item.glances) {
                glance.insertOrUpdate(context);
            }
        }

        LocalStorage.getInstance().setFlagValue(LocalStorage.NEW_GLANCES_POSTED, false);
    }
}
