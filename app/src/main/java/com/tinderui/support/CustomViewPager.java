package com.tinderui.support;

import android.content.Context;
import android.support.v4.view.MotionEventCompat;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

/**
 * Created by pokeforce on 4/27/16.
 */
/* ViewPager class that intercepts screen touches during animation of swipe to prevent bugs
 * May also slow down the swiping animation (Maybe). */
public class CustomViewPager extends ViewPager {
    public CustomViewPager(Context context) {
        super(context);
    }

    public CustomViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    /* onInterceptTouchEvent(): Can intercept touches from children views (the images/pages)
     * Called before onTouchEvent(). True = intercept & use our onTouchEvent(). False = default. */
    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        final int action = MotionEventCompat.getActionMasked(ev);

        /* Do not intercept event for the case of the touch gesture being complete (cancel or up),
         * or the case that the current page is the tinder image */
        return !(action == MotionEvent.ACTION_CANCEL || action == MotionEvent.ACTION_UP
                || getCurrentItem() == 1);
    }

    /* onTouchEvent(): Handle a touch event. Ignore events during a swipe animation */
    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        /* If current page is not the image, ignore the swipe event. */
        return getCurrentItem() == 1 && super.onTouchEvent(ev);
    }
}
