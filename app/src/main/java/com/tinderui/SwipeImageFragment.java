package com.tinderui;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.foodtinder.R;

/**
 * Created by pokeforce on 4/22/16.
 */
public class SwipeImageFragment extends Fragment {
    public static String INDEX = "Index";

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        int index = 0;
        if (getArguments() != null)
            index = getArguments().getInt(INDEX);

        View fragmentView = inflater.inflate(R.layout.fragment_slide_image, container, false);
        ImageView foodPicture = (ImageView) fragmentView.findViewById(R.id.imageview_tinder);

        /* Set the image resource here */
        if (index == 1) {
            foodPicture.setImageResource(R.drawable.main_screen_no_checkers);
            foodPicture.setBackgroundColor(0xc8000000);
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
