package com.tinderui;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.foodtinder.R;
import com.google.android.gms.common.api.GoogleApiClient;

import org.w3c.dom.Text;

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


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_liked_list);

        // Construct the data source
        ArrayList<ListItemClass> data = getIntent().getParcelableArrayListExtra("key");

        // font stuff
        String fontPath = "fonts/Hamburger_Heaven.TTF";
        TextView likes_title = (TextView) findViewById(R.id.likes_title);
        Typeface tf = Typeface.createFromAsset(getAssets(), fontPath);
        likes_title.setTypeface(tf);


        // Create the adapter to convert the array to views
        ListAdapter adapter = new ListAdapter(this, data);

        // Attach the adapter to a ListView
        ListView listView = (ListView) findViewById(R.id.listview_liked);

        listView.setAdapter(adapter);


    }


}

