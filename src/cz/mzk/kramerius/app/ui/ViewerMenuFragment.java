package cz.mzk.kramerius.app.ui;

import java.util.ArrayList;
import java.util.List;

import android.app.Fragment;
import android.content.ClipData.Item;
import android.content.res.Configuration;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import cz.mzk.kramerius.app.R;
import cz.mzk.kramerius.app.adapter.ViewerMenuArrayAdapter;
import cz.mzk.kramerius.app.api.K5Api;
import cz.mzk.kramerius.app.data.KrameriusContract.HistoryEntry;
import cz.mzk.kramerius.app.model.RecentMenuItem;
import cz.mzk.kramerius.app.util.ScreenUtil;

public class ViewerMenuFragment extends Fragment implements OnClickListener {

	public static final int MENU_NONE = -1;
	public static final int MENU_HOME = 0;
	public static final int MENU_SETTINGS = 1;

	private ViewerMenuListener mCallback;

	private ViewerMenuArrayAdapter mRecentMenuAdapter;
	private ListView mListView;
	private View mSettings;
	private View mHome;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_viewer_menu, container, false);
		Configuration config = getResources().getConfiguration();
		if (config.smallestScreenWidthDp >= 720) {
			// mDevice = TABLET;
			// setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		} else {
			ScreenUtil.setInsets(getActivity(), view, false);
		}
		mSettings = view.findViewById(R.id.menu_settings);
		mSettings.setOnClickListener(this);
		mHome = view.findViewById(R.id.menu_home);
		mHome.setOnClickListener(this);
		mListView = (ListView) view.findViewById(R.id.menu_list);
		//populateMenuList();
		return view;
	}

	private void populateMenuList() {
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
	
	public void refreshRecent() {
		populateMenuList();
	}

	public void setCallback(ViewerMenuListener callback) {
		mCallback = callback;
	}

	public interface ViewerMenuListener {
		public void onHome();

		public void onSettings();

		public void onRecent(String pid);

	}

	@Override
	public void onClick(View v) {
		if (v == mSettings) {
			if (mCallback != null) {
				mCallback.onSettings();
			}
		} else if (v == mHome) {
			if (mCallback != null) {
				mCallback.onHome();
			}
		}

	}

}
