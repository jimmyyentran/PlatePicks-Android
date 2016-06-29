package com.platepicks.util;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.support.v4.view.ViewPager;

import com.platepicks.*;
import com.platepicks.dynamoDB.TableFood;
import com.platepicks.objects.FoodReceive;
import com.platepicks.objects.ListItemClass;
import com.platepicks.support.CustomViewPager;

import java.util.HashMap;

/* Algorithm for Tinder Image Swiping Infinitely
 *
 * The goal is to display swiping and use a viewpager to imitate Tinder's image animation. The
 * issue with this is that viewpager does not let us loop pages. It's like a list - reach the
 * end, and don't loop back to the beginning = a limited number of times we can swipe left or
 * right.
 *
 * To get around this, we need to show an empty page temporarily when the user swipes,
 * change the second page to the correct image, then jump to the other empty page we have (0 or
 * 2) and animate a page change back to 1. This fakes a new image coming in from the correct
 * side. */
public class ImageChangeListener extends ViewPager.SimpleOnPageChangeListener {
    public int state = ViewPager.SCROLL_STATE_IDLE;

    TinderActivity caller;
    CustomViewPager imagePager;
    HashMap<FoodReceive, Boolean> cacheForDatabase; // Accumulate 6 likes/dislikes before request

    public ImageChangeListener(TinderActivity caller, CustomViewPager imagePager) {
        this.caller = caller;
        this.imagePager = imagePager;
        this.cacheForDatabase = new HashMap<>(6);
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {

    }

    /* onPageScrollStateChanged()
     * Tracks what state the swiping animation is in. */
    @Override
    public void onPageScrollStateChanged(int state) {
        this.state = state;

            /* Only do this animation if swiping away from the image */
        if (imagePager.getCurrentItem() != 1 && state == ViewPager.SCROLL_STATE_IDLE) {
            int otherPage;

            // Critical section (if request is active)
            caller.accessList.lock();

                /* If swiped left (1 -> 0), other page is 2. Otherwise, it's 0. */
            if (imagePager.getCurrentItem() == 0) { // Like
                otherPage = 2;

                caller.heartPulse();
                caller.update_list_number();

                /* create ListItemClass object passed into LikedListActivity */
                ListItemClass toAdd = caller.getListItems().get(0);
                Bitmap toSend = caller.getImageList().get(0);   // store "yes" bitmap in internal storage
                new ImageSaver(caller).
                        setFileName(toAdd.getFoodId()).
                        setDirectoryName("images").
                        save(toSend);

                /* Save file to internal storage */
                new WriteToLikedFileTask(caller, WriteToLikedFileTask.ADD_ITEM)
                        .execute(toAdd.getFileString());

                caller.addToLikedData(toAdd);

                // Send like to database
                cacheForDatabase.put(caller.getListItems().get(0).getOriginal(), true);
            } else {    // Dislike
                ListItemClass toAdd = caller.getListItems().get(0);

                otherPage = 0;
                caller.getImageList().get(0).recycle(); // Clear up data
                new ImageSaver(caller)
                        .setFileName(toAdd.getFoodId())
                        .setDirectoryName("images")
                        .delete();

                // Send dislike to database
                cacheForDatabase.put(caller.getListItems().get(0).getOriginal(), false);
            }

            /* Changing the image while image page is out of sight */
            /* If more images are still around */
            if (caller.getImageList().size() > 1) {
                caller.changeToNextFood();
            }
            /* Out of images */
            else {
                caller.changeToPlaceholder();
            }

            // Either way, remove old data from list
            if (!caller.getImageList().isEmpty()) {
                caller.getImageList().remove(0);        // Remove old image from list
                caller.getListItems().remove(0);        // Remove old data from list
            }

                /* Low on images */
            if (caller.getImageList().size() < 5 && !caller.isRequestMade()) {
                caller.setRequestMade(true);
                new RequestFromDatabaseTask(caller).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            }

            /* After certain number of requests are accumulated, they are all sent to database
               in one thread. */
            if (cacheForDatabase.size() >= 6)
                uploadLikesData();

            caller.accessList.unlock();
            // End critical section

                /* The "new image" animation. Only do it if an animation is idle. */
            imagePager.setCurrentItem(otherPage, false);    /* false = no animation on change */
            imagePager.setCurrentItem(1, true);             /* true = animate */
        }
    }

    // Send like/dislike data to amazon database
    private void uploadLikesData() {
        final HashMap<FoodReceive, Boolean> copyCache = new HashMap<>(cacheForDatabase);
        cacheForDatabase.clear();

        // If no internet, abort trying to send data
        if (!ConnectionCheck.isConnected(caller))
            return;

        new Thread(new Runnable() {
            public void run() {
                for (FoodReceive fr : copyCache.keySet()) {
                    if (copyCache.get(fr))
                        TableFood.likeFoodC(fr, caller);
                    else
                        TableFood.dislikeFoodC(fr, caller);
                }

                copyCache.clear();
            }
        }).start();
    }


}
