package com.platepicks.util;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.platepicks.Application;
import com.platepicks.objects.ListItemClass;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

/**
 * Created by pokeforce on 6/29/16.
 */


public class WriteToLikedFileTask extends AsyncTask<String, Void, Void> {
    Context caller;

    public WriteToLikedFileTask(Context caller) {
        this.caller = caller;
    }

    @Override
    protected Void doInBackground(String... params) {
        Application application = Application.getInstance();

        application.accessList.lock();
        List<ListItemClass> data = application.getLikedData();

        // Change file to account for click
        String[] array = new String[data.size()];
        for (int i = 0; i < array.length; i++)
            array[i] = data.get(i).getFileString();

        application.accessList.unlock();

        writeToFile(Context.MODE_PRIVATE, array);

        return null;
    }

    void writeToFile(final int mode, String... params) {
        FileOutputStream fos = null;
        try {
            fos = caller.openFileOutput(Application.SAVED_LIKED_FOODS, mode);

            for (String p : params) {
                fos.write(p.getBytes());
                fos.write('\n');
            }

            Log.d("WriteToLikedFileTask", "Finished writing (0)");
        } catch (IOException e) {
            if (e instanceof FileNotFoundException)
                Log.e("WriteToLikedFileTask", "File does not exist (0)");
            else
                e.printStackTrace();
        } finally {
            try {
                if (fos != null)
                    fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
