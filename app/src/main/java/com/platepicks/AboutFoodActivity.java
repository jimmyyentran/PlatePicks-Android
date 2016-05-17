package com.platepicks;

import android.content.Context;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutCompat;
import android.support.v7.widget.Toolbar;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TextView;

import org.w3c.dom.Text;

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

        /* set custom fonts */
        Typeface quicksand = Typeface.createFromAsset(getAssets(), "fonts/Quicksand-Regular.otf");
        Typeface archistico_bold = Typeface.createFromAsset(getAssets(), "fonts/Archistico_Bold.ttf");
        Typeface ham_heaven = Typeface.createFromAsset(getAssets(), "fonts/Hamburger_Heaven.TTF");

        TextView bar_name = (TextView) findViewById(R.id.bar_title);
        bar_name.setTypeface(ham_heaven);

        TextView restaurant = (TextView) findViewById(R.id.restaurant_name);
        restaurant.setTypeface(archistico_bold);

        TextView food = (TextView) findViewById(R.id.food_name);
        TextView tmp1 = (TextView) findViewById(R.id.street);
        tmp1.setTypeface(quicksand);
        tmp1 = (TextView) findViewById(R.id.city_state);
        tmp1.setTypeface(quicksand);
        tmp1 = (TextView) findViewById(R.id.zip_code);
        tmp1.setTypeface(quicksand);
        food.setTypeface(quicksand);


        /* handle font size for restaurant name */
        int str_length = restaurant.getText().length();

        if(str_length <= 15){
            restaurant.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 27);
        }
        else if(str_length <= 25){
            restaurant.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 21);
        }
        else if(str_length <= 35){
            restaurant.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 15);
        }
        else{
            restaurant.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 10);
        }

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

    public void backArrow (View view){
        super.onBackPressed();
    }

    public void openCommentInput (View view) {
        LinearLayout tmp = (LinearLayout) findViewById(R.id.comment_input_field);
        if(tmp.getVisibility() == view.GONE)
            tmp.setVisibility(view.VISIBLE);
        else
            tmp.setVisibility(view.GONE);

        /* Hide the soft keyboard if necessary */
        EditText edit = (EditText) findViewById((R.id.input_box));
        InputMethodManager mgr = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        mgr.hideSoftInputFromWindow(edit.getWindowToken(),0);

        /* emtpy the EditText view */
        TextView tmp1 = (TextView)findViewById(R.id.input_box);
        tmp1.setText("");
    }

    public void submitComment (View view) {
        TableLayout tabel = (TableLayout) findViewById(R.id.comment_list);
        TextView comment_input = (TextView) findViewById(R.id.input_box);

        LinearLayout ll = new LinearLayout(this);

        LayoutInflater inflater1 = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        ll = (LinearLayout) inflater1.inflate(R.layout.comment_item, null);

        TextView x = (TextView) ll.findViewById(R.id.item_comment);
        x.setText(comment_input.getText().toString());

        TextView y = (TextView) ll.findViewById(R.id.item_username);
        //y.setText();

        tabel.addView(ll);

        /* hide the comment input field */
        LinearLayout tmp = (LinearLayout) findViewById(R.id.comment_input_field);
        if(tmp.getVisibility() == view.VISIBLE)
            tmp.setVisibility(view.GONE);

        /* Hide the soft keyboard if necessary */
        EditText edit = (EditText) findViewById((R.id.input_box));
        InputMethodManager mgr = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        mgr.hideSoftInputFromWindow(edit.getWindowToken(),0);

        /* empty the EditText view */
        TextView tmp1 = (TextView)findViewById(R.id.input_box);
        tmp1.setText("");
    }
}
