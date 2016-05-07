package com.tinderui;

import android.app.LauncherActivity;
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
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.ImageButton;
import android.widget.Toast;

import com.foodtinder.ListItemClass;
import com.foodtinder.MainActivity;
import com.foodtinder.R;
import com.foodtinder.util.AWSIntegrator;
import com.tinderui.util.AWSIntegratorAsyncTask;
import com.tinderui.support.CustomViewPager;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;


/**
 * Created by pokeforce on 4/12/16.
 */
public class TinderActivity extends AppCompatActivity {

    SwipeImageFragment mainPageFragment;

    /* Local TextView variable to handle list notification number*/
    TextView notification_number = null;

    /* local seekBar variable */
    SeekBar rad_seekBar = null;

    private Toolbar toolbar;
    ArrayList<ListItemClass> data = new ArrayList<>();
    int cnt = 1; // used for notification count

    public ListItemClass createListItem(String foodName)
    {
        ListItemClass newItem = new ListItemClass();
        newItem.setFoodName(foodName);
        newItem.setRestaurantName("test_restaurant");
        newItem.setRestaurantAddress("test_address");
        return newItem;
    }

    /* Drawer declaration */
    public android.support.v4.widget.DrawerLayout my_drawer = null;

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

        /* initialize radius seekBar and link it to a listener*/
        rad_seekBar = (SeekBar) findViewById(R.id.radius_seekBar);
        rad_seekBar.setProgress(15);
        TextView radius_value = (TextView) findViewById(R.id.radius_number);
        radius_value.setText(String.valueOf(rad_seekBar.getProgress()));
        rad_seekBar.setOnSeekBarChangeListener(new rad_seekBar_listener());

        /* Yes and No Buttons:
         * Finding reference to buttons in xml layout to keep as objects in Java */
        Button noButton = (Button) findViewById(R.id.button_no);
        Button yesButton = (Button) findViewById(R.id.button_yes);

        /* Load custom YES/NO button text */
        Typeface Typeface_HamHeaven = Typeface.createFromAsset(getAssets(), "fonts/Hamburger_Heaven.TTF");
        noButton.setTypeface(Typeface_HamHeaven);
        yesButton.setTypeface(Typeface_HamHeaven);

        /* Custom font for Drawer's Header */
        TextView drawer_header = (TextView) findViewById(R.id.drawer_header_text);
        drawer_header.setTypeface(Typeface_HamHeaven);

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
                        && changeListener.state == ViewPager.SCROLL_STATE_IDLE) {
                    imagePager.setCurrentItem(0);
                }
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

            arguments.putInt(SwipeImageFragment.PAGE_POSITION, position);
            imageFragment.setArguments(arguments);

            if (position == 1) mainPageFragment = imageFragment;

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
        public int state = ViewPager.SCROLL_STATE_IDLE;
        ViewPager imagePager;
        int counter = 1, MAX = 3;

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
            if (imagePager.getCurrentItem() != 1 && state == ViewPager.SCROLL_STATE_IDLE) {
                int otherPage;

                /* If swiped left (1 -> 0), other page is 2. Otherwise, it's 0. */
                if (imagePager.getCurrentItem() == 0){
                    otherPage = 2;

                    update_list_number(cnt);

                    /* move liked image into fancy button */
                    ImageView fancy_image = (ImageView) findViewById(R.id.fancy_button_image);
                    //fancy_image.setBackgroundResource();
                    String name = "Food " + cnt;
                    ListItemClass toAdd = createListItem(name);
                    data.add(toAdd);
                    ++cnt;
                }
                else{
                    otherPage = 0;
                }

                /* Testing: changing the image while image page is out of sight */
                mainPageFragment.changeImage(counter);
                counter = (counter + 1) % MAX;

                /* The "new image" animation. Only do it if an animation is idle. */
                imagePager.setCurrentItem(otherPage, false);    /* false = no animation on change */
                imagePager.setCurrentItem(1, true);             /* true = animate */
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

    /* Moves to Like-List Activity */
    public void gotoList(View view) {
        Intent intent = new Intent(TinderActivity.this, LikedListActivity.class);
        intent.putParcelableArrayListExtra("key", data);
        TinderActivity.this.startActivity(intent);
    }

    /* Opens main drawer */
    public void openDrawer(View view) {
        my_drawer = (android.support.v4.widget.DrawerLayout) findViewById(R.id.drawer1);
        my_drawer.setVisibility(View.VISIBLE);
    }

    /* Closes main drawer */
    public void closeDrawer(View view) {
        my_drawer = (android.support.v4.widget.DrawerLayout) findViewById(R.id.drawer1);
        my_drawer.setVisibility(View.GONE);
    }

    public void viewFoodTypeList(View view) {
        FrameLayout tmp = (FrameLayout) findViewById(R.id.types_list);
        ImageView icon = (ImageView) findViewById(R.id.types_dropdown);
        if(tmp.getVisibility() == View.GONE){
            tmp.setVisibility(View.VISIBLE);
            icon.setRotation(180);
        }
        else{
            tmp.setVisibility(View.GONE);
            icon.setRotation(0);
        }
    }

    void update_list_number (int cnt) {
 /* Code for List Notification Number */
        notification_number = (TextView) findViewById(R.id.list_notification);
        if (cnt <= 0) {
            notification_number.setVisibility(View.GONE);
        } else if (cnt <= 99) {
            notification_number.setVisibility(View.VISIBLE);
            notification_number.setText(String.valueOf(cnt));
            if (cnt <= 9) {
                notification_number.setTextSize(17);
                notification_number.setPadding(0, 0, 0, 0);
            } else {
                notification_number.setTextSize(12);
                notification_number.setPadding(0, 0, 0, 3);
            }
        } else {
            notification_number.setVisibility(View.VISIBLE);
            notification_number.setText("+99");
            notification_number.setTextSize(10);
            notification_number.setPadding(0, 0, 0, 4);
        }
        return;
    }
    /* end list notification code */

    /* seekbar listener */
    private class rad_seekBar_listener implements SeekBar.OnSeekBarChangeListener {
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            TextView tmp = (TextView) findViewById(R.id.radius_number);
            if(progress <= 9) {
                if(progress < 5) {
                    progress = 5;
                }
                tmp.setText("  " + progress);
            }
            else {
                tmp.setText("" + progress);
            }
        }

        public void onStartTrackingTouch(SeekBar seekBar) {}

        public void onStopTrackingTouch(SeekBar seekBar) {}

    }
}

