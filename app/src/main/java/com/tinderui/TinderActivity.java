package com.tinderui;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Toast;

import com.foodtinder.R;
import com.foodtinder.util.AWSIntegrator;

import java.util.ArrayList;

/**
 * Created by pokeforce on 4/12/16.
 */
public class TinderActivity extends AppCompatActivity {
    private Toolbar toolbar;

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
        setupToolbar();
        if (getSupportActionBar() != null)
            getSupportActionBar().setTitle("Food Tinder");

        /* Yes and No Buttons:
         * Finding reference to buttons in xml layout to keep as objects in Java */
        Button noButton = (Button) findViewById(R.id.button_no);
        Button yesButton = (Button) findViewById(R.id.button_yes);

        /* On Click Listeners:
         * Functions that are called whenever the user clicks on the buttons */
        noButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(TinderActivity.this, "I clicked No!", Toast.LENGTH_SHORT).show();
            }
        });
        yesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(TinderActivity.this, "I clicked Yes!", Toast.LENGTH_SHORT).show();

            }
        });
        AWSIntegrator.call("hello-world", "{\n  \"key1\" : \"value1\",\n  \"key2\" : \"value2\",\n  \"key3\" : \"value3\"\n}");
    }


    /* onCreateOptionsMenu():
     * Creates the menu by loading the items in the xml into the toolbar */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_tinder, menu);
        return true;
    }

    /* OnOptionsItemSelected():
     * The function that is called when a menu option is clicked */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.item_like_list) {
            Toast.makeText(this, "Like list", Toast.LENGTH_SHORT).show();
        }

        return super.onOptionsItemSelected(item);
    }

    /* setupToolbar():
     * Fetches toolbar from loaded xml file and sets as the "action bar" (what Android calls the
     * top bar. Toolbar is a new class with extra features.) */
    void setupToolbar() {
        toolbar = (Toolbar) findViewById(R.id.toolbar_tinder);
        setSupportActionBar(toolbar);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return super.onTouchEvent(event);
    }
}
