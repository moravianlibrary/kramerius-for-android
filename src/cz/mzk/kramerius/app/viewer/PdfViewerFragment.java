package cz.mzk.kramerius.app.viewer;

import java.io.File;

import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.artifex.mupdfdemo.FilePicker;
import com.artifex.mupdfdemo.MuPDFCore;
import com.artifex.mupdfdemo.MuPDFPageAdapter;
import com.artifex.mupdfdemo.MuPDFReaderView;

import cz.mzk.kramerius.app.util.Constants;
import cz.mzk.kramerius.app.util.VersionUtils;


public class PdfViewerFragment extends Fragment implements FilePicker.FilePickerSupport {

	private static final String LOG_TAG = PdfViewerFragment.class.getSimpleName();

	public static final String EXTRA_DOMAIN = PdfViewerFragment.class.getSimpleName() + "_domain";
	public static final String EXTRA_PID = PdfViewerFragment.class.getSimpleName() + "_pid";

	private MuPDFCore mPdfCore;
	private MuPDFReaderView mPdfReaderView;

	private ViewGroup mContainer;

	private PdfListener mPdfListener;

	public interface PdfListener {
		public void onPdfPageChanged(int index);
		public void onPdfReady();
		public void onPdfSingleTap();
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (savedInstanceState != null) {
			//mDomain = savedInstanceState.getString(EXTRA_DOMAIN);
			//mPid = savedInstanceState.getString(EXTRA_PID);
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		mContainer = new RelativeLayout(getActivity());
		return mContainer;
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		//outState.putString(EXTRA_DOMAIN, mDomain);
		//outState.putString(EXTRA_PID, mPid);
		super.onSaveInstanceState(outState);
	}

	public void setEventListener(PdfListener listener) {
		mPdfListener = listener;
	}

	public void populate(int pageIndex) {
		try {
			File file = new File(getActivity().getFilesDir(), Constants.PDF_PATH);
			mPdfCore = new MuPDFCore(getActivity(), file.getAbsolutePath());
		} catch (Exception e) {
			if(VersionUtils.Debuggable()) {
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
				if (mPdfListener != null) {
					mPdfListener.onPdfPageChanged(i);
				}
				super.onMoveToChild(i);
			}

			@Override
			protected void onTapMainDocArea() {
				if (mPdfListener != null) {
					mPdfListener.onPdfSingleTap();
				}
			}

			@Override
			protected void onDocMotion() {

			}

		};
		mPdfReaderView.setAdapter(new MuPDFPageAdapter(getActivity(), this, mPdfCore));
		mContainer.removeAllViews();
		mContainer.addView(mPdfReaderView);
		showPage(pageIndex);
		if (mPdfListener != null) {
			mPdfListener.onPdfReady();
		}
	}

	
	public int getCurrentPageIndex() {
		return mPdfReaderView.getDisplayedViewIndex();
	}

	public void showPage(int pageIndex) {
		if (pageIndex >= 0 && pageIndex < mPdfCore.countPages()) {
			mPdfReaderView.setDisplayedViewIndex(pageIndex);
		} else {

		}
	}

	public void setBackgroundColor(int color) {
		mContainer.setBackgroundColor(color);
	}

	public int getNumberOfPages() {
		return mPdfCore.countPages();
	}


	@Override
	public void performPickFor(FilePicker picker) {
		// TODO
	}
	
	
	
	
	
	

	
	

}
