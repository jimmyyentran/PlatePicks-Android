package com.platepicks;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;

import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.view.animation.DecelerateInterpolator;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.platepicks.util.GetImagesAsyncTask;
import com.platepicks.util.ImageLoaderInterface;
import com.platepicks.util.ImageSaver;
import com.platepicks.objects.ListItemClass;

import java.io.File;
import java.util.List;

/**
 * Created by pokeforce on 4/24/16.
 */
public class AboutFoodActivity extends AppCompatActivity
        implements ImageSaver.OnCompleteListener,
        ImageLoaderInterface {
    final int DFLT_IMG_MAX_WIDTH = 1000, DFLT_IMG_MAX_HEIGHT = 1000;

    ImageView img;
    ListItemClass item;
    boolean isScaled;

    String origin = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        /* Basic setup of which layout we want to use (aboutfood) and toolbar (set as "action bar"
         * so Android puts menu options in it) */
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_aboutfood);

        item = getIntent().getParcelableExtra("key2");

        /* set custom fonts */
        Typeface quicksand = Typeface.createFromAsset(getAssets(), "fonts/Quicksand-Regular.otf");
        Typeface archistico_bold = Typeface.createFromAsset(getAssets(), "fonts/Archistico_Bold.ttf");
        Typeface ham_heaven = Typeface.createFromAsset(getAssets(), "fonts/Hamburger_Heaven.TTF");
        Typeface source_black_it = Typeface.createFromAsset(getAssets(), "fonts/SourceSansPro-BlackIt.otf");

        Typeface source_bold = Typeface.createFromAsset(getAssets(), "fonts/SourceSansPro-Bold.otf");

        TextView bar_name = (TextView) findViewById(R.id.bar_title);
        bar_name.setTypeface(source_bold);

        final TextView restaurant = (TextView) findViewById(R.id.restaurant_name);

        restaurant.setTypeface(source_black_it);
        restaurant.setText(item.getRestaurantName());
        restaurant.setTextSize(0);

        TextView food = (TextView) findViewById(R.id.food_name);
        food.setText(item.getFoodName());

        Typeface source_reg = Typeface.createFromAsset(getAssets(), "fonts/SourceSansPro-Regular.otf");

        TextView street = (TextView) findViewById(R.id.street);
        street.setTypeface(source_reg);
        TextView city = (TextView) findViewById(R.id.city_state);
        city.setTypeface(source_reg);
        TextView zip = (TextView) findViewById(R.id.zip_code);
        zip.setTypeface(source_reg);

        String whole_address = item.getRestaurantAddress();

        int comma_count = 0;
        for (int i = 0; i < whole_address.length(); ++i) {
            char x = whole_address.charAt(i);
            if (x == ',') {
                ++comma_count;
            }
        }

        if (comma_count <= 2) {
            street.setText(whole_address.split("\\,")[0]);
            city.setText(whole_address.split("\\, ")[1]);
            zip.setText(whole_address.split("\\, ")[2]);
        } else {
            street.setText(whole_address.split("\\,")[0] + ',' + whole_address.split("\\,")[1]);
            city.setText(whole_address.split("\\, ")[2]);
            zip.setText(whole_address.split("\\, ")[3]);
        }

        /* "Let's Eat!" text handling */
        final TextView eatBtn = (TextView) findViewById(R.id.eat_button);
        eatBtn.setTypeface(source_bold);

        /* handle font size for restaurant name */
        isScaled = false;
        final ViewTreeObserver vto = restaurant.getViewTreeObserver();
        vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    restaurant.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                }
                if(!isScaled) {
                    LinearLayout ll = (LinearLayout) findViewById(R.id.ll_1);
                    float width = ll.getWidth() - ll.getPaddingRight() - ll.getPaddingLeft();
                    scaleText(restaurant, width);
                    isScaled = true;

                    eatBtn.setWidth(eatBtn.getHeight() + (int) dipToPixels(getBaseContext(), 2));

                    RelativeLayout aboutImage = (RelativeLayout) findViewById(R.id.about_image_frame);

                    LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(aboutImage.getWidth(),
                            aboutImage.getWidth() * 6 / 8);

                    aboutImage.setLayoutParams(lp);

                    ImageView foodImage = (ImageView) findViewById(R.id.about_image);
                    foodImage.setScaleType(ImageView.ScaleType.CENTER_CROP);
                }
            }
        });

        // Food image
        img = (ImageView) findViewById(R.id.about_image);

        /* handle like/dislike buttons appearing on page */
        RelativeLayout aboutButtons = (RelativeLayout) findViewById(R.id.about_buttons_container);

        origin = getIntent().getStringExtra("origin");

        if (origin.equals("main page")) {
            // Load image from the global Application
            Application app = Application.getInstance();

            if (app.getImage() != null)
                img.setImageBitmap(app.getImage());

            aboutButtons.setVisibility(View.VISIBLE);
            Log.d("AboutFoodActivity", "From storage");
        }
        else if (origin.equals("list page")) {
            // Load image from storage through AsyncTask
            new ImageSaver(AboutFoodActivity.this).
                    setFileName(item.getFoodId()).
                    setDirectoryName("images").
                    load(img, this, false);

            item.setClicked(1);
            Log.d("ListAdapter", "::::::::::::::::::::::::::::::::::::::::::::::::::::::ITEM SET TO CLICKED: " + item.getFoodName() + "::::::::::::::::::::::::::::::::::::::::::::::::::::::");

            aboutButtons.setVisibility(View.GONE);
//            new GetImagesAsyncTask(this, this, DFLT_IMG_MAX_HEIGHT, DFLT_IMG_MAX_WIDTH)
//                    .execute(item);
//            Log.d("AboutFoodActivity", "From internet");
        }

        RelativeLayout yesButton = (RelativeLayout) findViewById(R.id.about_button_yes);
        RelativeLayout noButton = (RelativeLayout) findViewById(R.id.about_button_no);

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
    }
    /* onCreate End */

    /* OnOptionsItemSelected():
     * The function that is called when a menu option is clicked. If true is returned, we should
     * handle the menu click here. If false, Android will try to handle it.*/
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            default:
                return false;
        }
    }

    public void backArrow(View view) {
        // delete from internal storage
        File dir = getFilesDir();
        File file = new File(dir, item.getFoodId());
        boolean deleted = file.delete();
        setResult(0);
        finish();
        //super.onBackPressed();
    }


    public void openCommentInput(View view) {
        LinearLayout tmp = (LinearLayout) findViewById(R.id.comment_input_field);
        EditText edit = (EditText) findViewById((R.id.input_box));
        if (tmp.getVisibility() == View.GONE) {
            edit.setMaxLines(6);
            edit.setVerticalScrollBarEnabled(true);
            tmp.setVisibility(View.VISIBLE);
            edit.requestFocus();
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        } else
            tmp.setVisibility(View.GONE);

        /* Hide the soft keyboard if necessary */
        InputMethodManager mgr = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        mgr.hideSoftInputFromWindow(edit.getWindowToken(), 0);

        /* emtpy the EditText view */
        edit.setText("");
    }

    // ImageSaver.OnCompleteListener
    @Override
    public void doSomethingWithBitmap(ImageView imageView, Bitmap b, String foodId) {
        imageView.setImageBitmap(b);
    }

    // ImageLoaderInterface
    @Override
    public void doSomethingWithDownloadedImages(List<Bitmap> images) {
        if (images != null && !images.isEmpty())
            img.setImageBitmap(images.get(0));
    }

    @Override
    public void doSomethingOnImageError() {
        Log.e("AboutFoodActivity", "Error downloading image");
        img.setImageResource(R.drawable.no_pix);
    }

    @Override
    public void doSomethingWithImageView(ImageView imageView, Bitmap b, String foodID) {

    }

    private void scaleText (TextView s, float width) {
        int i = 1;
        s.setMaxLines(1);

        s.measure(View.MeasureSpec.UNSPECIFIED, s.getWidth());

        while(s.getMeasuredWidth() < width && s.getMeasuredHeight() < dipToPixels(s.getContext(), 50)){
            ++i;
            s.setTextSize(i);
            s.measure(View.MeasureSpec.UNSPECIFIED, s.getWidth());
        }
        --i;
        s.setTextSize(i);

        System.out.println("TextSize = " + Integer.toString(i));
        System.out.println("TextView width = " + Integer.toString(s.getMeasuredWidth()));
    }

    public static float dipToPixels(Context context, float dipValue) {
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dipValue, metrics);
    }

    public void goToMaps (View view){
        String address = item.getRestaurantAddress();
        Uri gmmIntentUri = Uri.parse("geo:0,0?q=" + address);
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
        mapIntent.setPackage("com.google.android.apps.maps");
        startActivity(mapIntent);
    }

    public void yesHold () {
        final ImageView yesIcon = (ImageView) findViewById(R.id.about_yes_icon);
        final ImageView yesCircle = (ImageView) findViewById(R.id.about_yes_circle);
        final ImageView yesShadow = (ImageView) findViewById(R.id.about_yes_shadow);

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
        final ImageView yesIcon = (ImageView) findViewById(R.id.about_yes_icon);
        final ImageView yesCircle = (ImageView) findViewById(R.id.about_yes_circle);
        final ImageView yesShadow = (ImageView) findViewById(R.id.about_yes_shadow);

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
                                        Log.d("yesReleased", "BEFORE FINISH");
                                        yesIcon.animate().setListener(null);
                                        setResult(1);
                                        finish();
                                        Log.d("yesReleased", "AFTER FINISH");

                                        //finishActivity(1);
                                    }
                                });
                    }
                });

    }

    public void noHeld () {
        final ImageView noIcon = (ImageView) findViewById(R.id.about_no_icon);
        final ImageView noCircle = (ImageView) findViewById(R.id.about_no_circle);
        final ImageView noShadow = (ImageView) findViewById(R.id.about_no_shadow);

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

        final ImageView noIcon = (ImageView) findViewById(R.id.about_no_icon);
        final ImageView noCircle = (ImageView) findViewById(R.id.about_no_circle);
        final ImageView noShadow = (ImageView) findViewById(R.id.about_no_shadow);

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
                                        Log.d("noReleased", "BEFORE FINISH");

                                        noIcon.animate().setListener(null);
                                        setResult(2);
                                        finish();
                                        Log.d("noReleased", "AFTER FINISH");
                                    }
                                });
                    }
                });
    }
}
