package com.platepicks;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import com.platepicks.objects.ListItemClass;
import com.platepicks.util.ListSwipeAdapter;

import java.util.List;

/**
 * Created by elizamae on 4/18/16.
 */
public class LikedListActivity extends AppCompatActivity {
    /**
     * Creates a list of all liked foods.
     * Add to list when swiped right.
     */
    ListSwipeAdapter adapter = null;

    // Construct the data source
    List<ListItemClass> data;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_liked_list);

        // font stuff
        TextView likes_title = (TextView) findViewById(R.id.likes_title);

        // Loading Font Face, Applying font
        Typeface source_bold = Typeface.createFromAsset(getAssets(), "fonts/SourceSansPro-Bold.otf");
        likes_title.setTypeface(source_bold);

        // Construct the data source
        data = Application.getInstance().getLikedData();

        // Attach the adapter to a ListView
        ListView listView = (ListView) findViewById(R.id.listview_liked);
//        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            @Override
//            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
//                int pos = data.size() - i - 1;
//                gotoAbout(pos);
//            }
//        });

        // Create the adapter to convert the array to views
        adapter = new ListSwipeAdapter(this);
        listView.setAdapter(adapter);
    }

    public void backArrow (View view){
        // set all data to be viewed
        Intent intent = new Intent(LikedListActivity.this, TinderActivity.class);
        intent.putExtra("items clicked", adapter.getItemsClicked());
        setResult(RESULT_OK, intent);
        finish();
    }

    @Override
    public void onBackPressed() {
        backArrow(null);
    }

    public void deleteItem(View view) {
        adapter.deleteItem(view);
    }
}

