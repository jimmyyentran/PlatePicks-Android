package com.tinderui;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.foodtinder.R;

import java.util.ArrayList;

/**
 * Created by Jordan on 4/18/2016.
 */
public class linked_list_activity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_liked_list);

        ListView my_list_view = (ListView) findViewById(R.id.liked_list_view);

        ArrayList<String> my_data = new ArrayList<>();
        my_data.add("Pikachu");
        my_data.add("Jigglypuff");
        my_data.add("Mewtwo");

        ArrayAdapter<String> my_adapter = new ArrayAdapter<String>(this, R.layout.list_item_liked_list_item, my_data);

        my_list_view.setAdapter(my_adapter);
    }
}
