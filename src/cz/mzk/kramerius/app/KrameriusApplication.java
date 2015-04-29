package cz.mzk.kramerius.app;

import java.net.HttpURLConnection;

import javax.net.ssl.HttpsURLConnection;

import android.app.Application;
import android.content.Context;
import android.util.Log;
import cz.mzk.androidzoomifyviewer.viewer.TiledImageView;
import cz.mzk.kramerius.app.ssl.SSLSocketFactoryProvider;
import cz.mzk.kramerius.app.viewer.VolleyRequestManager;

public class KrameriusApplication extends Application {

	private static final String TAG = KrameriusApplication.class.getSimpleName();
	
	@Override
	public void onCreate() {
		super.onCreate();
		setupHttpUrlConnection(this);
		TiledImageView.initialize(this);
		VolleyRequestManager.initialize(this);
	}
	
	private void setupHttpUrlConnection(Context context) {
		// must handle redirect myself
		// because some things don't allways work properly. For instance http://something -> https://something.
		HttpURLConnection.setFollowRedirects(false);
		HttpsURLConnection.setFollowRedirects(false);
		try {
			HttpsURLConnection.setDefaultSSLSocketFactory(SSLSocketFactoryProvider.instanceOf(context).getSslSocketFactory());
		} catch (Exception e) {
			Log.e(TAG, "error initializing SSL Socket factory", e);
		}
	}
}