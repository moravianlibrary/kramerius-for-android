package cz.mzk.kramerius.app.ui;

import java.io.InputStream;
import java.net.URL;
import java.util.List;

import org.apache.http.protocol.HTTP;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;

import com.google.analytics.tracking.android.EasyTracker;

import cz.mzk.kramerius.app.BaseActivity;
import cz.mzk.kramerius.app.OnItemSelectedListener;
import cz.mzk.kramerius.app.R;
import cz.mzk.kramerius.app.api.K5Api;
import cz.mzk.kramerius.app.api.K5Connector;
import cz.mzk.kramerius.app.model.Item;
import cz.mzk.kramerius.app.ui.LoginFragment.LoginListener;
import cz.mzk.kramerius.app.ui.MainFeaturedFragment.OnFeaturedListener;
import cz.mzk.kramerius.app.ui.MainMenuFragment.MainMenuListener;
import cz.mzk.kramerius.app.ui.UserInfoFragment.UserInfoListener;
import cz.mzk.kramerius.app.util.ModelUtil;

public class MainActivity extends BaseActivity implements MainMenuListener, LoginListener, UserInfoListener,
		OnFeaturedListener, OnItemSelectedListener {

	public static final String TAG = MainActivity.class.getName();

	private MainMenuFragment mMenuFragment;
	private boolean mMainFragmentActive;
	private FrameLayout mMenuContainer;
	private DrawerLayout mDrawerLayout;
	private ActionBarDrawerToggle mDrawerToggle;
	private int mDrawerTitle;
	private int mTitle;
	private MainFeaturedFragment mMainFragment;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (Intent.ACTION_VIEW.equals(getIntent().getAction())) {
			String pid = null;
			Uri data = getIntent().getData();
			if (data != null) {
				List<String> pathSegments = data.getPathSegments();
				if (pathSegments.size() >= 3 && "handle".equals(pathSegments.get(1))) {
					pid = pathSegments.get(2);
				} else {
					pid = data.getQueryParameter("pid");
				}
			}
			if (pid != null) {
				goToDocument(pid);
				return;
			}
		}
		setContentView(R.layout.activity_main);

		mTitle = R.string.main_title;
		mDrawerTitle = R.string.main_menu_title;
		getActionBar().setHomeButtonEnabled(true);

		mMenuFragment = new MainMenuFragment();
		mMenuFragment.setCallback(this);
		getFragmentManager().beginTransaction().replace(R.id.main_menu, mMenuFragment).commit();

		if (getDevice() == TABLET) {
			getActionBar().setDisplayHomeAsUpEnabled(false);
		} else {
			getActionBar().setDisplayHomeAsUpEnabled(true);
			mMenuContainer = (FrameLayout) findViewById(R.id.main_menu);
			mDrawerLayout = (DrawerLayout) findViewById(R.id.main_drawer_layout);
			mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.drawable.ic_navigation_drawer,
					R.string.drawer_open, R.string.drawer_close) {

				public void onDrawerClosed(View view) {
					super.onDrawerClosed(view);
					getActionBar().setTitle(mTitle);
					invalidateOptionsMenu();
				}

				public void onDrawerOpened(View drawerView) {
					super.onDrawerOpened(drawerView);
					getActionBar().setTitle(mDrawerTitle);
					invalidateOptionsMenu();
				}
			};
			mDrawerLayout.setDrawerListener(mDrawerToggle);
			mDrawerLayout.setDrawerListener(mDrawerToggle);
		}

		onHome();
	}

	@Override
	public void onHome() {
		if (mMainFragment == null) {
			mMainFragment = new MainFeaturedFragment();
		}
		mMainFragment.setCallback(this);
		mMainFragment.setOnItemSelectedListener(this);
		changeFragment(mMainFragment, true, R.string.main_title);
	}

	private void refreshTitle(int resId) {
		mTitle = resId;
		getActionBar().setTitle(resId);
	}

	@Override
	public void onSelectDomain() {
		// final Context context = this;
		// new AlertDialog.Builder(this).setTitle("Vyberte doménu")
		// .setItems(K5Api.KRAMERIUS_DOMAINS, new
		// DialogInterface.OnClickListener() {
		//
		// @Override
		// public void onClick(DialogInterface dialog, int which) {
		// PreferenceManager.getDefaultSharedPreferences(context).edit()
		// .putString("domain", K5Api.KRAMERIUS_DOMAINS[which]).commit();
		// K5Connector.getInstance().restart();
		// mMenuFragment.onDomainChanged();
		// closeSlidingMenu();
		// }
		// }).create().show();
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
		mMenuFragment.refreshUser();
		showUserInfo();
	}

	private void showLogin() {
		changeFragment(new LoginFragment(), false, R.string.login_title);
	}

	private void showUserInfo() {
		changeFragment(new UserInfoFragment(), false, R.string.user_info_title);
	}

	@Override
	public void onLogOut() {
		K5Api.logOut(this);
		mMenuFragment.refreshUser();
		showLogin();
	}

	@Override
	public void onVirtualCollections() {
		changeFragment(VirtualCollectionsFragment.newInstance(), false, R.string.virtual_collections_title);
	}

	@Override
	public void onSettings() {
		changeFragment(new SettingsFragment(), false, R.string.settings_title);
	}

	private void changeFragment(Fragment fragment, boolean main, int titleRes) {
		FragmentTransaction ft = getFragmentManager().beginTransaction();
		if (isTablet()) {
			ft.setCustomAnimations(R.animator.slide_in_left, R.animator.slide_out_left);
		}
		ft.replace(R.id.main_content, fragment).commit();
		mMainFragmentActive = main;
		refreshTitle(titleRes);
		closeSlidingMenu();
	}

	@Override
	public void onFeatured(int type) {
		FeaturedFragment fragment = FeaturedFragment.newInstance(type);
		fragment.setOnItemSelectedListener(this);
		mMenuFragment.setActiveMenuItem(MainMenuFragment.MENU_NONE);
		switch (type) {
		case K5Api.FEED_MOST_DESIRABLE:
			changeFragment(fragment, false, R.string.most_desirable_title);
			break;
		case K5Api.FEED_NEWEST:
			changeFragment(fragment, false, R.string.newest_title);
			break;
		case K5Api.FEED_SELECTED:
			changeFragment(fragment, false, R.string.selected_title);
			break;
		}
	}

	@Override
	public void onBackPressed() {
		if (closeSlidingMenu()) {
			return;
		}
		if (!mMainFragmentActive) {
			onHome();
			mMenuFragment.setActiveMenuItem(MainMenuFragment.MENU_HOME);
			return;
		}
		super.onBackPressed();
	}

	@Override
	public void onItemSelected(Item item) {
		Intent intent = null;
		if (ModelUtil.SOUND_RECORDING.equals(item.getModel())) {
			intent = new Intent(MainActivity.this, SoundRecordingActivity.class);
		} else if (ModelUtil.SOUND_UNIT.equals(item.getModel())) {
			intent = new Intent(MainActivity.this, SoundUnitActivity.class);
		} else if (ModelUtil.PERIODICAL.equals(item.getModel())) {
			intent = new Intent(MainActivity.this, PeriodicalActivity.class);
		} else if (ModelUtil.PERIODICAL_VOLUME.equals(item.getModel())) {
			intent = new Intent(MainActivity.this, PeriodicalActivity.class);
		} else if (item.getPdf() != null) {
			intent = new Intent(MainActivity.this, PdfViewerActivity.class);
		} else {
			intent = new Intent(MainActivity.this, PageActivity.class);
		}
		intent.putExtra(EXTRA_PID, item.getPid());
		startActivity(intent);
	}

	private void goToDocument(String pid) {
		new ResolveReferencedDocumentTask().execute(pid);
	}

	class ResolveReferencedDocumentTask extends AsyncTask<String, Void, Item> {

		@Override
		protected void onPreExecute() {

		}

		@Override
		protected Item doInBackground(String... params) {
			return K5Connector.getInstance().getItem(MainActivity.this, params[0]);
		}

		@Override
		protected void onPostExecute(Item item) {
			if (MainActivity.this == null || item == null) {
				return;
			}
			onItemSelected(item);
			finish();
		}

	}

	@Override
	public void onHelp() {
		changeFragment(new HelpFragment(), false, R.string.help_title);
	}

	@Override
	public void onAbout() {
		changeFragment(new AboutFragment(), false, R.string.about_title);
	}

	@Override
	public void onSearch() {
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
	
	

}