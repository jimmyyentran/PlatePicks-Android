package com.platepicks;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.platepicks.util.ListAdapter;
import com.platepicks.objects.ListItemClass;
import com.platepicks.util.ListSwipeAdapter;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
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
    int items_clicked;

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
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                int pos = data.size() - i - 1;
                gotoAbout(pos);
            }
        });

        // Create the adapter to convert the array to views
        adapter = new ListSwipeAdapter(this);
        listView.setAdapter(adapter);
        items_clicked = 0;

//        final EditText deleteField = (EditText) findViewById(R.id.editText);
//        final Button deleteButton = (Button) findViewById(R.id.button);
//        deleteButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                int del = data.size() - 1 - Integer.parseInt(deleteField.getText().toString());
//                writeToDeletedFile(del);
//                data.remove(del);
//                adapter.notifyDataSetChanged();
//            }
//        });
    }

    public void gotoAbout(int index) {
        ListItemClass item = data.get(index);
        if(!item.isClicked()) {
            item.setClicked(1);
            ++items_clicked;

            writeToClickedFile(index);
        }
        adapter.notifyDataSetChanged();

        Intent intent = new Intent(LikedListActivity.this, AboutFoodActivity.class);
        intent.putExtra("key2", item);
        intent.putExtra("origin", "list page");
        startActivity(intent);
    }

    public void backArrow (View view){
        // set all data to be viewed
        Intent intent = new Intent(LikedListActivity.this, TinderActivity.class);
        intent.putExtra("items clicked", items_clicked);
        setResult(RESULT_OK, intent);
        finish();
    }

    @Override
    public void onBackPressed() {
        backArrow(null);
    }

    void writeToClickedFile(int index) {
        FileOutputStream fos = null;
        try {
            fos = openFileOutput(Application.SAVED_CLICKED_FOODS, MODE_APPEND);
            fos.write(data.get(index).getFoodId().getBytes());
            fos.write('\n');

            Log.d("LikedListActivity", "Added to clicked list");
        } catch (IOException e) {
            if (e instanceof FileNotFoundException)
                Log.d("LikedListActivity", "Clicked list does not exist");
            else
                e.printStackTrace();
        } finally {
            try {
                if (fos != null)
                    fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    void writeToDeletedFile(int index) {
        FileOutputStream fos = null;
        try {
            fos = openFileOutput(Application.SAVED_DELETED_FOODS, MODE_APPEND);
            fos.write(data.get(index).getFoodId().getBytes());
            fos.write('\n');

            Log.d("LikedListActivity", "Added to deleted list");
        } catch (IOException e) {
            if (e instanceof FileNotFoundException)
                Log.d("LikedListActivity", "Deleted list does not exist");
            else
                e.printStackTrace();
        } finally {
            try {
                if (fos != null)
                    fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void deleteItem(View view) {
        int del = (int) view.getTag();
        writeToDeletedFile(del);
        data.remove(del);
        adapter.notifyDataSetChanged();
    }
}

