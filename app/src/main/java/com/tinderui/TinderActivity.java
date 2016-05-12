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
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.foodtinder.ListItemClass;
import com.foodtinder.R;
import com.tinderui.object.FoodRequest;
import com.tinderui.util.AWSIntegratorAsyncTask;
import com.tinderui.support.CustomViewPager;
import com.tinderui.util.AWSIntegratorInterface;
import com.tinderui.util.GetImagesAsyncTask;
import com.tinderui.util.ImageLoaderInterface;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;


/**
 * Created by pokeforce on 4/12/16.
 */
public class TinderActivity extends AppCompatActivity
        implements AWSIntegratorInterface, ImageLoaderInterface {
    final int DFLT_IMG_MAX_WIDTH = 1000, DFLT_IMG_MAX_HEIGHT = 1000;

    // Picture of food fragment
    SwipeImageFragment mainPageFragment;

    // Splash screen
    FrameLayout splashScreen;

    /* Local TextView variable to handle list notification number*/
    TextView notification_number = null; //(TextView) findViewById(R.id.list_notification);

    // View Pager for swiping
    CustomViewPager imagePager;

    ArrayList<ListItemClass> data = new ArrayList<>();
    int cnt = 1; // used for notification count

    // Contains downloaded data from backend. Currently just image urls.
    ArrayList<String> imageUrls;

    // List of images
    // locks: http://docs.oracle.com/javase/tutorial/essential/concurrency/newlocks.html
    List<Bitmap> imageList = new LinkedList<>();    // Main list used by front end
    ReentrantLock accessList = new ReentrantLock();
    boolean requestMade = false;

    public ListItemClass createListItem(String foodName)
    {
        ListItemClass newItem = new ListItemClass();
        newItem.setFoodName(foodName);
        newItem.setRestaurantName("test_restaurant");
        newItem.setRestaurantAddress("test_address");
        return newItem;
    }

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
        imagePager = (CustomViewPager) findViewById(R.id.viewPager_images);
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
                if (imagePager.getCurrentItem() == 1
                        && changeListener.state == ViewPager.SCROLL_STATE_IDLE) {
                    imagePager.setCurrentItem(0);
                }
            }
        });

        // First batch of images
        requestFromDatabase();

        /* Splash screen: Covers entire tinder activity for 3 seconds. Created here to simplify
         * calling networks requests in this activity (vs. a splash screen activity) */
        splashScreen = (FrameLayout) findViewById(R.id.framelayout_splashScreen);
        splashScreen.setVisibility(View.VISIBLE);
    }

    // Requests for urls from backend AWS database
    void requestFromDatabase() {
        // FIXME: Set up request to put array of image urls into field member imageUrls
        FoodRequest req = new FoodRequest("", 3, "33.7175, -117.8311", 4, 40000, "", 1);
        new AWSIntegratorAsyncTask().execute("yelpApi", req, TinderActivity.this);
    }

    // Called after requestFromDatabase in doSomethingWithResults()
    void requestImages() {
        // Network request to download images
        int maxHeight = imagePager.getHeight(),
            maxWidth = imagePager.getWidth();

        if (maxHeight == 0) maxHeight = DFLT_IMG_MAX_HEIGHT;
        if (maxWidth == 0) maxWidth = DFLT_IMG_MAX_WIDTH;

        new GetImagesAsyncTask(this, maxHeight, maxWidth).execute(imageUrls.toArray());
    }

    // Called by AWSIntegratorTask to return json
    @Override
    public void doSomethingWithResults(String ob) {
        Log.d("TinderActivity", ob);
        imageUrls = parseUrls(ob);
        for (String url : imageUrls) Log.d("TinderActivity", url);

        requestImages();
    }

    // Called by GetImagesAsyncTask to return list of bitmaps
    @Override
    public void doSomethingWithDownloadedImages(List<Bitmap> images) {
        // Critical Section: Add images to list here in activity
        accessList.lock();
        imageList.addAll(images);
        mainPageFragment.changeImage(images.get(0));
        requestMade = false;
        accessList.unlock();

        // Remove splash screen
        if (splashScreen.getVisibility() != View.GONE) {
            // Fade, then set to gone through listener
            splashScreen.animate()
                    .alpha(0f)
                    .setListener(new mAnimatorListener(splashScreen)); /* Listener to remove view once finished */
        }
    }

    ArrayList<String> parseUrls(String s) {
        int index = 0;
        ArrayList<String> urls = new ArrayList<>();

        while ((index = s.indexOf("http", index)) != -1) {
            int jpg = s.indexOf(".jpg", index) + 4;
            urls.add(s.substring(index, jpg));
            index = jpg;
        }

        return urls;
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
                    String name = "Food " + cnt;
                    ListItemClass toAdd = createListItem(name);
                    data.add(toAdd);
                    ++cnt;
                }
                else{
                    otherPage = 0;
                }

                /* FIXME----------------------------------------------------------------- */
                /* Changing the image while image page is out of sight */
                /* If no request is active */
                // Critical section (if request is active)
                boolean needLock = accessList.isLocked() || requestMade;
                if (needLock) {
                    accessList.lock();
                }

                /* If more images are still around */
                if (imageList.size() > 1) {
                    mainPageFragment.changeImage(imageList.get(1)); // Next image
                    imageList.remove(0);                            // Remove old image from list
                }
                /* Out of images */
                else {
                    // FIXME: Null argument should mean placeholder
                    mainPageFragment.changeImage(null);
                }
                /* Low on images */
                if (imageList.size() < 5 && !requestMade) {
                    requestFromDatabase();
                    requestMade = true;
                }

                // End critical section
                if (needLock) {
                    accessList.unlock();
                }
                /* FIXME----------------------------------------------------------------- */

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

    /* Toolbar button functions:
     * Settings,
     * Last-Liked item,
     * Liked-list
     */

    public void gotoList(View view) {
        Intent intent = new Intent(TinderActivity.this, LikedListActivity.class);
        intent.putParcelableArrayListExtra("key", data);
        TinderActivity.this.startActivity(intent);
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
                /* End list notification code */
}

