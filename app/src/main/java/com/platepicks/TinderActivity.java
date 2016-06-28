package com.platepicks;

import android.Manifest;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.animation.Animator;
import android.location.Location;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.GestureDetector;
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
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStates;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.platepicks.objects.FoodReceive;
import com.platepicks.support.ConnectivityReceiver;
import com.platepicks.support.CustomViewPager;
import com.platepicks.util.AWSIntegratorInterface;
import com.platepicks.util.ConnectionCheck;
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
public class TinderActivity extends AppCompatActivity implements AWSIntegratorInterface,
        ImageLoaderInterface,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {
    public final int MAX_RADIUS = 40000;    // meters
    public final int LIMIT_WITH_WIFI = 20, LIMIT_WITHOUT_WIFI = 5;

    final int DFLT_IMG_MAX_WIDTH = 1000, DFLT_IMG_MAX_HEIGHT = 1000;
    final int REQUEST_PERMISSION_LOCATION = 1;
    final int RESULT_LIKED_LIST = 1, RESULT_SETTINGS_LOCATION = 2;

    GoogleApiClient mGoogleApiClient;   // Google location client
    LocationRequest mLocationRequest;   // Google location request
    ImageChangeListener changeListener; // Listens to changes in image swiping from viewpager
    ImagePagerAdapter imageAdapter;     // List adapter for view pager images
    ConnectivityReceiver connectionRx;  // Listens to changes in internet connection

    SwipeImageFragment mainPageFragment = null; // Picture of food fragment
    RelativeLayout splashScreen = null;         // Splash screen
    SeekBar rad_seekBar = null;                 // local seekBar variable
    TextView notification_number = null;        // list notification number
    LinearLayout leftFoodTypes, rightFoodTypes; // 2 columns of food types
    CustomViewPager imagePager;                 // View pager for swiping
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

    int cnt = 1;                                    // used for notification count of new liked foods
    boolean triedLocSettingsFlag = false;           // flag to know if app attempted to get location setting enabled
    public int businessLimit = LIMIT_WITHOUT_WIFI;  // Number of businesses returned per request
    public int foodLimit = 3;                       // Number of food per business
    public int offset = 0;                          // Number of businesses to offset by in yelp request
    public String gpsLocation;                      // "Latitude, Longitude"

    // Function: creates list item
    public ListItemClass createListItem(String foodName) {
        ListItemClass newItem = new ListItemClass();
        newItem.setFoodName(foodName);
        newItem.setRestaurantName("test_restaurant");
        newItem.setRestaurantAddress("test_address");
        return newItem;
    }

    public SwipeImageFragment getMainPageFragment() {
        return mainPageFragment;
    }

    public boolean isRequestMade() {
        return requestMade;
    }

    public void setRequestMade(boolean requestMade) {
        this.requestMade = requestMade;
    }

    public void setLikedData(ArrayList<ListItemClass> likedData) {
        this.likedData = likedData;
        ;
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

        switch (requestCode) {
            case (RESULT_LIKED_LIST): {
                if (resultCode == Activity.RESULT_OK) {
                    this.likedData = data.getParcelableArrayListExtra(LikedListActivity.LIKED_LIST_TAG);
                }
                break;
            }
            case (RESULT_SETTINGS_LOCATION): {
                Log.d("TinderActivity", "Resolution for location");
                if (resultCode == Activity.RESULT_OK) {
                    Log.d("TinderActivity", "Approved!");
                }
                handleLocationSetting();
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
        createLocationRequest();

        /* XML Layout: selecting which file to set as layout */
        setContentView(R.layout.activity_tinderui);

        // Logo food imageView
        fancy_image = (ImageView) findViewById(R.id.fancy_button_image);

        /* ViewPager: A view that enables swiping images left and right
         * Has 3 pages, 0-2 (reason is explained in class definition below). */
        imagePager = (CustomViewPager) findViewById(R.id.viewPager_images);
        mainPageFragment = new SwipeImageFragment();
        imageAdapter = new ImagePagerAdapter(getSupportFragmentManager(), this);
        imagePager.setAdapter(imageAdapter);

        /* Ensure that we start on page 1, the middle page with the image. */
        imagePager.setCurrentItem(1, false);
//        imagePager.getCurrentItem();            // Ensure item is defined

        /* Listen for change in swipe animation's current state */
        changeListener = new ImageChangeListener(this, imagePager);
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


    }

    @Override
    protected void onStart() {
        mGoogleApiClient.connect();
        super.onStart();
    }

    @Override
    protected void onResume() {
        if (connectionRx != null)
            registerReceiver(connectionRx, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));

        super.onResume();
    }

    @Override
    protected void onPause() {
        if (connectionRx != null)
            unregisterReceiver(connectionRx);
        stopLocationUpdates();
        super.onPause();
    }

    @Override
    protected void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
    }

    @Override
    protected void onRestart() {
        Log.d("TinderActivity", "Here in restart");
        if (placeholderIsPresent && !ConnectionCheck.isConnected(this) && connectionRx == null)
            handleNoInternet(ConnectivityReceiver.REQUEST_FROM_DATABASE);
        super.onRestart();
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

        if (nothing_checked)
            categoryFilter = default_appender.toString();
        else
            categoryFilter = appender.toString();

        // truncate the last extra comma
        if (!categoryFilter.isEmpty())
            categoryFilter = categoryFilter.substring(0, categoryFilter.length() - 1);

        Log.d("TinderActivity", categoryFilter);

        return categoryFilter;
    }

    boolean isLocationPermitted() {
        return (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                == PackageManager.PERMISSION_GRANTED);
    }

    void askForLocationPermission() {
        Log.d("TinderActivity", "Not allowed to retrieve location.");
        ActivityCompat.requestPermissions(
                this,
                new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION},
                REQUEST_PERMISSION_LOCATION);
    }

    // Asks for change in location settings on first call
    // On second call or, if settings already set, on first call, gets location or asks for location
    // if user says no.
    // Has personal state variable (triedLocSettingsFlag)
    void handleLocationSetting() {
        // Check location settings
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(mLocationRequest);
        PendingResult<LocationSettingsResult> pendingResult = LocationServices
                .SettingsApi.checkLocationSettings(mGoogleApiClient, builder.build());
        pendingResult.setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @Override
            public void onResult(@NonNull LocationSettingsResult result) {
                Log.d("TinderActivity", "In callback");
                final Status status = result.getStatus();
                final LocationSettingsStates settingsStates = result.getLocationSettingsStates();

                switch (status.getStatusCode()) {
                    // Location settings are satisfied
                    case LocationSettingsStatusCodes.SUCCESS:
                        if (!setLocation()) {
                            // Permission not given: should not happen
                            if (!isLocationPermitted()) {
                                Log.e("TinderActivity", "Handling location setting but no " +
                                        "permission was given");
                                askForUserLocation();
                            } else {
                                Log.d("TinderActivity", "Location not set, waiting for callback");
                                startLocationUpdates();
                                // FIXME: Wait for callback
//                                askForUserLocation();
                            }
                        }
                        break;
                    // Location settings are not satisfied, but this can be fixed
                    // by showing the user a dialog.
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        // State 1: Show the dialog by calling startResolutionForResult(),
                        // and check the result in onActivityResult().
                        if (!triedLocSettingsFlag) {
                            triedLocSettingsFlag = true;

                            try {
                                status.startResolutionForResult(
                                        TinderActivity.this, RESULT_SETTINGS_LOCATION);
                            } catch (IntentSender.SendIntentException e) {
                                // Ignore the error.
                            }
                            return;
                        }
                        // State 2: Can't get location, just ask for location
                        else {
                            Log.e("TinderActivity", "User said no to location");
                            askForUserLocation();
                        }
                        break;
                    // Location settings are not satisfied. However, we have no way
                    // to fix the settings so we won't show the dialog.
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        Log.e("TinderActivity", "Unable to change location settings.");
                        askForUserLocation();
                        break;
                }
            }
        });
    }

    // Retrieves location and sets it to gpsLocation
    // Returns true if location successfully retrieved and gpsLocation is set
    // Returns false if there is no permission or last known location
    boolean setLocation() {
        // All location settings are satisfied. The client can
        // initialize location requests here.
        // Get location coordinates
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            return false;
        }

        // Try to get location (may fail)
        Location mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                mGoogleApiClient);
        if (mLastLocation != null) {
            Log.d("TinderActivity", "Location succeeded (old way)");
            gpsLocation = String.valueOf(mLastLocation.getLatitude()) + ", "
                    + String.valueOf(mLastLocation.getLongitude());

            // Location succeeded, release lock
            if (waitForGPSLock.isHeldByCurrentThread())
                waitForGPSLock.unlock();
            return true;
        }

        Log.d("TinderActivity", "last location was null");
        return false;
    }

    void askForUserLocation() {
        Toast.makeText(this, "Using Riverside for default", Toast.LENGTH_SHORT).show();
        gpsLocation = "33.7175, -117.8311"; // FIXME: Default is riverside

        if (waitForGPSLock.isHeldByCurrentThread())
            waitForGPSLock.unlock();

        // FIXME: if getting location fails
//        if (gpsLocation != null) {
        // Location succeeded, release lock
//            if (waitForGPSLock.isHeldByCurrentThread())
//                waitForGPSLock.unlock();
//        } else {
//            onNoLocationGiven();
//        }
    }

    void onNoLocationGiven() {
        try {
            throw new Exception("NO LOCATION AT ALL and NO PLAN");
        } catch (Exception e) {
            e.printStackTrace();
            finish();
        }
    }

    // Creates location request for database request
    void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setNumUpdates(1);              // Only 1 location update
        mLocationRequest.setInterval(60 * 60 * 1000);   // 60 minutes in milliseconds
        mLocationRequest.setFastestInterval(60 * 1000); // 1 minute
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
    }

    void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                == PackageManager.PERMISSION_GRANTED)
            LocationServices.FusedLocationApi.requestLocationUpdates(
                    mGoogleApiClient, mLocationRequest, this);
    }

    void stopLocationUpdates() {
        LocationServices.FusedLocationApi.removeLocationUpdates(
                mGoogleApiClient, this);
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
        startActivityForResult(intent, RESULT_LIKED_LIST);

        /* Heart is empty again */
        if (notification_number != null)
            notification_number.setVisibility(View.GONE);
    }

    // Called after requestFromDatabase in doSomethingWithResults()
    void requestImages() {
        Log.d("TinderActivity", "in requestImages");
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

        Log.d("TinderActivity", "in requestImages before task " + requestedImages.size());
        new GetImagesAsyncTask(this, maxHeight, maxWidth)
                .executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, requestedImages.toArray());
    }

    void handleNoInternet(int task) {
        Log.d("TinderActivity", "Internet error, task " + String.valueOf(task));

        mainPageFragment.changeText(SwipeImageFragment.OFFLINE);
        connectionRx = new ConnectivityReceiver(this, task);
        registerReceiver(connectionRx, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));

        // Fade, then set to gone through listener
        if (splashScreen.getVisibility() != View.GONE) {
            splashScreen.animate()
                    .alpha(0f)
                    .setListener(new SplashAnimatorListener()); /* Listener to remove view once finished */
        }
    }

    /* Called by connectionRx when back online if crash happened in database asynctask */
    public void requestFromDatabaseReceiver() {
        Log.d("TinderActivity", "Receiver called for database request");

        mainPageFragment.changeText(SwipeImageFragment.LOADING);
        unregisterReceiver(connectionRx);
        if (connectionRx != null) {
            new RequestFromDatabase(this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
        connectionRx = null;
    }

    /* Called by connectionRx when back online if crash happened in images asynctask */
    public void requestImagesReceiver() {
        Log.d("TinderActivity", "Receiver called for images");

        mainPageFragment.changeText(SwipeImageFragment.LOADING);
        unregisterReceiver(connectionRx);
        if (connectionRx != null) {
            requestImages();
        }
        connectionRx = null;
    }

    public void onCreatedUI() {
        // Release lock once UI is visible to let splash screen be removed and show first pic
        if (waitForUILock.isHeldByCurrentThread())
            waitForUILock.unlock();
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
                && changeListener.state == ViewPager.SCROLL_STATE_IDLE
                && imagePager.getSwiping()) {
            imagePager.setCurrentItem(2);
            MediaPlayer mp = MediaPlayer.create(getApplicationContext(), R.raw.click_sound1);
            //mp.start();
        }
    }

    public void onClickYes(View view) {
        if (imagePager.getCurrentItem() == 1
                && changeListener.state == ViewPager.SCROLL_STATE_IDLE
                && imagePager.getSwiping()) {
            imagePager.setCurrentItem(0);
            MediaPlayer mp = MediaPlayer.create(getApplicationContext(), R.raw.click_sound1);
            //mp.start();
        }
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

    // Called by AWSIntegratorTask if internet request fails
    @Override
    public void doSomethingOnAWSError() {
        handleNoInternet(ConnectivityReceiver.REQUEST_FROM_DATABASE);
    }

    // Called by GetImagesAsyncTask to return list of bitmaps
    @Override
    public void doSomethingWithDownloadedImages(List<Bitmap> images) {
        // Critical Section: Add images to list here in activity
        accessList.lock();

        Log.d("TinderActivity", "Finished request");

        if (images != null && !images.isEmpty()) {
            // Put images into list of images
            imageList.addAll(images);

            // Remove placeholder if one is made
            if (placeholderIsPresent) {
                mainPageFragment.changeFood(imageList.get(0), listItems.get(0));
                placeholderIsPresent = false;
            }

            imagePager.setSwiping(true);
        } else {
            Log.e("TinderActivity", "Request failed");
            mainPageFragment.changeText(SwipeImageFragment.OUT_OF_IMG);
        }

        // Reset various variables
        requestMade = false;
        accessList.unlock();

        // Remove splash] screen and post first pic
        if (firstRequest) {
            new PostFirstImageTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            firstRequest = false;
        }
    }

    @Override
    public void doSomethingOnImageError() {
        handleNoInternet(ConnectivityReceiver.GET_IMAGES);
    }

    @Override
    public void onConnected(Bundle bundle) {
        // Currently, just want 1 location. If statement avoids another location on return to
        // activity and repeat onConnected
        if (gpsLocation == null) {
            if (!isLocationPermitted())
                askForLocationPermission();
            else
                handleLocationSetting();
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.e("TinderActivity", "Error on connecting for location");
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_PERMISSION_LOCATION:
                if (grantResults.length == 0 || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    Log.d("TinderActivity", "Permission not granted");
                    askForUserLocation();
                } else {
                    Log.d("TinderActivity", "Permission granted");
                    handleLocationSetting();
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
                break;
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        if (location != null) {
            Log.d("TinderActivity", "Location succeeded (new way)");
            gpsLocation = String.valueOf(location.getLatitude()) + ", "
                    + String.valueOf(location.getLongitude());

            // Location succeeded, release lock
            if (waitForGPSLock.isHeldByCurrentThread())
                waitForGPSLock.unlock();
        } else {
            Toast.makeText(this, "Could not get location", Toast.LENGTH_SHORT).show();
            askForUserLocation();
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
            Log.d("PostFirstImageTask", "In postExecute");
            // Set first image
            if (!imageList.isEmpty() && !listItems.isEmpty()) {
                Log.d("PostFirstImageTask", "Changing image");
                mainPageFragment.changeFood(imageList.get(0), listItems.get(0));
            }

            // Fade, then set to gone through listener
            splashScreen.animate()
                    .alpha(0f)
                    .setListener(new SplashAnimatorListener()); /* Listener to remove view once finished */
        }
    }
}
