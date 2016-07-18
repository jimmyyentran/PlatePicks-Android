package com.platepicks.util;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.platepicks.SwipeImageFragment;
import com.platepicks.TinderActivity;

/**
 * Created by pokeforce on 6/16/16.
 */
/* ImagePagerAdapter:
 * Feeds ViewPager the imageViews for its pages */
public class ImagePagerAdapter extends FragmentStatePagerAdapter {
    TinderActivity caller;

    public ImagePagerAdapter(FragmentManager fm, TinderActivity caller) {
        super(fm);
        this.caller = caller;
    }

    /* getItem():
     * Create SwipeImageFragment object, then give it the position as an argument to determine
     * which picture to display for that fragment. Different arguments will be used for images
     * from the internet. */
    @Override
    public Fragment getItem(int position) {
        SwipeImageFragment imageFragment;

        if (position != 1) {
            imageFragment = new SwipeImageFragment();
        } else {
            imageFragment = caller.getMainPageFragment();
        }

        Bundle arguments = new Bundle();
        arguments.putInt(SwipeImageFragment.PAGE_POSITION, position);
        imageFragment.setArguments(arguments);

        return imageFragment;
    }

    /* getCount():
     * Number of pages in viewpager. 0 and 2 are empty pages. 1 is the image page */
    @Override
    public int getCount() {
        return 3;
    }

    /* getPageWidth():
     * If not page 1, page should be shorter to reduce whitespace */
    @Override
    public float getPageWidth(int position) {
        if (position != 1)
            return .98f;

        return 1f;
    }
}
