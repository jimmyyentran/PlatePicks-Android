package com.platepicks;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.platepicks.support.SquareImageButton;
import com.platepicks.util.ListItemClass;

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

                foodPicture.setImageBitmap(image);
                yelp_logo.setVisibility(View.VISIBLE);
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
        yelp_logo = (ImageView) fragmentView.findViewById(R.id.required_yelp);
        placeholder = (LinearLayout) fragmentView.findViewById(R.id.placeholder_container);
        RelativeLayout foodBorder = (RelativeLayout) fragmentView.findViewById(R.id.food_border);

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

}
