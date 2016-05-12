package com.tinderui;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.foodtinder.R;

import java.util.ArrayList;

/**
 * Created by elizamae on 5/1/16.
 */
public class ListAdapter extends ArrayAdapter<ListItemClass> {
    private Context mCtx; //<-- declare a Context reference

    public ListAdapter(Context context, ArrayList<ListItemClass> users) {
        super(context, 0, users);
        mCtx = context; //<-- fill it with the Context you are passed

    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        // get font
        String fontPath = "fonts/Hamburger_Heaven.TTF";

        // Loading Font Face
        Typeface tf = Typeface.createFromAsset(mCtx.getAssets(), fontPath);

        // Get the data item for this position
        ListItemClass item = getItem(position);

        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.likeditem_custom, parent, false);
        }

        // Lookup view for data population
        TextView fname = (TextView) convertView.findViewById(R.id.fname);
        TextView rname = (TextView) convertView.findViewById(R.id.rname);


        // Populate the data into the template view using the data object
        fname.setText(item.getFoodName());
        rname.setText(item.getRestaurantName());

        // set font
        fname.setTypeface(tf);

        // Return the completed view to render on screen
        return convertView;
    }
}
