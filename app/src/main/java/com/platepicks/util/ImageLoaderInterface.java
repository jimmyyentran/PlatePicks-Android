package com.platepicks.util;

import android.graphics.Bitmap;
import android.widget.ImageView;

import com.platepicks.objects.ListItemClass;

import java.util.List;

/**
 * Created by pokeforce on 5/10/16.
 */
public interface ImageLoaderInterface {
    void doSomethingWithDownloadedImages(List<Bitmap> images);
    void doSomethingOnImageError();
    void doSomethingWithImageView(ImageView imageView, Bitmap b, String foodID);
}
