package com.tinderui;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.foodtinder.R;

/**
 * Created by pokeforce on 4/24/16.
 */
public class AboutFoodActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        /* Basic setup of which layout we want to use (aboutfood) and toolbar (set as "action bar"
         * so Android puts menu options in it) */
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_aboutfood);

        /* Give the toolbar a "back" (aka "up") button. Goes back "up" to previous activity because
         * of specification in onOptionsItemSelected() */
        //noinspection ConstantConditions
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

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
}
