package com.platepicks.dynamoDB.nosql;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.platepicks.R;

public class NoSQLOperationListHeader implements NoSQLOperationListItem {
    final String headerText;

    NoSQLOperationListHeader(final String headerText) {
        this.headerText = headerText;
    }

    @Override
    public int getViewType() {
        return NoSQLOperationListAdapter.ViewType.HEADER.ordinal();
    }

    @Override
    public View getView(final LayoutInflater inflater, final View convertView) {
        final View itemView = inflater.inflate(R.layout.demo_nosql_select_operation_list_header, null);
        final TextView headerTextView = (TextView) itemView.findViewById(R.id.nosql_operation_list_header);
        headerTextView.setText(headerText);
        return itemView;
    }
}
