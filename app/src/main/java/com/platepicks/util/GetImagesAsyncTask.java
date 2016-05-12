package com.platepicks.util;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Build;
import android.util.Log;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.LinkedList;

/**
 * Created by pokeforce on 5/6/16.
 */
public class GetImagesAsyncTask extends AsyncTask<Object, Void, LinkedList<Bitmap>> {
//    String[] testArray = {
//    "http://s3-media1.fl.yelpcdn.com/bphoto/oYEqKGlXLqyz9eB0nOlJpw/o.jpg",
//            "http://s3-media2.fl.yelpcdn.com/bphoto/pwdLeRw0YGp48M8ZjCXrsA/o.jpg",
//            "http://s3-media4.fl.yelpcdn.com/bphoto/Zh-oY7L5i4GKggt1fmJNZg/o.jpg",
//            "http://s3-media1.fl.yelpcdn.com/bphoto/lFwxOdrokRYKT33PVW0rJQ/o.jpg",
//            "http://s3-media3.fl.yelpcdn.com/bphoto/F1_rPJaEypsYMhYeuqq__g/o.jpg",
//            "http://s3-media1.fl.yelpcdn.com/bphoto/NwbJkYOgBqOc4OAPDwTo5w/o.jpg",
//            "http://s3-media4.fl.yelpcdn.com/bphoto/JaVpbHJTRHTKi2VqrUxsTw/o.jpg",
//            "http://s3-media3.fl.yelpcdn.com/bphoto/z6wPUKX06ofmaKwqqcONAw/o.jpg",
//            "http://s3-media1.fl.yelpcdn.com/bphoto/zcda10Pklt4LLGyjkw2r3Q/o.jpg",
//            "http://s3-media3.fl.yelpcdn.com/bphoto/KlbsVMic2T5bkXqZtfbZGQ/o.jpg"};

    final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);  // In bytes
    final int limitMemory = maxMemory / 8;                                  // Use 1/8 of max memory
    int currentMemUsed = 0;
    int maxHeight, maxWidth;
    BitmapFactory.Options options;

    ImageLoaderInterface caller;

    public GetImagesAsyncTask(ImageLoaderInterface caller, int screenHeight, int screenWidth) {
        this.caller = caller;
        maxHeight = screenHeight;
        maxWidth = screenWidth;
    }

    @Override
    protected LinkedList<Bitmap> doInBackground(Object... params) {
        options = new BitmapFactory.Options();
        LinkedList<Bitmap> images = new LinkedList<>();

        for (Object url : params) {
//        for (Object url : testArray) {
            Bitmap b = downloadImage((String) url);
            images.add(b);

            // Size in terms of memory
            int bytes;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
                bytes = b.getAllocationByteCount();
            else
                bytes = b.getByteCount();

            // Add to total amount
            currentMemUsed += bytes;
            Log.d("GetImagesAsyncTask", "Bytes: " + bytes);
        }

        return images;
    }

    @Override
    protected void onPostExecute(LinkedList<Bitmap> images) {
//        test.setImageBitmap(images.get(0));
        caller.doSomethingWithDownloadedImages(images);
    }

    // Takes in url and downloads webpage, decoding it into a bitmap
    Bitmap downloadImage(String url) {
        Log.d("GetImagesAsyncTask", url);

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
            while (scaledHeight >= maxHeight) {
                Log.d("GetImagesAsyncTask", "Scaling height: " + url + "...");
                scaledHeight /= 2;
                scaledWidth /= 2;
                sampleSize *= 2;
            }
            while (scaledWidth >= maxWidth) {
                Log.d("GetImagesAsyncTask", "Scaling width" + url + "...");
                scaledHeight /= 2;
                scaledWidth /= 2;
                sampleSize *= 2;
            }

            // Resulting sample size is what we use to shrink image
            options.inSampleSize = sampleSize;

            Log.d("GetImagesAsyncTask", "width: " + scaledWidth + ", height: " + scaledHeight);

            // Decoding image from data to return
            options.inJustDecodeBounds = false;
            image = BitmapFactory.decodeStream(imageIn, null, options);
            imageIn.close();
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