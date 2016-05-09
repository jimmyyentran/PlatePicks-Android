package com.platepicks.demo.content;

import android.text.Layout;
import android.text.SpannableString;
import android.text.style.AlignmentSpan;

import java.util.ArrayList;
import java.util.List;

public final class ContentUtils {
    /** Utility Constructor has private access. */
    private ContentUtils() {
    }

    /** Cache size values for the spinner drop down control. */
    public static final long[] cacheSizeValues =
        {1024*1024, 1024*1024*5, 1024*1024*20, 1024*1024*50, 1024*1024*100};

    public static final List<SpannableString> cacheSizeStrings;
    static {
        cacheSizeStrings = new ArrayList<>();
        cacheSizeStrings.add(getCenteredString("1 MB"));
        cacheSizeStrings.add(getCenteredString("5 MB"));
        cacheSizeStrings.add(getCenteredString("20 MB"));
        cacheSizeStrings.add(getCenteredString("50 MB"));
        cacheSizeStrings.add(getCenteredString("100 MB"));
    }

    public static SpannableString getCenteredString(final String string) {
        SpannableString spannableString = new SpannableString(string);

        spannableString.setSpan(new AlignmentSpan.Standard(Layout.Alignment.ALIGN_CENTER), 0,
            spannableString.length(), 0);
        return spannableString;
    }
}
