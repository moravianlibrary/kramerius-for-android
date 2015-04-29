package cz.mzk.kramerius.app.viewer;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;
import com.polites.android.GestureImageView;

import cz.mzk.androidzoomifyviewer.viewer.TiledImageView;
import cz.mzk.androidzoomifyviewer.viewer.TiledImageView.ImageInitializationHandler;
import cz.mzk.androidzoomifyviewer.viewer.TiledImageView.SingleTapListener;
import cz.mzk.androidzoomifyviewer.viewer.TiledImageView.ViewMode;
import cz.mzk.kramerius.app.R;
import cz.mzk.kramerius.app.util.VersionUtils;
import cz.mzk.kramerius.app.viewer.IPageViewerFragment.EventListener;

public class SinglePageViewerFragment extends Fragment implements OnTouchListener, ImageInitializationHandler,
		SingleTapListener {

	private static final String TAG = SinglePageViewerFragment.class.getSimpleName();

	private static final int IMG_FULL_HEIGHT = 1500;

	private String mDomain;
	private String mPid;
	private int mBackgroud;
	private View mContainer;
	private View mProgressView;
	private TiledImageView mTiledImageView;
	private ViewGroup mImageViewContainer;

	private GestureDetector mGestureDetector;
	private EventListener mEventListener;

	private ImageRequest mImageRequest;
	private ViewMode mViewMode;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mDomain = getArguments().getString("domain");
		mPid = getArguments().getString("pid");
		mBackgroud = getArguments().getInt("background");
		mViewMode = ViewMode.values()[getArguments().getInt("viewmode")];
	}

	public static SinglePageViewerFragment newInstance(String domain, String pid, int backgroud, ViewMode viewMode) {
		SinglePageViewerFragment fragment = new SinglePageViewerFragment();
		Bundle args = new Bundle();
		args.putString("domain", domain);
		args.putString("pid", pid);
		args.putInt("background", backgroud);
		args.putInt("viewmode", viewMode.ordinal());
		fragment.setArguments(args);
		return fragment;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_single_page_viewer, container, false);
		mContainer = view.findViewById(R.id.container);
		mContainer.setOnTouchListener(this);
		mProgressView = view.findViewById(R.id.progressView);
		mProgressView.setOnTouchListener(this);
		mTiledImageView = (TiledImageView) view.findViewById(R.id.tiledImageView);
		mTiledImageView.setImageInitializationHandler(this);
		mTiledImageView.setSingleTapListener(this);
		mImageViewContainer = (ViewGroup) view.findViewById(R.id.imageContainer);
		setViewMode(mViewMode);
		setBackgroundColor(mBackgroud);
		showPage();
		return view;
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		try {
			mEventListener = (EventListener) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString() + " must implement EventListener");
		}
	}

	public boolean isSwipeEnabled() {
		return mTiledImageView.getInitialScaleFactor() >= mTiledImageView.getTotalScaleFactor();
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		mGestureDetector = initGestureDetector(getActivity());
	}

	private GestureDetector initGestureDetector(Context context) {
		return new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {

			@Override
			public boolean onSingleTapConfirmed(MotionEvent e) {
				if (mEventListener != null) {
					mEventListener.onSingleTap(e.getX(), e.getY(), null);
				}
				return true;
			}
		});
	}

	@Override
	public void onStop() {
		super.onStop();
		if (mImageRequest != null) {
			mImageRequest.cancel();
		}
	}

	private void showPage() {
		if (mImageRequest != null) {
			mImageRequest.cancel();
		}
		hideViews();
		mProgressView.setVisibility(View.VISIBLE);
		String url = buildZoomifyBaseUrl(mPid);
		mTiledImageView.loadImage(url.toString());
	}

	private String buildZoomifyBaseUrl(String pid) {
		StringBuilder builder = new StringBuilder();
		builder.append("http://");
		builder.append(mDomain).append('/');
		builder.append("search/zoomify/");
		builder.append(pid).append('/');
		return builder.toString();
	}

	private void hideViews() {
		mProgressView.setVisibility(View.INVISIBLE);
		mTiledImageView.setVisibility(View.INVISIBLE);
		mImageViewContainer.setVisibility(View.INVISIBLE);
	}

	@Override
	public void onImagePropertiesProcessed() {
		hideViews();
		mTiledImageView.setVisibility(View.VISIBLE);
	}

	private void setBackgroundColor(int color) {
		mTiledImageView.setBackgroundColor(color);
		mImageViewContainer.setBackgroundColor(color);
		mProgressView.setBackgroundColor(color);
	}

	@Override
	public void onImagePropertiesUnhandableResponseCodeError(String imagePropertiesUrl, int responseCode) {
		if (VersionUtils.Debuggable()) {
			Log.d(TAG, "onImagePropertiesUnhandableResponseCodeError, code: " + responseCode);
		}
		hideViews();
		switch (responseCode) {
		case 403: // FORBIDDEN
			if (mEventListener != null) {
				mEventListener.onAccessDenied();
			}
			break;
		// TODO: remove this temporary hack
		// @see https://github.com/ceskaexpedice/kramerius/issues/110
		case 500:
		case 404: // NOT FOUND
			mProgressView.setVisibility(View.VISIBLE);
			loadPageImageFromDatastream();
			break;
		case 401:// UNAUTHORIZED
			// TODO: urge user to log in
			if (mEventListener != null) {
				mEventListener.onAccessDenied();
			}
		default:
			if (mEventListener != null) {
				mEventListener.onNetworkError(responseCode);
			}
		}

	}

	private void inflateImage(Bitmap bitmap) {
		if (getActivity() == null) {
			return;
		}
		mImageViewContainer.removeAllViews();
		LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);

		GestureImageView view = new GestureImageView(getActivity());
		view.setImageBitmap(bitmap);
		view.setLayoutParams(params);
		view.setOnTouchListener(this);

		mImageViewContainer.addView(view);
		mImageViewContainer.setVisibility(View.VISIBLE);
	}

	private void loadPageImageFromDatastream() {
		final String url = buildScaledImageDatastreamUrl(mPid);
		if (VersionUtils.Debuggable()) {
			Log.d(TAG, "Url: " + url);
		}
		mImageRequest = new RedirectingImageRequest(url, new Response.Listener<Bitmap>() {
			@Override
			public void onResponse(Bitmap bitmap) {
				inflateImage(bitmap);
			}
		}, new Response.ErrorListener() {
			public void onErrorResponse(VolleyError error) {
				hideViews();
				int statusCode = error.networkResponse.statusCode;
				if (mEventListener != null) {
					if (statusCode == 403 || statusCode == 401) {
						mEventListener.onAccessDenied();
					} else {
						mEventListener.onNetworkError(statusCode);
					}
				}
			}
		});
		VolleyRequestManager.addToRequestQueue(mImageRequest);
	}

	private String buildScaledImageDatastreamUrl(String pid) {
		StringBuilder builder = new StringBuilder();
		builder.append("http://").append(mDomain);
		builder.append("/search/img?pid=").append(pid);
		builder.append("&stream=IMG_FULL");
		builder.append("&action=SCALE&scaledHeight=").append(determineHeightForScaledImageFromDatastream());
		return builder.toString();
	}

	private int determineHeightForScaledImageFromDatastream() {

		return IMG_FULL_HEIGHT;
	}

	@Override
	public void onImagePropertiesRedirectionLoopError(String imagePropertiesUrl, int redirections) {
		// Log.d(TAG, "onImagePropertiesRedirectionLoopError");
		hideViews();
		if (mEventListener != null) {
			mEventListener.onNetworkError(null);
		}
	}

	@Override
	public void onImagePropertiesDataTransferError(String imagePropertiesUrl, String errorMessage) {
		// Log.d(TAG, "onImagePropertiesDataTransferError");
		hideViews();
		Log.d("onImagePropertiesDataTransferError", imagePropertiesUrl + " - " + errorMessage);
		if (mEventListener != null) {
			mEventListener.onNetworkError(null);
		}
	}

	@Override
	public void onImagePropertiesInvalidDataError(String imagePropertiesUrl, String errorMessage) {
		// Log.d(TAG, "onImagePropertiesInvalidDataError");
		hideViews();
		if (mEventListener != null) {
			mEventListener.onInvalidDataError("imageProperties.xml: " + errorMessage);
		}
	}

	@Override
	public void onSingleTap(float x, float y, Rect boundingBox) {
		if (mEventListener != null) {
			mEventListener.onSingleTap(x, y, boundingBox);
		}
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		mGestureDetector.onTouchEvent(event);
		return true;
	}

	public String getPagePid(int pageIndex) {
		return "";
	}

	private void setViewMode(ViewMode mode) {
		mTiledImageView.setViewMode(mode);
	}

}
