package com.platepicks;

import android.Manifest;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.animation.Animator;
import android.location.Location;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.view.animation.BounceInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.ScaleAnimation;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.FacebookSdk;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.vision.Frame;
import com.platepicks.dynamoDB.TableFood;
import com.platepicks.objects.FoodReceive;
import com.platepicks.objects.FoodRequest;
import com.platepicks.support.CustomViewPager;
import com.platepicks.support.SquareImageButton;
import com.platepicks.util.AWSIntegratorAsyncTask;
import com.platepicks.util.AWSIntegratorInterface;
import com.platepicks.util.ConvertToObject;
import com.platepicks.util.GetImagesAsyncTask;
import com.platepicks.util.ImageLoaderInterface;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;


/**
 * Created by pokeforce on 4/12/16.
 */
public class TinderActivity extends AppCompatActivity
        implements AWSIntegratorInterface,
            ImageLoaderInterface,
            GoogleApiClient.ConnectionCallbacks,
            GoogleApiClient.OnConnectionFailedListener {
    final int DFLT_IMG_MAX_WIDTH = 1000, DFLT_IMG_MAX_HEIGHT = 1000;
    final int MAX_RADIUS = 40000;   // meters
    final int REQUEST_LOCATION = 1;

    GoogleApiClient mGoogleApiClient; // Google location client

    SwipeImageFragment mainPageFragment = null; // Picture of food fragment
    RelativeLayout splashScreen = null;         // Splash screen
    SeekBar rad_seekBar = null;                 // local seekBar variable
    TextView notification_number = null;        // list notification number
    LinearLayout leftFoodTypes, rightFoodTypes; // 2 columns of food types
    CustomViewPager imagePager;                 // View Pager for swiping
    DrawerLayout my_drawer = null;              // Drawer layout
    FrameLayout drawer_space = null;

    List<ListItemClass> listItems = new LinkedList<>();     // Data received from network request
    List<Bitmap> imageList = new LinkedList<>();            // Images downloaded in network request
    ArrayList<ListItemClass> likedData = new ArrayList<>(); // Food liked by user

    ReentrantLock waitForUILock = new ReentrantLock();  // Race condition between first network request and creation of UI
    ReentrantLock waitForGPSLock = new ReentrantLock(); // Wait for GPS location to be retrieved before making yelp request
    ReentrantLock accessList = new ReentrantLock();     // Race condition to access listItems or imageList
    boolean firstRequest;                               // Flag to indicate first request
    boolean placeholderIsPresent = false;               // Flag to indicate out of images
    boolean requestMade = false;                        // Flag to indicate making a request

    int cnt = 1;        // used for notification count of new liked foods
    int limit = 20;     // Number of businesses returned per request
    int foodLimit = 3;  // Number of food per business
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

    ImageChangeListener changeListener = null;

    boolean drawerOpened = false;

    /* bell ImageViews */
    ImageView bellShell = null;
    ImageView bellStand = null;
    ImageView bellHammer = null;

    /* bell animations */
    Animation hammer_drop = null;
    Animation hammer_rise = null;

    /* yes/no onHold constrictors */
    boolean yesHeld = false;
    boolean noHeld = false;

    /* grow/shrink scaleAnimation declarations */
    ScaleAnimation growAnim = null;
    ScaleAnimation shrinkAnim = null;

    /* onActivityResult() */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch(requestCode) {
            case (1) :
            {
                if (resultCode == Activity.RESULT_OK)
                {
                    this.likedData = data.getParcelableArrayListExtra("gohead");
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
        setContentView(R.layout.fresh_and_trendy);

        /* ViewPager: A view that enables swiping images left and right
         * Has 3 pages, 0-2 (reason is explained in class definition below). */
        imagePager = (CustomViewPager) findViewById(R.id.viewPager_images);
        imagePager.setAdapter(new ImagePagerAdapter(getSupportFragmentManager()));

        /* Ensure that we start on page 1, the middle page with the image. */
        imagePager.setCurrentItem(1, false);
        imagePager.getCurrentItem();            // Ensure item is defined

        /* Listen for change in swipe animation's current state */
        changeListener = new ImageChangeListener();
        imagePager.addOnPageChangeListener(changeListener);

        /* initialize radius seekBar and link it to a listener*/
        rad_seekBar = (SeekBar) findViewById(R.id.radius_seekBar);
        rad_seekBar.setProgress(15);
        TextView radius_value = (TextView) findViewById(R.id.radius_number);
        radius_value.setText(String.valueOf(rad_seekBar.getProgress()));
        rad_seekBar.setOnSeekBarChangeListener(new rad_seekBar_listener());

        leftFoodTypes = (LinearLayout) findViewById(R.id.food_types_left);
        rightFoodTypes = (LinearLayout) findViewById(R.id.food_types_right);

        /* assign bell imageviews */
        bellShell = (ImageView) findViewById(R.id.bell_shell);
        bellStand = (ImageView) findViewById(R.id.bell_stand);
        bellHammer = (ImageView) findViewById(R.id.bell_hammer);

        /* assign bell animations */
        hammer_drop = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.bell_hammer_drop);
        hammer_rise = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.bell_hammer_rise);

        hammer_drop.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                bellHammer.startAnimation(hammer_drop);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        /* Yes and No Buttons:
         * Finding reference to buttons in xml layout to keep as objects in Java */
        RelativeLayout noButton = (RelativeLayout) findViewById(R.id.button_no);
        final RelativeLayout yesButton = (RelativeLayout) findViewById(R.id.button_yes);

        // Load custom YES/NO button text

        /* On Click Listeners:
         * Functions that are called whenever the user clicks on the buttons or image */

        noButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getAction() == MotionEvent.ACTION_DOWN) {
                    noHeld();
                }
                else if(event.getAction() == MotionEvent.ACTION_UP){
                    noReleased();
                    //borderFlash("red");
                    /*
                    if (imagePager.getCurrentItem() == 1
                            && changeListener.state == ViewPager.SCROLL_STATE_IDLE) {
                        imagePager.setCurrentItem(2);
                        MediaPlayer mp = MediaPlayer.create(getApplicationContext(), R.raw.click_sound1);
                        //mp.start();
                    }
                    */
                }
                return true;
            }
        });

        yesButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getAction() == MotionEvent.ACTION_DOWN){
                    yesHold();
                }
                else if(event.getAction() == MotionEvent.ACTION_UP){
                    yesReleased();
                    //borderFlash("green");
                    /*
                    if (imagePager.getCurrentItem() == 1
                            && changeListener.state == ViewPager.SCROLL_STATE_IDLE) {
                        imagePager.setCurrentItem(0);
                        MediaPlayer mp = MediaPlayer.create(getApplicationContext(), R.raw.click_sound1);
                        //mp.start();
                    }
                    */
                }
                return true;
            }
        });

        // First batch of images
        waitForGPSLock.lock();  // Ensure that first network request waits for GPS first
        waitForUILock.lock();   // Ensure that first network request does not post image before UI is visible
        firstRequest = true;    // Flag to remove splash screen after first request
        new RequestFromDatabase().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

        /* Splash screen: Covers entire tinder activity for 3 seconds. Created here to simplify
         * calling networks requests in this activity (vs. a splash screen activity) */
        splashScreen = (RelativeLayout) findViewById(R.id.layout_splashScreen);
        splashScreen.setVisibility(View.VISIBLE);

        /* Drawer gesture detector */
        my_drawer= (android.support.v4.widget.DrawerLayout) findViewById(R.id.drawer1);
        GestureDetector gd;
        gd = new GestureDetector(my_drawer.getContext(), new GestureDetector.OnGestureListener() {
            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                closeDrawer(my_drawer);
                return true;
            }

            @Override
            public boolean onDown(MotionEvent e) {
                return false;
            }

            @Override
            public void onShowPress(MotionEvent e) {

            }

            @Override
            public boolean onSingleTapUp(MotionEvent e) {
                return false;
            }

            @Override
            public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
                return false;
            }

            @Override
            public void onLongPress(MotionEvent e) {

            }
        });

        /* NavDrawer onClick listeners */
        TextView okBtn = (TextView) findViewById(R.id.types_ok_button);
        TextView clearFavBtn = (TextView) findViewById(R.id.reset_list);
        TextView clearTypesBtn = (TextView) findViewById(R.id.types_clear_button);

        clearTypesBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for (int i = 0; i < leftFoodTypes.getChildCount(); i++){
                    if(((CheckBox) leftFoodTypes.getChildAt(i)).isChecked())
                        ((CheckBox) leftFoodTypes.getChildAt(i)).toggle();
                }
                for (int i = 0; i < rightFoodTypes.getChildCount(); i++){
                    if(((CheckBox) rightFoodTypes.getChildAt(i)).isChecked())
                        ((CheckBox) rightFoodTypes.getChildAt(i)).toggle();
                }
            }
        });

        /* slogan typeface setup */
        TextView line1 = (TextView) findViewById(R.id.new_food);
        TextView line2 = (TextView) findViewById(R.id.new_places);
        TextView line3 = (TextView) findViewById(R.id.you_pick);

        Typeface source_bold = Typeface.createFromAsset(getAssets(), "fonts/SourceSansPro-Bold.otf");

        line1.setTypeface(source_bold);
        line2.setTypeface(source_bold);
        line3.setTypeface(source_bold);

                /* Custom font for Drawer's Header */
        TextView drawer_header = (TextView) findViewById(R.id.drawer_header_text);
        drawer_header.setTypeface(source_bold);


        Typeface Ham_Heaven = Typeface.createFromAsset(getAssets(), "fonts/Hamburger_Heaven.TTF");
        TextView appName2 = (TextView) findViewById(R.id.app_name_2);
        appName2.setTypeface(Ham_Heaven);


        /* set shrink/grow animation */


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
        StringBuilder default_appender = new StringBuilder();

        /* check for default search */
        boolean nothing_checked = true;

        // Check left food type categories
        for (int i = 0; i < leftFoodTypes.getChildCount(); i++) {
            default_appender.append(FoodTypes.left[i]);
            default_appender.append(",");
            if (((CheckBox) leftFoodTypes.getChildAt(i)).isChecked()) {
                appender.append(FoodTypes.left[i]);
                appender.append(",");
                nothing_checked = false;
            }
        }

        // Check right food type categories
        for (int i = 0; i < rightFoodTypes.getChildCount(); i++) {
            default_appender.append(FoodTypes.right[i]);
            default_appender.append(",");
            if (((CheckBox) rightFoodTypes.getChildAt(i)).isChecked()) {
                appender.append(FoodTypes.right[i]);
                appender.append(",");
                nothing_checked = false;
            }
        }

        /* determine which appender to use */
        String categoryFilter;

        if(nothing_checked)
            categoryFilter = default_appender.toString();
        else
            categoryFilter = appender.toString();

        // truncate the last extra comma
        if (!categoryFilter.isEmpty())
            categoryFilter = categoryFilter.substring(0, categoryFilter.length() - 1);

        Log.d("TinderActivity", categoryFilter);

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

        new GetImagesAsyncTask(this, maxHeight, maxWidth)
                .executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, requestedImages.toArray());
    }

    // Called by AWSIntegratorTask to return json
    @Override
    public void doSomethingWithResults(String ob) {
        offset += limit;    // Successful request -> Increase offset for next request
        List<FoodReceive> foodReceives = ConvertToObject.toFoodReceiveList(ob);

        // Critical section
        accessList.lock();
        Log.d("TinderActivity", "First request done");
        listItems.addAll(ConvertToObject.toListItemClassList(foodReceives));
        requestImages();
        accessList.unlock();
    }

    // Called by GetImagesAsyncTask to return list of bitmaps
    @Override
    public void doSomethingWithDownloadedImages(List<Bitmap> images) {
        // Critical Section: Add images to list here in activity
        accessList.lock();

        Log.d("TinderActivity", "Finished request");
        // Put images into list of images
        imageList.addAll(images);

        // Remove placeholder if one is made
        if (placeholderIsPresent) {
            if (!imageList.isEmpty()) {
                mainPageFragment.changeFood(imageList.get(0), listItems.get(0));
                imagePager.setSwiping(true);
            } else {
                Toast.makeText(this, "Request failed", Toast.LENGTH_SHORT).show();
            }

            placeholderIsPresent = false;
        }

        Log.d("TinderActivity", "1. Setting request made to false: " + String.valueOf(requestMade));
        requestMade = false;
        Log.d("TinderActivity", "2. Setting request made to false: " + String.valueOf(requestMade));
        accessList.unlock();

        // Remove splash] screen and post first pic
        if (firstRequest) {
            new PostFirstImageTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            firstRequest = false;
        }
    }

    int convertMilesToMeters(int radius) {
        int meters = (int) (radius * 1609.344);

        return Math.min(meters, MAX_RADIUS);
    }

    @Override
    public void onConnected(Bundle bundle) {
        // If permission not granted
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.d("TinderActivity", "Not allowed to retrieve location.");
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_LOCATION);
        } else {
            getLocation();
        }
    }

    void getLocation() {
        // If permission not granted
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.d("TinderActivity", "Not allowed to retrieve location.");
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_LOCATION);
        }

        Location mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                mGoogleApiClient);
        if (mLastLocation != null) {
            gpsLocation = String.valueOf(mLastLocation.getLatitude()) + ", "
                    + String.valueOf(mLastLocation.getLongitude());
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
        Log.e("TinderActivity", "Error on connecting for location");
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
        HashMap<FoodReceive, Boolean> cacheForDatabase; // Accumulate 6 likes/dislikes before request

        public ImageChangeListener() {
            //this.fancy_image = (ImageView) findViewById(R.id.fancy_button_image);
            this.cacheForDatabase = new HashMap<>(6);
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

                    heartPulse();
                    update_list_number(cnt);

                    /* move liked image into fancy button */
                    //fancy_image.setImageDrawable(mainPageFragment.getFoodPicture().getDrawable());

                    /* create ListItemClass object passed into LikedListActivity */

                    // if we haven't moved to LikedList yet
                    if (getIntent().hasExtra("gohead"))
                    {
                        likedData = getIntent().getParcelableArrayListExtra("gohead");
                        Log.d("hello", "i'm getting here");
                    }

                    ListItemClass toAdd = listItems.get(0);

                    // store "yes" bitmap in internal storage
                    Bitmap toSend = imageList.get(0);
                    new ImageSaver(TinderActivity.this).
                            setFileName(toAdd.getFoodId()).
                            setDirectoryName("images").
                            save(toSend);

                    likedData.add(toAdd);
                    ++cnt;

                    // Send like to database
                    cacheForDatabase.put(listItems.get(0).getOriginal(), true);
                } else {    // Dislike
                    otherPage = 0;
                    imageList.get(0).recycle(); // Clear up data

                    // Send dislike to database
                    cacheForDatabase.put(listItems.get(0).getOriginal(), false);
                }

                /* Changing the image while image page is out of sight */
                /* If more images are still around */
                if (imageList.size() > 1) {
//                    Log.d("TinderActivity", listItems.get(1).getFoodId() + "," +
//                            listItems.get(1).getFoodName() + "," +
//                            listItems.get(1).getRestaurantName() + "," +
//                            listItems.get(1).getImageUrl());
                    mainPageFragment.changeFood(imageList.get(1), listItems.get(1)); // Next image
                }
                /* Out of images */
                else {
                    // FIXME: Null argument should mean placeholder
                    mainPageFragment.changeFood(null, null);
                    imagePager.setSwiping(false);
                    placeholderIsPresent = true;
                }

                // Either way, remove old data from list
                if (!imageList.isEmpty()) {
                    imageList.remove(0);        // Remove old image from list
                    listItems.remove(0);        // Remove old data from list
                }

                /* Low on images */
                if (imageList.size() < 5 && !requestMade) {
                    requestMade = true;
                    new RequestFromDatabase().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                }

                /* After certain number of requests are accumulated, they are all sent to database
                   in one thread. */
                if (cacheForDatabase.size() >= 6)
                    uploadLikesData();

                accessList.unlock();
                // End critical section

                /* The "new image" animation. Only do it if an animation is idle. */
                imagePager.setCurrentItem(otherPage, false);    /* false = no animation on change */
                imagePager.setCurrentItem(1, true);             /* true = animate */
            }
        }

        private void uploadLikesData() {
            final HashMap<FoodReceive, Boolean> copyCache = new HashMap<>(cacheForDatabase);
            cacheForDatabase.clear();

            new Thread(new Runnable() {
                public void run() {
                    for (FoodReceive fr : copyCache.keySet()) {
                        if (copyCache.get(fr))
                            TableFood.likeFood(fr);
                        else
                            TableFood.dislikeFood(fr);
                    }

                    copyCache.clear();
                }
            }).start();
        }
    }

    /* Class to listen to the state of splash screen animation. Only uses onAnimationEnd() to know
     * when the animation is finished. */
    class SplashAnimatorListener implements Animator.AnimatorListener {
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
        intent.putParcelableArrayListExtra("key", likedData);
        startActivityForResult(intent, 1);

        /* Heart is empty again */
        if (notification_number != null)
            notification_number.setVisibility(View.GONE);
    }

    /* Opens main drawer */
    public void openDrawer(View view) {
        toggleViews("open");
        final FrameLayout dimOverlay = (FrameLayout) findViewById(R.id.DimOverlay);

        my_drawer.animate().translationX(0).setDuration(400)
                .setInterpolator(new DecelerateInterpolator(3.0f))
                .setListener(new Animator.AnimatorListener() {
                                 @Override
                                 public void onAnimationStart(Animator animation) {
                                     my_drawer.setVisibility(View.VISIBLE);
                                     dimOverlay.setVisibility(View.VISIBLE);
                                 }

                                 @Override
                                 public void onAnimationEnd(Animator animation) {

                                 }

                                 @Override
                                 public void onAnimationCancel(Animator animation) {

                                 }

                                 @Override
                                 public void onAnimationRepeat(Animator animation) {

                                 }
                             });
    }

    /* Closes main drawer */
    public void closeDrawer(View view) {
        toggleViews("close");
        final FrameLayout dimOverlay = (FrameLayout) findViewById(R.id.DimOverlay);
        final FrameLayout typesList = (FrameLayout) findViewById(R.id.types_list);

        my_drawer.animate().translationX(-1 * my_drawer.getWidth()).setDuration(300)
                .setInterpolator(new DecelerateInterpolator())
                .setListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {
                        dimOverlay.setVisibility(View.GONE);
                        dimOverlay.setVisibility(View.GONE);
                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        my_drawer.setVisibility(View.GONE);
                        if(typesList.getVisibility() == View.VISIBLE)
                            viewFoodTypeList(my_drawer);
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {

                    }

                    @Override
                    public void onAnimationRepeat(Animator animation) {

                    }
                });

    }

    public void toggleViews (String s){
        ViewPager foodPic = (ViewPager) findViewById (R.id.viewPager_images);

        if(s == "close"){
            foodPic.setVisibility(View.VISIBLE);
        }
        else{
            foodPic.setVisibility(View.GONE);
        }
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

    public void newFilterSearch (View view){
        new RequestFromDatabase().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
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
                notification_number.setTextSize(13);
                notification_number.setPadding(0, 0, 0, 0);
            } else {
                notification_number.setTextSize(10);
                notification_number.setPadding(0, 0, 0, 5);
            }
        } else {
            notification_number.setVisibility(View.VISIBLE);
            notification_number.setText("+99");
            notification_number.setTextSize(7);
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
            Log.d("TinderActivity", "1. Start request");
            waitForGPSLock.lock();
            waitForGPSLock.unlock();
            Log.d("TinderActivity", "2. Start request");

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            int radius = convertMilesToMeters(rad_seekBar.getProgress());

            FoodRequest req = new FoodRequest("", foodLimit, gpsLocation, limit, radius, getAllFoodTypes(), 1, offset);
            new AWSIntegratorAsyncTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, "yelpApi2_8", req, TinderActivity.this);
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
            //mainPageFragment.changeFood(imageList.get(0), listItems.get(0));

            // Fade, then set to gone through listener
            splashScreen.animate()
                    .alpha(0f)
                    .setListener(new SplashAnimatorListener()); /* Listener to remove view once finished */
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

    public void toggleTitle (View view){
        ImageView style1 = (ImageView) findViewById(R.id.app_name);
        TextView style2 = (TextView) findViewById(R.id.app_name_2);

        if(style1.getVisibility() == View.GONE){
            style1.setVisibility(View.VISIBLE);
            style2.setVisibility(View.GONE);
        }
        else{
            style1.setVisibility(View.GONE);
            style2.setVisibility(View.VISIBLE);
        }
    }

    public void toggleTheme (View view){
        RelativeLayout ui_background = (RelativeLayout) findViewById(R.id.main_ui);
    }

    public void yesHold () {
        final FrameLayout yesIcon = (FrameLayout) findViewById(R.id.yes_icon);
        final ImageView yesCircle = (ImageView) findViewById(R.id.yes_circle);
        final ImageView yesShadow = (ImageView) findViewById(R.id.yes_shadow);

        yesIcon.animate().scaleX(0.85f).scaleY(0.85f)
            .setDuration(100)
            .setInterpolator(new DecelerateInterpolator());

        yesCircle.animate().scaleX(0.85f).scaleY(0.85f)
                .setDuration(100)
                .setInterpolator(new DecelerateInterpolator());

        yesShadow.animate().scaleX(0.0f).scaleY(0.0f)
                .setDuration(100)
                .setInterpolator(new DecelerateInterpolator());
    }

    public void yesReleased () {
        final FrameLayout yesIcon = (FrameLayout) findViewById(R.id.yes_icon);
        final ImageView yesCircle = (ImageView) findViewById(R.id.yes_circle);
        final ImageView yesShadow = (ImageView) findViewById(R.id.yes_shadow);


        bellHammer.animate().translationY(bellHammer.getHeight() / 19)
                .setDuration(100)
                .setInterpolator(new DecelerateInterpolator())
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        bellHammer.animate().translationY(0)
                                .setDuration(150)
                                .setInterpolator(new DecelerateInterpolator());
                        bellHammer.animate().setListener(null);
                    }
                });

        yesCircle.animate().scaleX(1f).scaleY(1f)
                .setDuration(100)
                .setInterpolator(new DecelerateInterpolator());

        yesShadow.animate().scaleX(1f).scaleY(1f)
                .setDuration(100)
                .setInterpolator(new DecelerateInterpolator());

        yesIcon.animate().scaleX(1f).scaleY(1f)
                .setDuration(100)
                .setInterpolator(new DecelerateInterpolator())
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        yesIcon.animate().scaleX(1f)
                                .setDuration(400)
                                .setListener(new AnimatorListenerAdapter() {
                                    @Override
                                    public void onAnimationEnd(Animator animation) {
                                        if (imagePager.getCurrentItem() == 1
                                                && changeListener.state == ViewPager.SCROLL_STATE_IDLE) {
                                            imagePager.setCurrentItem(0);
                                            MediaPlayer mp = MediaPlayer.create(getApplicationContext(), R.raw.click_sound1);
                                            //mp.start();
                                        }
                                        yesIcon.animate().setListener(null);
                                    }
                                });
                    }
                });

        borderFlash("green");

        /*
        if (imagePager.getCurrentItem() == 1
                && changeListener.state == ViewPager.SCROLL_STATE_IDLE) {
            imagePager.setCurrentItem(0);
            MediaPlayer mp = MediaPlayer.create(getApplicationContext(), R.raw.click_sound1);
            //mp.start();
        }
        */
    }
    
    public void noHeld () {
        final ImageView noIcon = (ImageView) findViewById(R.id.no_icon);
        final ImageView noCircle = (ImageView) findViewById(R.id.no_circle);
        final ImageView noShadow = (ImageView) findViewById(R.id.no_shadow);

        noIcon.setRotation(0);

        noIcon.animate().scaleX(0.85f).scaleY(0.85f)
                .setDuration(100)
                .setInterpolator(new DecelerateInterpolator());

        noCircle.animate().scaleX(0.85f).scaleY(0.85f)
                .setDuration(100)
                .setInterpolator(new DecelerateInterpolator());

        noShadow.animate().scaleX(0.0f).scaleY(0.0f)
                .setDuration(100)
                .setInterpolator(new DecelerateInterpolator());
    }

    public void noReleased () {

        Log.d("in noRelease", "IN NO RELEASE!!!");

        final ImageView noIcon = (ImageView) findViewById(R.id.no_icon);
        final ImageView noCircle = (ImageView) findViewById(R.id.no_circle);
        final ImageView noShadow = (ImageView) findViewById(R.id.no_shadow);

        noCircle.animate().scaleX(1f).scaleY(1f)
                .setDuration(100)
                .setInterpolator(new DecelerateInterpolator());

        noShadow.animate().scaleX(1f).scaleY(1f)
                .setDuration(100)
                .setInterpolator(new DecelerateInterpolator());

        noIcon.animate().scaleX(1f).scaleY(1f)
                .setDuration(100)
                .setInterpolator(new DecelerateInterpolator())
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                       noIcon.animate().scaleX(1f)
                               .setDuration(400)
                               .setListener(new AnimatorListenerAdapter() {
                                   @Override
                                   public void onAnimationEnd(Animator animation) {
                                       if (imagePager.getCurrentItem() == 1
                                               && changeListener.state == ViewPager.SCROLL_STATE_IDLE) {
                                           imagePager.setCurrentItem(2);
                                           MediaPlayer mp = MediaPlayer.create(getApplicationContext(), R.raw.click_sound1);
                                           //mp.start();
                                       }
                                       noIcon.animate().setListener(null);
                                   }
                               });
                    }
                });

        borderFlash("red");

        /*
        if (imagePager.getCurrentItem() == 1
                && changeListener.state == ViewPager.SCROLL_STATE_IDLE) {
            imagePager.setCurrentItem(2);
            MediaPlayer mp = MediaPlayer.create(getApplicationContext(), R.raw.click_sound1);
            //mp.start();
        }
        */


    }

    public void borderFlash (String color){
        mainPageFragment.borderFlash(color);
    }

    public void heartPulse () {
        final ImageView heartIcon = (ImageView) findViewById(R.id.heart_icon_green);
        heartIcon.setScaleX(0.4f);
        heartIcon.setScaleY(0.4f);
        heartIcon.setVisibility(View.VISIBLE);

        heartIcon.animate().scaleX(1.0f).scaleY(1.0f)
                .alpha(0.0f)
                .setDuration(700)
                .setInterpolator(new AccelerateInterpolator())
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        heartIcon.setVisibility(View.INVISIBLE);
                        heartIcon.setAlpha(1.0f);
                        heartIcon.animate().setListener(null);
                    }
                });
    }
}

    /* end Tinder Activity */
