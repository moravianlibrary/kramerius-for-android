package cz.mzk.kramerius.app;

import android.app.Application;
import cz.mzk.androidzoomifyviewer.CacheManager;
import cz.mzk.kramerius.app.viewer.VolleyRequestManager;

public class KrameriusApplication extends Application {

	@Override
	public void onCreate() {
		super.onCreate();
		CacheManager.initialize(this, false);//AppConfig.DEV_MODE && AppConfig.DEV_MODE_CLEAR_CACHE_ON_STARTUP);
		VolleyRequestManager.initialize(this);
	}
}