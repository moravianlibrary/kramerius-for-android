package cz.mzk.kramerius.app.ui;

import java.util.ArrayList;
import java.util.List;

import android.app.Fragment;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.widget.SwitchCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import cz.mzk.kramerius.app.R;
import cz.mzk.kramerius.app.adapter.ViewerMenuArrayAdapter;
import cz.mzk.kramerius.app.api.K5Api;
import cz.mzk.kramerius.app.data.KrameriusContract.HistoryEntry;
import cz.mzk.kramerius.app.model.RecentMenuItem;
import cz.mzk.kramerius.app.util.Analytics;
import cz.mzk.kramerius.app.view.MenuItemWidget;

public class ViewerMenuFragment extends Fragment implements OnClickListener {

	public static final int DOWNLOAD_NOTHING = 0;
	public static final int DOWNLOAD_PAGE = 1;
	public static final int DOWNLOAD_PDF = 2;

	public static final int MENU_NONE = -1;
	public static final int MENU_HOME = 0;
	public static final int MENU_SETTINGS = 1;

	private ViewerMenuListener mCallback;

	private ViewerMenuArrayAdapter mRecentMenuAdapter;
	private ListView mListView;
	private View mSettings;
	private View mHome;
	private View mHelp;
	private MenuItemWidget mDownload;
	private boolean mScreenLock = false;
	private MenuItemWidget mScreenLockView;
	
	private int mDownloadType = DOWNLOAD_NOTHING;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_viewer_menu, container, false);
		mSettings = view.findViewById(R.id.menu_settings);
		mSettings.setOnClickListener(this);
		mHome = view.findViewById(R.id.menu_home);
		mHome.setOnClickListener(this);
		mHelp = view.findViewById(R.id.menu_help);
		mHelp.setOnClickListener(this);
		mDownload = (MenuItemWidget) view.findViewById(R.id.menu_download);
		mDownload.setOnClickListener(this);
		mScreenLockView = (MenuItemWidget) view.findViewById(R.id.menu_screen_lock);
		mScreenLockView.setOnClickListener(this);
		refreshDownloadType();
		mListView = (ListView) view.findViewById(R.id.menu_list);
		populateMenuList();
		return view;
	}

	private void populateMenuList() {
		if (getActivity() == null) {
			return;
		}
		List<RecentMenuItem> list = new ArrayList<RecentMenuItem>();
		String domain = K5Api.getDomain(getActivity());

		Cursor c = getActivity().getContentResolver().query(HistoryEntry.CONTENT_URI,

		new String[] { HistoryEntry.COLUMN_TITLE, HistoryEntry.COLUMN_SUBTITLE, HistoryEntry.COLUMN_PID },
				HistoryEntry.COLUMN_DOMAIN + "=?", new String[] { domain },
				HistoryEntry.COLUMN_TIMESTAMP + " DESC LIMIT " + 10);
		while (c.moveToNext()) {
			RecentMenuItem item = new RecentMenuItem();
			item.setPid(c.getString(c.getColumnIndex(HistoryEntry.COLUMN_PID)));
			item.setTitle(c.getString(c.getColumnIndex(HistoryEntry.COLUMN_TITLE)));
			item.setSubtitle(c.getString(c.getColumnIndex(HistoryEntry.COLUMN_SUBTITLE)));
			list.add(item);
		}
		c.close();
		mRecentMenuAdapter = new ViewerMenuArrayAdapter(getActivity(), list);
		mListView.setAdapter(mRecentMenuAdapter);
		mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view, int position, long i) {
				if (mCallback != null) {
					mCallback.onRecent(mRecentMenuAdapter.getItem(position).getPid());
				}
			}
		});
	}

	public void setDownloadType(int type) {
		mDownloadType = type;
		refreshDownloadType();
	}
	
	public void refreshDownloadType() {
		if (mDownload == null) {
			return;
		} 
		switch (mDownloadType) {
		case DOWNLOAD_NOTHING:
			mDownload.setVisibility(View.GONE);
			break;
		case DOWNLOAD_PDF:
			mDownload.setVisibility(View.VISIBLE);
			mDownload.setTitle(R.string.viewer_menu_download_pdf);
			break;
		case DOWNLOAD_PAGE:
			mDownload.setVisibility(View.VISIBLE);
			mDownload.setTitle(R.string.viewer_menu_download);
			break;
		}
	}

	public void refreshRecent() {
		populateMenuList();
	}

	public void setCallback(ViewerMenuListener callback) {
		mCallback = callback;
	}

	public interface ViewerMenuListener {
		public void onHome();

		public void onHelp();
		
		public void onSettings();

		public void onOrientationLock(boolean locked);

		public void onRecent(String pid);

		public void onDownload();

	}

	@Override
	public void onClick(View v) {
		if (v == mSettings) {
			if (mCallback != null) {
				Analytics.sendEvent(getActivity(), "page_menu", "action", "Settings");
				mCallback.onSettings();
			}
		} else if (v == mHome) {
			if (mCallback != null) {
				Analytics.sendEvent(getActivity(), "page_menu", "action", "Home");
				mCallback.onHome();
			}
		} else if (v == mHelp) {
			if (mCallback != null) {
				Analytics.sendEvent(getActivity(), "page_menu", "action", "Help");
				mCallback.onHelp();
			}
		} else if (v == mScreenLockView) {
			Analytics.sendEvent(getActivity(), "page_menu", "action", "Screen Lock");
			setOrientationLock(!mScreenLock);
		} else if (v == mDownload) {
			if (mCallback != null) {
				Analytics.sendEvent(getActivity(), "page_menu", "action", "Download");
				mCallback.onDownload();
			}
		}
	}

	private void setOrientationLock(boolean lock) {
		mScreenLock = lock;
		if (mScreenLock) {
			mScreenLockView.setSelected(true);
		} else {
			mScreenLockView.setSelected(false);
		}
		if (mCallback != null) {
			mCallback.onOrientationLock(mScreenLock);
		}
	}

}
