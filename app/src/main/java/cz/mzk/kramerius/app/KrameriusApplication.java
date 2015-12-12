package cz.mzk.kramerius.app;

import android.app.Application;
import android.content.Context;
import android.database.Cursor;
import android.net.http.HttpResponseCache;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;

import cz.mzk.kramerius.app.data.KrameriusContract;
import cz.mzk.kramerius.app.model.Domain;
import cz.mzk.kramerius.app.ssl.SSLSocketFactoryProvider;
import cz.mzk.kramerius.app.util.Logger;
import cz.mzk.kramerius.app.viewer.VolleyRequestManager;

public class KrameriusApplication extends Application {

    private static final String TAG = KrameriusApplication.class.getSimpleName();

    private static KrameriusApplication INSTANCE;
    private List<Domain> mLibraries;

    @Override
    public void onCreate() {
        super.onCreate();
        INSTANCE = this;
        initHttpCache();
        setupHttpUrlConnection(this);
        VolleyRequestManager.initialize(this);
        clearAppCache();
    }

    public static KrameriusApplication getInstance() {
        return INSTANCE;
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

    private void clearAppCache() {
        getContentResolver().delete(KrameriusContract.CacheEntry.CONTENT_URI, null, null);
    }

    public List<Domain> getLibraries() {
        if(mLibraries == null) {
            reloadLibraries();
        }
        return mLibraries;
    }

    public void reloadLibraries() {
        if(mLibraries == null) {
            mLibraries = new ArrayList<>();
        }
        mLibraries.clear();

        String[] projection = new String[] {
                KrameriusContract.LibraryEntry.COLUMN_NAME,
                KrameriusContract.LibraryEntry.COLUMN_PROTOCOL,
                KrameriusContract.LibraryEntry.COLUMN_DOMAIN,
                KrameriusContract.LibraryEntry.COLUMN_CODE,
                KrameriusContract.LibraryEntry.COLUMN_LOCKED
        };
        String selection = null;
        String[] selectionArgs = null;
        Cursor c = getContentResolver().query(KrameriusContract.LibraryEntry.CONTENT_URI, projection, selection, selectionArgs, null);
        while (c.moveToNext()) {
            Domain d = new Domain();
            d.setTitle(c.getString(0));
            d.setProtocol(c.getString(1));
            d.setDomain(c.getString(2));

            int locked = c.getInt(4);
            d.setUnlocked(locked == 0);

            int logo = R.drawable.ic_empty_36dp;
            if(locked == 0) {
                String logoName = "logo_" + c.getString(3);
                Logger.debug(TAG, "logoName: " + logoName);
                logo = getResources().getIdentifier(logoName, "drawable", getPackageName());
            }
            Logger.debug(TAG, "logo: " + logo);
            d.setLogo(logo);
            mLibraries.add(d);
        }
        c.close();
    }


}