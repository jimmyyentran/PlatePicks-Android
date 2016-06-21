package com.platepicks.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * Created by pokeforce on 6/21/16.
 */
public class ConnectionCheck {
    static NetworkInfo getNetworkInfo(Context context) {
        ConnectivityManager cm =
                (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);

        return cm.getActiveNetworkInfo();
    }

    public static boolean isConnected(Context context) {
        NetworkInfo activeNetwork = getNetworkInfo(context);

        return activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();
    }

    public static boolean isWifi(Context context) {
        NetworkInfo activeNetwork = getNetworkInfo(context);

        return activeNetwork != null &&
                activeNetwork.getType() == ConnectivityManager.TYPE_WIFI;
    }
}
