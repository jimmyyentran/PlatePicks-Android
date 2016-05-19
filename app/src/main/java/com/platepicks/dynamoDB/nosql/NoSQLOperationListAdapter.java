package com.platepicks.dynamoDB.nosql;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

public class NoSQLOperationListAdapter extends ArrayAdapter<NoSQLOperationListItem> {

    public enum ViewType {
        HEADER, OPERATION
    }

    public NoSQLOperationListAdapter(final Context context, final int resource) {
        super(context, resource);
    }

    @Override
    public int getItemViewType(final int position) {
        return getItem(position).getViewType();
    }

    @Override
    public int getViewTypeCount() {
        return ViewType.values().length;
    }

    @Override
    public View getView(final int position, final View convertView,
                        final ViewGroup parent) {
        final LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());

        final NoSQLOperationListItem listItem = getItem(position);
        return listItem.getView(layoutInflater, convertView);
    }
}
