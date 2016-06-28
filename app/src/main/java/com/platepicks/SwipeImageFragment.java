package com.platepicks;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicBlur;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import com.platepicks.BlurImageTool;

import com.platepicks.support.SquareImageButton;

/**
 * Created by pokeforce on 4/22/16.
 */
public class SwipeImageFragment extends Fragment {
    public static String PAGE_POSITION = "Page position", PIC_INDEX = "Pic index";

    private SquareImageButton foodPicture = null;
    private SquareImageButton bg = null;
    private LinearLayout placeholder = null; // Only shown when out of images
    private ImageView yelp_logo = null;
    private Bitmap image;
    private ListItemClass item;
    public SquareImageButton flashingBorder;

    public SquareImageButton getFoodPicture() { return foodPicture; }

    /* Changes image in imagebutton from ImageChangeListner in TinderActivity, should only be called
     * when image page is out of sight. */
    public void changeFood(Bitmap image, ListItemClass item) {
        if (foodPicture != null) {
            // Put placeholder indicating that more images are loading
            if (image == null) {
                foodPicture.setImageDrawable(null);
                bg.setImageDrawable(null);
                yelp_logo.setVisibility(View.GONE);
                placeholder.setVisibility(View.VISIBLE);
            }
            // Change picture
            else {
                // Remove placeholder if need be
                if (placeholder.getVisibility() != View.GONE) {
                    placeholder.setVisibility(View.GONE);
                }

                flashingBorder.setAlpha(0.0f);
                foodPicture.setImageBitmap(image);
                if(Build.VERSION.SDK_INT >= 17) {
                    bg.setImageBitmap(BlurImageTool.blur(getContext(), image));
                }
                else {
                    foodPicture.setBackgroundColor(Color.WHITE);
                }
            }

            this.image = image;
            this.item = item;
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        int pagePosition = 0;   /* Page position in viewpager */
        if (getArguments() != null)
            pagePosition = getArguments().getInt(PAGE_POSITION);

        View fragmentView = inflater.inflate(R.layout.fragment_slide_image, container, false);
        foodPicture = (SquareImageButton) fragmentView.findViewById(R.id.imagebutton_tinder);
        bg = (SquareImageButton) fragmentView.findViewById(R.id.blurred_image);
        placeholder = (LinearLayout) fragmentView.findViewById(R.id.placeholder_container);
        RelativeLayout foodBorder = (RelativeLayout) fragmentView.findViewById(R.id.food_border);
        flashingBorder = (SquareImageButton) fragmentView.findViewById(R.id.flashing_border);

        /* Set the image resource here */
        if (pagePosition != 1) {
            foodBorder.setVisibility(View.GONE);
        }

        /* Open the about food activity here */
        foodPicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (item != null) {
                    new ImageSaver(getContext()).
                            setFileName(item.getFoodId()).
                            setDirectoryName("images").
                            save(image);

                    Intent aboutPage = new Intent(getActivity(), AboutFoodActivity.class);
                    aboutPage.putExtra("key2", item);
                    startActivity(aboutPage);
                }
//                Log.d("SwipeImageFragment", item.getFoodId() + "," +
//                        item.getFoodName() + "," +
//                        item.getRestaurantName() + "," +
//                        item.getImageUrl());
            }
        });
        // lat and longitude could be negative (west/east)
        // Meters not miles (radius)
        // Json with these things grab a list of restaurant ids, which divvy's code grabs
        // images/comments/food names/food ids for
        
        return fragmentView;
    }

    public void borderFlash (String color) {
        if(color == "red")
            flashingBorder.setImageResource(R.drawable.flashing_border_red);
        else if(color == "green")
            flashingBorder.setImageResource(R.drawable.flashing_border_green);

        flashingBorder.animate().alpha(1.0f)
                .setDuration(100)
                .setInterpolator(new AccelerateInterpolator())
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        flashingBorder.animate().setListener(null);
                        flashingBorder.animate().alpha(0.5f)
                                .setDuration(300)
                                .setInterpolator(new DecelerateInterpolator())
                                .setListener(null);
                    }
                });
    }

}
