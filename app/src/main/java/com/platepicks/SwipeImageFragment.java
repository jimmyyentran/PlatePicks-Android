package com.platepicks;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
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
import android.widget.ProgressBar;
import android.widget.TextView;

import com.platepicks.support.SquareImageButton;
import com.platepicks.util.ImageSaver;
import com.platepicks.objects.ListItemClass;

/**
 * Created by pokeforce on 4/22/16.
 */
public class SwipeImageFragment extends Fragment {
    public static final String PAGE_POSITION = "Page position";
    public static final int LOADING = 0, OFFLINE = 1, OUT_OF_IMG = 2;

    private SquareImageButton foodPicture = null;
    private SquareImageButton bg = null;
    private LinearLayout placeholder = null; // Only shown when out of images
    private Bitmap image;
    private ListItemClass item;
    public SquareImageButton flashingBorder;

    private int statusOfFragment = -1;

    public SquareImageButton getFoodPicture() { return foodPicture; }

    public void changeText(int statusType) {
        // View does not exist yet, so set boolean to call this function in onCreateView
        if (placeholder == null) {
            statusOfFragment = statusType;
            return;
        }

        TextView placeholderText = (TextView) placeholder.findViewById(R.id.textView_placeholder);
        ProgressBar placeholderProgress =
                (ProgressBar) placeholder.findViewById(R.id.progressBar_placeholder);

        switch (statusType) {
            case LOADING:
                placeholderText.setText(getResources().getText(R.string.placeholder_1));
                placeholderProgress.setVisibility(View.VISIBLE);
                break;
            case OFFLINE:
                placeholderText.setText(getResources().getText(R.string.placeholder_offline));
                placeholderProgress.setVisibility(View.GONE);
                break;
            case OUT_OF_IMG:
                placeholderText.setText(getResources().getText(R.string.placeholder_no_images));
                placeholderProgress.setVisibility(View.GONE);
                break;
        }
    }

    /* Changes image in imagebutton from ImageChangeListner in TinderActivity, should only be called
     * when image page is out of sight. */
    public void changeFood(Bitmap image, ListItemClass item) {
        if (foodPicture != null) {
            // Put placeholder indicating that more images are loading
            if (image == null) {
                foodPicture.setImageDrawable(null);
                bg.setImageDrawable(null);
                placeholder.setVisibility(View.VISIBLE);

                Log.d("SwipeImageFragment", "image is null");
            }
            // Change picture
            else {
                // Remove placeholder if need be
                if (placeholder.getVisibility() != View.GONE) {
                    placeholder.setVisibility(View.GONE);
                }

                flashingBorder.setAlpha(0.0f);
                foodPicture.setImageBitmap(image);

                // Set image to global Application class
                Application app = (Application) getActivity().getApplication();
                app.setImage(image);

                if(Build.VERSION.SDK_INT >= 17) {
                    bg.setImageBitmap(BlurImageTool.blur(getContext(), image));
//                    foodPicture.setBackgroundColor(Color.WHITE);
                }
                else {
                    foodPicture.setBackgroundColor(Color.WHITE);
                }

                Log.d("SwipeImageFragment", "change image");
            }

            this.image = image;
            this.item = item;
        } else {
            Log.d("SwipeImageFragment", "FoodPicture is null");
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        int pagePosition = 0;           /* Page position in viewpager */
        Bundle args = getArguments();   /* Arguments passed in */
        if (args != null) {
            // Must have this or null pointer exception
            if (!args.containsKey(PAGE_POSITION)) {
                throw new NullPointerException("No page position for SwipeImageFragment");
            }

            pagePosition = args.getInt(PAGE_POSITION);
        }

        /* Give empty layout instead if not main page*/
        if (pagePosition != 1)
            return inflater.inflate(R.layout.fragment_empty_slide, container, false);

        View fragmentView = inflater.inflate(R.layout.fragment_slide_image, container, false);
        foodPicture = (SquareImageButton) fragmentView.findViewById(R.id.imagebutton_tinder);
        bg = (SquareImageButton) fragmentView.findViewById(R.id.blurred_image);
        placeholder = (LinearLayout) fragmentView.findViewById(R.id.placeholder_container);
        flashingBorder = (SquareImageButton) fragmentView.findViewById(R.id.flashing_border);

        /* Put offline text in placeholder here */
        if (statusOfFragment != -1) {
            changeText(statusOfFragment);
            statusOfFragment = -1;
        }

        /* Tell TinderActivity that objects for SwipeImageFragment are ready */
        ((TinderActivity) getActivity()).onCreatedUI();

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
                    aboutPage.putExtra("origin", "main page");

                    int request = 99;
                    Log.d("SwipeImageFragment", "Before startActivity " + request);
                    getActivity().startActivityForResult(aboutPage, request);
                }
            }
        });
        
        return fragmentView;
    }

    public void borderFlash (String color) {
        if(color.equals("red"))
            flashingBorder.setImageResource(R.drawable.flashing_border_red);
        else if(color.equals("green"))
            flashingBorder.setImageResource(R.drawable.flashing_border_green);

        flashingBorder.animate().alpha(1.0f)
                .setDuration(100)
                .setInterpolator(new AccelerateInterpolator())
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        flashingBorder.animate().setListener(null);
                        flashingBorder.animate().alpha(0.5f)
                                .setDuration(250)
                                .setInterpolator(new DecelerateInterpolator())
                                .setListener(new AnimatorListenerAdapter() {
                                    @Override
                                    public void onAnimationEnd(Animator animation) {
                                        flashingBorder.animate().setListener(null);
                                        flashingBorder.animate().alpha(0.0f)
                                                .setDuration(100)
                                                .setInterpolator(new DecelerateInterpolator())
                                                .setListener(null);
                                    }
                                });
                    }
                });
    }

}
