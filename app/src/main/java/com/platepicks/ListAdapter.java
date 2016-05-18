package com.platepicks;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Typeface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by elizamae on 5/1/16.
 */
public class ListAdapter extends ArrayAdapter<ListItemClass> {
    private Context mCtx; //<-- declare a Context reference
    ArrayList<ListItemClass> data;
    private int selectedIndex;


    public ListAdapter(Context context, ArrayList<ListItemClass> data) {
        super(context, 0, data);
        mCtx = context; //<-- fill it with the Context you are passed
        this.data = data;
        selectedIndex = -1;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        int pos = data.size() - position - 1;

        // Get the data item for this position
        ListItemClass item = getItem(pos);

        // font stuff
        String fontPath = "fonts/Hamburger_Heaven.TTF";
        Typeface tf = Typeface.createFromAsset(mCtx.getAssets(), fontPath);

        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.likeditem_custom, parent, false);
        }


        // Lookup view for data population
        TextView fname = (TextView) convertView.findViewById(R.id.fname);
        TextView rname = (TextView) convertView.findViewById(R.id.rname);
        ImageView img = (ImageView) convertView.findViewById(R.id.food_circle);

        // set font
        fname.setTypeface(tf);

        // highlight if new food
        if (!item.isClicked()) {
            fname.setShadowLayer(24, 0, 0, Color.YELLOW);
        }
        else {}


        // Populate the data into the template view using the data object
        fname.setText(item.getFoodName());
        rname.setText(item.getRestaurantName());

        Bitmap bitmap = new ImageSaver(getContext()).
                setFileName(item.getFoodId()).
                setDirectoryName("images").
                load();

        img.setImageBitmap(RotateBitmap(bitmap, 180));

        // Return the completed view to render on screen
        return convertView;
    }


    public static Bitmap RotateBitmap(Bitmap source, float angle)
    {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
    }


    /* end ListAdapter */
}
