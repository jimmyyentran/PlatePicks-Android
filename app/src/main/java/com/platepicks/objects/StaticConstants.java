package com.platepicks.objects;

import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by pokeforce on 6/29/16.
 */
// FIXME: REPLACE THIS WITH http://stackoverflow.com/questions/708012/how-to-declare-global-variables-in-android
// FIXME: OR THIS: http://stackoverflow.com/questions/9732796/global-object-of-android-app/9732908#9732908
public class StaticConstants {
    public static final String SAVED_LIKED_FOODS = "Saved foods";

    static public ReentrantLock accessList = new ReentrantLock();     // Race condition to access listItems or imageList
}
