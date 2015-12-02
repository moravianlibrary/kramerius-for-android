package cz.mzk.kramerius.app.util;

import android.content.Context;
import android.preference.PreferenceManager;

import cz.mzk.kramerius.app.R;
import cz.mzk.kramerius.app.api.K5Constants;

public class PrefUtils {

    public static final String FISRT_VIEWER_VISIT = "p_first_viewer_visit";

    public static final String getPolicy(Context context) {
        String policy = isPublicOnly(context) ? K5Constants.POLICY_PUBLIC : null;
        return policy;
    }

    public static final boolean isPublicOnly(Context context) {
        return getBooleanValue(context, R.string.pref_public_only_key, R.string.pref_public_only_default);
    }

    public static final boolean useBookmarks(Context context) {
        return getBooleanValue(context, R.string.pref_bookmark_key, R.string.pref_bookmark_default);
    }

    public static final boolean useCardAnimation(Context context) {
        return getBooleanValue(context, R.string.pref_card_animation_key, R.string.pref_card_animation_default);
    }

    private static final boolean getBooleanValue(Context context, int key, int defValue) {
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(context.getString(key),
                Boolean.parseBoolean(context.getString(defValue)));
    }

    public static final boolean isFirstViewerVisit(Context context) {
        boolean first = PreferenceManager.getDefaultSharedPreferences(context).getBoolean(FISRT_VIEWER_VISIT, true);
        if (first) {
            PreferenceManager.getDefaultSharedPreferences(context).edit().putBoolean(FISRT_VIEWER_VISIT, false)
                    .commit();
            return true;
        }
        return false;
    }

}
