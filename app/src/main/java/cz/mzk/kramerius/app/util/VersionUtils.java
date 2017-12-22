package cz.mzk.kramerius.app.util;

import android.content.Context;
import android.content.pm.PackageManager.NameNotFoundException;

import cz.mzk.kramerius.app.R;

public class VersionUtils {

    private static final int DEV = 0;
    private static final int ALPHA = 1;
    private static final int BETA = 2;

    private static final String TYPE_DEV = "DEV";
    private static final String TYPE_ALPHA = "ALPHA";
    private static final String TYPE_BETA = "BETA";

    private static final int VERSION = BETA;


    public static boolean Debuggable() {
        return VERSION == DEV;
    }

    public static String getVersionType() {
        switch (VERSION) {
            case DEV:
                return TYPE_DEV;
            case ALPHA:
                return TYPE_ALPHA;
            case BETA:
                return TYPE_BETA;
            default:
                return TYPE_DEV;
        }
    }

    public static String getVersionName(Context context) {
        try {
            return context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName;
        } catch (NameNotFoundException e) {
            return context.getString(R.string.help_about_app_version_unknown);
        }
    }

    public static int getVersionCode(Context context) {
        try {
            return context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionCode;
        } catch (NameNotFoundException e) {
            return -1;
        }
    }

    public static String getVersion(Context context) {
        if (VERSION == BETA) {
            return getVersionName(context);
        }
        return getVersionName(context) + "/" + getVersionType();
    }


}
