package com.tinderui.util;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ImageView;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by pokeforce on 5/6/16.
 */
public class GetImagesAsyncTask extends AsyncTask<Object, Void, LinkedList<Bitmap>> {
    final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
    final int limitMemory = maxMemory / 8;
    int currentMemUsed = 0;
    int maxHeight, maxWidth;
    BitmapFactory.Options options;

    ImageView test;

    public GetImagesAsyncTask(ImageView view, int screenHeight, int screenWidth) {
        test = view;
        maxHeight = screenHeight;
        maxWidth = screenWidth;
    }

    @Override
    protected LinkedList<Bitmap> doInBackground(Object... params) {
        options = new BitmapFactory.Options();
        Bitmap b = downloadImage((String) params[0]);

        LinkedList<Bitmap> images = new LinkedList<>();
        images.add(b);

        return images;
    }

    @Override
    protected void onPostExecute(LinkedList<Bitmap> images) {
        test.setImageBitmap(images.get(0));
    }

    // Takes in url and downloads webpage, decoding it into a bitmap
    Bitmap downloadImage(String url) {
        InputStream is = null;
        Bitmap image = null;

        try {
            // Connecting to the url
            URL u = new URL(url);
            HttpURLConnection connection = (HttpURLConnection) u.openConnection();
            connection.setReadTimeout(10000);
            connection.setConnectTimeout(15000);
            connection.setRequestMethod("GET");
            connection.connect();

            // Decoding image from data, getting size in terms of memory and width/height
            is = connection.getInputStream();

            options.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(is, null, options);

            Log.d("GetImagesAsyncTask", "width: " + options.outWidth + ", height: " + options.outHeight);

            options.inJustDecodeBounds = false;
            image = BitmapFactory.decodeStream(is, null, options);

//            int bytes;
//            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT)
//                bytes = image.getAllocationByteCount();
//            else
//                bytes = image.getByteCount();
//            Log.d("GetImagesAsyncTask", "Bytes: " + bytes);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (is != null) try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // Return decoded bitmap. Is null if error occurred.
        return image;
    }
}
