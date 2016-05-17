package com.platepicks;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

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

    // Construct the data source
    ArrayList<ListItemClass> data;
    ListItemClass item = new ListItemClass();

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
        ListAdapter adapter = new ListAdapter(this, data);

        // Attach the adapter to a ListView
        ListView listView = (ListView) findViewById(R.id.listview_liked);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                gotoAbout(view);
            }
        });

        listView.setAdapter(adapter);


    }

    public void gotoAbout(View view) {
        TextView keyFood = (TextView) view.findViewById(R.id.fname);
        String foo = keyFood.getText().toString();
        item = lookUp(foo, data);

        Intent intent = new Intent(LikedListActivity.this, AboutFoodActivity.class);
        intent.putExtra("key2", item);
        LikedListActivity.this.startActivity(intent);
    }

    public ListItemClass lookUp (String key, ArrayList<ListItemClass> array)
    {
        for (int i = 0; i < array.size(); ++i) {
            Log.d(key, array.get(i).getFoodName());
            if ( (array.get(i).getFoodName().equals(key))) {
                Log.d(key, array.get(i).getFoodName());
                return array.get(i);
            }
        }
        return array.get(0);
    }



}

