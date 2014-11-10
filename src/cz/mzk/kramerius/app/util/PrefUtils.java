package cz.mzk.kramerius.app.util;

import android.content.Context;
import android.preference.PreferenceManager;
import cz.mzk.kramerius.app.R;
import cz.mzk.kramerius.app.api.K5Constants;

public class PrefUtils {

	public static final String getPolicy(Context context) {
		
		String policy = isPublicOnly(context) ? K5Constants.POLICY_PUBLIC : null;
		return policy;
	}
	
	public static final boolean isPublicOnly(Context context) {
		return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(
				context.getString(R.string.pref_public_only_key),
				Boolean.parseBoolean(context.getString(R.string.pref_public_only_default)));
	}	

}
