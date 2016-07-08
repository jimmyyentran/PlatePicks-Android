package com.platepicks.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Build;
import android.util.Log;

import com.platepicks.objects.ListItemClass;

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
public class GetImagesAsyncTask extends AsyncTask<ListItemClass, Void, LinkedList<Bitmap>> {
    final int maxMemory = (int) (Runtime.getRuntime().maxMemory());  // In bytes
    final int limitMemory = maxMemory / 8;                           // Use 1/8 of max memory

    int maxHeight, maxWidth;
    boolean internetErrorFlag = false;
    BitmapFactory.Options options;

    ImageLoaderInterface caller;
    Context c;

    public GetImagesAsyncTask(ImageLoaderInterface caller, Context c, int screenHeight, int screenWidth) {
        Log.d("GetImagesAsyncTask", "Creating task");

        this.caller = caller;
        this.c = c;

        // Memory optimization, 3/4 the width/height of the imageView
        options = new BitmapFactory.Options();
        maxHeight = (screenHeight * 3) / 4;
        maxWidth = (screenWidth * 3) / 4;
    }

    @Override
    protected LinkedList<Bitmap> doInBackground(ListItemClass... params) {
        Log.d("GetImagesAsyncTask", "Background");

        int currentMemUsed = 0;
        LinkedList<Bitmap> images = new LinkedList<>();

        for (ListItemClass item : params) {
            Log.d("GetImagesAsyncTask", "Background");
            Bitmap b = downloadImage(item.getImageUrl(), 2);

            if (b != null) {
                // Add image to list of successful downloads
                images.add(b);
                item.setDownloaded(true);

                // Size in terms of memory
                int bytes;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
                    bytes = b.getAllocationByteCount();
                else
                    bytes = b.getByteCount();

                // Add to total amount
                currentMemUsed += bytes;
                Log.d("GetImagesAsyncTask", "Bytes: " + bytes + " " + item.getFoodName());
            }

            if (currentMemUsed >= limitMemory || internetErrorFlag) break;
        }

        return images;
    }

    @Override
    protected void onPostExecute(LinkedList<Bitmap> images) {
        if (internetErrorFlag)
            caller.doSomethingOnImageError();

        caller.doSomethingWithDownloadedImages(images);
    }

    /**
     * No scaling factor is included
     * @param url
     * @return
     */
    Bitmap downloadImage(String url) {
        return downloadImageScaled(url, 0);
    }

    /**
     * If scaling factor is included in constructor
     * @param url
     * @param scaleFactor
     * @return
     */
    Bitmap downloadImage(String url, int scaleFactor) {
        return downloadImageScaled(url, scaleFactor);
    }

    /**
     * Takes in url and downloads webpage, decoding it into a bitmap
     *
     * @param url
     * @param scaleFactor : if 0, use automatic scaling which has the side effects of sometimes not
     *                    scaling image due to algorithm's preference over larger images.
     *                    if > 0, scale down the iamge by the given factor. Images are guaranteed
     *                    to be smaller but risk clarity
     * @return
     */
    Bitmap downloadImageScaled(String url, int scaleFactor) {
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

            InputStream imageIn = new ByteArrayInputStream(baos.toByteArray());

            if (scaleFactor == 0) {
                InputStream boundsIn = new ByteArrayInputStream(baos.toByteArray());

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
//            options.inSampleSize = sampleSize;
                options.inSampleSize = 2;

                Log.d("GetImagesAsyncTask", "width: " + scaledWidth + ", height: " + scaledHeight + " " + url);

                // Decoding image from data to return
                options.inJustDecodeBounds = false;
            } else {
                options.inSampleSize = scaleFactor;
                options.inJustDecodeBounds = false;
                Log.d("GetImagesAsyncTask", "Scale Factor: " + scaleFactor);
            }
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


}
