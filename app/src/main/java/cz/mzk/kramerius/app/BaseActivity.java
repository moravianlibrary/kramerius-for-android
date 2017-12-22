package cz.mzk.kramerius.app;

import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import com.readystatesoftware.systembartint.SystemBarTintManager;

public class BaseActivity extends ActionBarActivity {

    public static final String EXTRA_PID = "extra_pid";
    public static final String EXTRA_TITLE = "extra_title";
    public static final String EXTRA_DOMAIN = "extra_domain";

    public static final int PHONE = 0;
    public static final int TABLET = 1;

    private int mDevice;

    private Animation mLoaderAnimation;
    private View mLoaderView;
    private View mLoaderContainer;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mLoaderAnimation = AnimationUtils.loadAnimation(this, R.anim.rotation);
        mLoaderAnimation.setRepeatCount(Animation.INFINITE);
        Configuration config = getResources().getConfiguration();
        if (config.smallestScreenWidthDp >= 720) {
            mDevice = TABLET;
            //setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
        } else {
            mDevice = PHONE;
            //setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
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

    public boolean isLandscape() {
        return getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE;
    }

    public boolean isPortrait() {
        return getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT;
    }

    protected void stopLoaderAnimation() {
        if (mLoaderContainer == null || mLoaderView == null) {
            return;
        }
        mLoaderView.clearAnimation();
        mLoaderContainer.setVisibility(View.GONE);
    }

    protected void startLoaderAnimation() {
        if (mLoaderContainer == null) {
            return;
        }
        mLoaderContainer.setVisibility(View.VISIBLE);
        mLoaderView.startAnimation(mLoaderAnimation);
    }

    protected void inflateLoader(ViewGroup root) {
        mLoaderContainer = getLayoutInflater().inflate(R.layout.view_loader, root, false);
        mLoaderView = mLoaderContainer.findViewById(R.id.loader);
        root.addView(mLoaderContainer);
    }


}
