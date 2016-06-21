package com.platepicks.support;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;

import com.platepicks.TinderActivity;

/**
 * Created by pokeforce on 6/20/16.
 */
public class ConnectivityReceiver extends BroadcastReceiver {
    public static final int REQUEST_FROM_DATABASE = 1, GET_IMAGES = 2;

    TinderActivity caller;
    int taskToRedo;

    public ConnectivityReceiver(TinderActivity caller, int taskToRedo) {
        this.caller = caller;
        this.taskToRedo = taskToRedo;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        NetworkInfo info =
                ((ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE))
                .getActiveNetworkInfo();
        if (info != null && info.isConnected()) {
            switch (taskToRedo) {
                case REQUEST_FROM_DATABASE:
                    caller.requestFromDatabaseReceiver();
                    break;
                case GET_IMAGES:
                    caller.requestImagesReceiver();
                    break;
            }
        }
    }
}
