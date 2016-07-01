package com.platepicks.util;

import android.os.AsyncTask;
import android.util.Log;

import com.platepicks.TinderActivity;
import com.platepicks.objects.ListItemClass;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by pokeforce on 6/29/16.
 */
public class ReadLikedFileTask extends AsyncTask<Void, Void, Void> {
    TinderActivity caller;
    int cnt;

    public ReadLikedFileTask(TinderActivity caller) {
        this.caller = caller;
        caller.accessList.lock();
    }


    @Override
    protected Void doInBackground(Void... params) {
        Log.d("ReadLikedFileTask", "Reading");

        FileInputStream fis = null;
        StringBuilder builder = new StringBuilder();

        try {
            fis = caller.openFileInput(caller.getLikedFileName());
            int c;

            while ((c = fis.read()) != -1) {
                builder.append((char) c);
            }

            cnt = 0;
            String[] lines = builder.toString().split("\n");
            ArrayList<ListItemClass> likedData = new ArrayList<>(lines.length);
            for (String s : lines) {
                ListItemClass item = ListItemClass.createFrom(s);
                likedData.add(item);
                if (!item.isClicked())
                    cnt++;

                Log.d("ReadLikedFileTask", s);
            }

            caller.setLikedData(likedData);
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

    @Override
    protected void onPostExecute(Void aVoid) {
        caller.update_list_number(cnt);
        caller.accessList.unlock();
    }
}
