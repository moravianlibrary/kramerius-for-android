package cz.mzk.kramerius.app.viewer;

import java.io.File;
import java.util.List;

import android.app.Fragment;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.artifex.mupdfdemo.FilePicker;
import com.artifex.mupdfdemo.MuPDFCore;
import com.artifex.mupdfdemo.MuPDFPageAdapter;
import com.artifex.mupdfdemo.MuPDFReaderView;

import cz.mzk.androidzoomifyviewer.viewer.TiledImageView.ViewMode;
import cz.mzk.kramerius.app.util.Constants;

public class PdfViewerFragment extends Fragment implements IPageViewerFragment, FilePicker.FilePickerSupport {

	private static final String LOG_TAG = PdfViewerFragment.class.getSimpleName();

	public static final String EXTRA_DOMAIN = PdfViewerFragment.class.getSimpleName() + "_domain";
	public static final String EXTRA_PID = PdfViewerFragment.class.getSimpleName() + "_pid";

	private MuPDFCore mPdfCore;
	private MuPDFReaderView mPdfReaderView;

	private String mDomain;
	private String mPid;
	private boolean mPopulated = false;
	private ViewGroup mContainer;

	private EventListener mEventListener;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (savedInstanceState != null) {
			mDomain = savedInstanceState.getString(EXTRA_DOMAIN);
			mPid = savedInstanceState.getString(EXTRA_PID);
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		mContainer = new RelativeLayout(getActivity());
		return mContainer;
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		outState.putString(EXTRA_DOMAIN, mDomain);
		outState.putString(EXTRA_PID, mPid);
		super.onSaveInstanceState(outState);
	}

	@Override
	public void setEventListener(EventListener eventListener) {
		mEventListener = eventListener;
	}

	@Override
	public void populate(String domain, List<String> pagePids) {
		if (pagePids != null && !pagePids.isEmpty()) {
			mPid = pagePids.get(0);
		}
		mDomain = domain;

		try {
			File file = new File(getActivity().getFilesDir(), Constants.PDF_PATH);
			mPdfCore = new MuPDFCore(getActivity(), file.getAbsolutePath());
		} catch (Exception e) {
			if(Constants.DEBUG_MODE) {
				Log.d(LOG_TAG, "PDF CORE exception: " + e.getMessage());
			}
		}
		if (mPdfCore == null) {
			return;
		}
	
		mPdfReaderView = new MuPDFReaderView(getActivity()) {
			@Override
			protected void onMoveToChild(int i) {
				if (mPdfCore == null) {
					return;
				}
				if (mEventListener != null) {
					mEventListener.onPageChanged(i);
				}
				super.onMoveToChild(i);
			}

			@Override
			protected void onTapMainDocArea() {
				if (mEventListener != null) {
					mEventListener.onSingleTap(-1, -1, null);
				}
			}

			@Override
			protected void onDocMotion() {

			}

		};
		mPdfReaderView.setAdapter(new MuPDFPageAdapter(getActivity(), this, mPdfCore));
		mContainer.removeAllViews();
		mContainer.addView(mPdfReaderView);
		mPopulated = true;
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
		return mPdfReaderView.getDisplayedViewIndex();
	}

	@Override
	public void showPage(int pageIndex) {
		if (pageIndex >= 0 && pageIndex < mPdfCore.countPages()) {
			mPdfReaderView.setDisplayedViewIndex(pageIndex);
		} else {

		}
	}

	@Override
	public void setBackgroundColor(int color) {
		mContainer.setBackgroundColor(color);
	//	mPdfReaderView.setBackgroundColor(color);
	}

	@Override
	public int getNumberOfPage() {
		return mPdfCore.countPages();
	}

	@Override
	public void setViewMode(ViewMode mode) {
		// TODO
	}

	@Override
	public void performPickFor(FilePicker picker) {
		// TODO
	}
	
	
	
	
	
	

	
	

}
