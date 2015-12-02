package cz.mzk.kramerius.app.service;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.LruCache;

import com.android.volley.Response;

import java.util.logging.Logger;

import cz.mzk.kramerius.app.R;
import cz.mzk.kramerius.app.api.K5Api;
import cz.mzk.kramerius.app.model.Track;
import cz.mzk.kramerius.app.util.BitmapUtil;
import cz.mzk.kramerius.app.viewer.RedirectingImageRequest;
import cz.mzk.kramerius.app.viewer.VolleyRequestManager;

public class NotificationThumbnailManager {

    public static interface DownloadHandler {
        /**
         * Must run on primary thread
         */
        public void onDownloaded();
    }

    private static final Logger LOGGER = Logger.getLogger(NotificationThumbnailManager.class.getSimpleName());

    private final Context mContext;
    private final LruCache<String, Bitmap> mBmpCache = new LruCache<String, Bitmap>(3);
    private final int mThumbMaxWidthPx;
    private final int mThumbMaxHeightPx;
    private Bitmap mDefaultThumb;
    private boolean mStopped = false;

    public NotificationThumbnailManager(Context context) {
        LOGGER.info("initializing");
        this.mContext = context;
        // TODO: use size for expanded_large_icon in MediaStyle when it is available somehow
        // see http://stackoverflow.com/questions/27984766/size-of-mediastyle-largeicon
        // Resources res = context.getResources();
        // mThumbMaxWidthPx = (int) res.getDimensionPixelSize(android.R.dimen.notification_large_icon_width);
        // mThumbMaxHeightPx = (int) res.getDimensionPixelSize(android.R.dimen.notification_large_icon_height);
        mThumbMaxWidthPx = 128;
        mThumbMaxHeightPx = 128;
    }

    public Bitmap getBitmap(Context context, Track track, DownloadHandler handler) {
        LOGGER.fine("getBitmap");
        String thumbUrl = K5Api.getThumbnailPath(context, track.getSoundRecordingPid());
        if (thumbUrl != null && !thumbUrl.isEmpty()) {
            Bitmap bitmap = mBmpCache.get(thumbUrl);
            if (bitmap != null) {
                return bitmap;
            } else {
                enqueThumbDownload(thumbUrl, handler);
                return getDefaultThumb(context);
            }
        } else {
            LOGGER.warning("no url, ignoring");
            return getDefaultThumb(context);
        }
    }

    private void enqueThumbDownload(final String thumbUrl, final DownloadHandler handler) {
        VolleyRequestManager.addToRequestQueue(new RedirectingImageRequest(thumbUrl, new Response.Listener<Bitmap>() {
            @Override
            public void onResponse(Bitmap fetched) {
                if (fetched != null) {
                    Bitmap scaled = BitmapUtil.scaleBitmap(fetched, mThumbMaxWidthPx, mThumbMaxHeightPx);
                    if (scaled != null) {
                        // Checked because handler creates pending intent to show notification. Without this,
                        // if onResponse is called after service was stopped, notification would be shown again.
                        if (!mStopped) {
                            mBmpCache.put(thumbUrl, scaled);
                            handler.onDownloaded();
                        }
                    }
                }
            }
        }, null));
    }

    private Bitmap getDefaultThumb(Context context) {
        if (mDefaultThumb == null) {
            mDefaultThumb = BitmapUtil.decodeAndScaleBitmap(context.getResources(), R.drawable.track_thumb_default,
                    mThumbMaxWidthPx, mThumbMaxHeightPx);
        }
        return mDefaultThumb;
    }

    public void stop() {
        mStopped = true;
    }

}
