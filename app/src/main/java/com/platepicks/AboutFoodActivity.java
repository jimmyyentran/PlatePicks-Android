package com.platepicks;

import android.content.Context;
import android.app.LauncherActivity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutCompat;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;

import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TableLayout;
import android.widget.TextView;

import com.platepicks.dynamoDB.TableComment;
import com.platepicks.dynamoDB.nosql.CommentDO;

import java.io.File;
import java.util.List;
import static com.platepicks.dynamoDB.TableComment.insertComment;

import static com.platepicks.dynamoDB.TableComment.getCommentsFromFoodID;
import org.w3c.dom.Text;

/**
 * Created by pokeforce on 4/24/16.
 */
public class AboutFoodActivity extends AppCompatActivity implements ImageSaver.OnCompleteListener {

    ListItemClass item;
    boolean isScaled;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        /* Basic setup of which layout we want to use (aboutfood) and toolbar (set as "action bar"
         * so Android puts menu options in it) */
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_aboutfood);

        item = getIntent().getParcelableExtra("key2");
        item.setClicked(1);

        /* set custom fonts */
        Typeface quicksand = Typeface.createFromAsset(getAssets(), "fonts/Quicksand-Regular.otf");
        Typeface archistico_bold = Typeface.createFromAsset(getAssets(), "fonts/Archistico_Bold.ttf");
        Typeface ham_heaven = Typeface.createFromAsset(getAssets(), "fonts/Hamburger_Heaven.TTF");

        TextView bar_name = (TextView) findViewById(R.id.bar_title);
        bar_name.setTypeface(ham_heaven);

        final TextView restaurant = (TextView) findViewById(R.id.restaurant_name);

        restaurant.setTypeface(archistico_bold);
        restaurant.setText(item.getRestaurantName());
        restaurant.setTextSize(0);

        TextView food = (TextView) findViewById(R.id.food_name);
        food.setText(item.getFoodName());

        TextView street = (TextView) findViewById(R.id.street);
        //street.setTypeface(quicksand);
        TextView city = (TextView) findViewById(R.id.city_state);
        //city.setTypeface(quicksand);
        TextView zip = (TextView) findViewById(R.id.zip_code);
        //zip.setTypeface(quicksand);

        String whole_address = item.getRestaurantAddress();

        int comma_count = 0;
        for (int i = 0; i < whole_address.length(); ++i) {
            char x = whole_address.charAt(i);
            if (x == ',') {
                ++comma_count;
            }
        }

        if (comma_count <= 2) {
            street.setText(whole_address.split("\\,")[0]);
            city.setText(whole_address.split("\\, ")[1]);
            zip.setText(whole_address.split("\\, ")[2]);
        } else {
            street.setText(whole_address.split("\\,")[0] + ',' + whole_address.split("\\,")[1]);
            city.setText(whole_address.split("\\, ")[2]);
            zip.setText(whole_address.split("\\, ")[3]);
        }

        /* "Let's Eat!" text handling */
        final TextView eatBtn = (TextView) findViewById(R.id.eat_button);
        eatBtn.setTypeface(ham_heaven);

        // Food image
        ImageView img = (ImageView) findViewById(R.id.about_image);
        new ImageSaver(AboutFoodActivity.this).
                setFileName(item.getFoodId()).
                setDirectoryName("images").
                load(img, this);

        /* handle font size for restaurant name */
        isScaled = false;
        final ViewTreeObserver vto = restaurant.getViewTreeObserver();
        vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    restaurant.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                }
                if(!isScaled) {
                    LinearLayout ll = (LinearLayout) findViewById(R.id.ll_1);
                    float width = ll.getWidth() - ll.getPaddingRight() - ll.getPaddingLeft();
                    scaleText(restaurant, width);
                    isScaled = true;

                    eatBtn.setWidth(eatBtn.getHeight() + (int) dipToPixels(getBaseContext(), 2));

                    RelativeLayout aboutImage = (RelativeLayout) findViewById(R.id.about_image_frame);

                    LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(aboutImage.getWidth(),
                            aboutImage.getWidth() * 5 / 8);

                    aboutImage.setLayoutParams(lp);

                    ImageView foodImage = (ImageView) findViewById(R.id.about_image);
                    foodImage.setScaleType(ImageView.ScaleType.CENTER_CROP);
                }
            }
        });




        // Execute the AsyncTask by passing in foodId
        new QueryCommentsTask(this).execute(item.getFoodId());
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

    public void backArrow(View view) {
        // delete from internal storage
        File dir = getFilesDir();
        File file = new File(dir, item.getFoodId());
        boolean deleted = file.delete();
        super.onBackPressed();
    }


    public void openCommentInput(View view) {
        LinearLayout tmp = (LinearLayout) findViewById(R.id.comment_input_field);
        EditText edit = (EditText) findViewById((R.id.input_box));
        if (tmp.getVisibility() == view.GONE) {
            edit.setMaxLines(6);
            edit.setVerticalScrollBarEnabled(true);
            tmp.setVisibility(view.VISIBLE);
            edit.requestFocus();
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        } else
            tmp.setVisibility(view.GONE);

        /* Hide the soft keyboard if necessary */
        InputMethodManager mgr = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        mgr.hideSoftInputFromWindow(edit.getWindowToken(), 0);

        /* emtpy the EditText view */
        TextView tmp1 = (TextView) findViewById(R.id.input_box);
        tmp1.setText("");
    }

    public void submitComment(View view) {
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
        if (tmp.getVisibility() == view.VISIBLE)
            tmp.setVisibility(view.GONE);

        /* Hide the soft keyboard if necessary */
        EditText edit = (EditText) findViewById((R.id.input_box));
        InputMethodManager mgr = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        mgr.hideSoftInputFromWindow(edit.getWindowToken(), 0);

        /* empty the EditText view */
        TextView tmp1 = (TextView) findViewById(R.id.input_box);
        tmp1.setText("");

        new TableComment().execute("Foodie_93", item.getFoodId(), x.getText().toString());
    }

    @Override
    public void doSomethingWithBitmap(ImageView imageView, Bitmap b, String foodId) {
        imageView.setImageBitmap(b);
    }

    public void loadComments(String comment, String userID, long date) {
        System.out.println("Loading the comments");
        //String final_date = getLocalTime (date)
        LinearLayout ll = new LinearLayout(this);

        LayoutInflater lf = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        ll = (LinearLayout) lf.inflate(R.layout.comment_item, null);

        TextView x = (TextView) ll.findViewById(R.id.item_comment);
        TextView y = (TextView) ll.findViewById(R.id.item_username);
//        TextView z = (TextView) ll.findViewById(R.id.item_date);

        x.setText(comment);
        y.setText(userID);
//        z.setText(final_date);

        TableLayout tl = (TableLayout) findViewById(R.id.comment_list);
        tl.addView(ll);
    }

    private void scaleText (TextView s, float width) {
        int i = 1;
        s.setMaxLines(1);

        s.measure(View.MeasureSpec.UNSPECIFIED, s.getWidth());

        while(s.getMeasuredWidth() < width && s.getMeasuredHeight() < dipToPixels(s.getContext(), 50)){
            ++i;
            s.setTextSize(i);
            s.measure(View.MeasureSpec.UNSPECIFIED, s.getWidth());
        }
        --i;
        s.setTextSize(i);

        System.out.println("TextSize = " + Integer.toString(i));
        System.out.println("TextView width = " + Integer.toString(s.getMeasuredWidth()));
    }

    public static float dipToPixels(Context context, float dipValue) {
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dipValue, metrics);
    }

    public void goToMaps (View view){
        String address = item.getRestaurantAddress();
        Uri gmmIntentUri = Uri.parse("geo:0,0?q=" + address);
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
        mapIntent.setPackage("com.google.android.apps.maps");
        startActivity(mapIntent);
    }
}

class QueryCommentsTask extends AsyncTask<String, Void, List<CommentDO>> {
    AboutFoodActivity activity;

    public QueryCommentsTask(AboutFoodActivity activity) {
        this.activity = activity;
    }
    /** The system calls this to perform work in a worker thread and
     * delivers it the parameters given to AsyncTask.execute() */
    protected List<CommentDO> doInBackground(String... foodId) {
        return getCommentsFromFoodID(foodId[0]);
    }

    /** The system calls this to perform work in the UI thread and delivers
     * the result from doInBackground() */
    protected void onPostExecute(List<CommentDO> result) {
        for (CommentDO comment : result) {
            activity.loadComments(comment.getContent(), comment.getUserId(), comment.getTime());
        }
    }
}
