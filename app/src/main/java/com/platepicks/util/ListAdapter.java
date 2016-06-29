package com.platepicks.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Typeface;
import android.util.LruCache;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.platepicks.R;
import com.platepicks.objects.ListItemClass;

import java.util.ArrayList;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by elizamae on 5/1/16.
 */
public class ListAdapter extends ArrayAdapter<ListItemClass> implements ImageSaver.OnCompleteListener {
    private Context mCtx; //<-- declare a Context reference
    ArrayList<ListItemClass> data;
    LruCache<String,Bitmap> mMemoryCache;
    ReentrantLock accessCache;

    public ListAdapter(Context context, ArrayList<ListItemClass> data) {
        super(context, 0, data);
        mCtx = context; //<-- fill it with the Context you are passed
        this.data = data;
        accessCache = new ReentrantLock();
    }

    public Bitmap getBitmapFromMemCache(String key) {
        accessCache.lock();
        Bitmap b = mMemoryCache.get(key);
        accessCache.unlock();

        return b;
    }

    public void addBitmapToMemoryCache(String key, Bitmap bitmap) {
        accessCache.lock();
        if (getBitmapFromMemCache(key) == null) {
            mMemoryCache.put(key, bitmap);
        }
        accessCache.unlock();
    }

    public static Bitmap RotateBitmap(Bitmap source, float angle)
    {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        int pos = data.size() - position - 1;

        // maxMemory for LruCache
        final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);

        // Use 1/8th of the available memory for this memory cache.
        final int cacheSize = maxMemory / 8;

        mMemoryCache = new LruCache<String, Bitmap>(cacheSize) {
            @Override
            protected int sizeOf(String key, Bitmap bitmap) {
                // The cache size will be measured in kilobytes rather than
                // number of items.
                return bitmap.getByteCount() / 1024;
            }
        };



        // Get the data item for this position
        ListItemClass item = getItem(pos);

        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.likeditem_custom, parent, false);
        }

        // font stuff
        String fontPath = "fonts/Hamburger_Heaven.TTF";
        Typeface tf = Typeface.createFromAsset(mCtx.getAssets(), fontPath);

        // lookup view for data population
        TextView fname = (TextView) convertView.findViewById(R.id.fname);
        TextView rname = (TextView) convertView.findViewById(R.id.rname);
        ImageView img = (ImageView) convertView.findViewById(R.id.food_circle);

        // set font
        fname.setTypeface(tf);

        // highlight if new food
        if (!item.isClicked()) {
            fname.setShadowLayer(24, 0, 0, Color.YELLOW);
        }
        else
        {
            fname.setShadowLayer(0,0,0,0);
        }

        // populate the data into the template view using the data object
        fname.setText(item.getFoodName());
        rname.setText(item.getRestaurantName());

        String key = item.getFoodId();
        Bitmap.Config conf = Bitmap.Config.ARGB_8888;
        Bitmap bitmap = Bitmap.createBitmap(60,60,conf);

        if (getBitmapFromMemCache(key) == null)
        {
            new ImageSaver(getContext()).
                    setFileName(item.getFoodId()).
                    setDirectoryName("images").
                    load(img, this);
        }
        else
        {
            bitmap = getBitmapFromMemCache(key);
            img.setImageBitmap(bitmap);
        }

        // Return the completed view to render on screen
        return convertView;
    }

    @Override
    public void doSomethingWithBitmap(ImageView imageView, Bitmap b, String foodId) {

        addBitmapToMemoryCache(foodId, b);

        if (imageView != null)
            imageView.setImageBitmap(RotateBitmap(b, 180));
    }


    /* end ListAdapter */
}
