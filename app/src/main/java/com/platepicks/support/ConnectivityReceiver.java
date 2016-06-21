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
    TinderActivity caller;

    public ConnectivityReceiver(TinderActivity caller) {
        this.caller = caller;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        NetworkInfo info =
                ((ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE))
                .getActiveNetworkInfo();
        if (info != null && info.isConnected())
            caller.requestImagesReceiver();
    }
}
