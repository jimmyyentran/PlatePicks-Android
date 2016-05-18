package com.platepicks;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.gms.common.api.GoogleApiClient;

import java.util.ArrayList;

/**
 * Created by elizamae on 4/18/16.
 */
public class LikedListActivity extends AppCompatActivity {

    /**
     * Creates a list of all liked foods.
     * Add to list when swiped right.
     */
    private GoogleApiClient client;

    // Construct the data source
    ArrayList<ListItemClass> data = new ArrayList<ListItemClass>();
    ListItemClass item = new ListItemClass();
    SharedPreferences myPrefs;
    SharedPreferences.Editor prefsEditor;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_liked_list);

        // font stuff
        String fontPath = "fonts/Hamburger_Heaven.TTF";
        TextView likes_title = (TextView) findViewById(R.id.likes_title);

        // Loading Font Face
        Typeface tf = Typeface.createFromAsset(getAssets(), fontPath);

        // Applying font
        likes_title.setTypeface(tf);

        // Construct the data source
        data = getIntent().getParcelableArrayListExtra("key");

        // Create the adapter to convert the array to views
        final ListAdapter adapter = new ListAdapter(this, data);

        // Attach the adapter to a ListView
        ListView listView = (ListView) findViewById(R.id.listview_liked);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                int pos = data.size() - i - 1;
                gotoAbout(pos);
            }
        });

        listView.setAdapter(adapter);
    }

    public void gotoAbout(int index) {
        item = data.get(index);
        Intent intent = new Intent(LikedListActivity.this, AboutFoodActivity.class);
        intent.putExtra("key2", item);
        LikedListActivity.this.startActivity(intent);
    }

    public void backArrow (View view){

        // set all data to be viewed
        for(int i = 0; i < data.size(); ++i)
        {
            data.get(i).setClicked(1);
        }

        Intent intent = new Intent(LikedListActivity.this, TinderActivity.class);
        intent.putParcelableArrayListExtra("gohead", data);
        setResult(RESULT_OK, intent);
        finish();
        super.onBackPressed();
    }


}

