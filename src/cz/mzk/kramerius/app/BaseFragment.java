package cz.mzk.kramerius.app;

import android.app.Fragment;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

public abstract class BaseFragment extends Fragment {

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
