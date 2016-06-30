package com.platepicks.objects;

import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by pokeforce on 6/29/16.
 */
public class StaticConstants {
    public static final String SAVED_LIKED_FOODS = "Saved foods";

    static public ReentrantLock accessList = new ReentrantLock();     // Race condition to access listItems or imageList
}
