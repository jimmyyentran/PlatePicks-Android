package com.platepicks;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.platepicks.util.ListAdapter;
import com.platepicks.objects.ListItemClass;
import com.platepicks.util.WriteToLikedFileTask;

import java.util.ArrayList;

/**
 * Created by elizamae on 4/18/16.
 */
public class LikedListActivity extends AppCompatActivity {

    /**
     * Creates a list of all liked foods.
     * Add to list when swiped right.
     */

    public static final String LIKED_LIST_TAG = "gohead";
    public static final String CHANGE_LIST = "change list";

    ListAdapter adapter = null;
    int items_clicked;

    // Construct the data source
    ArrayList<ListItemClass> data = new ArrayList<>();
    ListItemClass item = new ListItemClass();
    boolean listChanged = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_liked_list);

        // font stuff
        String fontPath = "fonts/Hamburger_Heaven.TTF";
        TextView likes_title = (TextView) findViewById(R.id.likes_title);

        // Loading Font Face
        Typeface tf = Typeface.createFromAsset(getAssets(), fontPath);
        Typeface source_bold = Typeface.createFromAsset(getAssets(), "fonts/SourceSansPro-Bold.otf");

        // Applying font
        likes_title.setTypeface(source_bold);

        // Construct the data source
        data = getIntent().getParcelableArrayListExtra("key");

        // Create the adapter to convert the array to views
        adapter = new ListAdapter(this, data);

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
        items_clicked = 0;
    }

    public void gotoAbout(int index) {
        item = data.get(index);
        if(!item.isClicked()) {
            item.setClicked(1);
            ++items_clicked;

            // Change file to account for click
            String[] array = new String[data.size()];
            for (int i = 0; i < array.length; i++)
                array[i] = data.get(i).getFileString();

            new WriteToLikedFileTask(this, WriteToLikedFileTask.REPLACE_ALL).execute(array);
        }
        adapter.notifyDataSetChanged();
        Intent intent = new Intent(LikedListActivity.this, AboutFoodActivity.class);
        intent.putExtra("key2", item);
        intent.putExtra("origin", "list page");
        LikedListActivity.this.startActivity(intent);
    }

    public void backArrow (View view){
        // set all data to be viewed
        Intent intent = new Intent(LikedListActivity.this, TinderActivity.class);
        intent.putParcelableArrayListExtra(LIKED_LIST_TAG, data);
        intent.putExtra("items clicked", items_clicked);
        setResult(RESULT_OK, intent);
        finish();
    }

    @Override
    public void onBackPressed() {
        backArrow(null);
    }
}

