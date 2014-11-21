package cz.mzk.kramerius.app.ui;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.Pair;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import com.google.analytics.tracking.android.EasyTracker;
import com.readystatesoftware.systembartint.SystemBarTintManager;

import cz.mzk.androidzoomifyviewer.viewer.TiledImageView.ViewMode;
import cz.mzk.kramerius.app.BaseActivity;
import cz.mzk.kramerius.app.BaseFragment.onWarningButtonClickedListener;
import cz.mzk.kramerius.app.R;
import cz.mzk.kramerius.app.api.K5Api;
import cz.mzk.kramerius.app.api.K5Connector;
import cz.mzk.kramerius.app.data.KrameriusContract.HistoryEntry;
import cz.mzk.kramerius.app.dialog.MaterialDialog;
import cz.mzk.kramerius.app.model.Item;
import cz.mzk.kramerius.app.model.ParentChildrenPair;
import cz.mzk.kramerius.app.ui.PageSelectionFragment.OnPageNumberSelected;
import cz.mzk.kramerius.app.ui.ViewerMenuFragment.ViewerMenuListener;
import cz.mzk.kramerius.app.util.Constants;
import cz.mzk.kramerius.app.util.FileUtils;
import cz.mzk.kramerius.app.util.MessageUtils;
import cz.mzk.kramerius.app.util.ModelUtil;
import cz.mzk.kramerius.app.util.ScreenUtil;
import cz.mzk.kramerius.app.util.TextUtil;
import cz.mzk.kramerius.app.viewer.IPageViewerFragment;
import cz.mzk.kramerius.app.viewer.IPageViewerFragment.EventListener;

