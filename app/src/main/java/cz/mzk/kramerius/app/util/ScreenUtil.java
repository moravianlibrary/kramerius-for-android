package cz.mzk.kramerius.app.util;

import android.app.Activity;
import android.os.Build;
import android.view.View;

public class ScreenUtil {

//	public static void setInsets(Activity context, View view, boolean withActionbar) {
//		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
//			return;
//		}
//		SystemBarTintManager tintManager = new SystemBarTintManager(context);
//		SystemBarTintManager.SystemBarConfig config = tintManager.getConfig();
//		view.setPadding(0, config.getPixelInsetTop(withActionbar), config.getPixelInsetRight(), config.getPixelInsetBottom());
//	}
//	
//	
//	
//	public static void setInsets(Activity context, View view) {
//		setInsets(context, view, true);
//	}

    public static void fullscreenInsets(Activity context, View view) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
            return;
        }
        view.setPadding(0, 0, 0, 0);
    }

}
