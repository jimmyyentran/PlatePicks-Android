package com.platepicks;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.animation.Animator;
import android.location.Location;
import android.net.wifi.WifiConfiguration;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.FacebookSdk;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.platepicks.objects.FoodReceive;
import com.platepicks.objects.FoodRequest;
import com.platepicks.support.CustomViewPager;
import com.platepicks.util.AWSIntegratorAsyncTask;
import com.platepicks.util.AWSIntegratorInterface;
import com.platepicks.util.ConvertToObject;
import com.platepicks.util.GetImagesAsyncTask;
import com.platepicks.util.ImageLoaderInterface;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;


/**
 * Created by pokeforce on 4/12/16.
 */
public class TinderActivity extends AppCompatActivity
        implements AWSIntegratorInterface, ImageLoaderInterface, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    final int DFLT_IMG_MAX_WIDTH = 1000, DFLT_IMG_MAX_HEIGHT = 1000;
    final int MAX_RADIUS = 40000;   // meters
    final int REQUEST_LOCATION = 1;

    // Location
    GoogleApiClient mGoogleApiClient;

    // Picture of food fragment
    SwipeImageFragment mainPageFragment = null;

    // Splash screen
    FrameLayout splashScreen = null;

    /* local seekBar variable */
    SeekBar rad_seekBar = null;

    /* Local TextView variable to handle list notification number*/
    TextView notification_number = null; //(TextView) findViewById(R.id.list_notification);

    LinearLayout leftFoodTypes, rightFoodTypes; // 2 columns of food types
    // View Pager for swiping
    CustomViewPager imagePager;

    // Data
    ArrayList<ListItemClass> data = new ArrayList<>();
    int cnt = 1; // used for notification count

    // Contains downloaded data from backend. Currently just image urls.
    List<ListItemClass> listItems = new LinkedList<>();  // Actual received data

    ReentrantLock waitForUILock = new ReentrantLock();  // Race condition between first network request and creation of UI
    boolean firstRequest;                               // Flag to indicate first request
    boolean placeholderIsPresent = false;               // Flag to indicate out of images

    ReentrantLock waitForGPSLock = new ReentrantLock(); // Wait for GPS location to be retrieved before making yelp request

    // List of images
    // locks: http://docs.oracle.com/javase/tutorial/essential/concurrency/newlocks.html
    List<Bitmap> imageList = new LinkedList<>();    // Main list used by front end
    ReentrantLock accessList = new ReentrantLock();
    boolean requestMade = false;

    int limit = 4;      // Number of businesses returned per request
    int offset = 0;     // Number of businesses to offset by in yelp request
    String gpsLocation; // "Latitude, Longitude"

    // Function: creates list item
    public ListItemClass createListItem(String foodName) {
        ListItemClass newItem = new ListItemClass();
        newItem.setFoodName(foodName);
        newItem.setRestaurantName("test_restaurant");
        newItem.setRestaurantAddress("test_address");
        return newItem;
    }

    /* Drawer declaration */
    public android.support.v4.widget.DrawerLayout my_drawer = null;

    /* onActivityResult() */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch(requestCode) {
            case (1) :
            {
                if (resultCode == Activity.RESULT_OK)
                {
                    this.data = data.getParcelableArrayListExtra("gohead");
                }
                break;
            }
        }
    }

    /* onCreate():
         * First function called by Android when creating an activity
         * */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /* initialize facebook SDK first */
        FacebookSdk.sdkInitialize(getApplicationContext());

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

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
        final ImageChangeListener changeListener = new ImageChangeListener();
        imagePager.addOnPageChangeListener(changeListener);

        /* initialize radius seekBar and link it to a listener*/
        rad_seekBar = (SeekBar) findViewById(R.id.radius_seekBar);
        rad_seekBar.setProgress(15);
        TextView radius_value = (TextView) findViewById(R.id.radius_number);
        radius_value.setText(String.valueOf(rad_seekBar.getProgress()));
        rad_seekBar.setOnSeekBarChangeListener(new rad_seekBar_listener());

        leftFoodTypes = (LinearLayout) findViewById(R.id.food_types_left);
        rightFoodTypes = (LinearLayout) findViewById(R.id.food_types_right);

        /* Yes and No Buttons:
         * Finding reference to buttons in xml layout to keep as objects in Java */
        Button noButton = (Button) findViewById(R.id.button_no);
        Button yesButton = (Button) findViewById(R.id.button_yes);

        // Load custom YES/NO button text

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

        // First batch of images
        waitForGPSLock.lock();  // Ensure that first network request waits for GPS first
        waitForUILock.lock();   // Ensure that first network request does not post image before UI is visible
        firstRequest = true;    // Flag to remove splash screen after first request
        new RequestFromDatabase().execute();

        /* Splash screen: Covers entire tinder activity for 3 seconds. Created here to simplify
         * calling networks requests in this activity (vs. a splash screen activity) */
        splashScreen = (FrameLayout) findViewById(R.id.framelayout_splashScreen);
        splashScreen.setVisibility(View.VISIBLE);
    }
    /* end onCreate() */

    @Override
    protected void onResume() {
        super.onResume();

        // Release lock once UI is visible to let splash screen be removed and show first pic
        if (waitForUILock.isHeldByCurrentThread())
            waitForUILock.unlock();
    }

    @Override
    protected void onStart() {
        mGoogleApiClient.connect();
        super.onStart();
    }

    @Override
    protected void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
    }

    String getAllFoodTypes() {
        StringBuilder appender = new StringBuilder();

        // Check left food type categories
        for (int i = 0; i < leftFoodTypes.getChildCount(); i++)
            if (((CheckBox) leftFoodTypes.getChildAt(i)).isChecked()) {
                appender.append(FoodTypes.left[i]);
                appender.append(",");
            }

        // Check right food type categories
        for (int i = 0; i < rightFoodTypes.getChildCount(); i++)
            if (((CheckBox) rightFoodTypes.getChildAt(i)).isChecked()) {
                appender.append(FoodTypes.right[i]);
                appender.append(",");
            }

        String categoryFilter = appender.toString();

        // truncate the last extra comma
        if (!categoryFilter.isEmpty())
            categoryFilter = categoryFilter.substring(0, categoryFilter.length() - 1);

        Log.d("TinderActivity", categoryFilter);

//        return "tradamerican," +
//                "chinese";
//                "mexican," +
//                "japanese,jpsweets";
//                "italian",
//                "vietnamese",
//                "thai",
//                "indpak",
//                "mediterranean",
//                "korean"

//        return "japanese,chinese";

        return categoryFilter;
    }

    // Called after requestFromDatabase in doSomethingWithResults()
    void requestImages() {
        int maxHeight = 0;
        int maxWidth = 0;

        // Network request to download images
        if (imagePager != null) {
            maxHeight = imagePager.getHeight();
            maxWidth = imagePager.getWidth();
        }

        if (maxHeight == 0) maxHeight = DFLT_IMG_MAX_HEIGHT;
        if (maxWidth == 0) maxWidth = DFLT_IMG_MAX_WIDTH;

        ArrayList<ListItemClass> requestedImages = new ArrayList<>();
        for (ListItemClass item : listItems)
            if (!item.isDownloaded())
                requestedImages.add(item);

        new GetImagesAsyncTask(this, maxHeight, maxWidth).execute(requestedImages.toArray());
    }

    // Called by AWSIntegratorTask to return json
    @Override
    public void doSomethingWithResults(String ob) {
        offset += limit;    // Successful request -> Increase offset for next request
        List<FoodReceive> foodReceives = ConvertToObject.toFoodReceiveList(ob);

        // Critical section
        accessList.lock();
        listItems.addAll(ConvertToObject.toListItemClassList(foodReceives));
        requestImages();
        accessList.unlock();
    }

    // Called by GetImagesAsyncTask to return list of bitmaps
    @Override
    public void doSomethingWithDownloadedImages(List<Bitmap> images) {
        // Critical Section: Add images to list here in activity
        accessList.lock();

        // Put images into list of images
        imageList.addAll(images);
        requestMade = false;

        // Remove placeholder if one is made
        if (placeholderIsPresent) {
            if (!imageList.isEmpty()) {
                mainPageFragment.changeFood(images.get(0), listItems.get(0));
                imagePager.setSwiping(true);
            } else {
                Toast.makeText(this, "Request failed", Toast.LENGTH_SHORT).show();
            }
        }

        accessList.unlock();

        // Remove splash screen and post first pic
        if (firstRequest) {
            new PostFirstImageTask().execute();
            firstRequest = false;
        }
    }

    int convertMilesToMeters(int radius) {
        int meters = (int) (radius * 1609.344);

        return Math.min(meters, MAX_RADIUS);
    }

    @Override
    public void onConnected(Bundle bundle) {
        Log.d("TinderActivity", "Connected!");

        // If permission not granted
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.d("TinderActivity", "Not allowed...");
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_LOCATION);
        } else {
            getLocation();
        }
    }

    void getLocation() {
        // If permission not granted
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.d("TinderActivity", "Not allowed...");
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_LOCATION);
        }

        Location mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                mGoogleApiClient);
        if (mLastLocation != null) {
            gpsLocation = String.valueOf(mLastLocation.getLatitude()) + ", "
                    + String.valueOf(mLastLocation.getLongitude());
            Log.d("TinderActivity", gpsLocation);
        } else {
            Log.e("TinderActivity", "No location!");
            gpsLocation = "33.7175, -117.8311"; // FIXME: Default is riverside. Should ask for permission/ask for place.
            // Maybe add location services to some settings?
            // what if locations are off?
        }

        if (waitForGPSLock.isHeldByCurrentThread())
            waitForGPSLock.unlock();
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.d("TinderActivity", connectionResult.getErrorMessage());
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case REQUEST_LOCATION:
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getLocation();
                } else {
                    gpsLocation = "33.7175, -117.8311"; // FIXME: Default is riverside. Should ask for permission/ask for place.
                    Toast.makeText(this, "Using Riverside for default", Toast.LENGTH_SHORT).show();
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
                break;
        }
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
        ImageView fancy_image;

        public ImageChangeListener() {
            this.fancy_image = (ImageView) findViewById(R.id.fancy_button_image);
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
                accessList.lock();

                /* If swiped left (1 -> 0), other page is 2. Otherwise, it's 0. */
                if (imagePager.getCurrentItem() == 0) { // Like
                    otherPage = 2;

                    update_list_number(cnt);

                    /* move liked image into fancy button */
                    fancy_image.setImageDrawable(mainPageFragment.getFoodPicture().getDrawable());

                    /* create ListItemClass object passed into LikedListActivity */

                    // if we haven't moved to LikedList yet
                    if (getIntent().hasExtra("gohead"))
                    {
                        data = getIntent().getParcelableArrayListExtra("gohead");
                        Log.d("hello", "i'm getting here");
                    }

                    ListItemClass toAdd = listItems.get(0);

                    // store "yes" bitmap in internal storage
                    Bitmap toSend = imageList.get(0);
                    new ImageSaver(TinderActivity.this).
                            setFileName(toAdd.getFoodId()).
                            setDirectoryName("images").
                            save(toSend);

                    data.add(toAdd);
                    ++cnt;
                } else {    // Dislike
                    otherPage = 0;
                    imageList.get(0).recycle(); // Clear up data
                }

                /* Changing the image while image page is out of sight */
                /* If more images are still around */
                if (imageList.size() > 1) {
                    Log.d("TinderActivity items", listItems.get(1).getFoodId() + "," +
                            listItems.get(1).getFoodName() + "," +
                            listItems.get(1).getRestaurantName() + "," +
                            listItems.get(1).getImageUrl());

                    mainPageFragment.changeFood(imageList.get(1), listItems.get(1)); // Next image
                }
                /* Out of images */
                else {
                    mainPageFragment.changeFood(null, null);    // Put placeholder
                    imagePager.setSwiping(false);               // Disable swipe
                    placeholderIsPresent = true;                // Set flag
                }

                // Either way, remove old data from list
                if (!imageList.isEmpty()) {
                    imageList.remove(0);        // Remove old image from list
                    listItems.remove(0);        // Remove old data from list
                }

                /* Low on images */
                if (imageList.size() < 5 && !requestMade) {
                    new RequestFromDatabase().execute();
                    requestMade = true;
                }

                accessList.unlock();
                // End critical section

                /* The "new image" animation. Only do it if an animation is idle. */
                imagePager.setCurrentItem(otherPage, false);    /* false = no animation on change */
                imagePager.setCurrentItem(1, true);             /* true = animate */
            }
        }
    }

    /* Class to listen to the state of splash screen animation. Only uses onAnimationEnd() to know
     * when the animation is finished. */
    class mAnimatorListener implements Animator.AnimatorListener {
        @Override
        public void onAnimationStart(Animator animation) {
        }

        /* Removes splash screen to save memory (set to GONE) */
        @Override
        public void onAnimationEnd(Animator animation) {
            splashScreen.setVisibility(View.GONE);
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

        /* Count starts over */
        cnt = 1;

        Intent intent = new Intent(TinderActivity.this, LikedListActivity.class);
        intent.putParcelableArrayListExtra("key", data);
        startActivityForResult(intent, 1);

        /* Heart is empty again */
        if (notification_number != null)
            notification_number.setVisibility(View.GONE);

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

    public void removeCheckers (View view) {
        ImageView tmp = (ImageView) findViewById(R.id.top_checkers);
        ImageView tmp2 = (ImageView)findViewById(R.id.bot_checkers);
        if(tmp.getVisibility() == (View.INVISIBLE)) {
            tmp.setVisibility(View.VISIBLE);
            tmp2.setVisibility(View.VISIBLE);
        }
        else{
            tmp.setVisibility(View.INVISIBLE);
            tmp2.setVisibility(View.INVISIBLE);
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

    class RequestFromDatabase extends AsyncTask<Void, Void, Void> {
        // Personal note: the request is called at least by the time the UI is visible in onStart()
        // so we should be able to access all the checkBoxes through getAllFoodTypes() without issue.
        // However, if it turns out that we will access saved food types settings, then this cuts
        // out the reliance on the checkboxes being visible, making 1 less race.

        @Override
        protected Void doInBackground(Void... params) {
            waitForGPSLock.lock();
            waitForGPSLock.unlock();
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            int radius = convertMilesToMeters(rad_seekBar.getProgress());

            FoodRequest req = new FoodRequest("", 3, gpsLocation, limit, radius, getAllFoodTypes(), 1, offset);
            new AWSIntegratorAsyncTask().execute("yelpApi", req, TinderActivity.this);
        }
    }

    // Called after the first network request completes to remove the splash screen and add the
    // first pic. Created because a race condition existed between the UI and the first request.
    class PostFirstImageTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            // Wait for UI thread to release this given lock to indicate that UI is on screen
            waitForUILock.lock();
            waitForUILock.unlock();

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            // Set first image
            mainPageFragment.changeFood(imageList.get(0), listItems.get(0));

            // Fade, then set to gone through listener
            splashScreen.animate()
                    .alpha(0f)
                    .setListener(new mAnimatorListener()); /* Listener to remove view once finished */
        }
    }

    static class FoodTypes {
        static public final String[] left = {
                "tradamerican,newamerican",
                "chinese",
                "mexican",
                "japanese",
                "italian",
                "vietnamese",
                "thai",
                "indpak",
                "mediterranean",
                "korean"
        };

        static public final String[] right = {
                "french",
                "pizza",
                "seafood",
                "dimsum",
                "asianfusion",
                "sandwiches",
                "burgers",
                "hotdogs",
                "coffee",
                "breakfast_brunch"
        };
    }
}

    /* end Tinder Activity */
