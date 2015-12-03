package cz.mzk.kramerius.app;

import android.app.Application;
import android.content.Context;
import android.net.http.HttpResponseCache;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;

import javax.net.ssl.HttpsURLConnection;

import cz.mzk.androidzoomifyviewer.viewer.TiledImageView;
import cz.mzk.kramerius.app.ssl.SSLSocketFactoryProvider;
import cz.mzk.kramerius.app.viewer.VolleyRequestManager;

public class KrameriusApplication extends Application {

    private static final String TAG = KrameriusApplication.class.getSimpleName();

    @Override
    public void onCreate() {
        super.onCreate();
        initHttpCache();
        setupHttpUrlConnection(this);
        TiledImageView.initialize(this);
        VolleyRequestManager.initialize(this);
    }



    /**
     * This is used by plain HttpURLConnection (not Volley)
     */
    private void initHttpCache() {
        try {
            File httpCacheDir = new File(getCacheDir(), "http");
            long httpCacheSize = 10 * 1024 * 1024; // 10 MB
            HttpResponseCache.install(httpCacheDir, httpCacheSize);
            Log.i(TAG, "HTTP response cache installed into:" + httpCacheDir.getAbsolutePath());
        } catch (IOException e) {
            Log.w(TAG, "HTTP response cache installation failed", e);
        }
    }

    private void setupHttpUrlConnection(Context context) {
        // must handle redirect myself
        // because some things don't allways work properly. For instance http://something -> https://something.
        HttpURLConnection.setFollowRedirects(false);
        HttpsURLConnection.setFollowRedirects(false);
        try {
            HttpsURLConnection.setDefaultSSLSocketFactory(SSLSocketFactoryProvider.instanceOf(context)
                    .getSslSocketFactory());
        } catch (Exception e) {
            Log.e(TAG, "error initializing SSL Socket factory", e);
        }
    }
}