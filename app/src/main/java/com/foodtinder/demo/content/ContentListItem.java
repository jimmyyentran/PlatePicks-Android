package com.foodtinder.demo.content;

import com.amazonaws.mobile.content.ContentItem;

import java.util.Comparator;

public class ContentListItem {
    /** The underlying contentItem. */
    private ContentItem contentItem;

    /** The bytes transferred for progress updates. */
    private long bytesTransferred;

    public ContentListItem(final ContentItem contentItem) {
        this.contentItem = contentItem;
        bytesTransferred = 0;
    }

    public static final Comparator<ContentListItem> contentAlphebeticalComparator
        = new Comparator<ContentListItem>() {
        @Override
        public int compare(final ContentListItem lhs, final ContentListItem rhs) {
            final ContentItem rhi = rhs.contentItem;
            final ContentItem lhi = lhs.contentItem;
            final int cmpResult = lhi.getFilePath().compareToIgnoreCase(rhi.getFilePath());
            if (cmpResult == 0) {
                return lhi.getFilePath().compareTo(rhi.getFilePath());
            }
            return cmpResult;
        }
    };

    public long getBytesTransferred() {
        return bytesTransferred;
    }

    public void setBytesTransferred(long bytesTransferred) {
        this.bytesTransferred = bytesTransferred;
    }

    public ContentItem getContentItem() {
        return contentItem;
    }

    public void setContentItem(final ContentItem contentItem) {
        this.contentItem = contentItem;
    }
}
