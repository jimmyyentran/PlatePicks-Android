package com.platepicks.dynamoDB.nosql;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.amazonaws.AmazonClientException;
import com.amazonaws.mobile.AWSMobileClient;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMapper;

import java.util.Set;

public class NoSQLListResult implements NoSQLResult {
    private static final int KEY_TEXT_COLOR = 0xFF333333;
    private final ListDO result;

    NoSQLListResult(final ListDO result) {
        this.result = result;
    }
    @Override
    public void updateItem() {
        final DynamoDBMapper mapper = AWSMobileClient.defaultMobileClient().getDynamoDBMapper();
        final double originalValue = result.getCreationDate();
        result.setCreationDate(SampleDataGenerator.getRandomSampleNumber());
        try {
            mapper.save(result);
        } catch (final AmazonClientException ex) {
            // Restore original data if save fails, and re-throw.
            result.setCreationDate(originalValue);
            throw ex;
        }
    }

    @Override
    public void deleteItem() {
        final DynamoDBMapper mapper = AWSMobileClient.defaultMobileClient().getDynamoDBMapper();
        mapper.delete(result);
    }

    private void setKeyTextViewStyle(final TextView textView) {
        textView.setTextColor(KEY_TEXT_COLOR);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.MATCH_PARENT);
        layoutParams.setMargins(dp(5), dp(2), dp(5), 0);
        textView.setLayoutParams(layoutParams);
    }

    /**
     * @param dp number of design pixels.
     * @return number of pixels corresponding to the desired design pixels.
     */
    private int dp(int dp) {
        DisplayMetrics metrics = Resources.getSystem().getDisplayMetrics();
        return dp * (metrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT);
    }
    private void setValueTextViewStyle(final TextView textView) {
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.MATCH_PARENT);
        layoutParams.setMargins(dp(15), 0, dp(15), dp(2));
        textView.setLayoutParams(layoutParams);
    }

    private void setKeyAndValueTextViewStyles(final TextView keyTextView, final TextView valueTextView) {
        setKeyTextViewStyle(keyTextView);
        setValueTextViewStyle(valueTextView);
    }

    private static String bytesToHexString(byte[] bytes) {
        final StringBuilder builder = new StringBuilder();
        builder.append(String.format("%02X", bytes[0]));
        for(int index = 1; index < bytes.length; index++) {
            builder.append(String.format(" %02X", bytes[index]));
        }
        return builder.toString();
    }

    private static String byteSetsToHexStrings(Set<byte[]> bytesSet) {
        final StringBuilder builder = new StringBuilder();
        int index = 0;
        for (byte[] bytes : bytesSet) {
            builder.append(String.format("%d: ", ++index));
            builder.append(bytesToHexString(bytes));
            if (index < bytesSet.size()) {
                builder.append("\n");
            }
        }
        return builder.toString();
    }

    @Override
    public View getView(final Context context, final View convertView, int position) {
        final LinearLayout layout;
        final TextView resultNumberTextView;
        final TextView userIdKeyTextView;
        final TextView userIdValueTextView;
        final TextView foodIdKeyTextView;
        final TextView foodIdValueTextView;
        final TextView creationDateKeyTextView;
        final TextView creationDateValueTextView;
        final TextView typeKeyTextView;
        final TextView typeValueTextView;
        if (convertView == null) {
            layout = new LinearLayout(context);
            layout.setOrientation(LinearLayout.VERTICAL);
            resultNumberTextView = new TextView(context);
            resultNumberTextView.setGravity(Gravity.CENTER_HORIZONTAL);
            layout.addView(resultNumberTextView);


            userIdKeyTextView = new TextView(context);
            userIdValueTextView = new TextView(context);
            setKeyAndValueTextViewStyles(userIdKeyTextView, userIdValueTextView);
            layout.addView(userIdKeyTextView);
            layout.addView(userIdValueTextView);

            foodIdKeyTextView = new TextView(context);
            foodIdValueTextView = new TextView(context);
            setKeyAndValueTextViewStyles(foodIdKeyTextView, foodIdValueTextView);
            layout.addView(foodIdKeyTextView);
            layout.addView(foodIdValueTextView);

            creationDateKeyTextView = new TextView(context);
            creationDateValueTextView = new TextView(context);
            setKeyAndValueTextViewStyles(creationDateKeyTextView, creationDateValueTextView);
            layout.addView(creationDateKeyTextView);
            layout.addView(creationDateValueTextView);

            typeKeyTextView = new TextView(context);
            typeValueTextView = new TextView(context);
            setKeyAndValueTextViewStyles(typeKeyTextView, typeValueTextView);
            typeValueTextView.setTypeface(Typeface.MONOSPACE);
            layout.addView(typeKeyTextView);
            layout.addView(typeValueTextView);
        } else {
            layout = (LinearLayout) convertView;
            resultNumberTextView = (TextView) layout.getChildAt(0);

            userIdKeyTextView = (TextView) layout.getChildAt(1);
            userIdValueTextView = (TextView) layout.getChildAt(2);

            foodIdKeyTextView = (TextView) layout.getChildAt(3);
            foodIdValueTextView = (TextView) layout.getChildAt(4);

            creationDateKeyTextView = (TextView) layout.getChildAt(5);
            creationDateValueTextView = (TextView) layout.getChildAt(6);

            typeKeyTextView = (TextView) layout.getChildAt(7);
            typeValueTextView = (TextView) layout.getChildAt(8);
        }

        resultNumberTextView.setText(String.format("#%d", + position+1));
        userIdKeyTextView.setText("userId");
        userIdValueTextView.setText(result.getUserId());
        foodIdKeyTextView.setText("foodId");
        foodIdValueTextView.setText(result.getFoodId());
        creationDateKeyTextView.setText("creationDate");
        creationDateValueTextView.setText("" + (long) result.getCreationDate());
        typeKeyTextView.setText("type");
        typeValueTextView.setText(bytesToHexString(result.getType()));
        return layout;
    }
}
