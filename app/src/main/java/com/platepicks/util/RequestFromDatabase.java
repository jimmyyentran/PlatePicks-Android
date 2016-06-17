package com.platepicks.util;

import android.os.AsyncTask;
import android.util.Log;

import com.platepicks.TinderActivity;
import com.platepicks.objects.FoodRequest;

/**
 * Created by pokeforce on 6/16/16.
 */
public class RequestFromDatabase extends AsyncTask<Void, Void, Void> {
    // Personal note: the request is called at least by the time the UI is visible in onStart()
    // so we should be able to access all the checkBoxes through getAllFoodTypes() without issue.
    // However, if it turns out that we will access saved food types settings, then this cuts
    // out the reliance on the checkboxes being visible, making 1 less race.
    TinderActivity caller;

    public RequestFromDatabase(TinderActivity caller) {
        this.caller = caller;
    }

    // Pass in: search radius, number of food items
    @Override
    protected Void doInBackground(Void... params) {
        Log.d("TinderActivity", "1. Start request");
        caller.waitForGPSLock.lock();
        caller.waitForGPSLock.unlock();
        Log.d("TinderActivity", "2. Start request");

        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        int radius = convertMilesToMeters(Math.min(caller.getRadius(), caller.MAX_RADIUS));

        FoodRequest req = new FoodRequest("", caller.foodLimit, caller.gpsLocation,
                caller.businessLimit, radius, caller.getAllFoodTypes(), 1, caller.offset);
        new AWSIntegratorAsyncTask()
                .executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, "yelpApi2_8", req, caller);
    }

    int convertMilesToMeters(int radius) {
        return (int) (radius * 1609.344);
    }
}
