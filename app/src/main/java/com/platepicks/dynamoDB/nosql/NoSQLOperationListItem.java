package com.platepicks.dynamoDB.nosql;

import android.view.LayoutInflater;
import android.view.View;

public interface NoSQLOperationListItem {
    int getViewType();
    View getView(LayoutInflater inflater, View convertView);
}
