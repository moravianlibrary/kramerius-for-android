package cz.mzk.kramerius.app;

import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;

import com.readystatesoftware.systembartint.SystemBarTintManager;

public class BaseActivity extends ActionBarActivity {

	public static final String EXTRA_PID = "extra_pid";
	public static final String EXTRA_TITLE = "extra_title";

	public static final int PHONE = 0;
	public static final int TABLET = 1;

	private int mDevice;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Configuration config = getResources().getConfiguration();
		if (config.smallestScreenWidthDp >= 720) {
			mDevice = TABLET;
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
		} else {
			mDevice = PHONE;
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		}
		SystemBarTintManager tintManager = new SystemBarTintManager(this);
		tintManager.setStatusBarTintEnabled(true);
		tintManager.setStatusBarTintResource(R.color.status_bar);
	}

	public int getDevice() {
		return mDevice;
	}

	public boolean isPhone() {
		return mDevice == PHONE;
	}

	public boolean isTablet() {
		return mDevice == TABLET;
	}

}
