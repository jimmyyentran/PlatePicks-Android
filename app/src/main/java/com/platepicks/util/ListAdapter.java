package com.platepicks.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Typeface;
import android.util.Log;
import android.util.LruCache;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.platepicks.R;
import com.platepicks.objects.ListItemClass;

import java.util.ArrayList;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by elizamae on 5/1/16.
 */
public class ListAdapter extends ArrayAdapter<ListItemClass>
        implements ImageSaver.OnCompleteListener,
        GetOneTinyImageTask.OnCompleteListener {
    final int DFLT_IMG_MAX_WIDTH = 258, DFLT_IMG_MAX_HEIGHT = 258;

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
        Typeface tf = Typeface.createFromAsset(mCtx.getAssets(), "fonts/SourceSansPro-Black.otf");

        // lookup view for data population
        TextView fname = (TextView) convertView.findViewById(R.id.fname);
        TextView rname = (TextView) convertView.findViewById(R.id.rname);
        ImageView hollowCircle = (ImageView) convertView.findViewById(R.id.hollow_circle);
        ImageView foodImg = (ImageView) convertView.findViewById(R.id.list_food_picture);

        // set font
        fname.setTypeface(tf);


        // handle if food has been clicked or not
        RelativeLayout itemContainer = (RelativeLayout) convertView.findViewById(R.id.item_container);

        if (!item.isClicked()) {
            Log.d("ListAdapter", "::::::::::::::::::::::::::::::::::::::::::::::::::::::ITEM NOT CLICKED BEFORE::::::::::::::::::::::::::::::::::::::::::::::::::::::");
            itemContainer.setAlpha(1.0f);
        }
        else {
            Log.d("ListAdapter", "::::::::::::::::::::::::::::::::::::::::::::::::::::::ITEM CLICKED BEFORE::::::::::::::::::::::::::::::::::::::::::::::::::::::");

            itemContainer.setAlpha(0.5f);
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
                    load(foodImg, this);
//            new GetOneTinyImageTask(this, foodImg, mCtx, DFLT_IMG_MAX_HEIGHT, DFLT_IMG_MAX_WIDTH)
//                    .execute(item);
        }
        else
        {
            bitmap = getBitmapFromMemCache(key);
            foodImg.setImageBitmap(bitmap);
            foodImg.setScaleType(ImageView.ScaleType.CENTER_CROP);
        }

        // Return the completed view to render on screen
        return convertView;
    }

    @Override
    public void doSomethingWithBitmap(ImageView imageView, Bitmap b, String foodId) {
        addBitmapToMemoryCache(foodId, b);

        if (imageView != null)
            imageView.setImageBitmap(RotateBitmap(b, 0));
    }
    /* end ListAdapter */
}
