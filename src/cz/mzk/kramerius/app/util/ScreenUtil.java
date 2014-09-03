package cz.mzk.kramerius.app.util;

import android.app.Activity;
import android.os.Build;
import android.view.View;

import com.readystatesoftware.systembartint.SystemBarTintManager;

public class ScreenUtil {

	public static void setInsets(Activity context, View view) {
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
			return;
		}
		SystemBarTintManager tintManager = new SystemBarTintManager(context);
		SystemBarTintManager.SystemBarConfig config = tintManager.getConfig();
		view.setPadding(0, config.getPixelInsetTop(true), config.getPixelInsetRight(), config.getPixelInsetBottom());
	}
}
