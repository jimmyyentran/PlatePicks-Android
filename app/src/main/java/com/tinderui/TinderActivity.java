package com.tinderui;


import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.animation.Animator;
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
import android.widget.TextView;
import android.widget.Toast;

import com.foodtinder.R;
import com.google.android.gms.common.api.GoogleApiClient;
import com.tinderui.support.CustomViewPager;
import com.tinderui.support.SquareImageView;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;


/**
 * Created by pokeforce on 4/12/16.
 */
public class TinderActivity extends AppCompatActivity {
    private Toolbar toolbar;
    ArrayList<String> data = new ArrayList<>();
    int cnt = 0;
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;



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
        //setupToolbar();
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

        // Load custom YES/NO button text
        Typeface Typeface_HamHeaven = Typeface.createFromAsset(getAssets(), "fonts/Hamburger_Heaven.TTF");
        noButton.setTypeface(Typeface_HamHeaven);
        yesButton.setTypeface(Typeface_HamHeaven);

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
                String toAdd = "Food " + cnt;
                data.add(toAdd);
                cnt++;
                if (imagePager.getCurrentItem() == 1
                        && changeListener.state == ViewPager.SCROLL_STATE_IDLE)
                    imagePager.setCurrentItem(0);

                /* Code for List Notification Number */
                final TextView notification_number = (TextView) findViewById(R.id.list_notification);
                if(cnt <= 0){
                    notification_number.setVisibility(View.GONE);
                }
                else if(cnt <= 99){
                    notification_number.setVisibility(View.VISIBLE);
                    notification_number.setText(String.valueOf(cnt));
                    if(cnt <= 9){
                        notification_number.setTextSize(17);
                        notification_number.setPadding(0, 0, 0, 0);
                    }
                    else{
                        notification_number.setTextSize(12);
                        notification_number.setPadding(0, 0, 0, 3);
                    }
                }
                else{
                    notification_number.setVisibility(View.VISIBLE);
                    notification_number.setText("+99");
                    notification_number.setTextSize(10);
                    notification_number.setPadding(0, 0, 0, 4);
                }
                /* End list notification code */

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


    /* Queue of bitmaps: storing images
     * acting as a cache
     * collecting all results from url lookups
     */
    Queue<Bitmap> imageCache = new LinkedList<>();

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

    /* Toolbar button functions:
     * Settings,
     * Last-Liked item,
     * Liked-list
     */

    public void gotoList(View view) {
        Intent intent = new Intent(TinderActivity.this, LikedListActivity.class);
        intent.putExtra("whatever", data);
        intent.putExtra("cnt", cnt);
        TinderActivity.this.startActivity(intent);
    }

}
