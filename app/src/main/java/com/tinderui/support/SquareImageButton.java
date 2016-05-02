package com.tinderui.support;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.widget.ImageButton;
import android.widget.ImageView;

/* Created by pokeforce on 4/12/16. Specialized version of ImageView to always be sqaure. */
public class SquareImageButton extends ImageButton {
    public SquareImageButton(Context context) {
        super(context);
    }

    public SquareImageButton(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SquareImageButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public SquareImageButton(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    /* onMeasure():
     * The custom part of this class. When the view is "measuring" how big its width and height are,
     * it should set its height to its width. */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        // Set height and width to minimum of either (contained in checkers vertically or contained in sides horizontally)
        int width = getMeasuredWidth();
        int height = getMeasuredHeight();
        int squareSide = Math.min(width, height);

        setMeasuredDimension(squareSide, squareSide);
    }
}
