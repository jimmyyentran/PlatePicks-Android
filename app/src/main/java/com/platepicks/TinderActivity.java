package com.platepicks;

import android.Manifest;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.app.Activity;
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
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
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
import com.platepicks.objects.FoodReceive;
import com.platepicks.support.CustomViewPager;
import com.platepicks.util.AWSIntegratorInterface;
import com.platepicks.util.ConvertToObject;
import com.platepicks.util.FoodTypes;
import com.platepicks.util.GetImagesAsyncTask;
import com.platepicks.util.ImageChangeListener;
import com.platepicks.util.ImageLoaderInterface;
import com.platepicks.util.ImagePagerAdapter;
import com.platepicks.util.ListItemClass;
import com.platepicks.util.RequestFromDatabase;

import java.util.ArrayList;
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
    public final int MAX_RADIUS = 40000;    // meters

    final int DFLT_IMG_MAX_WIDTH = 1000, DFLT_IMG_MAX_HEIGHT = 1000;
    final int REQUEST_LOCATION = 1;

    GoogleApiClient mGoogleApiClient;   // Google location client
    ImageChangeListener changeListener; // Listens to changes in image swiping from viewpager

    SwipeImageFragment mainPageFragment = null; // Picture of food fragment
    RelativeLayout splashScreen = null;         // Splash screen
    SeekBar rad_seekBar = null;                 // local seekBar variable
    TextView notification_number = null;        // list notification number
    LinearLayout leftFoodTypes, rightFoodTypes; // 2 columns of food types
    CustomViewPager imagePager;                 // View pager for swiping
    ImagePagerAdapter imageAdapter;             // List adapter for view pager images
    DrawerLayout my_drawer = null;              // Drawer layout
    FrameLayout drawer_space = null;
    ImageView fancy_image;

    List<ListItemClass> listItems = new LinkedList<>();     // Data received from network request
    List<Bitmap> imageList = new LinkedList<>();            // Images downloaded in network request
    ArrayList<ListItemClass> likedData = new ArrayList<>(); // Food liked by user

    public ReentrantLock waitForUILock = new ReentrantLock();  // Race condition between first network request and creation of UI
    public ReentrantLock waitForGPSLock = new ReentrantLock(); // Wait for GPS location to be retrieved before making yelp request
    public ReentrantLock accessList = new ReentrantLock();     // Race condition to access listItems or imageList
    boolean requestMade = false;                        // Flag to indicate making a request
    boolean firstRequest;                               // Flag to indicate first request
    boolean placeholderIsPresent = false;               // Flag to indicate out of images

    int cnt = 1;                // used for notification count of new liked foods
    public int businessLimit = 20;     // Number of businesses returned per request
    public int foodLimit = 3;          // Number of food per business
    public int offset = 0;             // Number of businesses to offset by in yelp request
    public String gpsLocation;         // "Latitude, Longitude"

    // Function: creates list item
    public ListItemClass createListItem(String foodName) {
        ListItemClass newItem = new ListItemClass();
        newItem.setFoodName(foodName);
        newItem.setRestaurantName("test_restaurant");
        newItem.setRestaurantAddress("test_address");
        return newItem;
    }

    public void setMainPageFragment(SwipeImageFragment f) {
        mainPageFragment = f;
    }

    public boolean isMainPageFragmentSet() {
        return mainPageFragment != null;
    }

    public boolean isRequestMade() {
        return requestMade;
    }

    public void setRequestMade(boolean requestMade) {
        this.requestMade = requestMade;
    }

    public void setLikedData(ArrayList<ListItemClass> likedData) {
        this.likedData = likedData;;
    }

    public void addToLikedData(ListItemClass toAdd) {
        this.likedData.add(toAdd);
    }

    public void makeCurrentImageFancy() {
        fancy_image.setImageBitmap(imageList.get(0));
    }

    public void changeToNextFood() {
        mainPageFragment.changeFood(imageList.get(1), listItems.get(1));
    }

    public void changeToPlaceholder() {
        mainPageFragment.changeFood(null, null);
        imagePager.setSwiping(false);
        placeholderIsPresent = true;
    }

    public List<Bitmap> getImageList() {
        return imageList;
    }

    public List<ListItemClass> getListItems() {
        return listItems;
    }

    public int getRadius() {
        return rad_seekBar.getProgress();
    }

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
        setContentView(R.layout.activity_tinderui);

        // Logo food imageView
        fancy_image = (ImageView) findViewById(R.id.fancy_button_image);

        /* ViewPager: A view that enables swiping images left and right
         * Has 3 pages, 0-2 (reason is explained in class definition below). */
        imagePager = (CustomViewPager) findViewById(R.id.viewPager_images);
        imageAdapter = new ImagePagerAdapter(getSupportFragmentManager(), this);
        imagePager.setAdapter(imageAdapter);

        /* Ensure that we start on page 1, the middle page with the image. */
        imagePager.setCurrentItem(1, false);
