package com.platepicks;

import android.Manifest;
import android.animation.AnimatorListenerAdapter;
import android.app.*;
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
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.ScaleAnimation;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
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
import com.platepicks.objects.ListItemClass;
import com.platepicks.util.RequestFromDatabaseTask;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashSet;
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
    NavigationView navigationView;
    ActionBarDrawerToggle mDrawerToggle;
    View headerLayout;

    List<ListItemClass> listItems = new LinkedList<>();     // Data received from network request
    List<Bitmap> imageList = new LinkedList<>();            // Images downloaded in network request

    public ReentrantLock waitForUILock = new ReentrantLock();  // Race condition between first network request and creation of UI
    public ReentrantLock waitForGPSLock = new ReentrantLock(); // Wait for GPS location to be retrieved before making yelp request
    boolean requestMade = false;                        // Flag to indicate making a request
    boolean firstRequest;                               // Flag to indicate first request
    boolean placeholderIsPresent = false;               // Flag to indicate out of images
    boolean isDrawerOpen = false;                       // Flag to indicate drawer status
    boolean saveFileEdited = false;                     // Flag to indicate added clicks or food removed

    int cnt = 1;                                    // used for notification count of new liked foods
    boolean triedLocSettingsFlag = false;           // flag to know if app attempted to get location setting enabled
    boolean haveLocationUpdatesFlag = false;        // flag for location updates
    public int businessLimit = LIMIT_WITHOUT_WIFI;  // Number of businesses returned per request
    public int foodLimit = 3;                       // Number of food per business
    public int offset = 0;                          // Number of businesses to offset by in yelp request
    public String gpsLocation;                      // "Latitude, Longitude"

    /* yes/no onHold constrictors */
    boolean yesHeld = false;
    boolean noHeld = false;

    /* bell ImageViews */
    ImageView bellShell = null;
    ImageView bellStand = null;
    ImageView bellHammer = null;

    /* bell animations */
    Animation hammer_drop = null;
    Animation hammer_rise = null;

    /* grow/shrink scaleAnimation declarations */
    ScaleAnimation growAnim = null;
    ScaleAnimation shrinkAnim = null;

    /* counter for ClearFavs button */
    int clearCounter = 0;

    public SwipeImageFragment getMainPageFragment() {
        return mainPageFragment;
    }

    public boolean isRequestMade() {
        return requestMade;
    }

    public void clearLikedData(View view) {
        final TextView tapTwice = (TextView) findViewById(R.id.tap_twice);

        if(clearCounter == 0){
            ++clearCounter;
            tapTwice.setText("(tap once)");
            tapTwice.animate().scaleX(1.0f)
                    .setDuration(1000)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            clearCounter = 0;
                            tapTwice.setText("(tap twice)");
                            tapTwice.animate().setListener(null);
                        }
                    });
        }
        else {
            Application.getInstance().clearLikedData();
            deleteFile(Application.SAVED_LIKED_FOODS);
            System.gc();
            update_list_number(0);
            tapTwice.setText("(tap twice)");
        }
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

        Log.d("onActivityResult", "BEFORE SWITCH CASES: " + requestCode);

        switch (requestCode) {
            case (RESULT_LIKED_LIST): {
                if (resultCode == Activity.RESULT_OK) {
                    int tmp = data.getIntExtra("items clicked", 0);
                    Log.d("TinderActivity", ":::::::::::::::::::::::::::::::::::::::::ITEMS CLICKED = " + tmp + ":::::::::::::::::::::::::::::::::::::::::");
                    cnt = cnt - tmp - 1;
                    Log.d("TinderActivity", ":::::::::::::::::::::::::::::::::::::::::NEW CNT = " + cnt + ":::::::::::::::::::::::::::::::::::::::::");

                    update_list_number();
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
            case (99): {
                Log.d("onActivityResult", "INISED CASE #99");
                if(resultCode == 0){

                }
                else if(resultCode == 1){
                    Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            onClickYes(null);
                        }
                    }, 400);
                }
                else if(resultCode == 2){
                    Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            onClickNo(null);
                        }
                    }, 400);
                }
                break;
            }
        }

        Log.d("onActivityResult", "AFTER SWITCH CASES");

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
        setContentView(R.layout.fresh_and_trendy);

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
//        rad_seekBar.setProgress(15);
        TextView radius_value = (TextView) findViewById(R.id.radius_number);
        radius_value.setText(String.valueOf(rad_seekBar.getProgress()));
        rad_seekBar.setOnSeekBarChangeListener(new rad_seekBar_listener());

        notification_number = (TextView) findViewById(R.id.list_notification); // Like Button Text

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
                }
                return true;
            }
        });

        /* Create task to load likedData with persistent data */
        cnt = loadSavedFoods();
        if (saveFileEdited) {
            saveChangesLikedFood();
            saveFileEdited = false;
        }
        update_list_number();

        // First batch of images
        waitForGPSLock.lock();  // Ensure that first network request waits for GPS first
        waitForUILock.lock();   // Ensure that first network request does not post image before UI is visible
        firstRequest = true;    // Flag to remove splash screen after first request
        newFilterSearch();

        /* Splash screen: Covers entire tinder activity for 3 seconds. Created here to simplify
         * calling networks requests in this activity (vs. a splash screen activity) */
        splashScreen = (RelativeLayout) findViewById(R.id.layout_splashScreen);
        splashScreen.setVisibility(View.VISIBLE);

        /* Drawer gesture detector */
        my_drawer = (android.support.v4.widget.DrawerLayout) findViewById(R.id.drawer_layout_fat);
        navigationView = (NavigationView) findViewById(R.id.nav_view_fat);

        mDrawerToggle = new ActionBarDrawerToggle(this, my_drawer,
                R.string.drawer_open, R.string.drawer_close);
        my_drawer.addDrawerListener(mDrawerToggle);

