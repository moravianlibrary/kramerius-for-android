package cz.mzk.kramerius.app.ui;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.analytics.tracking.android.EasyTracker;

import cz.mzk.kramerius.app.BaseActivity;
import cz.mzk.kramerius.app.OnItemSelectedListener;
import cz.mzk.kramerius.app.R;
import cz.mzk.kramerius.app.api.K5Api;
import cz.mzk.kramerius.app.model.Item;
import cz.mzk.kramerius.app.ui.LoginFragment.LoginListener;
import cz.mzk.kramerius.app.ui.MainFeaturedFragment.OnFeaturedListener;
import cz.mzk.kramerius.app.ui.MainMenuFragment.MainMenuListener;
import cz.mzk.kramerius.app.ui.SearchFragment.OnSearchListener;
import cz.mzk.kramerius.app.ui.UserInfoFragment.UserInfoListener;
import cz.mzk.kramerius.app.ui.VirtualCollectionsFragment.OnVirtualCollectionListener;
import cz.mzk.kramerius.app.util.Analytics;
import cz.mzk.kramerius.app.util.ModelUtil;
import cz.mzk.kramerius.app.util.PrefUtils;

public class MainActivity extends BaseActivity implements MainMenuListener, LoginListener, UserInfoListener,
		OnFeaturedListener, OnItemSelectedListener, OnSearchListener, OnVirtualCollectionListener {

	public static final String TAG = MainActivity.class.getName();

	private static final int FRAGMENT_HOME = 0;
	private static final int FRAGMENT_SEARCH = 1;
	private static final int FRAGMENT_COLLECTIOS = 2;
	private static final int FRAGMENT_RECENT = 3;
	private static final int FRAGMENT_HELP = 4;
	private static final int FRAGMENT_SETTINGS = 5;
	private static final int FRAGMENT_FEATURED = 6;
	private static final int FRAGMENT_LOGIN = 7;
	private static final int FRAGMENT_USER_INFO = 8;

	private MainMenuFragment mMenuFragment;
	private int mSelectedFragment;
	private FrameLayout mMenuContainer;
	private DrawerLayout mDrawerLayout;
	private ActionBarDrawerToggle mDrawerToggle;
	private int mDrawerTitle;
	private String mTitle;
	private MainFeaturedFragment mMainFragment;

	private Toolbar mToolbar;
	private boolean mLastPublicOnly;
	private Spinner mSearchSpinner;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		mTitle = getString(R.string.main_title);
		mDrawerTitle = R.string.main_menu_title;

		mMenuFragment = new MainMenuFragment();
		mMenuFragment.setCallback(this);
		getFragmentManager().beginTransaction().replace(R.id.main_menu, mMenuFragment).commit();

		mToolbar = (Toolbar) findViewById(R.id.main_toolbar);
		setSupportActionBar(mToolbar);
		getSupportActionBar().setTitle(R.string.main_title);

		if (getDevice() == TABLET) {
			//
		} else {
			mMenuContainer = (FrameLayout) findViewById(R.id.main_menu);
			mDrawerLayout = (DrawerLayout) findViewById(R.id.main_drawer_layout);

			mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View view) {

					if (mDrawerLayout.isDrawerOpen(Gravity.START)) {
						mDrawerLayout.closeDrawer(Gravity.START);

					} else {
						mDrawerLayout.openDrawer(Gravity.START);
					}
				}
			});

			mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.string.drawer_open, R.string.drawer_close) {

				public void onDrawerClosed(View view) {
					getSupportActionBar().setTitle(mTitle);
					if (mSelectedFragment == FRAGMENT_SEARCH) {
						mSearchSpinner.setVisibility(View.VISIBLE);
					}
					supportInvalidateOptionsMenu();
				}

				public void onDrawerOpened(View drawerView) {
					getSupportActionBar().setTitle(mDrawerTitle);
					if (mSelectedFragment == FRAGMENT_SEARCH) {
						mSearchSpinner.setVisibility(View.GONE);
					}
					supportInvalidateOptionsMenu();

				}
			};

			mDrawerLayout.setDrawerListener(mDrawerToggle);

		}

		mSearchSpinner = (Spinner) findViewById(R.id.search_spinner);

		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(mToolbar.getContext(),
				R.array.search_type_entries, R.layout.support_simple_spinner_dropdown_item);
		adapter.setDropDownViewResource(R.layout.item_spinner_toolbar);

		mSearchSpinner.setAdapter(adapter);
		mSearchSpinner.setVisibility(View.GONE);

		mSearchSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
				if (mSelectedFragment == FRAGMENT_SEARCH) {
					int type = SearchFragment.TYPE_BASIC;
					if (position == 1) {
						type = SearchFragment.TYPE_FULLTEXT;
					}
					SearchFragment fragment = SearchFragment.getInstance(type);
					fragment.setOnSearchListener(MainActivity.this);
					changeFragment(fragment, FRAGMENT_SEARCH, R.string.search_title);
				}
			}

			@Override
			public void onNothingSelected(AdapterView<?> parentView) {
			}
		});

		onHome();
	}

	@Override
	public void onHome() {
		boolean publicOnly = PrefUtils.isPublicOnly(this);
		if (mMainFragment == null || publicOnly != mLastPublicOnly) {
			mMainFragment = new MainFeaturedFragment();
		}
		mLastPublicOnly = publicOnly;
		mMainFragment.setCallback(this);
		mMainFragment.setOnItemSelectedListener(this);
		changeFragment(mMainFragment, FRAGMENT_HOME, R.string.main_title);
	}

	private void refreshTitle(String title) {
		mTitle = title;
		mToolbar.setTitle(title);
	}

	private boolean closeSlidingMenu() {
		if (mDrawerLayout != null && mDrawerLayout.isDrawerOpen(mMenuContainer)) {
			mDrawerLayout.closeDrawer(mMenuContainer);
			return true;
		}
		return false;
	}

	@Override
	public void onLogin() {
		if (K5Api.isLoggedIn(this)) {
			showUserInfo();
		} else {
			showLogin();
		}
	}

	@Override
	public void onLoginSuccess() {
		showUserInfo();
	}

	private void showLogin() {
		changeFragment(new LoginFragment(), FRAGMENT_LOGIN, R.string.login_title);
	}

	private void showUserInfo() {
		changeFragment(new UserInfoFragment(), FRAGMENT_USER_INFO, R.string.user_info_title);
	}

	@Override
	public void onLogOut() {
		K5Api.logOut(this);
		showLogin();
	}

	@Override
	public void onVirtualCollections() {
		VirtualCollectionsFragment fragment = VirtualCollectionsFragment.newInstance();
		fragment.setOnVirtualCollectionListener(this);
		changeFragment(fragment, FRAGMENT_COLLECTIOS, R.string.virtual_collections_title);
	}

	@Override
	public void onSettings() {
		changeFragment(new SettingsFragment(), FRAGMENT_SETTINGS, R.string.settings_title);
	}

	private void changeFragment(Fragment fragment, int type, int titleRes) {
		changeFragment(fragment, type, getString(titleRes));
	}

	private void changeFragment(Fragment fragment, int type, String title) {
		FragmentTransaction ft = getFragmentManager().beginTransaction();
		if (isTablet()) {
			ft.setCustomAnimations(R.animator.slide_in_left, R.animator.slide_out_left);
		}
		ft.replace(R.id.main_content, fragment).commit();

		if (type == FRAGMENT_SEARCH) {
			mSearchSpinner.setVisibility(View.VISIBLE);
			refreshTitle("");
		} else {
			mSearchSpinner.setVisibility(View.GONE);
			refreshTitle(title);
		}
		mSelectedFragment = type;
		closeSlidingMenu();
	}

	@Override
	public void onFeatured(int type) {
		FeaturedFragment fragment = FeaturedFragment.newInstance(type);
		fragment.setOnItemSelectedListener(this);
		mMenuFragment.setActiveMenuItem(MainMenuFragment.MENU_NONE);
		switch (type) {
		case K5Api.FEED_MOST_DESIRABLE:
			changeFragment(fragment, FRAGMENT_FEATURED, R.string.most_desirable_title);
			break;
		case K5Api.FEED_NEWEST:
			changeFragment(fragment, FRAGMENT_FEATURED, R.string.newest_title);
			break;
		case K5Api.FEED_CUSTOM:
			changeFragment(fragment, FRAGMENT_FEATURED, R.string.selected_title);
			break;
		}
	}

	@Override
	public void onBackPressed() {
		if (closeSlidingMenu()) {
			return;
		}
		if (mSelectedFragment != FRAGMENT_HOME) {
			onHome();
			mMenuFragment.setActiveMenuItem(MainMenuFragment.MENU_HOME);
			return;
		}
		super.onBackPressed();
	}

	@Override
	public void onItemSelected(Item item) {
		ModelUtil.startActivityByModel(this, item);
	}

	@Override
	public void onHelp() {
		changeFragment(new HelpFragment(), FRAGMENT_HELP, R.string.help_title);
	}

	@Override
	public void onAbout() {
		changeFragment(new AboutFragment(), FRAGMENT_HELP, R.string.about_title);
	}

	@Override
	public void onSearch() {
		SearchFragment fragment = SearchFragment.getInstance(SearchFragment.TYPE_BASIC);
		fragment.setOnSearchListener(this);
		changeFragment(fragment, FRAGMENT_SEARCH, R.string.search_title);
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		if (mDrawerToggle != null) {
			mDrawerToggle.syncState();
		}
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		if (mDrawerToggle != null) {
			mDrawerToggle.onConfigurationChanged(newConfig);
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (mDrawerToggle != null && mDrawerToggle.onOptionsItemSelected(item)) {
			return true;
		}
		return super.onOptionsItemSelected(item);
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
	public void onSearchQuery(String query) {
		Intent intent = new Intent(MainActivity.this, SearchResultActivity.class);
		intent.putExtra(SearchResultActivity.EXTRA_QUERY, query);
		startActivity(intent);
	}

	@Override
	public void onVirtualCollectionSelected(Item vc) {
		Intent intent = new Intent(MainActivity.this, VirtualCollectionActivity.class);
		intent.putExtra(EXTRA_PID, vc.getPid());
		intent.putExtra(EXTRA_TITLE, vc.getTitle());
		startActivity(intent);
	}

	@Override
	public void onFeedback() {
		closeSlidingMenu();
		Analytics.sendEvent(this, "feedback", "from_menu");
		Intent i = new Intent(Intent.ACTION_SEND);
		i.setType("message/rfc822");
		i.putExtra(Intent.EXTRA_EMAIL, new String[] { getString(R.string.feedback_email) });
		String subject = getString(R.string.about_app_feedback_prefix) + " " + getString(R.string.about_app_version);
		i.putExtra(Intent.EXTRA_SUBJECT, subject);
		i.putExtra(Intent.EXTRA_TEXT, "");
		try {
			startActivity(Intent.createChooser(i, getString(R.string.feedback_chooseEmailClient)));
		} catch (android.content.ActivityNotFoundException ex) {
			Toast.makeText(MainActivity.this, getString(R.string.feedback_noEmailClient), Toast.LENGTH_SHORT).show();
		}
	}

	@Override
	public void onRecent() {
		HistoryFragment fragment = new HistoryFragment();
		fragment.setOnItemSelectedListener(this);
		changeFragment(fragment, FRAGMENT_RECENT, R.string.recent_title);
	}

}
