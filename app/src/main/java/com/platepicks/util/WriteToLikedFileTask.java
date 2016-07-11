package com.platepicks.util;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.platepicks.Application;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by pokeforce on 6/29/16.
 */


public class WriteToLikedFileTask extends AsyncTask<String, Void, Void> {
    static final public int ADD_ITEM = 0, SET_ALL_CLICKED = 1, REPLACE_ALL = 2, CLEAR_FILE = 3;

    Context caller;
    int mode;

    public WriteToLikedFileTask(Context caller, int mode) {
        this.caller = caller;
        this.mode = mode;
        Application.getInstance().accessList.lock();
    }

    @Override
    protected Void doInBackground(String... params) {
        switch (mode) {
            case ADD_ITEM:
                writeToFile(Context.MODE_APPEND, params);
                break;
            case SET_ALL_CLICKED:
                setAllClicked();
                break;
            case REPLACE_ALL:
                writeToFile(Context.MODE_PRIVATE, params);
                break;
            case CLEAR_FILE:
                clearFile();
                break;
            default:
                Log.e("WriteToLikedFileTask", "Invalid mode argument");
                break;
        }

        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        Application.getInstance().accessList.unlock();
    }

    void writeToFile(final int mode, String... params) {
        FileOutputStream fos = null;
        try {
            fos = caller.openFileOutput(Application.SAVED_LIKED_FOODS, mode);

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
            fos = caller.openFileOutput(Application.SAVED_LIKED_FOODS, Context.MODE_PRIVATE);
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
            fis = caller.openFileInput(Application.SAVED_LIKED_FOODS);
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

    void clearFile() {
        caller.deleteFile(Application.SAVED_LIKED_FOODS);
    }
}
