package com.platepicks.dynamoDB;

import android.util.Log;

import com.platepicks.objects.FoodReceive;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by jimmytran on 5/15/16.
 */
public final class TableFood {
    private static final String LOG_TAG = TableFood.class.getSimpleName();

    /*
    * Query the dabase and return a FoodReceive
    * - requires two calls:
    *   1. Call to food table to get restaurantId
    *   2. Call to restaurant table for the rest
    *
    * - package all info into a FoodReceive
    * */
    public static FoodReceive getFood (String foodId){
        URL myURL = null;
        try{
            myURL = new URL("http://google.com");
        }catch (MalformedURLException e){
            System.out.println("Bad URL");
        }
        return new FoodReceive("1", null , "3", myURL);
    }

    /*
    * Append the 'like' attribute in table by 1
    *
    * Log if successful
    * */
    public static void likeFood (String foodId){
//        Log.d(LOG_TAG, "Like food update successful: " + foodId);
    }

    /*
    * Append the 'dislike' attribute in table by 1
    *
    * Log if successful
    * */
    public static void dislikeFood (String foodId){
//        Log.d(LOG_TAG, "Dislike food update successful: " + foodId);
    }


}
