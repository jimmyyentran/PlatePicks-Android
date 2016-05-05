package com.tinderui;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.foodtinder.R;
import com.tinderui.support.SquareImageButton;

/**
 * Created by pokeforce on 4/22/16.
 */
public class SwipeImageFragment extends Fragment {
    public static String PAGE_POSITION = "Page position", PIC_INDEX = "Pic index";

    SquareImageButton foodPicture;
    Bitmap bitmap;

    static public SwipeImageFragment newInstance(Bitmap bitmap) {
        SwipeImageFragment fragment = new SwipeImageFragment();
        fragment.setBitmap(bitmap);
        return fragment;
    }

    private void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
    }

    // FIXME: param will be bitmap once we have them
    /* Changes image in imagebutton from ImageChangeListner in TinderActivity, should only be called
     * when image page is out of sight. */
    public void changeImage(int index) {
        foodPicture.setImageResource(android.R.color.white); /* In case processing takes a while */

        if (index == 0)
            foodPicture.setImageResource(R.drawable.main_screen_no_checkers);
        else if (index == 1)
            foodPicture.setImageResource(R.drawable.mango_demo);
        else if (index == 2)
            foodPicture.setImageResource(R.drawable.raisins);
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
        if (pagePosition == 1) {
            foodPicture.setImageResource(R.drawable.main_screen_no_checkers);
        } else {
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
