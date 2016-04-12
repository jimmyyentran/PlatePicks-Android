package com.amazonaws.mobile.downloader.service;

import java.lang.ref.WeakReference;
import java.util.concurrent.Callable;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.util.Log;

import com.amazonaws.mobile.downloader.query.DownloadState;
import com.amazonaws.mobile.downloader.query.DownloadQueueProvider;

/**
 * A class to represent the task of initializing the queue from the content provider.
 */
/* package */ final class QueueReaderTask implements Callable<Integer> {

    /** Our logger, for informational and error messages. */
    private static final String LOG_TAG = QueueReaderTask.class.getSimpleName();

    /** Our parent downloader. */
    private final WeakReference<Downloader> parent;

    /** The context to use. */
    private final Context context;

    /**
     * Create a new instance.
     * 
     * @param downloader the parent Downloader that created this task.
     * @param context the android context.
     */
    /* package */QueueReaderTask(final Downloader downloader,
        final Context context) {
        this.parent = new WeakReference<Downloader>(downloader);
        this.context = context;
    }

    /**
     * Perform the task.
     * 
     * @return the number of rows that were read from the content provider.
     */
    @Override
    public Integer call() {
        Log.d(LOG_TAG, "initializing the download queue.");
        final ContentResolver resolver = context.getContentResolver();

        // get all the rows that aren't complete or failed.
        final Cursor rows = resolver.query(DownloadQueueProvider.getDownloadContentUri(context),
            new String[] {
                DownloadQueueProvider.COLUMN_DOWNLOAD_ID,
                DownloadQueueProvider.COLUMN_DOWNLOAD_STATUS,
                DownloadQueueProvider.COLUMN_DOWNLOAD_USER_FLAGS
            },
            DownloadQueueProvider.COLUMN_DOWNLOAD_STATUS + " not in (?, ?)",
            new String[] {
                DownloadState.COMPLETE
                    .toString(),
                DownloadState.FAILED
                    .toString(),
            },
            null);

        // Iterate through rows, adding them as download tasks.
        int count = 0;
        try {
            if (rows.moveToFirst()) {
                do {
                    Log.i(LOG_TAG, "Processing a row!");
                    final Downloader parentObj = parent.get();
                    if (null != parentObj) {
                        // If a request is paused by user request, it can only be restarted by user request.
                        // If it got paused for some other reason, it can be re-queued.
                        final int flags = rows.getInt(2);
                        if (DownloadState.PAUSED.toString().equals(rows.getString(1))
                            && DownloadFlags.isUserRequestFlagSet(flags)) {
                            continue;
                        }

                        // Add the qualifying row.
                        parentObj.addDownloadTask(rows.getLong(0));
                    }
                    ++count;
                    Log.i(LOG_TAG, "Done processing a row!");
                } while (rows.moveToNext());
            }
        } finally {
            rows.close();
        }
        Log.i(LOG_TAG, count + "rows read.");

        // We're no longer initializing, so if we're still empty, it's okay to finish the service.
        final Downloader parentObj = parent.get();
        if (null != parentObj) {
            parentObj.doneInitializing();
        }

        return count;
    }
}
