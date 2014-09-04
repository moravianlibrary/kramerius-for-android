package cz.mzk.kramerius.app.util;

import android.content.Context;

import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.Fields;
import com.google.analytics.tracking.android.MapBuilder;

import cz.mzk.kramerius.app.R;

public class Analytics {

	public static void sendEvent(Context context, String category, String action, String label, Long value) {
		if (context == null) {
			return;
		}
		EasyTracker easyTracker = EasyTracker.getInstance(context);
		easyTracker.send(MapBuilder.createEvent(category, action, label, value).build());
	}

	public static void sendEvent(Context context, String category, String action, String label) {
		sendEvent(context, category, action, label, null);
	}

	public static void sendEvent(Context context, String category, String action) {
		sendEvent(context, category, action, null, null);
	}
	
	public static void sendScreenView(Context context, String screenName) {
		if(context == null) {
			return;
		}
		EasyTracker tracker = EasyTracker.getInstance(context);
	    tracker.set(Fields.SCREEN_NAME, screenName);
	    tracker.send(MapBuilder.createAppView().build());		
	}
	
	public static void sendScreenView(Context context, int screenNameRes) {
		if(context == null) {
			return;
		}
		sendScreenView(context, context.getString(screenNameRes));
	}

}
