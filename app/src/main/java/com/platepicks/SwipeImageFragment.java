package com.platepicks;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.platepicks.support.SquareImageButton;

/**
 * Created by pokeforce on 4/22/16.
 */
public class SwipeImageFragment extends Fragment {
    public static String PAGE_POSITION = "Page position", PIC_INDEX = "Pic index";

    private SquareImageButton foodPicture;

    public SquareImageButton getFoodPicture() { return foodPicture; }

    /* Changes image in imagebutton from ImageChangeListner in TinderActivity, should only be called
     * when image page is out of sight. */
    public void changeImage(Bitmap image) {
        if (image == null) {
            foodPicture.setBackgroundColor(Color.BLUE);
            foodPicture.setImageDrawable(null);
        }

        foodPicture.setImageBitmap(image);
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
        RelativeLayout foodBorder = (RelativeLayout) fragmentView.findViewById(R.id.food_border);

        /* Set the image resource here */
        if (pagePosition != 1) {
            foodBorder.setVisibility(View.GONE);
        }

        /* Open the about food activity here */
        foodPicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent aboutPage = new Intent(getActivity(), AboutFoodActivity.class);
                startActivity(aboutPage);
            }
        });
        // lat and longitude could be negative (west/east)
        // Meters not miles (raidus)
        // Json with these things grab a list of restaurant ids, which divvy's code grabs
        // images/comments/food names/food ids for
        
        return fragmentView;
    }
}
