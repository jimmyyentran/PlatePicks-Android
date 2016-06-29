package com.platepicks.util;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.platepicks.TinderActivity;
import com.platepicks.objects.ListItemClass;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by pokeforce on 6/29/16.
 */
public class WriteToLikedFileTask extends AsyncTask<String, Void, Void>{
    static final public int ADD_ITEM = 0, SET_ALL_CLICKED = 1;

    TinderActivity caller;
    int mode;

    public WriteToLikedFileTask(TinderActivity caller, int mode) {
        this.caller = caller;
        this.mode = mode;
        caller.accessList.lock();
    }

    @Override
    protected Void doInBackground(String... params) {
        switch (mode) {
            case ADD_ITEM:
                addItems(params);
                break;
            case SET_ALL_CLICKED:
                setAllClicked();
                break;
            default:
                Log.e("WriteToLikedFileTask", "Invalid mode argument");
                break;
        }

        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        caller.accessList.unlock();
    }

    void addItems(String... params) {
        FileOutputStream fos = null;
        try {
            fos = caller.openFileOutput(caller.getLikedFileName(), Context.MODE_APPEND);

            for (String p : params) {
                fos.write(p.getBytes());
                fos.write(10);  // newline
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

    void setAllClicked() {
        String file = readFile();
        file = file.replaceAll("0\n", "1\n");

        FileOutputStream fos = null;
        try {
            fos = caller.openFileOutput(caller.getLikedFileName(), Context.MODE_PRIVATE);
            fos.write(file.getBytes());

            Log.d("WriteToLikedFileTask", "Finished writing (1)");
        } catch (IOException e) {
            if (e instanceof FileNotFoundException)
                Log.e("WriteToLikedFileTask", "File does not exist (1)");
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

    String readFile() {
        FileInputStream fis = null;
        StringBuilder builder = new StringBuilder();

        try {
            fis = caller.openFileInput(caller.getLikedFileName());
            int c;

            while ((c = fis.read()) != -1) {
                builder.append((char) c);
            }

            return builder.toString();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (fis != null)
                    fis.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return null;
    }
}
