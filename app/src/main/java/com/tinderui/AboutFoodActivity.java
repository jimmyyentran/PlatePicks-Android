package com.tinderui;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import com.foodtinder.R;

/**
 * Created by pokeforce on 4/24/16.
 */
public class AboutFoodActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_aboutfood);
        setupToolbar();
        //noinspection ConstantConditions
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    /* setupToolbar():
     * Fetches toolbar from loaded xml file and sets as the "action bar" (what Android calls the
     * top bar. Toolbar is a new class with extra features.) */
    void setupToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_tinder);
        setSupportActionBar(toolbar);
    }
}
