package com.tinderui;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.foodtinder.ListItemClass;
import com.foodtinder.R;
import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
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

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_liked_list);

        // Construct the data source
        ArrayList<ListItemClass> data = getIntent().getParcelableArrayListExtra("key");

        // Create the adapter to convert the array to views
        ListAdapter adapter = new ListAdapter(this, data);

        // Attach the adapter to a ListView
        ListView listView = (ListView) findViewById(R.id.listview_liked);

        listView.setAdapter(adapter);

      /*  likedList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

            }
        }; */
    }


}