//        GestureDetector gd;
//        gd = new GestureDetector(my_drawer.getContext(), new GestureDetector.OnGestureListener() {
//            @Override
//            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
//                closeDrawer(my_drawer);
//                return true;
//            }
//
//            @Override
//            public boolean onDown(MotionEvent e) {
//                return false;
//            }
//
//            @Override
//            public void onShowPress(MotionEvent e) {
//            }
//
//            @Override
//            public boolean onSingleTapUp(MotionEvent e) {
//                return false;
//            }
//
//            @Override
//            public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
//                return false;
//            }
//
//            @Override
//            public void onLongPress(MotionEvent e) {
//            }
//        });

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

        Typeface source_bold = Typeface.createFromAsset(getAssets(), "fonts/SourceSansPro-Bold.otf");

        /* Custom font for Drawer's Header */
        TextView drawer_header = (TextView) findViewById(R.id.drawer_header_text);
        drawer_header.setTypeface(source_bold);

        /* bitmap optimizations */
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
                == PackageManager.PERMISSION_GRANTED) {
            LocationServices.FusedLocationApi.requestLocationUpdates(
                    mGoogleApiClient, mLocationRequest, this);
            haveLocationUpdatesFlag = true;
        }
    }

    void stopLocationUpdates() {
        if (haveLocationUpdatesFlag) {
            LocationServices.FusedLocationApi.removeLocationUpdates(
                    mGoogleApiClient, this);
            haveLocationUpdatesFlag = false;
        }
    }

    public void update_list_number () {
        /* Code for List Notification Number */
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

        cnt++;  // Increment cnt after call (only called by imageChangeListener
    }

    public void update_list_number(int cnt) {
        this.cnt = cnt;
        update_list_number();
    }

    /* Moves to Like-List Activity */
    public void gotoList(View view) {
        /* Count starts over */
        Intent intent = new Intent(TinderActivity.this, LikedListActivity.class);
        startActivityForResult(intent, RESULT_LIKED_LIST);
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

        // Run through array twice to get primitive array
        int cnt = 0;
        for (ListItemClass item : listItems)
            if (!item.isDownloaded())
                cnt++;

        ListItemClass[] requestedImages = new ListItemClass[cnt];
        int i = 0;
        for (ListItemClass item : listItems) {
            if (!item.isDownloaded()) {
                requestedImages[i] = item;
                i++;
            }
        }

        Log.d("TinderActivity", "in requestImages before task " + requestedImages.length);
        new GetImagesAsyncTask(this, this, maxHeight, maxWidth)
                .executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, requestedImages);
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
            newFilterSearch();
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

    public void newFilterSearch(){
        if (!requestMade) {
            requestMade = true;

            changeSearchButton(true);
            new RequestFromDatabaseTask(this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
    }

    public void onCreatedUI() {
        Log.d("TinderActivity", "in onCreatedUI");
        
        // Release lock once UI is visible to let splash screen be removed and show first pic
        if (waitForUILock.isHeldByCurrentThread())
            waitForUILock.unlock();
    }

    void changeSearchButton(boolean requestMade) {
        TextView searchButton = (TextView) findViewById(R.id.new_search_button);
        ProgressBar searchPB = (ProgressBar) findViewById(R.id.new_search_progress_bar);

        if (requestMade) {
            searchButton.setClickable(false);
            searchButton.setBackgroundResource(R.drawable.gray_button);
            searchButton.animate().alpha(0.5f)
                    .setDuration(300)
                    .setInterpolator(new DecelerateInterpolator());
            searchPB.setVisibility(View.VISIBLE);
        } else {
            searchButton.setClickable(true);
            searchButton.setBackgroundResource(R.drawable.red_button_handler);
            searchButton.animate().alpha(1.0f)
                    .setDuration(300)
                    .setInterpolator(new DecelerateInterpolator());
            searchPB.setVisibility(View.INVISIBLE);

            closeDrawer(null);
        }
    }

    /* Opens main drawer */
    public void openDrawer(View view) {
//        this.openDrawer(view);
        my_drawer.openDrawer(GravityCompat.START);
//        if (isDrawerOpen)
//            return;
//
//        toggleViews("open");
//        final FrameLayout dimOverlay = (FrameLayout) findViewById(R.id.DimOverlay);
//
//        my_drawer.animate().translationX(0).setDuration(400)
//                .setInterpolator(new DecelerateInterpolator(3.0f))
//                .setListener(new Animator.AnimatorListener() {
//                                 @Override
//                                 public void onAnimationStart(Animator animation) {
//                                     my_drawer.setVisibility(View.VISIBLE);
//                                     dimOverlay.setVisibility(View.VISIBLE);
//                                 }
//
//                                 @Override
//                                 public void onAnimationEnd(Animator animation) {
//
//                                 }
//
//                                 @Override
//                                 public void onAnimationCancel(Animator animation) {
//
//                                 }
//
//                                 @Override
//                                 public void onAnimationRepeat(Animator animation) {
//
//                                 }
//                             });
//        isDrawerOpen = true;
    }

    /* Closes main drawer */
    public void closeDrawer(View view) {
        my_drawer.closeDrawer(GravityCompat.START);
//        if (!isDrawerOpen)  // Cancel if closed already
//            return;
//
//        toggleViews("close");
//        final FrameLayout dimOverlay = (FrameLayout) findViewById(R.id.DimOverlay);
//        final FrameLayout typesList = (FrameLayout) findViewById(R.id.types_list);
//
//        my_drawer.animate().translationX(-1 * my_drawer.getWidth()).setDuration(300)
//                .setInterpolator(new DecelerateInterpolator())
//                .setListener(new Animator.AnimatorListener() {
//                    @Override
//                    public void onAnimationStart(Animator animation) {
//                        dimOverlay.setVisibility(View.GONE);
//                        dimOverlay.setVisibility(View.GONE);
//                    }
//
//                    @Override
//                    public void onAnimationEnd(Animator animation) {
//                        my_drawer.setVisibility(View.GONE);
//                        if(typesList.getVisibility() == View.VISIBLE)
//                            viewFoodTypeList(my_drawer);
//                    }
//
//                    @Override
//                    public void onAnimationCancel(Animator animation) {
//
//                    }
//
//                    @Override
//                    public void onAnimationRepeat(Animator animation) {
//
//                    }
//                });
//
//        isDrawerOpen = false;
    }

    public void toggleViews (String s){
        ViewPager foodPic = (ViewPager) findViewById (R.id.viewPager_images);

        if (s.equals("close")) {
            foodPic.setVisibility(View.VISIBLE);
        }
        else {
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

    public void startNewSearch(View view) {
        mainPageFragment.changeFood(null, null);
        firstRequest = true;
        offset = 0;
        listItems.clear();
        imageList.clear();
        imageAdapter.notifyDataSetChanged();
        newFilterSearch();
    }

    // Called by AWSIntegratorTask to return json
    @Override
    public void doSomethingWithResults(String ob) {
        offset += businessLimit;    // Successful request -> Increase offset for next request
        List<FoodReceive> foodReceives = ConvertToObject.toFoodReceiveList(ob);

        // Critical section
        Application.getInstance().accessList.lock();
        Log.d("TinderActivity", "First request done");
        listItems.addAll(ConvertToObject.toListItemClassList(foodReceives));
        requestImages();
        Application.getInstance().accessList.unlock();
    }

    // Called by AWSIntegratorTask if internet request fails
    @Override
    public void doSomethingOnAWSError() {
        handleNoInternet(ConnectivityReceiver.REQUEST_FROM_DATABASE);
        requestMade = false;
        changeSearchButton(false);
    }

    // Called by GetImagesAsyncTask to return list of bitmaps
    @Override
    public void doSomethingWithDownloadedImages(List<Bitmap> images) {
        // Critical Section: Add images to list here in activity
        Application.getInstance().accessList.lock();

        Log.d("TinderActivity", "Finished request");

        if (images != null && !images.isEmpty()) {
            // Put images into list of images
            imageList.addAll(images);

            // Remove placeholder if one is made
            if (placeholderIsPresent) {
                Log.d("TinderActivity", "Removing placeholder");
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
        changeSearchButton(false);
        Application.getInstance().accessList.unlock();

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
    public void doSomethingWithImageView(ImageView imageView, Bitmap b, String foodID) {

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

    public void yesHold () {
        final ImageView yesIcon = (ImageView) findViewById(R.id.yes_icon);
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
        final ImageView yesIcon = (ImageView) findViewById(R.id.yes_icon);
        final ImageView yesCircle = (ImageView) findViewById(R.id.yes_circle);
        final ImageView yesShadow = (ImageView) findViewById(R.id.yes_shadow);

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
                                .setDuration(350)
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

    // Jordan's read function: Now returns count instead of setting it here
    private int loadSavedFoods() {
        // FIXME: Then write all results to file
        HashSet<String> clicked = readClickedFile();
        deleteFile(Application.SAVED_CLICKED_FOODS);

        FileInputStream fis = null;
        StringBuilder builder = new StringBuilder();
        int cnt = 0;

        try {
            fis = openFileInput(Application.SAVED_LIKED_FOODS);
            int c;

            while ((c = fis.read()) != -1)
                builder.append((char) c);

            String[] lines = builder.toString().split("\n");
            LinkedList<ListItemClass> likedData = new LinkedList<>();

            for (String s : lines) {
                ListItemClass item = ListItemClass.createFrom(s);
                likedData.add(item);

                if (!item.isClicked()) {
                    if (clicked != null && clicked.contains(item.getFoodId())) {
                        item.setClicked(1);
                        saveFileEdited = true;
                    } else {
                        cnt++;
                    }
                }
            }

            Application.getInstance().setLikedData(likedData);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (fis != null)
                    fis.close();
            } catch (IOException e) {
                if (e instanceof FileNotFoundException)
                    Log.d("TinderActivity", "No saved file yet");
                else
                    e.printStackTrace();
            }
        }

        return cnt;
    }

    HashSet<String> readClickedFile() {
        FileInputStream fis = null;
        StringBuilder builder = new StringBuilder();

        try {
            fis = openFileInput(Application.SAVED_CLICKED_FOODS);
            int c;

            while ((c = fis.read()) != -1)
                builder.append((char) c);

            String[] lines = builder.toString().split("\n");
            for (String s : lines)
                Log.d("Test", s);

            HashSet<String> clickedFoods = new HashSet<>(lines.length);
            for (String s : lines)
                clickedFoods.add(s);

            return clickedFoods;
        } catch (IOException e) {
            if (e instanceof FileNotFoundException)
                Log.d("TinderActivity", "No clicked file yet");
            else
                e.printStackTrace();
        } finally {
            try {
                if (fis != null)
                    fis.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return null;
    }

    void saveChangesLikedFood() {
        FileOutputStream fos = null;
        try {
            fos = openFileOutput(Application.SAVED_LIKED_FOODS, MODE_PRIVATE);
            List<ListItemClass> data = Application.getInstance().getLikedData();

            for (ListItemClass item : data) {
                fos.write(item.getFileString().getBytes());
                fos.write('\n');
            }

            Log.d("TinderActivity", "Finished writing changes");
        } catch (IOException e) {
            if (e instanceof FileNotFoundException)
                Log.e("TinderActivity", "Liked file does not exist");
            else
                e.printStackTrace();
        } finally {
            try {
                if (fos != null)
                    fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
