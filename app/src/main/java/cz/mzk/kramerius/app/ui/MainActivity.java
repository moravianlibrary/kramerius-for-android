package cz.mzk.kramerius.app.ui;

import android.app.Fragment;
import android.app.FragmentManager;
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
import cz.mzk.kramerius.app.ui.MainFragment.OnFeaturedListener;
import cz.mzk.kramerius.app.ui.MainMenuFragment.MainMenuListener;
import cz.mzk.kramerius.app.ui.SearchFragment.OnSearchListener;
import cz.mzk.kramerius.app.ui.UserInfoFragment.UserInfoListener;
import cz.mzk.kramerius.app.ui.VirtualCollectionsFragment.OnVirtualCollectionListener;
import cz.mzk.kramerius.app.util.Analytics;
import cz.mzk.kramerius.app.util.ModelUtil;
import cz.mzk.kramerius.app.util.PrefUtils;
import cz.mzk.kramerius.app.util.VersionUtils;

public class MainActivity extends BaseActivity implements MainMenuListener, LoginListener, UserInfoListener,
		OnFeaturedListener, OnItemSelectedListener, OnSearchListener, OnVirtualCollectionListener {

	
	
	public static final String TAG = MainActivity.class.getName();
	public static final String CURRENT_FRAGMENT_KEY = "key_current_fragment";
	
	
	private static final int FRAGMENT_HOME = 0;
	private static final int FRAGMENT_SEARCH = 1;
	private static final int FRAGMENT_COLLECTIOS = 2;
	private static final int FRAGMENT_RECENT = 3;
	private static final int FRAGMENT_FEATURED_NEWEST = 4;
	private static final int FRAGMENT_FEATURED_CUSTOM = 5;
	private static final int FRAGMENT_FEATURED_MOST_DESIRABLE = 6;

	private MainMenuFragment mMenuFragment;
	private int mSelectedFragment;
	private FrameLayout mMenuContainer;
	private DrawerLayout mDrawerLayout;
	private ActionBarDrawerToggle mDrawerToggle;
	private int mDrawerTitle;
	private String mTitle;
	private MainFragment mMainFragment;

	private Toolbar mToolbar;
	private boolean mLastPublicOnly;
	private Spinner mSearchSpinner;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		mTitle = getString(R.string.main_title);
		mDrawerTitle = R.string.main_menu_title;

		
		
		
		
		FragmentManager fm = getFragmentManager();
		FragmentTransaction transaction = fm.beginTransaction();
		if (fm.findFragmentByTag("menu_fragment") == null) {
			mMenuFragment = new MainMenuFragment();
			//mMenuFragment.setCallback(this);
			//transaction.replace(R.id.main_menu, mMenuFragment, "menu_fragment").commit();
		} else {
			mMenuFragment = (MainMenuFragment) fm.findFragmentByTag("menu_fragment");
		    //transaction.detach(mMenuFragment);
		    //transaction.replace(R.id.main_menu, mMenuFragment, "menu_fragment").commit();
		    //transaction.attach(mFragment);
		}
		mMenuFragment.setCallback(this);
		transaction.replace(R.id.main_menu, mMenuFragment, "menu_fragment");
		transaction.commit();
		
		
		
		/*
		mMenuFragment = new MainMenuFragment();
		mMenuFragment.setCallback(this);
		getFragmentManager().beginTransaction().replace(R.id.main_menu, mMenuFragment, "menu_fragment").commit();
		*/


		mToolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(mToolbar);
		getSupportActionBar().setTitle(R.string.main_title);

		if (getDevice() == TABLET && isLandscape()) {
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

			mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, mToolbar, R.string.drawer_open, R.string.drawer_close) {

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
		mLastPublicOnly = PrefUtils.isPublicOnly(this);

		mSearchSpinner = (Spinner) findViewById(R.id.spinner);
		//mSearchSpinner = new Spinner(this);
		//mToolbar.addView(mSearchSpinner);
		
		
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
		
		if (savedInstanceState != null) {
             resolveLastState(savedInstanceState.getInt(CURRENT_FRAGMENT_KEY, FRAGMENT_HOME));
        } else {        
        	onHome();
        }
	}

	
	private void resolveLastState(int state) {
		
		switch (state) {
		case FRAGMENT_HOME:
			//mMenuFragment.setActiveMenuItem(MainMenuFragment.MENU_HOME);
			onHome();			
			break;
		case FRAGMENT_COLLECTIOS:
			//mMenuFragment.setActiveMenuItem(MainMenuFragment.MENU_VIRTUAL_COLLECTION);
			onVirtualCollections();
			break;
		case FRAGMENT_SEARCH:
		//	mMenuFragment.setActiveMenuItem(MainMenuFragment.MENU_SEARCH);
			onSearch();
			break;
		case FRAGMENT_RECENT:
		//	mMenuFragment.setActiveMenuItem(MainMenuFragment.MENU_RECENT);
			onRecent();
			break;
		case FRAGMENT_FEATURED_CUSTOM:
		//	mMenuFragment.setActiveMenuItem(MainMenuFragment.MENU_NONE);
			onFeatured(K5Api.FEED_CUSTOM);
			break;
		case FRAGMENT_FEATURED_NEWEST:
			//mMenuFragment.setActiveMenuItem(MainMenuFragment.MENU_NONE);
			onFeatured(K5Api.FEED_NEWEST);
			break;
		case FRAGMENT_FEATURED_MOST_DESIRABLE:
			//mMenuFragment.setActiveMenuItem(MainMenuFragment.MENU_NONE);
			onFeatured(K5Api.FEED_MOST_DESIRABLE);
			break;
		default:
		//	mMenuFragment.setActiveMenuItem(MainMenuFragment.MENU_HOME);
			onHome();
			break;
		}
	}
	
	@Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(CURRENT_FRAGMENT_KEY, mSelectedFragment);
    }	
	
	
	
	
	@Override
	public void onHome() {
		boolean publicOnly = PrefUtils.isPublicOnly(this);
		if (mMainFragment == null || publicOnly != mLastPublicOnly) {
			mMainFragment = new MainFragment();
		}
		mLastPublicOnly = publicOnly;
		mMainFragment.setCallback(this);
		mMainFragment.setOnItemSelectedListener(this);
		changeFragment(mMainFragment, FRAGMENT_HOME, R.string.main_title);
	}
	
	@Override
	protected void onResume() {	
		super.onResume();
		if(mSelectedFragment == FRAGMENT_HOME && PrefUtils.isPublicOnly(this) != mLastPublicOnly) {
			onHome();
		}
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
		//changeFragment(new LoginFragment(), FRAGMENT_LOGIN, R.string.login_title);
	}

	private void showUserInfo() {
		//changeFragment(new UserInfoFragment(), FRAGMENT_USER_INFO, R.string.user_info_title);
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
		//changeFragment(new SettingsFragment(), FRAGMENT_SETTINGS, R.string.settings_title);
		closeSlidingMenu();
		Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
		startActivity(intent);
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
			if(mSelectedFragment != type) {
				mSearchSpinner.setSelection(0);
			}
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
			changeFragment(fragment, FRAGMENT_FEATURED_MOST_DESIRABLE, R.string.most_desirable_title);
			break;
		case K5Api.FEED_NEWEST:
			changeFragment(fragment, FRAGMENT_FEATURED_NEWEST, R.string.newest_title);
			break;
		case K5Api.FEED_CUSTOM:
			changeFragment(fragment, FRAGMENT_FEATURED_CUSTOM, R.string.selected_title);
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
		closeSlidingMenu();
		Intent intent = new Intent(MainActivity.this, HelpActivity.class);
		startActivity(intent);		
	}

	@Override
	public void onAbout() {
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
		Analytics.sendEvent(this, "collection", "selected", vc.getTitle());
		Intent intent = new Intent(MainActivity.this, VirtualCollectionActivity.class);
		intent.putExtra(EXTRA_PID, vc.getPid());
		intent.putExtra(EXTRA_TITLE, vc.getTitle());		
		startActivity(intent);
	}


	@Override
	public void onRecent() {
		HistoryFragment fragment = new HistoryFragment();
		fragment.setOnItemSelectedListener(this);
		changeFragment(fragment, FRAGMENT_RECENT, R.string.recent_title);
	}

}
