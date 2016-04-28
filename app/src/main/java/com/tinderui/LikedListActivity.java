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

import com.foodtinder.R;
import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;

import java.util.ArrayList;

/**
 * Created by elizamae on 4/18/16.
 */
public class LikedListActivity extends AppCompatActivity {

    ListView likedList;
    /**
     * Creates a list of all liked foods.
     * Add to list when swiped right.
     */
    private GoogleApiClient client;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_liked_list);

        likedList = (ListView) findViewById(R.id.listview_liked);


        /* Get array from TinderActivity using Intent::getStringArrayListExtra */
        Intent intent = getIntent();
        ArrayList<String> data = intent.getStringArrayListExtra("whatever");

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.list_item_liked, data);

        likedList.setAdapter(adapter);


      /*  likedList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

            }
        }; */
    }
}