public class PageActivity extends ActionBarActivity implements OnClickListener, OnSeekBarChangeListener,
		OnPageNumberSelected, ViewerMenuListener, EventListener {

	public static final String EXTRA_SECURE = "extra_secure";

	private static final String EXTRA_TITLE = "extra_title";
	private static final String EXTRA_SUBTITLE = "extra_subtitle";
	private static final String EXTRA_COMPLEX_TITLE = "extra_complex_title";
	private static final String EXTRA_ITEMS = "extra_items";
	private static final String EXTRA_PARENT_PID = "extra_parent_pid";
	private static final String EXTRA_CURRENT_PAGE = "extra_current_page";
	private static final String EXTRA_FULLSCREEN = "extra_fullscreen";
	private static final String EXTRA_PDF = "extra_pdf";
	private static final String EXTRA_PDF_STATUS = "extra_pdf_status";

	private static final int MENU_DETAILS = 101;

	private static final String LOG_TAG = PageActivity.class.getSimpleName();

	private View mLoader;
	private TextView mIndex;

	private Animation mLoaderAnimation;

	private List<Item> mPageList;
	private int mCurrentPage;

	private boolean mFullscreen = true;
	private View mBottomPanel;
	private SeekBar mSeekBar;
	private int mLastProgress;
	private TextView mSeekPosition;

	private SystemBarTintManager mSystemBarTintManager;
	private String mParentPid;

	private View mListButton;
	// private View mMetadataButton;

	private PageSelectionFragment mPageSelectionFragment;
	private boolean mListShown;

	private String mSubtitle;
	private String mTitle;
	private boolean mComplexTitle;

	private IPageViewerFragment mPageViewerFragment;
	private ViewGroup mContainer;

	private ViewerMenuFragment mMenuFragment;

	private FrameLayout mMenuContainer;
	private DrawerLayout mDrawerLayout;
	private ActionBarDrawerToggle mDrawerToggle;
	private Toolbar mToolbar;

	private FrameLayout mViewerWrapper;
	private FrameLayout mMessageContainer;

	private String mPid;

	private View mImageViewerContainer;
	private View mPdfViewerContainer;

	private boolean mIsPdf;
	private int mPdfStatus;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		// if(getIntent().hasExtra(EXTRA_SECURE)) {
		// if(getIntent().getBooleanExtra(EXTRA_SECURE, false)) {
		// getWindow( ).addFlags(WindowManager.LayoutParams.FLAG_SECURE);
		// }
		// }
		boolean keepScreenOn = PreferenceManager.getDefaultSharedPreferences(this).getBoolean(
				getString(R.string.pref_keep_screen_on_key),
				Boolean.parseBoolean(getString(R.string.pref_keep_screen_on_default)));
		if (keepScreenOn) {
			getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		}
		mSystemBarTintManager = new SystemBarTintManager(this);
		mSystemBarTintManager.setStatusBarTintEnabled(false);
		mSystemBarTintManager.setStatusBarTintResource(R.color.status_bar);

		setContentView(R.layout.activity_page);

		mViewerWrapper = (FrameLayout) findViewById(R.id.page_viewer_wrapper);
		mMessageContainer = (FrameLayout) findViewById(R.id.page_message_container);

		mImageViewerContainer = findViewById(R.id.fragmentImageViewerContainer);
		mImageViewerContainer.setVisibility(View.GONE);
		mPdfViewerContainer = findViewById(R.id.fragmentPdfViewerContainer);
		mPdfViewerContainer.setVisibility(View.GONE);
		mViewerWrapper.setVisibility(View.INVISIBLE);
		mContainer = (ViewGroup) findViewById(R.id.page_container);

		mMenuFragment = new ViewerMenuFragment();
		mMenuFragment.setCallback(this);
		getFragmentManager().beginTransaction().replace(R.id.viewer_menu, mMenuFragment).commit();

		mPid = getIntent().getExtras().getString(BaseActivity.EXTRA_PID);
		mIndex = (TextView) findViewById(R.id.page_index);

		mLoader = findViewById(R.id.page_loader);
		mBottomPanel = findViewById(R.id.page_bottomPanel);
		mBottomPanel.setVisibility(View.GONE);
		mSeekPosition = (TextView) findViewById(R.id.page_seek_position);
		mSeekPosition.setVisibility(View.GONE);
		mSeekBar = (SeekBar) findViewById(R.id.page_seekBar);
		mSeekBar.setOnSeekBarChangeListener(this);
		mListButton = findViewById(R.id.page_list);
		mListButton.setOnClickListener(this);
		// mMetadataButton = findViewById(R.id.page_metadata);
		// mMetadataButton.setOnClickListener(this);

		mLoaderAnimation = AnimationUtils.loadAnimation(this, R.anim.rotation);
		mLoaderAnimation.setRepeatCount(Animation.INFINITE);

		mPageSelectionFragment = (PageSelectionFragment) getFragmentManager().findFragmentById(R.id.page_listFragment);

		getFragmentManager().beginTransaction().hide(mPageSelectionFragment).commit();
		mListShown = false;

		mMenuContainer = (FrameLayout) findViewById(R.id.viewer_menu);
		mDrawerLayout = (DrawerLayout) findViewById(R.id.main_drawer_layout);

		mToolbar = (Toolbar) findViewById(R.id.toolbar);
		mToolbar.setVisibility(View.GONE);
		setSupportActionBar(mToolbar);
		mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {

				if (mDrawerLayout.isDrawerOpen(mMenuContainer)) {
					mDrawerLayout.closeDrawer(mMenuContainer);

				} else {
					mDrawerLayout.openDrawer(mMenuContainer);
					hidePageSelection();
				}
			}
		});

		mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.string.drawer_open, R.string.drawer_close) {

			public void onDrawerClosed(View view) {
				supportInvalidateOptionsMenu();
			}

			public void onDrawerOpened(View drawerView) {
				supportInvalidateOptionsMenu();

			}
		};

		mDrawerLayout.setDrawerListener(mDrawerToggle);

		mCurrentPage = 0;
		if (savedInstanceState != null) {
			if (savedInstanceState.containsKey(EXTRA_CURRENT_PAGE)) {
				mCurrentPage = savedInstanceState.getInt(EXTRA_CURRENT_PAGE);
			}
			if (savedInstanceState.containsKey(EXTRA_ITEMS)) {
				mPageList = savedInstanceState.getParcelableArrayList(EXTRA_ITEMS);
			}
			mParentPid = savedInstanceState.getString(EXTRA_PARENT_PID);
			mTitle = savedInstanceState.getString(EXTRA_TITLE);
			mSubtitle = savedInstanceState.getString(EXTRA_SUBTITLE);
			mComplexTitle = savedInstanceState.getBoolean(EXTRA_COMPLEX_TITLE);
			mFullscreen = savedInstanceState.getBoolean(EXTRA_FULLSCREEN);
			mIsPdf = savedInstanceState.getBoolean(EXTRA_PDF);
			mPdfStatus = savedInstanceState.getInt(EXTRA_PDF_STATUS);
			if (!mFullscreen) {
				setFullscreen(false);
			}
		}
		if (mPageList == null) {
			new LoadPagesTask(this).execute(mPid);
		} else {
			init();
		}
	}

	private void initViewerFragment(boolean pdf) {
		IPageViewerFragment pdfFragment = (IPageViewerFragment) getFragmentManager().findFragmentById(
				R.id.fragmentPdfViewer);
		IPageViewerFragment imageFragment = (IPageViewerFragment) getFragmentManager().findFragmentById(
				R.id.fragmentImageViewer);
		if (pdf) {
			mMenuFragment.setDownloadType(ViewerMenuFragment.DOWNLOAD_PDF);
			mImageViewerContainer.setVisibility(View.GONE);
			mPdfViewerContainer.setVisibility(View.VISIBLE);
			mPageViewerFragment = pdfFragment;
		} else {
			mMenuFragment.setDownloadType(ViewerMenuFragment.DOWNLOAD_PAGE);
			mImageViewerContainer.setVisibility(View.VISIBLE);
			mPdfViewerContainer.setVisibility(View.GONE);
			mPageViewerFragment = imageFragment;
		}
		if (mPageViewerFragment == null) {
			return;
		}
		mPageViewerFragment.setEventListener(this);
		String vm = PreferenceManager.getDefaultSharedPreferences(this).getString(
				getString(R.string.pref_view_mode_key), getString(R.string.pref_view_mode_default));
		String[] vms = getResources().getStringArray(R.array.view_mode_values);
		if (vms[0].equals(vm)) {
			mPageViewerFragment.setViewMode(ViewMode.FIT_TO_SCREEN);
		} else if (vms[1].equals(vm)) {
			mPageViewerFragment.setViewMode(ViewMode.NO_FREE_SPACE_ALIGN_HORIZONTAL_LEFT_VERTICAL_TOP);
		} else if (vms[2].equals(vm)) {
			mPageViewerFragment.setViewMode(ViewMode.NO_FREE_SPACE_ALIGN_HORIZONTAL_CENTER_VERTICAL_CENTER);
		} else if (vms[3].equals(vm)) {
			mPageViewerFragment.setViewMode(ViewMode.NO_FREE_SPACE_ALIGN_HORIZONTAL_CENTER_VERTICAL_TOP);
		}
		setBackgroundColor();
		String domain = K5Api.getDomain(this);
		if (mPageViewerFragment != null) {
			mPageViewerFragment.populate(domain, itemsToPids());
		}
	}

	private List<String> itemsToPids() {
		List<String> pids = new ArrayList<String>();
		if (mPageList != null) {
			for (Item item : mPageList) {
				pids.add(item.getPid());
			}
		}
		return pids;
	}

	private void setBackgroundColor() {
		if (mPageViewerFragment == null) {
			return;
		}
		String bgColorValue = PreferenceManager.getDefaultSharedPreferences(this).getString(
				getString(R.string.pref_viewer_bg_color_key), getString(R.string.pref_viewer_bg_color_default));
		if ("white".equals(bgColorValue)) {
			mPageViewerFragment.setBackgroundColor(Color.WHITE);
		} else if ("black".equals(bgColorValue)) {
			mPageViewerFragment.setBackgroundColor(Color.BLACK);
		}
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		if (mPageList != null) {
			outState.putParcelableArrayList(EXTRA_ITEMS, (ArrayList<Item>) mPageList);
		}
		outState.putInt(EXTRA_CURRENT_PAGE, mCurrentPage);
		outState.putString(EXTRA_PARENT_PID, mParentPid);
		outState.putString(EXTRA_TITLE, mTitle);
		outState.putString(EXTRA_SUBTITLE, mSubtitle);
		outState.putBoolean(EXTRA_COMPLEX_TITLE, mComplexTitle);
		outState.putBoolean(EXTRA_FULLSCREEN, mFullscreen);
		outState.putBoolean(EXTRA_PDF, mIsPdf);
		outState.putInt(EXTRA_PDF_STATUS, mPdfStatus);
		super.onSaveInstanceState(outState);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(Menu.NONE, MENU_DETAILS, Menu.NONE, R.string.popup_card_details)
				.setIcon(R.drawable.ic_action_info_outline).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case MENU_DETAILS:
			showMetadata();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	private void previousPage() {
		if (mPageList == null) {
			return;
		}
		if (mCurrentPage > 0) {
			mCurrentPage--;
			loadPage();
		}
	}

	private void nextPage() {
		if (mPageList == null) {
			return;
		}
		if (mCurrentPage < mPageList.size() - 1) {
			mCurrentPage++;
			loadPage();
		}
	}

	private void loadPage() {
		if (mPageViewerFragment == null || !mPageViewerFragment.isPopulated() || mPageList == null
				|| mPageList.isEmpty()) {
			return;
		}
		clearMessages();
		mPageViewerFragment.showPage(mCurrentPage);
		mIndex.setText((mCurrentPage + 1) + "/" + mPageList.size());
		mSeekBar.setProgress(mCurrentPage);

	}

	private void showMetadata() {
		Intent intent = new Intent(PageActivity.this, MetadataActivity.class);
		intent.putExtra(BaseActivity.EXTRA_PID, mPageList.get(mCurrentPage).getPid());
		startActivity(intent);
	}

	class LoadPagesTask extends AsyncTask<String, Void, ParentChildrenPair> {

		private Context tContext;

		public LoadPagesTask(Context context) {
			tContext = context;
		}

		@Override
		protected void onPreExecute() {
			clearMessages();
			// mDrawerLayout.setActivated(false);
			mViewerWrapper.setVisibility(View.INVISIBLE);
			mLoader.setVisibility(View.VISIBLE);
			mLoader.startAnimation(mLoaderAnimation);
		}

		@Override
		protected ParentChildrenPair doInBackground(String... params) {
			Item item = K5Connector.getInstance().getItem(tContext, params[0]);
			if (item == null) {
				return null;
			}
			if (ModelUtil.PAGE.equals(item.getModel())) {
				List<Pair<String, String>> hierarchy = K5Connector.getInstance().getHierarychy(tContext, item.getPid());
				if (hierarchy == null) {
					return null;
				}
				if (hierarchy.size() > 1) {
					String parentPid = hierarchy.get(hierarchy.size() - 2).first;
					Item parentItem = K5Connector.getInstance().getItem(tContext, parentPid);
					if (parentItem != null) {
						parentItem.setSelectedChild(item.getPid());
						item = parentItem;
					}
				}
			}
			if (ModelUtil.PERIODICAL_ITEM.equals(item.getModel())) {
				List<Pair<String, String>> hierarchy = K5Connector.getInstance().getHierarychy(tContext, item.getPid());
				for (int i = 0; i < hierarchy.size(); i++) {
					if (ModelUtil.PERIODICAL_VOLUME.equals(hierarchy.get(i).second)) {
						Item parent = K5Connector.getInstance().getItem(tContext, hierarchy.get(i).first);
						if (parent != null) {
							item.setTitle(getString(R.string.metadata_periodical_volume) + " "
									+ parent.getVolumeTitle() + ", " + getString(R.string.metadata_periodical_item)
									+ " " + item.getIssueTitle());
						}
					}
				}
			}
			int status = K5Api.STATUS_UNKNOWN;
			if (item.getPdf() != null) {
				status = downloadPdf(item);
			}
			return new ParentChildrenPair(item, K5Connector.getInstance().getChildren(tContext, item.getPid()), status);
		}

		@Override
		protected void onPostExecute(ParentChildrenPair result) {
			mLoader.clearAnimation();
			mLoader.setVisibility(View.GONE);
			if (tContext == null) {
				return;
			}
			if (result == null || result.getParent() == null) {
				showWarningMessage(R.string.warn_data_loading_failed, R.string.gen_again,
						new onWarningButtonClickedListener() {

							@Override
							public void onWarningButtonClicked() {
								new LoadPagesTask(PageActivity.this).execute(mPid);
							}
						}, true);
				return;
			}

			mPdfStatus = result.getStatus();
			if (result.getStatus() == K5Api.STATUS_PDF_FORBIDDEN) {
				showWarningMessage(R.string.warn_pdf_private_message, R.string.warn_page_private_button,
						new onWarningButtonClickedListener() {

							@Override
							public void onWarningButtonClicked() {
								showInaccessibleDocumentActivity();
							}
						}, false);
				return;
			} else if (result.getStatus() == K5Api.STATUS_PDF_FAILED) {
				showWarningMessage(R.string.warn_pdf_loading_failed, R.string.gen_again,
						new onWarningButtonClickedListener() {

							@Override
							public void onWarningButtonClicked() {
								new LoadPagesTask(PageActivity.this).execute(mPid);
							}
						}, true);
				return;
			}

			mPageList = new ArrayList<Item>();
			if (result.getChildren() == null || result.getChildren().isEmpty()) {
				mPageList.add(result.getParent());
			} else {
				mPageList = result.getChildren();
			}
			mCurrentPage = 0;
			mTitle = TextUtil.parseTitle(result.getParent().getRootTitle());
			if (ModelUtil.PERIODICAL_ITEM.equals(result.getParent().getModel())) {
				mComplexTitle = true;
				mSubtitle = result.getParent().getTitle();
			} else {
				mComplexTitle = false;
				mSubtitle = null;
			}

			mParentPid = result.getParent().getPid();
			mIsPdf = result.getParent().getPdf() != null;
			String selectedPid = result.getParent().getSelectedChild();
			if (selectedPid == null) {
				String domain = K5Api.getDomain(PageActivity.this);
				Cursor c = getContentResolver().query(HistoryEntry.CONTENT_URI,
						new String[] { HistoryEntry.COLUMN_PID },
						HistoryEntry.COLUMN_DOMAIN + "=? AND " + HistoryEntry.COLUMN_PARENT_PID + " =?",
						new String[] { domain, mParentPid }, null);
				if (c.moveToFirst()) {
					selectedPid = c.getString(0);
				}
			}
			if (selectedPid != null) {
				int index = getIndexFromPid(selectedPid);
				if (index > -1 && mCurrentPage < mPageList.size()) {
					mCurrentPage = index;
				}
			}
			mMenuFragment.refreshRecent();
			init();
		}
	}

	private void init() {
		if (mComplexTitle) {
			mToolbar.setTitle(mTitle);
			mToolbar.setSubtitle(mSubtitle);
		} else {
			mToolbar.setTitle(mTitle);
		}
		mSeekBar.setMax(mPageList.size() - 1);
		mSeekBar.setProgress(mCurrentPage);
		initViewerFragment(mIsPdf);

		if (mIsPdf) {
			hidePageSelection();
			mListButton.setVisibility(View.GONE);
		} else {
			mPageSelectionFragment.setOnPageNumberSelected(PageActivity.this);
			mPageSelectionFragment.assignItems(mPageList);
			mListButton.setVisibility(View.VISIBLE);
		}

		mViewerWrapper.setVisibility(View.VISIBLE);
	}

	private int getIndexFromPid(String pid) {
		if (mPageList == null || pid == null) {
			return -1;
		}
		int index = 0;
		boolean match = false;
		for (Item item : mPageList) {
			if (item.getPid().equals(pid)) {
				match = true;
				break;
			}
			index++;
		}
		if (match) {
			return index;
		}
		return -1;
	}

	@Override
	protected void onPause() {
		putToHistory();
		super.onPause();
	}

	private void putToHistory() {
		if (mParentPid == null || mPageList == null) {
			return;
		}
		String domain = K5Api.getDomain(this);
		Cursor c = getContentResolver().query(HistoryEntry.CONTENT_URI, new String[] { HistoryEntry._ID },
				HistoryEntry.COLUMN_DOMAIN + "=? AND " + HistoryEntry.COLUMN_PARENT_PID + " =?",
				new String[] { domain, mParentPid }, null);
		boolean insert = true;
		if (c.moveToFirst()) {
			insert = false;
		}
		c.close();
		if (insert) {
			ContentValues cv = new ContentValues();
			cv.put(HistoryEntry.COLUMN_DOMAIN, domain);
			cv.put(HistoryEntry.COLUMN_PARENT_PID, mParentPid);
			cv.put(HistoryEntry.COLUMN_PID, mPageList.get(mCurrentPage).getPid());
			cv.put(HistoryEntry.COLUMN_TITLE, mTitle);
			cv.put(HistoryEntry.COLUMN_SUBTITLE, mSubtitle);
			cv.put(HistoryEntry.COLUMN_TIMESTAMP, System.currentTimeMillis());
			getContentResolver().insert(HistoryEntry.CONTENT_URI, cv);
		} else {
			ContentValues cv = new ContentValues();
			cv.put(HistoryEntry.COLUMN_DOMAIN, domain);
			cv.put(HistoryEntry.COLUMN_PARENT_PID, mParentPid);
			cv.put(HistoryEntry.COLUMN_PID, mPageList.get(mCurrentPage).getPid());
			cv.put(HistoryEntry.COLUMN_TITLE, mTitle);
			cv.put(HistoryEntry.COLUMN_SUBTITLE, mSubtitle);
			cv.put(HistoryEntry.COLUMN_TIMESTAMP, System.currentTimeMillis());
			getContentResolver().update(HistoryEntry.CONTENT_URI, cv,
					HistoryEntry.COLUMN_DOMAIN + "=? AND " + HistoryEntry.COLUMN_PARENT_PID + " =?",
					new String[] { domain, mParentPid });
		}

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
					previousPage();
				}
				return true;
			case KeyEvent.KEYCODE_VOLUME_DOWN:
				if (action == KeyEvent.ACTION_DOWN) {
					nextPage();
				}
				return true;
			default:
				return super.dispatchKeyEvent(event);
			}
		} else {
			return super.dispatchKeyEvent(event);
		}
	}

	@Override
	public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
		if (fromUser) {
			mLastProgress = progress;
			mSeekPosition.setText(String.valueOf(progress + 1));
		}
	}

	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {
		mLastProgress = -1;
		mSeekPosition.setVisibility(View.VISIBLE);
		mSeekPosition.setText(String.valueOf(mCurrentPage + 1));
	}

	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {
		mSeekPosition.setVisibility(View.GONE);
		if (mLastProgress > -1) {
			mCurrentPage = mLastProgress;
			loadPage();
		}

	}

	@Override
	public void onClick(View v) {
		if (v == mListButton) {
			if (mListShown) {
				hidePageSelection();
			} else {
				showPageSelection();
			}
		}
	}

	private void hidePageSelection() {
		if (!mListShown) {
			return;
		}
		getFragmentManager().beginTransaction().hide(mPageSelectionFragment).commit();
		mSeekBar.setEnabled(true);
		mListShown = false;
	}

	private void showPageSelection() {
		if (mListShown) {
			return;
		}

		getFragmentManager().beginTransaction().show(mPageSelectionFragment).commit();
		mSeekBar.setEnabled(false);
		mListShown = true;
	}

	@Override
	public void onPageNumberSelected(int index) {
		hidePageSelection();
		mCurrentPage = index;
		loadPage();
	}

	private void showWarningMessage(int message, int buttonText, final onWarningButtonClickedListener callback,
			boolean hideAfterClick) {
		mMessageContainer.removeAllViews();
		mMessageContainer.setVisibility(View.VISIBLE);
		MessageUtils.inflateMessage(this, mMessageContainer, getString(message), getString(buttonText), callback,
				hideAfterClick);
	}

	private void clearMessages() {
		mMessageContainer.removeAllViews();
		mMessageContainer.setVisibility(View.GONE);
	}

	@Override
	public void onReady() {
		loadPage();
	}

	@Override
	public void onAccessDenied() {
		showWarningMessage(R.string.warn_page_private_message, R.string.warn_page_private_button,
				new onWarningButtonClickedListener() {

					@Override
					public void onWarningButtonClicked() {
						showInaccessibleDocumentActivity();
					}
				}, false);
	}

	private void showInaccessibleDocumentActivity() {
		Intent intent = new Intent(PageActivity.this, InaccessibleDocumentActivity.class);
		startActivity(intent);
	}

	@Override
	public void onNetworkError(Integer statusCode) {
		showWarningMessage(R.string.warn_page_loading_failed, R.string.gen_again, new onWarningButtonClickedListener() {

			@Override
			public void onWarningButtonClicked() {
				loadPage();
			}
		}, true);
		return;
	}

	@Override
	public void onInvalidDataError(String errorMessage) {
		showWarningMessage(R.string.warn_page_loading_failed, R.string.gen_again, new onWarningButtonClickedListener() {

			@Override
			public void onWarningButtonClicked() {
				loadPage();
			}
		}, true);
		return;
	}

	@Override
	public void onSingleTap(float x, float y, Rect boundingBox) {
		float borderRatio = 0.15f;
		float openingBorderMax = borderRatio;
		float closingBorderMin = 1.0f - borderRatio;
		if (boundingBox != null) {
			float w = (x - boundingBox.left) / boundingBox.width();
			float h = (y - boundingBox.top) / boundingBox.height();
			if (x < boundingBox.left || y < boundingBox.top || w < openingBorderMax || h < openingBorderMax) {
				previousPage();
			} else if (x > boundingBox.right || y > boundingBox.bottom || w > closingBorderMin || h > closingBorderMin) {
				nextPage();
			} else {
				mFullscreen = !mFullscreen;
				setFullscreen(mFullscreen);
			}
		} else if (x >= 0 && y >= 0) {
			float w = x / mContainer.getWidth();
			float h = y / mContainer.getHeight();
			if (w < openingBorderMax || h < openingBorderMax) {
				previousPage();
			} else if (w > closingBorderMin || h > closingBorderMin) {
				nextPage();
			} else {
				mFullscreen = !mFullscreen;
				setFullscreen(mFullscreen);
			}
		} else {
			mFullscreen = !mFullscreen;
			setFullscreen(mFullscreen);
		}
	}

	private void setFullscreen(boolean fullscreen) {
		mFullscreen = fullscreen;
		if (mFullscreen) {
			getWindow()
					.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
			mBottomPanel.setVisibility(View.GONE);
			mToolbar.setVisibility(View.GONE);
			mSystemBarTintManager.setStatusBarTintEnabled(false);
			ScreenUtil.fullscreenInsets(this, mMenuContainer);
			ScreenUtil.fullscreenInsets(this, mContainer);
		} else {
			getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
			mBottomPanel.setVisibility(View.VISIBLE);
			mToolbar.setVisibility(View.VISIBLE);
			mSystemBarTintManager.setStatusBarTintEnabled(true);
		}
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

	@Override
	public void onHome() {
		Intent intent = new Intent(PageActivity.this, MainActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
		startActivity(intent);
	}

	@Override
	public void onSettings() {
		closeSlidingMenu();
		Intent intent = new Intent(PageActivity.this, SettingsActivity.class);
		startActivity(intent);

	}

	@Override
	public void onBackPressed() {
		if (closeSlidingMenu()) {
			return;
		} else if (mListShown) {
			hidePageSelection();
		} else {
			super.onBackPressed();
		}
	}

	@Override
	public void onRecent(String pid) {
		hidePageSelection();
		closeSlidingMenu();
		if (!mFullscreen) {
			setFullscreen(true);
		}
		putToHistory();
		mPageList = null;
		new LoadPagesTask(this).execute(pid);
	}

	private boolean closeSlidingMenu() {
		if (mDrawerLayout != null && mDrawerLayout.isDrawerOpen(mMenuContainer)) {
			mDrawerLayout.closeDrawer(mMenuContainer);
			return true;
		}
		return false;
	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		mDrawerToggle.syncState();
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		mDrawerToggle.onConfigurationChanged(newConfig);
	}

	@Override
	public void onOrientationLock(boolean locked) {
		if (locked) {
			Log.d(LOG_TAG, "orientation: " + getResources().getConfiguration().orientation);
			if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
				setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
			} else {
				setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
			}
		} else {
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
		}
	}

	@Override
	public void onDownload() {
		closeSlidingMenu();
		if (mPageList == null) {
			return;
		}
		final Item item = mPageList.get(mCurrentPage);
		if (mIsPdf) {
			onPdfDownload(item);
		} else {
			onImageDownload(item);
		}
	}

	private void onImageDownload(final Item item) {
		if (item.isPrivate()) {
			new MaterialDialog.Builder(this).title(R.string.dialog_download_private_title)
					.content(R.string.dialog_download_private_content).positiveText(R.string.gen_ok).build().show();
		} else {
			new MaterialDialog.Builder(this).title(R.string.dialog_download_title)
					.content(R.string.dialog_download_content).positiveText(R.string.gen_yes)
					.negativeText(R.string.gen_no).callback(new MaterialDialog.onActionButtonClickedListener() {

						@Override
						public void onPositiveButtonClicked() {
							savePageImage(item);
						}

						@Override
						public void onNegativeButtonClicked() {

						}
					}).build().show();
		}
	}
	
	private void onPdfDownload(final Item item) {
		if (item.isPrivate()) {
			new MaterialDialog.Builder(this).title(R.string.dialog_download_pdf_private_title)
					.content(R.string.dialog_download_pdf_private_content).positiveText(R.string.gen_ok).build().show();
		} else {
			new MaterialDialog.Builder(this).title(R.string.dialog_download_pdf_title)
					.content(R.string.dialog_download_pdf_content).positiveText(R.string.gen_yes)
					.negativeText(R.string.gen_no).callback(new MaterialDialog.onActionButtonClickedListener() {

						@Override
						public void onPositiveButtonClicked() {
							savePdfDocument(item);
						}

						@Override
						public void onNegativeButtonClicked() {

						}
					}).build().show();
		}
	}

	private void savePdfDocument(Item item) {
		File storagePath = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/"
				+ Constants.DOWNLOAD_PATH);
		if (!storagePath.exists()) {
			storagePath.mkdirs();
		}
		File dstFile = new File(storagePath, item.getRootTitle() + ".pdf");
		File srcFile = new File(Constants.PDF_PATH);
		try {
			FileUtils.copy(srcFile, dstFile);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void savePageImage(Item item) {
		String url = K5Api.getFullImagePath(this, item.getPid());
		String filename = item.getRootTitle() + " [" + mCurrentPage + "]";
		new savePageImageTask().execute(url, filename);
	}

	class savePageImageTask extends AsyncTask<String, Void, Boolean> {

		@Override
		protected Boolean doInBackground(String... params) {
			InputStream input = null;
			try {
				URL url = new URL(params[0]);
				input = url.openStream();

				File storagePath = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/"
						+ Constants.DOWNLOAD_PATH);
				if (!storagePath.exists()) {
					storagePath.mkdirs();
				}
				OutputStream output = new FileOutputStream(storagePath + "/" + params[1] + ".jpg");
				try {
					byte[] buffer = new byte[1024];
					int bytesRead = 0;
					while ((bytesRead = input.read(buffer, 0, buffer.length)) >= 0) {
						output.write(buffer, 0, bytesRead);
					}
				} finally {
					output.close();
				}
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				if (input != null) {
					try {
						input.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
			return true;
		}

		@Override
		protected void onPostExecute(Boolean success) {

		}
	}

	private int downloadPdf(Item item) {
		String pdf = item.getPdf();
		if (pdf == null) {
			return K5Api.STATUS_PDF_FAILED;
		}
		InputStream input = null;
		OutputStream output = null;
		HttpURLConnection connection = null;
		try {
			URL url = new URL(pdf);
			connection = (HttpURLConnection) url.openConnection();
			connection.connect();
			int responseCode = connection.getResponseCode();
			if (Constants.DEBUG_MODE) {
				Log.d(LOG_TAG, "Downloading pdf, response code: " + responseCode);
			}
			if (responseCode == HttpURLConnection.HTTP_FORBIDDEN || responseCode == HttpURLConnection.HTTP_UNAUTHORIZED) {
				return K5Api.STATUS_PDF_FORBIDDEN;
			}
			if (responseCode != HttpURLConnection.HTTP_OK) {
				return K5Api.STATUS_PDF_FAILED;
			}
			input = connection.getInputStream();
			output = new FileOutputStream(Constants.PDF_PATH);
			byte data[] = new byte[4096];
			int count;
			while ((count = input.read(data)) != -1) {
				output.write(data, 0, count);
			}
		} catch (Exception e) {
			return K5Api.STATUS_PDF_FAILED;
		} finally {
			try {
				if (output != null)
					output.close();
				if (input != null)
					input.close();
			} catch (IOException ignored) {
			}
			if (connection != null) {
				connection.disconnect();
			}
		}
		return K5Api.STATUS_PDF_OK;
	}

	@Override
	public void onPageChanged(int index) {
		int pageCount = 0;
		if (mPageList != null) {
			pageCount = mPageList.size();
		}
		mCurrentPage = index;
		clearMessages();
		mIndex.setText((mCurrentPage + 1) + "/" + pageCount);
		mSeekBar.setProgress(mCurrentPage);
	}

}
