package cz.mzk.kramerius.app;

import android.app.Fragment;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.TextView;

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

	protected void showWarningMessage(String message, String buttonText, final onWarningButtonClickedListener callback) {
		Log.d(LOG_TAG, "showWarningMessage:" + message);
		final ViewGroup vg = (ViewGroup) getView();
		Context c = getActivity();
		Log.d(LOG_TAG, "showWarningMessage: vg:" + vg + ", c:" + c);
		if (vg == null || c == null) {
			return;
		}
		LayoutInflater inflater = LayoutInflater.from(c);
		final View view = inflater.inflate(R.layout.view_warning, vg, false);
		TextView text = (TextView) view.findViewById(R.id.warning_message);
		Button button = (Button) view.findViewById(R.id.warning_button);
		text.setText(message);
		if (buttonText == null || callback == null) {
			button.setVisibility(View.GONE);
		} else {
			button.setText(buttonText);
			button.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					vg.removeView(view);
					callback.onWarningButtonClicked();
				}
			});
		}
		Log.d(LOG_TAG, "showWarningMessage:end");
		vg.addView(view);
	}

	public interface onWarningButtonClickedListener {
		public void onWarningButtonClicked();
	}

	protected void stopLoaderAnimation() {
		if (mLoaderContainer == null) {
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

}
