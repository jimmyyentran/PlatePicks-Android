package com.platepicks.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.os.AsyncTask;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.ImageView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by elizamae on 5/12/16.
 */
public class ImageSaver {
    static private ReentrantLock accessFiles = new ReentrantLock();

    private String directoryName = "images";
    private String[] fileNames;
    private Context context;
    boolean small = false;

    public ImageSaver(Context context) {
        this.context = context;
    }

    public ImageSaver setFileName(String... fileNames) {
        this.fileNames = fileNames;
        return this;
    }

    public ImageSaver setDirectoryName(String directoryName) {
        this.directoryName = directoryName;
        return this;
    }

    public void save(Bitmap... bitmapImage) {
        new SaveImageTask().execute(bitmapImage);
    }

    @NonNull
    private File createFile(String file) {
        File directory = context.getDir(directoryName, Context.MODE_PRIVATE);

//        for (String s : directory.list())
//            Log.d("ImageSaver", "File: " + s);

        return new File(directory, file);
    }

    public void load(ImageView imageView, OnCompleteListener caller, boolean small) {
        Log.d("ImageSaver", "In Load");
        this.small = small;
        new LoadImageAsyncTask(imageView, caller).execute();
    }

    public void delete() {
        new DeleteImageAsyncTask().execute();
    }

    class SaveImageTask extends AsyncTask<Bitmap, Void, Void> {
        @Override
        protected Void doInBackground(Bitmap... params) {
            if (params.length < 1)
                return null;

            FileOutputStream fileOutputStream = null;
            try {
                accessFiles.lock();
                for (int i = 0; i < params.length; i++) {
                    File newImg = createFile(fileNames[i]);

                    if (!newImg.exists()) {
                        newImg.deleteOnExit();
                        fileOutputStream = new FileOutputStream(newImg);
                        params[i].compress(Bitmap.CompressFormat.PNG, 100, fileOutputStream);
                    }
                }
                accessFiles.unlock();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    if (fileOutputStream != null) {
                        fileOutputStream.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }
    }

    class LoadImageAsyncTask extends AsyncTask<Void, Void, Bitmap> {
        WeakReference<ImageView> imageViewRef;  // To not stop garbage collection if imageview is gone
        WeakReference<OnCompleteListener> callerRef;

        LoadImageAsyncTask(ImageView imageView, OnCompleteListener caller) {
            this.imageViewRef = new WeakReference<ImageView>(imageView);
            this.callerRef = new WeakReference<OnCompleteListener>(caller);
        }

        @Override
        protected Bitmap doInBackground(Void... params) {
            Log.d("ImageSaver", "In loading task");

            FileInputStream inputStream = null;
            try {
                File imgFile = createFile(fileNames[0]);
                accessFiles.lock();

                while (!imgFile.exists()) {
                    accessFiles.unlock();
                    SystemClock.sleep(100);
                    accessFiles.lock();
                }

                inputStream = new FileInputStream(imgFile);
                accessFiles.unlock();

                BitmapFactory.Options options = new BitmapFactory.Options();
                if (small)
                    options.inSampleSize = 4;
                return BitmapFactory.decodeStream(inputStream, null, options);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    if (accessFiles.isHeldByCurrentThread())
                        accessFiles.unlock();

                    if (inputStream != null) {
                        inputStream.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            if (callerRef.get() != null)
                callerRef.get().doSomethingWithBitmap(imageViewRef.get(), bitmap, fileNames[0]);
        }
    }

    class DeleteImageAsyncTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            accessFiles.lock();

            Log.d("ImageSaver", "In delete");
            File file = createFile(fileNames[0]);
            if (file.exists()) {
                if (!file.delete())
                    Log.e("ImageSaver", "Failed to delete " + fileNames[0]);
                else
                    Log.d("ImageSaver", "Deleted " + fileNames[0]);
            }

            accessFiles.unlock();

            return null;
        }
    }

    public interface OnCompleteListener {
        void doSomethingWithBitmap(ImageView imageView, Bitmap b, String foodId);
    }
}