//        imagePager.getCurrentItem();            // Ensure item is defined

        /* Listen for change in swipe animation's current state */
//        changeListener = new ImageChangeListener(this, imagePager);
//        imagePager.addOnPageChangeListener(changeListener);

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

        // First batch of images
        waitForGPSLock.lock();  // Ensure that first network request waits for GPS first
        waitForUILock.lock();   // Ensure that first network request does not post image before UI is visible
        firstRequest = true;    // Flag to remove splash screen after first request
        new RequestFromDatabase(this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

        /* Splash screen: Covers entire tinder activity for 3 seconds. Created here to simplify
         * calling networks requests in this activity (vs. a splash screen activity) */
        splashScreen = (RelativeLayout) findViewById(R.id.layout_splashScreen);
        splashScreen.setVisibility(View.VISIBLE);

        // Like Button Text
        notification_number = (TextView) findViewById(R.id.list_notification);

        /* Drawer gesture detector */
        my_drawer = (android.support.v4.widget.DrawerLayout) findViewById(R.id.drawer1);
        GestureDetector gd;
        gd = new GestureDetector(my_drawer.getContext(), new GestureDetector.OnGestureListener() {
            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                closeDrawer(my_drawer);
                return true;
            }

            @Override
            public boolean onDown(MotionEvent e) { return false; }

            @Override
            public void onShowPress(MotionEvent e) {}

            @Override
            public boolean onSingleTapUp(MotionEvent e) { return false; }

            @Override
            public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) { return false; }

            @Override
            public void onLongPress(MotionEvent e) {}
        });

        /* NavDrawer onClick listeners */
        TextView okBtn = (TextView) findViewById(R.id.types_ok_button);
        TextView clearFavBtn = (TextView) findViewById(R.id.reset_list);
        TextView clearTypesBtn = (TextView) findViewById(R.id.types_clear_button);


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

    public String getAllFoodTypes() {
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

    public void update_list_number () {
        /* Code for List Notification Number */
        if (cnt <= 0) {
            notification_number.setVisibility(View.GONE);
        } else if (cnt <= 99) {
            notification_number.setVisibility(View.VISIBLE);
            notification_number.setText(String.valueOf(cnt));
            if (cnt <= 9) {
                notification_number.setTextSize(14);
                notification_number.setPadding(0, 0, 0, 0);
            } else {
                notification_number.setTextSize(11);
                notification_number.setPadding(0, 0, 0, 3);
            }
        } else {
            notification_number.setVisibility(View.VISIBLE);
            notification_number.setText("+99");
            notification_number.setTextSize(8);
            notification_number.setPadding(0, 0, 0, 4);
        }

        cnt++;  // Increment cnt after call (only called by imageChangeListener
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

    /* Opens main drawer */
    public void openDrawer(View view) {
        my_drawer = (android.support.v4.widget.DrawerLayout) findViewById(R.id.drawer1);
        final FrameLayout dim_overlay = (FrameLayout) findViewById(R.id.DimOverlay);

        /* Fade in a dim overlay on top of the main interface */
        ObjectAnimator fade_in = ObjectAnimator.ofFloat(dim_overlay, "alpha", 0f, 0.25f);
        fade_in.setDuration(150);

        /* Move drawer out of sight */
        ObjectAnimator anim = ObjectAnimator.ofFloat(my_drawer, "translationX", -1 * my_drawer.getWidth());
        anim.setDuration(1);

        /* Actual sliding out animation */
        final ObjectAnimator openDrawerAnim = ObjectAnimator.ofFloat(my_drawer, "translationX", 0f);
        openDrawerAnim.setDuration(200);

        /* Move THEN slide out */
        anim.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                openDrawerAnim.start();
                my_drawer.setVisibility(View.VISIBLE);
            }
        });
        my_drawer.setVisibility(View.INVISIBLE);
        anim.start();
        dim_overlay.setVisibility(View.VISIBLE);
    }

    /* Closes main drawer */
    public void closeDrawer(View view) {
        my_drawer = (android.support.v4.widget.DrawerLayout) findViewById(R.id.drawer1);
        final FrameLayout dim_overlay = (FrameLayout) findViewById(R.id.DimOverlay);

        /* fade out the dim overlay */
        ObjectAnimator fade_out = ObjectAnimator.ofFloat(dim_overlay, "alpha", 0.25f, 0f);
        fade_out.setDuration(150);
        fade_out.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                dim_overlay.setVisibility(View.GONE);
            }
        });

        /* sliding in animation */
        ObjectAnimator closeDrawerAnim = ObjectAnimator.ofFloat(my_drawer, "translationX", -1 * my_drawer.getWidth());
        closeDrawerAnim.setDuration(200);
        closeDrawerAnim.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                my_drawer.setVisibility(View.GONE);
            }
        });
        closeDrawerAnim.start();
        dim_overlay.setVisibility(View.GONE);
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

    public void onClickNo(View view) {
        if (imagePager.getCurrentItem() == 1
                && changeListener.state == ViewPager.SCROLL_STATE_IDLE) {
            imagePager.setCurrentItem(2);
            MediaPlayer mp = MediaPlayer.create(getApplicationContext(), R.raw.click_sound1);
            //mp.start();
        }
    }

    public void onClickYes(View view) {
        if (imagePager.getCurrentItem() == 1
                && changeListener.state == ViewPager.SCROLL_STATE_IDLE) {
            imagePager.setCurrentItem(0);
            MediaPlayer mp = MediaPlayer.create(getApplicationContext(), R.raw.click_sound1);
            //mp.start();
        }
    }

    public void onClickTest(View view) {
        CheckBox testBox = (CheckBox) view;
        Log.d("TinderActivity", "Click successful and box is " + String.valueOf(testBox.isChecked()));

        mainPageFragment.setOffline(testBox.isChecked());
    }

    // Called by AWSIntegratorTask to return json
    @Override
    public void doSomethingWithResults(String ob) {
        offset += businessLimit;    // Successful request -> Increase offset for next request
        List<FoodReceive> foodReceives = ConvertToObject.toFoodReceiveList(ob);

        // Critical section
        accessList.lock();
        Log.d("TinderActivity", "First request done");
        listItems.addAll(ConvertToObject.toListItemClassList(foodReceives));
        requestImages();
        accessList.unlock();
    }

    @Override
    public void doSomethingOnError() {
        // FIXME: Figure out algorithm after putting offline error message here instead of onClickTest

        // Fade, then set to gone through listener
        splashScreen.animate()
                .alpha(0f)
                .setListener(new SplashAnimatorListener()); /* Listener to remove view once finished */
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

        requestMade = false;
        accessList.unlock();

        // Remove splash] screen and post first pic
        if (firstRequest) {
            new PostFirstImageTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            firstRequest = false;
        }
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
                    .setListener(new SplashAnimatorListener()); /* Listener to remove view once finished */
        }
    }
}
