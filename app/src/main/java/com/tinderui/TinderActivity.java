package com.tinderui;

import android.animation.Animator;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.foodtinder.R;
import com.tinderui.support.CustomViewPager;
import com.tinderui.support.SquareImageView;

import java.util.ArrayList;

/**
 * Created by pokeforce on 4/12/16.
 */
public class TinderActivity extends AppCompatActivity {

    /* onCreate():
     * First function called by Android when creating an activity
     * */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /* XML Layout: selecting which file to set as layout */
        setContentView(R.layout.activity_tinderui);

        /* Toolbar: The red bar at the top of the app
         * Will contain our heart-shaped like button and last-recently-liked button */
        setupToolbar();
        if (getSupportActionBar() != null)
            getSupportActionBar().setTitle("Food Tinder");

        /* ViewPager: A view that enables swiping images left and right
         * Has 3 pages, 0-2 (reason is explained in class definition below). */
        final CustomViewPager imagePager = (CustomViewPager) findViewById(R.id.viewPager_images);
        imagePager.setAdapter(new ImagePagerAdapter(getSupportFragmentManager()));

        /* Ensure that we start on page 1, the middle page with the image. */
        imagePager.setCurrentItem(1, false);

        /* Listen for change in swipe animation's current state */
        final ImageChangeListener changeListener = new ImageChangeListener(imagePager);
        imagePager.addOnPageChangeListener(changeListener);

        /* Yes and No Buttons:
         * Finding reference to buttons in xml layout to keep as objects in Java */
        Button noButton = (Button) findViewById(R.id.button_no);
        Button yesButton = (Button) findViewById(R.id.button_yes);

        /* On Click Listeners:
         * Functions that are called whenever the user clicks on the buttons or image */
        noButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (imagePager.getCurrentItem() == 1
                        && changeListener.state == ViewPager.SCROLL_STATE_IDLE)
                    imagePager.setCurrentItem(2);
            }
        });
        yesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (imagePager.getCurrentItem() == 1
                        && changeListener.state == ViewPager.SCROLL_STATE_IDLE)
                    imagePager.setCurrentItem(0);
            }
        });

        /* Splash screen: Covers entire tinder activity for 3 seconds. Created here to simplify
         * calling networks requests in this activity (vs. a splash screen activity) */
        FrameLayout splashScreen = (FrameLayout) findViewById(R.id.framelayout_splashScreen);
        splashScreen.setVisibility(View.VISIBLE);
        splashScreen.animate()
                .alpha(0f)
                .setStartDelay(3000)
                .setListener(new mAnimatorListener(splashScreen)); /* Listener to remove view once finished */
    }

    /* onCreateOptionsMenu():
     * Creates the menu by loading the items in the xml into the toolbar */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_tinder, menu);
        return true;
    }

    /* OnOptionsItemSelected():
     * The function that is called when a menu option is clicked. If true is returned, we should
     * handle the menu click here. If false, Android will try to handle it.*/
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.item_like_list) {
            Toast.makeText(this, "Like list", Toast.LENGTH_SHORT).show();
            return true;
        }

        return false;
    }

    /* setupToolbar():
     * Fetches toolbar from loaded xml file and sets as the "action bar" (what Android calls the
     * top bar. Toolbar is a new class with extra features.) */
    void setupToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_tinder);
        setSupportActionBar(toolbar);
    }

    /* ImagePagerAdapter:
     * Feeds ViewPager the imageViews for its pages */
    class ImagePagerAdapter extends FragmentStatePagerAdapter {
        public ImagePagerAdapter(FragmentManager fm) {
            super(fm);
        }

        /* getItem():
         * Create SwipeImageFragment object, then give it the position as an argument to determine
         * which picture to display for that fragment. Different arguments will be used for images
         * from the internet. */
        @Override
        public Fragment getItem(int position) {
            SwipeImageFragment imageFragment = new SwipeImageFragment();
            Bundle arguments = new Bundle();

            arguments.putInt(SwipeImageFragment.INDEX, position);
            imageFragment.setArguments(arguments);

            return imageFragment;
        }

        @Override
        public int getCount() {
            return 3;
        }
    }

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
    class ImageChangeListener extends ViewPager.SimpleOnPageChangeListener {
        ViewPager imagePager;
        public int state = ViewPager.SCROLL_STATE_IDLE;

        public ImageChangeListener(ViewPager imagePager) {
            this.imagePager = imagePager;
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
            if (imagePager.getCurrentItem() != 1) {
                int otherPage;

                /* If swiped left (1 -> 0), other page is 2. Otherwise, it's 0. */
                if (imagePager.getCurrentItem() == 0) otherPage = 2;
                else otherPage = 0;

                /* The "new image" animation. Only do it if an animation is idle. */
                if (state == ViewPager.SCROLL_STATE_IDLE) {
                    imagePager.setCurrentItem(otherPage, false);
                    imagePager.setCurrentItem(1, true);
                }
            }
        }
    }

    /* Class to listen to the state of splash screen animation. Only uses onAnimationEnd() to know
     * when the animation is finished. */
    class mAnimatorListener implements Animator.AnimatorListener {
        FrameLayout splashScreen;

        mAnimatorListener(FrameLayout splashScreen) {
            this.splashScreen = splashScreen;
        }

        @Override
        public void onAnimationStart(Animator animation) {

        }

        /* Removes splash screen to save memory (set to GONE) */
        @Override
        public void onAnimationEnd(Animator animation) {
            splashScreen.setVisibility(View.GONE);
            splashScreen = null;
        }

        @Override
        public void onAnimationCancel(Animator animation) {

        }

        @Override
        public void onAnimationRepeat(Animator animation) {

        }
    }
}
