package com.tinderui.support;

import android.content.Context;
import android.support.v4.view.MotionEventCompat;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ViewConfiguration;

/**
 * Created by pokeforce on 4/27/16.
 */
/* ViewPager class that intercepts screen touches during animation of swipe to prevent bugs
 * May also slow down the swiping animation (Maybe).
 * Useful: http://developer.android.com/training/gestures/viewgroup.html */
public class CustomViewPager extends ViewPager {
    AnimationStateListener listener;

    public CustomViewPager(Context context) {
        super(context);

        /* To prevent interference during swipe animation */
        listener = new AnimationStateListener();
        addOnPageChangeListener(listener);
    }

    public CustomViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);

        /* To prevent interference during swipe animation */
        listener = new AnimationStateListener();
        addOnPageChangeListener(listener);
    }

    /* onInterceptTouchEvent(): Can intercept touches from children views (the images/pages)
     * Called before onTouchEvent(). True = intercept & use our onTouchEvent(). False = default. */
    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (getCurrentItem() != 1 || listener.state != ViewPager.SCROLL_STATE_IDLE)
            return true;

        /* Do not intercept event for the case of the touch gesture being complete (cancel or up),
         * or the case that the current page is the tinder image */
        return super.onInterceptTouchEvent(ev);
    }

    /* onTouchEvent(): Handle a touch event. Ignore events during a swipe animation.
     * If current page is the image and no animation is running (User is dragging or screen is
     * idle, accept the event. */
    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        return getCurrentItem() == 1 && listener.state != ViewPager.SCROLL_STATE_SETTLING
                && super.onTouchEvent(ev);
    }

    class AnimationStateListener extends SimpleOnPageChangeListener {
        public int state = ViewPager.SCROLL_STATE_IDLE;

        @Override
        public void onPageScrollStateChanged(int state) {
            this.state = state;
            super.onPageScrollStateChanged(state);
        }
    }
}
