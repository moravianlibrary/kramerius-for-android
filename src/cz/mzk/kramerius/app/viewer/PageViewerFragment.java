package cz.mzk.kramerius.app.viewer;

import java.util.Arrays;
import java.util.List;

import android.app.Fragment;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;

import cz.mzk.androidzoomifyviewer.viewer.TiledImageView;
import cz.mzk.androidzoomifyviewer.viewer.TiledImageView.ImageInitializationHandler;
import cz.mzk.androidzoomifyviewer.viewer.TiledImageView.SingleTapListener;
import cz.mzk.androidzoomifyviewer.viewer.TiledImageView.ViewMode;
import cz.mzk.androidzoomifyviewer.viewer.Utils;
import cz.mzk.kramerius.app.R;

/**
 * @author Martin Řehánek
 * 
 */
public class PageViewerFragment extends Fragment implements IPageViewerFragment, OnTouchListener,
		ImageInitializationHandler, SingleTapListener {

	private static final String TAG = PageViewerFragment.class.getSimpleName();

	public static final String KEY_DOMAIN = PageViewerFragment.class.getSimpleName() + "_domain";
	public static final String KEY_PAGE_PIDS = PageViewerFragment.class.getSimpleName() + "_pagePids";
	public static final String KEY_CURRENT_PAGE_INDEX = PageViewerFragment.class.getSimpleName() + "_pageIndex";
	public static final String KEY_POPULATED = PageViewerFragment.class.getSimpleName() + ":_populated";
	private static final int MAX_IMG_FULL_HEIGHT = 1000;
	private static final int IMG_FULL_SCALE_QUOTIENT = 100;

	private String mDomain;
	private List<String> mPagePids;
	private int mCurrentPageIndex;

	private View mContainer;
	private View mProgressView;
	private TiledImageView mTiledImageView;
	private ImageView mImageView;

	private GestureDetector mGestureDetector;
	private EventListener mEventListener;

	private boolean mPopulated = false;
	private ImageRequest mImageRequest;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (savedInstanceState != null) {
			mDomain = savedInstanceState.getString(KEY_DOMAIN);
			mPopulated = savedInstanceState.getBoolean(KEY_POPULATED);
			mCurrentPageIndex = savedInstanceState.getInt(KEY_CURRENT_PAGE_INDEX, 0);
			if (savedInstanceState.containsKey(KEY_PAGE_PIDS)) {
				mPagePids = Arrays.asList(savedInstanceState.getStringArray(KEY_PAGE_PIDS));
			}
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_page_viewer, container, false);
		mContainer = view.findViewById(R.id.container);
		mContainer.setOnTouchListener(this);
		mProgressView = view.findViewById(R.id.progressView);
		mProgressView.setOnTouchListener(this);
		mTiledImageView = (TiledImageView) view.findViewById(R.id.tiledImageView);
		mTiledImageView.setImageInitializationHandler(this);
		// mTiledImageView.setTileDownloadHandler(this);
		mTiledImageView.setSingleTapListener(this);
		mImageView = (ImageView) view.findViewById(R.id.imageView);
		mImageView.setOnTouchListener(this);
		return view;
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
				mEventListener.onSingleTap(e.getX(), e.getY());
				return true;
			}
		});
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		outState.putString(KEY_DOMAIN, mDomain);
		outState.putInt(KEY_CURRENT_PAGE_INDEX, mCurrentPageIndex);
		outState.putBoolean(KEY_POPULATED, mPopulated);
		if (mPagePids != null) {
			String[] pidsArray = new String[mPagePids.size()];
			outState.putStringArray(KEY_PAGE_PIDS, mPagePids.toArray(pidsArray));
		}
		super.onSaveInstanceState(outState);
	}

	@Override
	public void onStop() {
		super.onStop();
		if (mImageRequest != null) {
			mImageRequest.cancel();
		}
	}

	@Override
	public void setEventListener(EventListener eventListener) {
		this.mEventListener = eventListener;
	}

	@Override
	public void populate(String domain, List<String> pagePids) {
		// Log.d(TAG, "populating");
		this.mDomain = domain;
		this.mPagePids = pagePids;
		this.mCurrentPageIndex = 0;
		this.mPopulated = true;
		hideViews();
		if (mEventListener != null) {
			mEventListener.onReady();
		}
	}

	@Override
	public boolean isPopulated() {
		return mPopulated;
	}

	@Override
	public int getCurrentPageIndex() {
		return mCurrentPageIndex;
	}

	@Override
	public Integer getNextPageIndex() {
		int next = mCurrentPageIndex + 1;
		return next == mPagePids.size() ? null : Integer.valueOf(next);
	}

	@Override
	public Integer getPreviousPageIndex() {
		int next = mCurrentPageIndex - 1;
		return next == -1 ? null : Integer.valueOf(next);
	}

	@Override
	public void showPage(int pageIndex) {
		Log.d(TAG, "Showing page " + pageIndex);
		if (pageIndex >= 0 && pageIndex < mPagePids.size()) {
			if (mImageRequest != null) {
				mImageRequest.cancel();
			}
			hideViews();
			mProgressView.setVisibility(View.VISIBLE);
			mCurrentPageIndex = pageIndex;
			String pid = mPagePids.get(pageIndex);
			String url = buildZoomifyBaseUrl(pid);
			mTiledImageView.loadImage(url.toString());
		} else {
			Log.w(TAG, "Page index out of range: " + pageIndex);
		}
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
		mImageView.setVisibility(View.INVISIBLE);
	}

	@Override
	public void onImagePropertiesProcessed() {
		// Log.d(TAG, "onImagePropertiesProcessed");
		hideViews();
		mTiledImageView.setVisibility(View.VISIBLE);
	}

	@Override
	public void onImagePropertiesUnhandableResponseCodeError(String imagePropertiesUrl, int responseCode) {
		Log.d(TAG, "onImagePropertiesUnhandableResponseCodeError, code: " + responseCode);
		hideViews();
		switch (responseCode) {
		case 403: // FORBIDDEN
			mEventListener.onAccessDenied();
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
			mEventListener.onAccessDenied();
		default:
			mEventListener.onNetworkError(responseCode);
		}

	}

	private void loadPageImageFromDatastream() {
		String pid = mPagePids.get(mCurrentPageIndex);
		final String url = buildScaledImageDatastreamUrl(pid);
		Log.d(TAG, "Url: " + url);
		mImageRequest = new ImageRequest(url, new Response.Listener<Bitmap>() {
			@Override
			public void onResponse(Bitmap bitmap) {
				mImageView.setImageBitmap((Bitmap) bitmap);
				mImageView.setVisibility(View.VISIBLE);
			}
		}, 0, 0, null, new Response.ErrorListener() {
			public void onErrorResponse(VolleyError error) {
				hideViews();
				int statusCode = error.networkResponse.statusCode;
				if (statusCode == 403 || statusCode == 401) {
					mEventListener.onAccessDenied();
				} else {
					mEventListener.onNetworkError(statusCode);
				}
			}
		});
		VolleyRequestManager.addToRequestQueue(mImageRequest);
	}

	private String buildScaledImageDatastreamUrl(String pid) {
		StringBuilder builder = new StringBuilder();
		builder.append("http://").append(mDomain);
		builder.append("/search/img?pid=").append(pid);
		builder.append("&stream=IMG_FULL&action=SCALE");
		builder.append("&scaledHeight=").append(determineHeightForScaledImageFromDatastream());
		return builder.toString();
	}

	private int determineHeightForScaledImageFromDatastream() {
		int heightPx = mImageView.getHeight();
		int heightDp = Utils.pxToDp(heightPx);
		int result = heightDp > MAX_IMG_FULL_HEIGHT ? MAX_IMG_FULL_HEIGHT : (heightDp / IMG_FULL_SCALE_QUOTIENT + 1)
				* IMG_FULL_SCALE_QUOTIENT;
		Log.d(TAG, "View height: " + heightDp + " dp (" + heightPx + " px); scaling image to: " + result + " px");
		return result;
	}

	@Override
	public void onImagePropertiesRedirectionLoopError(String imagePropertiesUrl, int redirections) {
		// Log.d(TAG, "onImagePropertiesRedirectionLoopError");
		hideViews();
		mEventListener.onNetworkError(null);
	}

	@Override
	public void onImagePropertiesDataTransferError(String imagePropertiesUrl, String errorMessage) {
		// Log.d(TAG, "onImagePropertiesDataTransferError");
		hideViews();
		mEventListener.onNetworkError(null);
	}

	@Override
	public void onImagePropertiesInvalidDataError(String imagePropertiesUrl, String errorMessage) {
		// Log.d(TAG, "onImagePropertiesInvalidDataError");
		hideViews();
		mEventListener.onInvalidDataError("imageProperties.xml: " + errorMessage);
	}

	@Override
	public void onSingleTap(float x, float y) {
		if (mEventListener != null) {
			mEventListener.onSingleTap(x, y);
		}
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		mGestureDetector.onTouchEvent(event);
		return true;
	}

	@Override
	public String getPagePid(int pageIndex) {
		return mPagePids.get(pageIndex);
	}

	@Override
	public int getPageNumber() {
		return mPagePids.size();
	}

	@Override
	public void setViewMode(ViewMode mode) {
		mTiledImageView.setViewMode(mode);
	}

}
