package cz.mzk.kramerius.app;

import android.app.Application;
import cz.mzk.androidzoomifyviewer.viewer.TiledImageView;
import cz.mzk.kramerius.app.viewer.VolleyRequestManager;

public class KrameriusApplication extends Application {

	@Override
	public void onCreate() {
		super.onCreate();
		TiledImageView.initialize(this);
		VolleyRequestManager.initialize(this);
	}
}