package cz.mzk.kramerius.app.ui;

import java.util.ArrayList;
import java.util.List;

import android.app.Fragment;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import cz.mzk.kramerius.app.MenuListItem;
import cz.mzk.kramerius.app.R;
import cz.mzk.kramerius.app.adapter.MainMenuArrayAdapter;
import cz.mzk.kramerius.app.api.K5Api;
import cz.mzk.kramerius.app.util.ScreenUtil;

public class MainMenuFragment extends Fragment implements OnClickListener {

	public static final int MENU_NONE = -1;
	public static final int MENU_HOME = 0;
	public static final int MENU_VIRTUAL_COLLECTION = 1;
	public static final int MENU_SEARCH = 2;
	public static final int MENU_ABOUT = 3;
	public static final int MENU_HELP = 4;
	public static final int MENU_FEEDBACK = 5;
	public static final int MENU_SETTINGS = 6;

	private TextView mUser;
	private MainMenuListener mCallback;
	private MainMenuArrayAdapter mMenuAdapter;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_main_menu, container, false);
		Configuration config = getResources().getConfiguration();
		if (config.smallestScreenWidthDp >= 720) {
			// mDevice = TABLET;
			// setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		} else {
			ScreenUtil.setInsets(getActivity(), view);
		}
		mUser = (TextView) view.findViewById(R.id.menu_user);
		mUser.setOnClickListener(this);

		refreshUser();
		ListView listView = (ListView) view.findViewById(R.id.menu_item_list);
		populateMenuList(listView);
		return view;
	}

	private void populateMenuList(ListView listView) {
		List<MenuListItem> list = new ArrayList<MenuListItem>();
		list.add(new MenuListItem(getString(R.string.main_menu_home), R.drawable.ic_home_grey, R.drawable.ic_home_green));
		list.add(new MenuListItem(getString(R.string.main_menu_virtual_colections), R.drawable.ic_group_grey,
				R.drawable.ic_group_green));
		list.add(new MenuListItem(getString(R.string.main_menu_search), R.drawable.ic_search_grey,
				R.drawable.ic_search_green));
		list.add(new MenuListItem(getString(R.string.main_menu_about), R.drawable.ic_about_grey,
				R.drawable.ic_about_green));
		list.add(new MenuListItem(getString(R.string.main_menu_help), R.drawable.ic_help_grey, R.drawable.ic_help_green));
		list.add(new MenuListItem(getString(R.string.main_menu_feedback), R.drawable.ic_attach_grey, R.drawable.ic_attach_green));
		list.add(new MenuListItem(getString(R.string.main_menu_settings), R.drawable.ic_settings_grey,
				R.drawable.ic_settings_green));

		mMenuAdapter = new MainMenuArrayAdapter(getActivity(), list);
		listView.setAdapter(mMenuAdapter);
		listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view, int position, long i) {
				onMenuItemSelected(position);
			}
		});
	}

	public void onMenuItemSelected(int index) {
		if (mCallback == null) {
			return;
		}		
		switch (index) {
		case MENU_HOME:
			mMenuAdapter.setSelection(index);
			mCallback.onHome();
			break;
		case MENU_VIRTUAL_COLLECTION:
			mMenuAdapter.setSelection(index);
			mCallback.onVirtualCollections();
			break;
		case MENU_SEARCH:
			mMenuAdapter.setSelection(index);
			mCallback.onSearch();
			break;
		case MENU_ABOUT:
			mMenuAdapter.setSelection(index);
			mCallback.onAbout();
			break;
		case MENU_HELP:
			mMenuAdapter.setSelection(index);
			mCallback.onHelp();
			break;
		case MENU_SETTINGS:
			mMenuAdapter.setSelection(index);
			mCallback.onSettings();
			break;
		case MENU_FEEDBACK:
			mCallback.onFeedback();
			break;			
		}
	}

	public void setActiveMenuItem(int index) {
		if (mMenuAdapter == null) {
			return;
		}
		mMenuAdapter.setSelection(index);
	}

	public void setCallback(MainMenuListener callback) {
		mCallback = callback;
	}

	public interface MainMenuListener {
		public void onSelectDomain();

		public void onLogin();

		public void onHome();

		public void onSettings();

		public void onVirtualCollections();

		public void onSearch();

		public void onHelp();
		
		public void onFeedback();

		public void onAbout();

	}

	public void refreshUser() {
		String user = K5Api.getUser(getActivity());
		if (user == null) {
			mUser.setText("Nepřihlášen");
		} else {
			mUser.setText(user);
		}
	}

	@Override
	public void onClick(View v) {
		if (mCallback == null) {
			return;
		}

		if (v == mUser) {
			setActiveMenuItem(MENU_NONE);
			mCallback.onLogin();
		}
	}

}
