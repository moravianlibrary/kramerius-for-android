package cz.mzk.kramerius.app;

import android.app.Fragment;
import android.content.Context;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import cz.mzk.kramerius.app.util.MessageUtils;

public abstract class BaseFragment extends Fragment {

	private static final String LOG_TAG = BaseFragment.class.getName();

	private Animation mLoaderAnimation;
	private View mLoaderView;
	private View mLoaderContainer;
	private int mDevice;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mLoaderAnimation = AnimationUtils.loadAnimation(getActivity(), R.anim.rotation);
		mLoaderAnimation.setRepeatCount(Animation.INFINITE);
		Configuration config = getResources().getConfiguration();
		if (config.smallestScreenWidthDp >= 720) {
			mDevice = BaseActivity.TABLET;
		} else {
			mDevice = BaseActivity.PHONE;
		}
	}

	protected void inflateLoader(ViewGroup root, LayoutInflater inflater) {
		mLoaderContainer = inflater.inflate(R.layout.view_loader, root, false);
		mLoaderView = mLoaderContainer.findViewById(R.id.loader);
		root.addView(mLoaderContainer);
	}

	protected void showWarningMessage(String message, String buttonText, final onWarningButtonClickedListener callback,
			boolean hideAfterClick) {
		final ViewGroup vg = (ViewGroup) getView();
		Context c = getActivity();
		if (vg == null || c == null) {
			return;
		}
		MessageUtils.inflateMessage(c, vg, message, buttonText, callback, hideAfterClick);
	}

	protected void showWarningMessage(String message, String buttonText, final onWarningButtonClickedListener callback) {
		showWarningMessage(message, buttonText, callback, true);
	}

	protected void showWarningMessage(int message, int buttonText, final onWarningButtonClickedListener callback) {
		showWarningMessage(getString(message), getString(buttonText), callback, true);
	}
	
	public interface onWarningButtonClickedListener {
		public void onWarningButtonClicked();
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

	protected void stopLoaderAnimation(View view) {
		view.clearAnimation();
		view.setVisibility(View.GONE);
	}

	protected void startLoaderAnimation(View view) {
		view.setVisibility(View.VISIBLE);
		view.startAnimation(mLoaderAnimation);
	}

	public boolean isPhone() {
		return mDevice == BaseActivity.PHONE;
	}

	public boolean isTablet() {
		return mDevice == BaseActivity.TABLET;
	}
	
	
	protected ActionBar getSupportActionBar() {
		if(getActivity() == null) {
			return null;
		}
		return ((ActionBarActivity) getActivity()).getSupportActionBar();
	}

}
