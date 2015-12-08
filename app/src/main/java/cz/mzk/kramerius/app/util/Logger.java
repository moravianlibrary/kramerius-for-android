package cz.mzk.kramerius.app.util;

import android.util.Log;

/**
 * Created by Jan Rychtar on 8.12.15.
 */
public class Logger {

    public static void debug(String tag, String message) {
        if(VersionUtils.Debuggable()) {
            Log.d(tag, message);
        }
    }
}
