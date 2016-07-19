package com.platepicks.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.Typeface;
import android.util.Log;
import android.util.LruCache;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.daimajia.swipe.SwipeLayout;
import com.daimajia.swipe.adapters.BaseSwipeAdapter;
import com.platepicks.Application;
import com.platepicks.R;
import com.platepicks.objects.ListItemClass;

import java.util.List;

/**
 * Created by pokeforce on 7/18/16.
 */
public class ListSwipeAdapter extends BaseSwipeAdapter implements ImageSaver.OnCompleteListener {
    Context context;
    List<ListItemClass> data;
    LruCache<String,Bitmap> mMemoryCache;

    public ListSwipeAdapter(Context context) {
        this.context = context;
        this.data = Application.getInstance().getLikedData();

        final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);  // maxMemory for LruCache
        final int cacheSize = maxMemory / 8;    // Use 1/8th of the available memory for this memory cache.
        this.mMemoryCache = new LruCache<String, Bitmap>(cacheSize) {
            @Override
            protected int sizeOf(String key, Bitmap bitmap) {
                // The cache size will be measured in kilobytes rather than
                // number of items.
                return bitmap.getByteCount() / 1024;
            }
        };
    }

    @Override
    public int getSwipeLayoutResourceId(int position) {
        return R.id.swipe_item;
    }

    @Override
    public View generateView(int position, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.likeditem_custom, null);
    }

    @Override
    public void fillValues(int position, View convertView) {
        int pos = data.size() - position - 1;

        // Get the data item for this position
        ListItemClass item = data.get(pos);
        ViewHolder viewHolder = (ViewHolder) convertView.getTag();

        if (viewHolder == null) {
            SwipeLayout swipeLayout =  (SwipeLayout) convertView.findViewById(R.id.swipe_item);
            swipeLayout.setShowMode(SwipeLayout.ShowMode.LayDown);  // How swipe is shown
            swipeLayout.setClickToClose(true);                      // Clicks close the swipe layout

            // font stuff
            Typeface tf = Typeface.createFromAsset(context.getAssets(), "fonts/SourceSansPro-Black.otf");

            // lookup view for data population
            TextView fname = (TextView) convertView.findViewById(R.id.fname);
            TextView rname = (TextView) convertView.findViewById(R.id.rname);
            ImageView foodImg = (ImageView) convertView.findViewById(R.id.list_food_picture);
            ImageView delete = (ImageView) convertView.findViewById(R.id.delete_item);

            // set font
            fname.setTypeface(tf);

            viewHolder = new ViewHolder();
            viewHolder.swipeLayout = swipeLayout;
            viewHolder.fname = fname;
            viewHolder.rname = rname;
            viewHolder.foodImg = foodImg;
            viewHolder.delete = delete;
            viewHolder.isOpen = false;
            convertView.setTag(viewHolder);
        }

        if (viewHolder.isOpen)
            viewHolder.swipeLayout.open(false, false);
        else
            viewHolder.swipeLayout.close(false, false);

        // populate the data into the template view using the data object
        viewHolder.fname.setText(item.getFoodName());
        viewHolder.rname.setText(item.getRestaurantName());

        String key = item.getFoodId();
//        Bitmap.Config conf = Bitmap.Config.ARGB_8888;
        Bitmap bitmap;

        // Fetch image from storage or cache
        if (getBitmapFromMemCache(key) == null)
        {
            new ImageSaver(context).
                    setFileName(item.getFoodId()).
                    setDirectoryName("images").
                    load(viewHolder.foodImg, this, true);
        }
        else
        {
            bitmap = getBitmapFromMemCache(key);
            viewHolder.foodImg.setImageBitmap(bitmap);
            viewHolder.foodImg.setScaleType(ImageView.ScaleType.CENTER_CROP);
        }

        // Give imageButton the index of view's item for delete button
        viewHolder.delete.setTag(pos);

        // handle if food has been clicked or not
        if (!item.isClicked()) {
            Log.d("ListAdapter", "::::::::::::::::::::::::::::::::::::::::::::::::::::::ITEM NOT CLICKED BEFORE::::::::::::::::::::::::::::::::::::::::::::::::::::::");
            viewHolder.foodImg.setAlpha(1.0f);
            viewHolder.fname.setAlpha(1.0f);
            viewHolder.rname.setAlpha(1.0f);
        }
        else {
            Log.d("ListAdapter", "::::::::::::::::::::::::::::::::::::::::::::::::::::::ITEM CLICKED BEFORE::::::::::::::::::::::::::::::::::::::::::::::::::::::");
            viewHolder.foodImg.setAlpha(0.5f);
            viewHolder.fname.setAlpha(0.5f);
            viewHolder.rname.setAlpha(0.5f);
        }
    }

    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public void doSomethingWithBitmap(ImageView imageView, Bitmap b, String foodId) {
        addBitmapToMemoryCache(foodId, b);

        if (imageView != null)
            imageView.setImageBitmap(RotateBitmap(b, 0));
    }

    Bitmap getBitmapFromMemCache(String key) {
        Log.d("ListAdapter", String.valueOf(key == null));
        Log.d("ListAdapter", String.valueOf(mMemoryCache == null));
        return mMemoryCache.get(key);
    }

    void addBitmapToMemoryCache(String key, Bitmap bitmap) {
        if (getBitmapFromMemCache(key) == null) {
            mMemoryCache.put(key, bitmap);
        }
    }

    static Bitmap RotateBitmap(Bitmap source, float angle)
    {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
    }

    static class ViewHolder {
        SwipeLayout swipeLayout;
        TextView fname;
        TextView rname;
        ImageView foodImg;
        ImageView delete;

        boolean isOpen;
    }
}
