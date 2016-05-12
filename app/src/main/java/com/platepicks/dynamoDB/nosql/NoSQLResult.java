package com.platepicks.dynamoDB.nosql;

import android.content.Context;
import android.view.View;

public interface NoSQLResult {
    /**
     * Synchronously update an item.
     */
    void updateItem();

    /**
     * Synchronously delete an item.
     */
    void deleteItem();

    View getView(Context context, final View convertView, int position);
}
