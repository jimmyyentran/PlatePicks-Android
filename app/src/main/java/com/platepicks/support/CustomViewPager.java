package com.platepicks.support;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

/**
 * Created by pokeforce on 4/27/16.
 */
/* ViewPager class that intercepts screen touches during animation of swipe to prevent bugs
 * May also slow down the swiping animation (Maybe).
 * Useful: http://developer.android.com/training/gestures/viewgroup.html */
public class CustomViewPager extends ViewPager {
    AnimationStateListener listener;
    boolean canSwipe = false;

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
        /* If swipe is disabled, do not intercept touch. */
        if (!canSwipe)
            return false;

        /* If it's not first item or first item is not stable, do not react to swipe (intercept). */
        if (getCurrentItem() != 1 || listener.state != ViewPager.SCROLL_STATE_IDLE)
            return true;

        /* Do not intercept event for the case of the touch gesture being complete (cancel or up),
         * or the case that the current page is the tinder image */
        return super.onInterceptTouchEvent(ev);
    }

    @Override
    public void setCurrentItem(int item) {
        // If swipe is disabled (and front page not visible), do not change item
        if (canSwipe || getCurrentItem() != 1)
            super.setCurrentItem(item);
    }

    @Override
    public void setCurrentItem(int item, boolean smoothScroll) {
        // If swipe is disabled (and front page not visible), do not change item
        if (canSwipe || getCurrentItem() != 1)
            super.setCurrentItem(item, smoothScroll);
    }

    /* setSwiping(): Enable or disable swiping */
    public void setSwiping(boolean enable) {
        this.canSwipe = enable;
    }

    public boolean getSwiping() {
        return this.canSwipe;
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
