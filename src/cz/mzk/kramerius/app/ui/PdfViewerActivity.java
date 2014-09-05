package cz.mzk.kramerius.app.ui;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import com.google.analytics.tracking.android.EasyTracker;
import com.readystatesoftware.systembartint.SystemBarTintManager;

import cz.mzk.kramerius.app.BaseActivity;
import cz.mzk.kramerius.app.R;
import cz.mzk.kramerius.app.api.K5Connector;
import cz.mzk.kramerius.app.model.Item;
import cz.mzk.kramerius.app.util.TextUtil;
import cz.mzk.kramerius.pdfviewer.PdfViewerFragment;
import cz.mzk.kramerius.pdfviewer.PdfViewerFragment.PdfViewerListener;

public class PdfViewerActivity extends Activity implements OnClickListener, PdfViewerListener {

	private View mLoader;
	private TextView mIndex;

	private Animation mLoaderAnimation;

	private boolean mFullscreen = true;
	private View mBottomPanel;
	private View mTopPanel;
	private TextView mTitle;

	private SystemBarTintManager mSystemBarTintManager;
	private String mParentPid;

	private View mMetadataButton;
	private View mZoomInButton;
	private View mZoomOutButton;
	private PdfViewerFragment mPdfViewer;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		boolean keepScreenOn = PreferenceManager.getDefaultSharedPreferences(this).getBoolean(
				getString(R.string.pref_keep_screen_on_key),
				Boolean.parseBoolean(getString(R.string.pref_keep_screen_on_default)));
		if(keepScreenOn) {
			getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		}	
		mSystemBarTintManager = new SystemBarTintManager(this);
		mSystemBarTintManager.setStatusBarTintEnabled(false);
		mSystemBarTintManager.setStatusBarTintResource(R.color.status_bar);
		setContentView(R.layout.activity_pdf);
		String pid = getIntent().getExtras().getString(BaseActivity.EXTRA_PID);
		mIndex = (TextView) findViewById(R.id.pdf_index);
		mIndex.setOnClickListener(this);
		mLoader = findViewById(R.id.pdf_loader);
		mBottomPanel = findViewById(R.id.pdf_bottomPanel);
		mBottomPanel.setVisibility(View.GONE);
		mTopPanel = findViewById(R.id.pdf_topPanel);
		mTopPanel.setVisibility(View.GONE);
		mTitle = (TextView) findViewById(R.id.pdf_title);
		mMetadataButton = findViewById(R.id.pdf_metadata);
		mMetadataButton.setOnClickListener(this);
		mZoomInButton = findViewById(R.id.pdf_zoomin);
		mZoomInButton.setOnClickListener(this);
		mZoomOutButton = findViewById(R.id.pdf_zoomout);
		mZoomOutButton.setOnClickListener(this);

		mLoaderAnimation = AnimationUtils.loadAnimation(this, R.anim.rotation);
		mLoaderAnimation.setRepeatCount(Animation.INFINITE);

		mPdfViewer = (PdfViewerFragment) getFragmentManager().findFragmentById(R.id.pdf_viewer);
		mPdfViewer.setPdfViewerListener(this);
		new LoadPdfTask(this).execute(pid);

	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			finish();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	private void onPreviousPage() {
		if (mPdfViewer.getCurrentPageNumber() > 0) {
			mPdfViewer.prevPage();
		}
	}

	private void onNextPage() {
		if (mPdfViewer.getCurrentPageNumber() < mPdfViewer.getNumberOfPages()) {
			mPdfViewer.nextPage();
		}
	}

	private void showMetadata() {
		Intent intent = new Intent(PdfViewerActivity.this, MetadataActivity.class);
		intent.putExtra(BaseActivity.EXTRA_PID, mParentPid);
		startActivity(intent);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (PreferenceManager.getDefaultSharedPreferences(this).getBoolean(
				getString(R.string.pref_hardware_buttons_key),
				Boolean.parseBoolean(getString(R.string.pref_hardware_buttons_key))

		)) {
			if ((keyCode == KeyEvent.KEYCODE_VOLUME_DOWN)) {
				onPreviousPage();
				return true;
			} else if ((keyCode == KeyEvent.KEYCODE_VOLUME_UP)) {
				onNextPage();
				return true;
			}
		}
		return super.onKeyDown(keyCode, event);
	}
	
	@Override
	public boolean dispatchKeyEvent(KeyEvent event) {
		boolean useHardwareButton = PreferenceManager.getDefaultSharedPreferences(this).getBoolean(
				getString(R.string.pref_hardware_buttons_key),
				Boolean.parseBoolean(getString(R.string.pref_hardware_buttons_default)));
		if (useHardwareButton) {
			int action = event.getAction();
			int keyCode = event.getKeyCode();
			switch (keyCode) {
			case KeyEvent.KEYCODE_VOLUME_UP:
				if (action == KeyEvent.ACTION_DOWN) {					
					onPreviousPage();
				}
				return true;
			case KeyEvent.KEYCODE_VOLUME_DOWN:
				if (action == KeyEvent.ACTION_DOWN) {
					onNextPage();
				}
				return true;
			default:
				return super.dispatchKeyEvent(event);
			}
		} else {
			return super.dispatchKeyEvent(event);
		}
	}
	
	
	

