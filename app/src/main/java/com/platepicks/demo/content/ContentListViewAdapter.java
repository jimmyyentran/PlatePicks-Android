package com.platepicks.demo.content;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.amazonaws.mobile.content.ContentDownloadPolicy;
import com.amazonaws.mobile.content.ContentItem;
import com.amazonaws.mobile.content.ContentManager;
import com.amazonaws.mobile.content.ContentProgressListener;
import com.amazonaws.mobile.content.ContentRemovedListener;
import com.amazonaws.mobile.content.ContentState;
import com.amazonaws.mobile.util.StringFormatUtils;
import com.platepicks.R;

import java.io.File;
import java.util.HashMap;

public class ContentListViewAdapter extends ArrayAdapter<ContentListItem>
    implements ContentProgressListener, ContentRemovedListener {
    private final String LOG_TAG = ContentListViewAdapter.class.getSimpleName();

    private final LayoutInflater layoutInflater;
    private final String folderText;

    private final ContentManager contentManager;
    private final ContentListPathProvider pathProvider;
    private final ContentListCacheObserver cacheObserver;

    /** Map from file name to content list item. */
    HashMap<String, ContentListItem> contentListItemMap = new HashMap<>();

    public interface ContentListPathProvider {
        String getCurrentPath();
    }

    public interface ContentListCacheObserver {
        void onCacheChanged();
    }


    public ContentListViewAdapter(final Context context,
                           final ContentManager contentManager,
                           final ContentListPathProvider pathProvider,
                           final ContentListCacheObserver cacheObserver,
                           final int resource) {
        super(context, resource);
        layoutInflater = LayoutInflater.from(context);
        folderText = getContext().getString(R.string.content_folder_text);
        this.contentManager = contentManager;
        this.pathProvider = pathProvider;
        this.cacheObserver = cacheObserver;
    }

    @Override
    public void add(ContentListItem item) {
        if (item.getContentItem() != null) {
            contentListItemMap.put(item.getContentItem().getFilePath(), item);
        }
        super.add(item);
    }

    @Override
    public void remove(ContentListItem item) {
        contentListItemMap.remove(item.getContentItem().getFilePath());
        super.remove(item);
    }

    @Override
    public void clear() {
        contentListItemMap.clear();
        super.clear();
    }

    @Override
    public View getView(final int position, final View convertView, final ViewGroup parent) {
        return generateContentItem(layoutInflater, getItem(position), convertView);
    }

    /**
     * This exists only to optimize performance for finding the views in a list view item.
     */
    private class ViewHolder {
        TextView fileNameText;
        TextView fileSizeText;
        ImageView downloadImage;
        TextView downloadPercentText;
        ImageView favoriteImage;
    }

    private View generateContentItem(final LayoutInflater inflater, final ContentListItem listItem,
                                     final View convertView) {
        final ContentItem contentItem = listItem.getContentItem();
        final View itemView;
        final TextView fileNameText;
        final TextView fileSizeText;
        final ImageView downloadImage;
        final TextView downloadPercentText;
        final ViewHolder holder;
        final ImageView favoriteImage;
        if (convertView != null) {
            itemView = convertView;
            holder = (ViewHolder) itemView.getTag();
            fileNameText = holder.fileNameText;
            fileSizeText = holder.fileSizeText;
            downloadImage = holder.downloadImage;
            downloadPercentText = holder.downloadPercentText;
            favoriteImage = holder.favoriteImage;
        } else {
            itemView = inflater.inflate(
                R.layout.demo_content_list_item, null);
            holder = new ViewHolder();
            holder.fileNameText = fileNameText = (TextView) itemView.findViewById(
                R.id.content_delivery_file_name);
            holder.fileSizeText = fileSizeText = (TextView) itemView.findViewById(
                R.id.content_delivery_file_size_text);
            holder.downloadImage = downloadImage = (ImageView) itemView
                .findViewById(R.id.content_delivery_file_download_image);
            holder.downloadPercentText = downloadPercentText = (TextView) itemView.findViewById(
                R.id.content_delivery_download_percentage);
            holder.favoriteImage = favoriteImage = (ImageView) itemView.findViewById(
                R.id.content_delivery_favorite_image);
            itemView.setTag(holder);
        }

        final String displayName = contentItem.getFilePath()
            .substring(pathProvider.getCurrentPath()
                .length());
        fileNameText.setText(displayName.isEmpty() ? ".." : displayName);
        fileNameText.setTextColor(
            ContentState.REMOTE_DIRECTORY.equals(contentItem.getContentState()) ? Color.BLUE : Color.BLACK);

        ContentState contentState = contentItem.getContentState();
        if (ContentState.REMOTE_DIRECTORY.equals(contentState)) {
            fileSizeText.setText(folderText);
            downloadImage.setVisibility(View.INVISIBLE);
            downloadPercentText.setWidth(0);
            favoriteImage.setVisibility(View.INVISIBLE);
            return itemView;
        } else {
            fileSizeText.setText(StringFormatUtils.getBytesString(contentItem.getSize(), false));
        }

        if (ContentState.isTransferring(contentState) && listItem.getBytesTransferred() == 0) {
            // Override the transferState to waiting if we haven't received a progress update yet
            // for this item.  At the next progress update it will reflect the appropriate
            // percentage.
            contentState = ContentState.TRANSFER_WAITING;
        }

        switch (contentState) {
            case REMOTE:
                downloadImage.setVisibility(View.INVISIBLE);
                break;
            case TRANSFER_WAITING:
            case CACHED_NEW_VERSION_TRANSFER_WAITING:
                downloadImage.setImageResource(R.mipmap.icon_delay);
                break;
            case TRANSFERRING:
            case CACHED_TRANSFERRING_NEW_VERSION:
                downloadPercentText.setWidth(favoriteImage.getLayoutParams().width);
                downloadPercentText.setText(
                    String.format("%.0f%%",
                        100.0 * listItem.getBytesTransferred() / contentItem.getSize()));
                downloadImage.setVisibility(View.INVISIBLE);
                downloadImage.getLayoutParams().width = 0;
                downloadImage.requestLayout();
                break;
            case CACHED:
                // Show the item as available by displaying the check icon.
                downloadImage.setImageResource(R.mipmap.icon_check);
                break;
            case CACHED_WITH_NEWER_VERSION_AVAILABLE:
                // Show the check mark with the download icon on top.
                downloadImage.setImageResource(R.mipmap.icon_check_dated);
                break;
        }

        if (!ContentState.isTransferring(contentItem.getContentState())) {
            downloadPercentText.setText("");
            downloadPercentText.setWidth(0);
            if (contentState != ContentState.REMOTE) {
                downloadImage.setVisibility(View.VISIBLE);
                downloadImage.getLayoutParams().width = favoriteImage.getLayoutParams().width;
                downloadImage.requestLayout();
            }
        }

        final String contentName = contentItem.getFilePath();
        if (contentManager.isContentPinned(contentName)) {
            favoriteImage.setImageResource(R.mipmap.icon_star);
            favoriteImage.setVisibility(View.VISIBLE);;
        } else {
            favoriteImage.setVisibility(View.INVISIBLE);;
        }

        return itemView;
    }

    @Override
    public void onProgressUpdate(final String filePath, final boolean isWaiting,
                                 final long bytesCurrent, final long bytesTotal) {
        // This is always called on the main thread.
        final ContentListItem item = contentListItemMap.get(filePath);

        if (item == null) {
            Log.w(LOG_TAG, String.format(
                "Warning progress update for item '%s' is not in the content list.", filePath));
            return;
        }

        if (isWaiting) {
            item.getContentItem().setContentState(ContentState.TRANSFER_WAITING);
        } else {
            if (!ContentState.isTransferring(item.getContentItem().getContentState())) {
                item.getContentItem().setContentState(ContentState.TRANSFERRING);
            }
            item.setBytesTransferred(bytesCurrent);
        }

        notifyDataSetChanged();
    }

    @Override
    public void onSuccess(final ContentItem contentItem) {
        final ContentListItem item = contentListItemMap.get(contentItem.getFilePath());
        if (item == null) {
            Log.w(LOG_TAG, String.format("Warning: item '%s' completed," +
                " but is not in the content list.", contentItem.getFilePath()));
            return;
        }
        item.setContentItem(contentItem);

        // sort calls notifyDataSetChanged()
        sort(ContentListItem.contentAlphebeticalComparator);
        cacheObserver.onCacheChanged();
    }


    @Override
    public void onError(final String filePath, final Exception ex) {
        final Context context = getContext();
        final AlertDialog.Builder errorDialogBuilder = new AlertDialog.Builder(context);
        errorDialogBuilder.setTitle(context.getString(R.string.content_transfer_failure_text));
        errorDialogBuilder.setMessage(ex.getMessage());
        errorDialogBuilder.setNegativeButton(
            context.getString(R.string.content_dialog_ok), null);
        errorDialogBuilder.show();

        if (filePath != null) {
            final ContentListItem item = contentListItemMap.get(filePath);
            if (item == null) {
                Log.w(LOG_TAG, String.format(
                    "Warning file removed for item '%s' is not in the content list.", filePath));
                return;
            }
            item.getContentItem().setContentState(ContentState.REMOTE);

            notifyDataSetChanged();
        }
    }

    private String getRelativeFilePath(final String absolutePath) {
        final String localPath = contentManager.getLocalContentPath();

        if (absolutePath.startsWith(localPath)) {
            return absolutePath.substring(localPath.length() + 1);
        }
        return null;
    }

    @Override
    public void onFileRemoved(final File file) {
        final String filePath = getRelativeFilePath(file.getAbsolutePath());

        cacheObserver.onCacheChanged();

        final ContentListItem item = contentListItemMap.get(filePath);
        if (item == null) {
            Log.w(LOG_TAG, String.format(
                "Warning file removed for item '%s' is not in the content list.", filePath));
            return;
        }
        // Content state needs to be reverted to remote.
        item.getContentItem().setContentState(ContentState.REMOTE);

        notifyDataSetChanged();

        // get the item state from the server.
        contentManager.getContent(filePath, 0, ContentDownloadPolicy.DOWNLOAD_METADATA_IF_NOT_CACHED, false,
            new ContentProgressListener() {
                @Override
                public void onSuccess(final ContentItem contentItem) {
                    item.setContentItem(contentItem);
                    ContentListViewAdapter.this.sort(ContentListItem.contentAlphebeticalComparator);
                }

                @Override
                public void onProgressUpdate(String fileName, boolean isWaiting,
                                             long bytesCurrent, long bytesTotal) {
                    // Nothing to do here.
                }

                @Override
                public void onError(String fileName, Exception ex) {
                    // Remove the item since we can't determine if it exists anymore.
                    ContentListViewAdapter.this.remove(item);
                }
            });
    }

    @Override
    public void onRemoveError(File file) {
        final AlertDialog.Builder errorDialogBuilder = new AlertDialog.Builder(getContext());
        errorDialogBuilder.setTitle(getContext().getString(R.string.content_removal_error_text));
        errorDialogBuilder.setMessage(String.format("Can't remove file '%s'.", file.getName()));
        errorDialogBuilder.setNegativeButton(
            getContext().getString(R.string.content_dialog_ok), null);
        errorDialogBuilder.show();
    }
}
