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

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import cz.mzk.kramerius.app.R;
import cz.mzk.kramerius.app.api.K5Api;
import cz.mzk.kramerius.app.search.TextBox;
import cz.mzk.kramerius.app.util.VersionUtils;
import cz.mzk.tiledimageview.TiledImageView;
import cz.mzk.tiledimageview.TiledImageView.MetadataInitializationHandler;
import cz.mzk.tiledimageview.TiledImageView.SingleTapListener;
import cz.mzk.tiledimageview.TiledImageView.ViewMode;
import cz.mzk.tiledimageview.images.TiledImageProtocol;
import cz.mzk.tiledimageview.rectangles.FramingRectangle;

public class SinglePageViewerFragment extends Fragment implements OnTouchListener, MetadataInitializationHandler,
        SingleTapListener {

    private static final String TAG = SinglePageViewerFragment.class.getSimpleName();

    private static final int IMG_FULL_HEIGHT = 1500;

    private String mDomain;
    private String mPid;
    private int mBackgroud;
    private View mContainer;
    private View mProgressView;
    private TiledImageView mTiledImageView;
    private GestureImageView mGestureImageView;
    private ViewGroup mImageViewContainer;

    private GestureDetector mGestureDetector;
    private PageEventListener mEventListener;

    private ImageRequest mImageRequest;
    private ViewMode mViewMode;
    private List<FramingRectangle> mRects = null;

    private float mInitialImageScale = -1;


    public interface PageEventListener {

        public void onAccessDenied();

        public void onNetworkError(Integer statusCode);

        public void onInvalidDataError(String errorMessage);

        public void onSingleTap(float x, float y, Rect boundingBox);
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        //Log.e("test", "fragment: onCreate");
        super.onCreate(savedInstanceState);
        mDomain = getArguments().getString("domain");
        mPid = getArguments().getString("pid");
        mBackgroud = getArguments().getInt("background");
        mViewMode = ViewMode.values()[getArguments().getInt("viewmode")];
    }

    public static SinglePageViewerFragment newInstance(String domain, String pid, int backgroud, ViewMode viewMode) {
        //Log.e("test", "fragment: newInstance");
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
        //Log.e("test", "fragment: onCreateView");
        Log.v(TAG, "onCreateView");
        View view = inflater.inflate(R.layout.fragment_single_page_viewer, container, false);
        mContainer = view.findViewById(R.id.container);
        mContainer.setOnTouchListener(this);
        mProgressView = view.findViewById(R.id.progressView);
        mProgressView.setOnTouchListener(this);
        mTiledImageView = (TiledImageView) view.findViewById(R.id.tiledImageView);
        mTiledImageView.setMetadataInitializationHandler(this);
        mTiledImageView.setSingleTapListener(this);
        mTiledImageView.setFramingRectangles(mRects);
        mImageViewContainer = (ViewGroup) view.findViewById(R.id.imageContainer);
        setViewMode(mViewMode);
        setBackgroundColor(mBackgroud);
        showPage();
        return view;
    }

    public void invalidateViewer() {
        if (mTiledImageView == null) {
            return;
        }
        mTiledImageView.postInvalidateDelayed(100);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mEventListener = (PageEventListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement EventListener");
        }
    }

    public boolean isSwipeEnabled() {
        if (mGestureImageView != null) {
            float scale = mGestureImageView.getScale();
            if (scale == 1.0f) {
                return true;
            } else {
                if (mInitialImageScale == -1) {
                    mInitialImageScale = scale;
                    return true;
                }
            }
            return mInitialImageScale == -1 || scale <= mInitialImageScale;
        }
        if (mTiledImageView == null) {
            return true;
        }
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
        String url = K5Api.getZoomifyBaseUrl(getActivity(), mPid);
        mTiledImageView.loadImage(TiledImageProtocol.ZOOMIFY, url.toString());
    }

    private void hideViews() {
        mProgressView.setVisibility(View.INVISIBLE);
        mTiledImageView.setVisibility(View.INVISIBLE);
        mImageViewContainer.setVisibility(View.INVISIBLE);
    }

    @Override
    public void onMetadataInitialized() {
        hideViews();
        mTiledImageView.setVisibility(View.VISIBLE);
    }

    private void setBackgroundColor(int color) {
        mTiledImageView.setBackgroundColor(color);
        mImageViewContainer.setBackgroundColor(color);
        mProgressView.setBackgroundColor(color);
    }

    @Override
    public void onMetadataUnhandableResponseCode(String imagePropertiesUrl, int responseCode) {
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
        mInitialImageScale = -1;
        mImageViewContainer.removeAllViews();
        LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);

        mGestureImageView = new GestureImageView(getActivity());
        mGestureImageView.setImageBitmap(bitmap);
        mGestureImageView.setLayoutParams(params);
        mGestureImageView.setOnTouchListener(this);

        mImageViewContainer.addView(mGestureImageView);
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
    public void onMetadataRedirectionLoop(String imagePropertiesUrl, int redirections) {
        // Log.d(TAG, "onImagePropertiesRedirectionLoopError");
        hideViews();
        if (mEventListener != null) {
            mEventListener.onNetworkError(null);
        }
    }

    @Override
    public void onMetadataDataTransferError(String imagePropertiesUrl, String errorMessage) {
        // Log.d(TAG, "onImagePropertiesDataTransferError");
        hideViews();
        Log.d("onImgPropDataTransErr", imagePropertiesUrl + " - " + errorMessage);
        if (mEventListener != null) {
            mEventListener.onNetworkError(null);
        }
    }

    @Override
    public void onMetadataInvalidData(String imagePropertiesUrl, String errorMessage) {
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

    public void setTextBoxes(Set<TextBox> boxes) {
        if (boxes != null) {
            mRects = new ArrayList<>(boxes.size());
            for (TextBox box : boxes) {
                mRects.add(new FramingRectangle(box.getRectangle(), new FramingRectangle.Border(R.color.text_box_border, 1), R.color.text_box_filling));
            }
            //Log.v(TAG, String.format("framing rectangles: %d", mRects.size()));
        } else {
            mRects = null;
        }
        if (mTiledImageView != null) {
            mTiledImageView.setFramingRectangles(mRects);
        }
    }

    private void setViewMode(ViewMode mode) {
        mTiledImageView.setViewMode(mode);
    }

}
