package com.tinderui;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.VisibleForTesting;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import com.foodtinder.ListItemClass;
import com.foodtinder.MainActivity;
import com.foodtinder.R;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;



/**
 * Created by pokeforce on 4/12/16.
 */
public class TinderActivity extends AppCompatActivity {
    private Toolbar toolbar;
    ArrayList<ListItemClass> data = new ArrayList<>();
    int cnt = 1;

    public ListItemClass createListItem(String foodName)
    {
        ListItemClass newItem = new ListItemClass();
        newItem.setFoodName(foodName);
        newItem.setRestaurantName("test_restaurant");
        newItem.setRestaurantAddress("test_address");
        return newItem;
    }

    /* onCreate():
     * First function called by Android when creating an activity
     * */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /* XML Layout: selecting which file to set as layout */
        setContentView(R.layout.activity_tinderui);

        /* Toolbar: The red bar at the top of the app
         * Will contain our heart-shaped like button and last-recently-liked button */
        //setupToolbar();
        if (getSupportActionBar() != null)
            getSupportActionBar().setTitle("Food Tinder");

        /* Yes and No Buttons:
         * Finding reference to buttons in xml layout to keep as objects in Java */
        Button noButton = (Button) findViewById(R.id.button_no);
        Button yesButton = (Button) findViewById(R.id.button_yes);

        // LOAD YES/NO BUTTON TEXT

        Typeface Typeface_HamHeaven = Typeface.createFromAsset(getAssets(), "fonts/Hamburger_Heaven.TTF");
        noButton.setTypeface(Typeface_HamHeaven);
        yesButton.setTypeface(Typeface_HamHeaven);

        /* On Click Listeners:
         * Functions that are called whenever the user clicks on the buttons */
        noButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(TinderActivity.this, "I clicked No!", Toast.LENGTH_SHORT).show();
            }
        });

        /* when pressed, Item goes to LikedList */
        yesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(TinderActivity.this, "I clicked Yes!", Toast.LENGTH_SHORT).show();
                String name = "Food" + cnt;
                ListItemClass toAdd = createListItem(name);
                data.add(toAdd);
                cnt++;
            }
        });

        // list button
        ImageButton listButton = (ImageButton) findViewById(R.id.heart_button);
        listButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {

                Intent intent = new Intent(TinderActivity.this, LikedListActivity.class);
                intent.putParcelableArrayListExtra("key", data);
                startActivity(intent);
            }
        });
    }



}

    /* onCreateOptionsMenu():
     * Creates the menu by loading the items in the xml into the toolbar
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_tinder, menu);
        return true;
    }

    /* OnOptionsItemSelected():
     * The function that is called when a menu option is clicked
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.item_like_list) {
            Intent listbutton = new Intent(this, LikedListActivity.class);
            listbutton.putExtra("whatever", data);
            startActivity(listbutton);
        }

        return super.onOptionsItemSelected(item);
    }
}*/





