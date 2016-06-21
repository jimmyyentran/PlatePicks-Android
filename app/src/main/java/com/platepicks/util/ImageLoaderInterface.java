package com.platepicks.util;

import android.graphics.Bitmap;

import java.util.List;

/**
 * Created by pokeforce on 5/10/16.
 */
public interface ImageLoaderInterface {
    void doSomethingWithDownloadedImages(List<Bitmap> images);
    void doSomethingOnImageError();
}