	// @Override
	// public void onTap() {
	// // Toast.makeText(PageActivity.this, "view clicked",
	// // Toast.LENGTH_LONG).show();
	// if (!mFullscreen) {
	// getWindow()
	// .setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
	// WindowManager.LayoutParams.FLAG_FULLSCREEN);
	// mBottomPanel.setVisibility(View.GONE);
	// mTopPanel.setVisibility(View.GONE);
	// mSystemBarTintManager.setStatusBarTintEnabled(false);
	// } else {
	// getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
	// mBottomPanel.setVisibility(View.VISIBLE);
	// mTopPanel.setVisibility(View.VISIBLE);
	// mSystemBarTintManager.setStatusBarTintEnabled(true);
	// }
	// mFullscreen = !mFullscreen;
	//
	// }

	@Override
	public void onClick(View v) {
		if (v == mMetadataButton) {
			showMetadata();
		} else if (v == mZoomInButton) {
			mPdfViewer.zoomIn();
		} else if (v == mZoomOutButton) {
			mPdfViewer.zoomOut();
		} else if (v == mIndex) {
			showPageSelectionDialog();
		}
	}

	private void showPageSelectionDialog() {
		LayoutInflater factory = LayoutInflater.from(this);
		final View pagenumView = factory.inflate(R.layout.dialog_pagenumber, null);
		final int pageNumber = mPdfViewer.getCurrentPageNumber();
		final EditText edPagenum = (EditText) pagenumView.findViewById(R.id.pagenum_edit);
		edPagenum.setText(Integer.toString(pageNumber));
		edPagenum.setOnEditorActionListener(new TextView.OnEditorActionListener() {

			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				if (event == null || (event.getAction() == 1)) {
					// Hide the keyboard
					InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
					imm.hideSoftInputFromWindow(edPagenum.getWindowToken(), 0);
				}
				return true;
			}
		});
		new AlertDialog.Builder(this).setTitle("Jump to page").setView(pagenumView)
				.setPositiveButton("OK", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						String strPagenum = edPagenum.getText().toString();
						int pageNum = pageNumber;
						try {
							pageNum = Integer.parseInt(strPagenum);
						} catch (NumberFormatException ignore) {
						}
						mPdfViewer.goToPage(pageNum);
					}
				}).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
					}
				}).create().show();

	}

	class LoadPdfTask extends AsyncTask<String, Void, Item> {

		private Context tContext;

		public LoadPdfTask(Context context) {
			tContext = context;
		}

		@Override
		protected void onPreExecute() {
			mLoader.setVisibility(View.VISIBLE);
			mLoader.startAnimation(mLoaderAnimation);
		}

		@Override
		protected Item doInBackground(String... params) {
			Item item = K5Connector.getInstance().getItem(tContext, params[0]);
			// if(1 == 1) {
			// return item;
			// }
			String pdf = item.getPdf();
			if (pdf == null) {
				return null;
			}
			InputStream input = null;
			OutputStream output = null;
			HttpURLConnection connection = null;
			try {
				URL url = new URL(pdf);
				connection = (HttpURLConnection) url.openConnection();
				connection.connect();

				if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
					return null;
				}
				input = connection.getInputStream();
				output = new FileOutputStream("/sdcard/tmppdf.pdf");

				byte data[] = new byte[4096];
				int count;
				while ((count = input.read(data)) != -1) {
					if (isCancelled()) {
						input.close();
						return null;
					}
					output.write(data, 0, count);
				}
			} catch (Exception e) {
				return null;
			} finally {
				try {
					if (output != null)
						output.close();
					if (input != null)
						input.close();
				} catch (IOException ignored) {
				}

				if (connection != null)
					connection.disconnect();
			}
			return item;

		}

		@Override
		protected void onPostExecute(Item item) {
			mLoader.clearAnimation();
			mLoader.setVisibility(View.GONE);
			if (tContext == null || item == null) {
				return;
			}
			mParentPid = item.getPid();
			mTitle.setText(TextUtil.shortenforActionBar(item.getTitle()));
			mPdfViewer.setContent("/sdcard/tmppdf.pdf");
		}
	}

	@Override
	public void onPdfPageLoaded() {
		mIndex.setText(mPdfViewer.getCurrentPageNumber() + "/" + mPdfViewer.getNumberOfPages());
	}

	@Override
	public void onPdfReady() {
		// TODO Auto-generated method stub

	}

	@Override
	public void onPdfTap() {
		if (!mFullscreen) {
			getWindow()
					.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
			mBottomPanel.setVisibility(View.GONE);
			mTopPanel.setVisibility(View.GONE);
			mSystemBarTintManager.setStatusBarTintEnabled(false);
		} else {
			getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
			mBottomPanel.setVisibility(View.VISIBLE);
			mTopPanel.setVisibility(View.VISIBLE);
			mSystemBarTintManager.setStatusBarTintEnabled(true);
		}
		mFullscreen = !mFullscreen;
	}

	
	@Override
	public void onStart() {
		super.onStart();
		EasyTracker.getInstance(this).activityStart(this);
	}

	@Override
	public void onStop() {
		super.onStop();
		EasyTracker.getInstance(this).activityStop(this);
	}
}
