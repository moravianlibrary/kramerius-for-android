package cz.mzk.kramerius.app.api;

import android.util.Log;
import cz.mzk.kramerius.app.util.VersionUtils;

public class K5ConnectorFactory {

	private static final String LOG_TAG = K5ConnectorFactory.class.getName();
	public static final boolean USE_LEGACY_CONNECTOR = false;

	private static K5Connector INSTANCE;

	public static K5Connector getConnector() {
		if (INSTANCE == null) {
			if (USE_LEGACY_CONNECTOR) {
				if (VersionUtils.Debuggable()) {
					Log.d(LOG_TAG, "initializing  " + K5ConnectorImplAndroidHttpClient.class.getSimpleName());
				}
				INSTANCE = new K5ConnectorImplAndroidHttpClient();
			} else {
				if (VersionUtils.Debuggable()) {
					Log.d(LOG_TAG, "initializing  " + K5ConnectorImplHttpUrlConnection.class.getSimpleName());
				}
				INSTANCE = new K5ConnectorImplHttpUrlConnection();
			}
		}
		return INSTANCE;
	}

}
