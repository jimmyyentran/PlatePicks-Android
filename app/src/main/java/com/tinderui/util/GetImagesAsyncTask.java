package com.tinderui.util;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Build;
import android.util.Log;
import android.widget.ImageView;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
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
            Bitmap b = downloadImage((String) url);
            images.add(b);

            // Size in terms of memory
            int bytes;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
                bytes = b.getAllocationByteCount();
            else
                bytes = b.getByteCount();
            Log.d("GetImagesAsyncTask", "Bytes: " + bytes);
        }

        return images;
    }

    @Override
    protected void onPostExecute(LinkedList<Bitmap> images) {
//        test.setImageBitmap(images.get(0));
        caller.loadImages(images);

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

            Log.d("GetImagesAsyncTask", "width: " + options.outWidth + ", height: " + options.outHeight);

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
