package com.tinderui;

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
import android.widget.Toast;

import com.foodtinder.R;

import java.util.ArrayList;

/**
 * Created by pokeforce on 4/12/16.
 */
public class TinderActivity extends AppCompatActivity {
    private Toolbar toolbar;

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

        /* Yes and No Buttons:
         * Finding reference to buttons in xml layout to keep as objects in Java */
        Button noButton = (Button) findViewById(R.id.button_no);
        Button yesButton = (Button) findViewById(R.id.button_yes);

        /* On Click Listeners:
         * Functions that are called whenever the user clicks on the buttons */
        noButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(TinderActivity.this, "I clicked No!", Toast.LENGTH_SHORT).show();
            }
        });
        yesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(TinderActivity.this, "I clicked Yes!", Toast.LENGTH_SHORT).show();
            }
        });

        /* ViewPager:
         * A view that enables swiping images left and right */
        final ViewPager imagePager = (ViewPager) findViewById(R.id.viewPager_images);
        imagePager.setAdapter(new ImagePagerAdapter(getSupportFragmentManager()));

        /* Ensure that we start on page 1. The goal is to display swiping and use a viewpager
         * to imitate Tinder's image animation. The issue with this is that viewpager does not have
         * an unlimited number of imageViews. It's like a list - reach the end, and don't loop back
         * to the beginning = a limited number of times we can swipe left or right. More on this
         * with the onPageChangeListener code. */
        imagePager.setCurrentItem(1, false);

//        imagePager.addOnPageChangeListener(new ImageChangeListener(imagePager));
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
     * The function that is called when a menu option is clicked */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.item_like_list) {
            Toast.makeText(this, "Like list", Toast.LENGTH_SHORT).show();
        }

        return super.onOptionsItemSelected(item);
    }

    /* setupToolbar():
     * Fetches toolbar from loaded xml file and sets as the "action bar" (what Android calls the
     * top bar. Toolbar is a new class with extra features.) */
    void setupToolbar() {
        toolbar = (Toolbar) findViewById(R.id.toolbar_tinder);
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

    /* To use viewpager, we need to show an empty page temporarily when the user swipes, change the
     * second page to the correct image, then jump to the other empty page we have (0 or 2) and
     * animate a page change back to two. */
    enum PageState { NOT_CHANGING, ON_SWIPE, BACK_TO_CENTER };

    class ImageChangeListener implements ViewPager.OnPageChangeListener {
        ViewPager imagePager;
        PageState state = PageState.NOT_CHANGING;

        public ImageChangeListener(ViewPager imagePager) {
            this.imagePager = imagePager;
        }

        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

        }

        @Override
        public void onPageSelected(int position) {
            // FIXME
            // States to consider
            // Not changing -> Change to 0 or 2 -> Change to 1 -> No change

            switch (state) {
                case NOT_CHANGING:
                    Log.d("TinderActivity", "changed page on swipe");
                    state = PageState.ON_SWIPE;

                    if (position == 0) imagePager.setCurrentItem(2, false);
                    else if (position == 2) imagePager.setCurrentItem(0, false);

                    break;
                case ON_SWIPE:
                    Log.d("TinderActivity", "back to center");
                    state = PageState.BACK_TO_CENTER;
                    imagePager.setCurrentItem(1, true);
                    break;
                case BACK_TO_CENTER:
                    Log.d("TinderActivity", "not changing");
                    state = PageState.NOT_CHANGING;
                    break;
            }
        }

        @Override
        public void onPageScrollStateChanged(int state) {

        }
    }
}
