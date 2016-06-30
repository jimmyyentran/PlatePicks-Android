package com.platepicks.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ImageView;

import com.platepicks.R;
import com.platepicks.objects.ListItemClass;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by pokeforce on 6/29/16.
 */
public class GetOneTinyImageTask extends AsyncTask<ListItemClass, Void, Void> {
    OnCompleteListener caller;  // arguments
    ImageView imageView;
    Context c;
    int maxHeight, maxWidth;

    Bitmap requestedBmp = null; // results
    String foodId = null;

    boolean internetErrorFlag = false;  // extra
    BitmapFactory.Options options;

    public GetOneTinyImageTask(OnCompleteListener caller, ImageView imgView, Context c,
                               int screenHeight, int screenWidth) {
        this.caller = caller;
        this.imageView = imgView;
        this.c = c;

        maxHeight = screenHeight;
        maxWidth = screenWidth;
        options = new BitmapFactory.Options();
    }

    @Override
    protected Void doInBackground(ListItemClass... params) {
        if (params.length == 1) {
            foodId = params[0].getFoodId();
            requestedBmp = downloadImage(params[0].getTinyImageUrl());
        }

        if (requestedBmp == null)
            requestedBmp = BitmapFactory.decodeResource(c.getResources(), R.drawable.no_pix_small);

        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        caller.doSomethingWithBitmap(imageView, requestedBmp, foodId);
    }

    // Takes in url and downloads webpage, decoding it into a bitmap
    Bitmap downloadImage(String url) {
        Log.d("GetImagesAsyncTask", url);

        if (!ConnectionCheck.isConnected(c)) {
            Log.d("GetImagesAsyncTask", "Successful cast");
            internetErrorFlag = true;
            return null;
        }

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

            // Copying the inputStream to decode the image twice (once for size, twice for the actual image)
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] data = new byte[1024];
            int len;

            is = connection.getInputStream();

            while ((len = is.read(data)) != -1)
                baos.write(data, 0, len);
            baos.flush();

            InputStream boundsIn = new ByteArrayInputStream(baos.toByteArray());
            InputStream imageIn = new ByteArrayInputStream(baos.toByteArray());

            // Size in terms of width/height
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(boundsIn, null, options);
            boundsIn.close();

            // Scaling image down to fit the dimensions of the specified imageView
            int scaledHeight = options.outHeight,
                    scaledWidth = options.outWidth;
            int sampleSize = 1;     // How much to scale the image down by

            // Check if height or width of image is greater than those of imageView
            while (scaledHeight > maxHeight) {
                Log.d("GetImagesAsyncTask", "Scaling height: " + url + "...");
                scaledHeight /= 2;
                scaledWidth /= 2;
                sampleSize *= 2;
            }
            while (scaledWidth > maxWidth) {
                Log.d("GetImagesAsyncTask", "Scaling width" + url + "...");
                scaledHeight /= 2;
                scaledWidth /= 2;
                sampleSize *= 2;
            }

            // Resulting sample size is what we use to shrink image
            options.inSampleSize = sampleSize;

            Log.d("GetImagesAsyncTask", "width: " + scaledWidth + ", height: " + scaledHeight + " " + url);

            // Decoding image from data to return
            options.inJustDecodeBounds = false;
            image = BitmapFactory.decodeStream(imageIn, null, options);
            imageIn.close();
        } catch (IOException e) {
            e.printStackTrace();
            internetErrorFlag = true;
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

    public interface OnCompleteListener {
        void doSomethingWithBitmap(ImageView imageView, Bitmap b, String foodId);
    }
}
