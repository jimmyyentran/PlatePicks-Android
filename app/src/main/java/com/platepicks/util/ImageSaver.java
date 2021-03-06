package com.platepicks.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.SystemClock;
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
        new SaveImageTask(context, directoryName, fileNames).execute(bitmapImage);
    }

    private static File createFile(Context context, String directoryName, String file) {
        if (context == null || directoryName == null || file == null)
            return null;

        File directory = context.getDir(directoryName, Context.MODE_PRIVATE);
//        for (String s : directory.list())
//            Log.d("ImageSaver", "File: " + s);
        return new File(directory, file);
    }

    public void load(ImageView imageView, OnCompleteListener caller, boolean small) {
        Log.d("ImageSaver", fileNames[0] + " In Load");
        this.small = small;

        if (cancelPotentialLoad(imageView, fileNames[0])) {
            LoadImageAsyncTask loadTask = new LoadImageAsyncTask(imageView, caller, context, directoryName, fileNames[0], small);
            DownloadedDrawable drawable = new DownloadedDrawable(loadTask);
            imageView.setImageDrawable(drawable);
            drawable.getLoadImageAsyncTask().execute();
        }
    }

    public void delete() {
        new DeleteImageAsyncTask(context, directoryName, fileNames[0]).execute();
    }

    private static boolean cancelPotentialLoad(ImageView imageView, String filename) {
        LoadImageAsyncTask loadTask = getLoadImageAsyncTask(imageView);

        if (loadTask != null) {
            String bitmapFile = loadTask.filename;
            if (!bitmapFile.equals(filename)) {
                loadTask.cancel(true);
                Log.d("ImageSaver", "Cancelled previous task");
            } else {
                // The same file is being retrieved
                return false;
            }
        }

        return true;
    }

    private static LoadImageAsyncTask getLoadImageAsyncTask(ImageView imageView) {
        if (imageView != null) {
            Drawable drawable = imageView.getDrawable();
            if (drawable instanceof DownloadedDrawable) {
                return ((DownloadedDrawable) drawable).getLoadImageAsyncTask();
            }
        }

        return null;
    }

    static class SaveImageTask extends AsyncTask<Bitmap, Void, Void> {
        WeakReference<Context> contextRef;
        String directoryName;
        String[] filenames;

        public SaveImageTask(Context context, String directoryName, String[] filenames) {
            this.contextRef = new WeakReference<>(context);
            this.directoryName = directoryName;
            this.filenames = filenames;
        }

        @Override
        protected Void doInBackground(Bitmap... params) {
            if (params.length < 1)
                return null;

            FileOutputStream fileOutputStream = null;
            try {
                accessFiles.lock();
                for (int i = 0; i < params.length; i++) {
                    if (contextRef != null && contextRef.get() != null) {
                        File newImg = createFile(contextRef.get(), directoryName, filenames[i]);

                        if (newImg != null && !newImg.exists()) {
                            newImg.deleteOnExit();
                            fileOutputStream = new FileOutputStream(newImg);
                            params[i].compress(Bitmap.CompressFormat.PNG, 100, fileOutputStream);
                        }
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

    static class LoadImageAsyncTask extends AsyncTask<Void, Void, Bitmap> {
        WeakReference<ImageView> imageViewRef;  // To not stop garbage collection if imageview is gone
        WeakReference<OnCompleteListener> callerRef;
        WeakReference<Context> contextRef;
        String directoryName;
        String filename;
        boolean small;

        LoadImageAsyncTask(ImageView imageView, OnCompleteListener caller, Context context, String directoryName, String filename, boolean small) {
            this.imageViewRef = new WeakReference<>(imageView);
            this.callerRef = new WeakReference<>(caller);
            this.contextRef = new WeakReference<>(context);
            this.directoryName = directoryName;
            this.filename = filename;
            this.small = small;
        }

        @Override
        protected Bitmap doInBackground(Void... params) {
            Log.d("ImageSaver", filename + " In loading task");

            FileInputStream inputStream = null;
            try {
                accessFiles.lock();
                File imgFile = createFile(contextRef.get(), directoryName, filename);

                if (imgFile == null) {
                    Log.e("ImageSaver Load", "Null returned file. Context garbage collected?");
                    accessFiles.unlock();
                    return null;
                }

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
            if (isCancelled())
                bitmap = null;

            if (callerRef != null && imageViewRef != null)
                if (callerRef.get() != null && imageViewRef.get() != null)
                    if (this == getLoadImageAsyncTask(imageViewRef.get()))
                        callerRef.get().doSomethingWithBitmap(imageViewRef.get(), bitmap, filename);
        }
    }

    static class DeleteImageAsyncTask extends AsyncTask<Void, Void, Void> {
        WeakReference<Context> contextRef;
        String directoryName;
        String filename;

        public DeleteImageAsyncTask(Context context, String directoryName, String filename) {
            this.contextRef = new WeakReference<Context>(context);
            this.directoryName = directoryName;
            this.filename = filename;
        }

        @Override
        protected Void doInBackground(Void... params) {
            accessFiles.lock();

            Log.d("ImageSaver", "In delete");

            if (contextRef == null || contextRef.get() == null) {   // Ensure not null
                accessFiles.unlock();
                return null;
            }

            File file = createFile(contextRef.get(), directoryName, filename);

            if (file == null) {
                Log.e("ImageSaver Delete", "Null returned file. Context garbage collected?");
                accessFiles.unlock();
                return null;
            }

            if (file.exists()) {
                if (!file.delete())
                    Log.e("ImageSaver", "Failed to delete " + filename);
                else
                    Log.d("ImageSaver", "Deleted " + filename);
            }

            accessFiles.unlock();

            return null;
        }
    }

    static class DownloadedDrawable extends ColorDrawable {
        private final WeakReference<LoadImageAsyncTask> loadImageTask;

        public DownloadedDrawable(LoadImageAsyncTask loadImageTask) {
            super(Color.TRANSPARENT);
            this.loadImageTask = new WeakReference<>(loadImageTask);
        }

        public LoadImageAsyncTask getLoadImageAsyncTask() {
            return loadImageTask.get();
        }
    }

    public interface OnCompleteListener {
        void doSomethingWithBitmap(ImageView imageView, Bitmap b, String foodId);
    }
}