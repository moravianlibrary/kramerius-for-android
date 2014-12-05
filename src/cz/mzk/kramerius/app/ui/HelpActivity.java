package cz.mzk.kramerius.app.ui;

import com.google.analytics.tracking.android.EasyTracker;

import android.app.FragmentTransaction;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.Toast;
import cz.mzk.kramerius.app.BaseActivity;
import cz.mzk.kramerius.app.R;
import cz.mzk.kramerius.app.ui.HelpMenuFragment.HelpMenuListener;
import cz.mzk.kramerius.app.util.Analytics;
import cz.mzk.kramerius.app.util.VersionUtils;

public class HelpActivity extends BaseActivity implements HelpMenuListener {

	private boolean mIsMenuShown;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_help);
		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);
		getSupportActionBar().setHomeButtonEnabled(true);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		showMenu();
	}

	private void showMenu() {
		getSupportActionBar().setTitle(R.string.help_title);
		HelpMenuFragment fragment = new HelpMenuFragment();
		fragment.setCallback(this);
		FragmentTransaction ft = getFragmentManager().beginTransaction();
		ft.replace(R.id.help_menu, fragment).commit();
		mIsMenuShown = true;
	}

	@Override
	public void onBackPressed() {
		if (!mIsMenuShown) {
			showMenu();
			return;
		}
		super.onBackPressed();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			if (!mIsMenuShown) {
				showMenu();
			} else {
				finish();
			}
			return true;
		default:
			return super.onOptionsItemSelected(item);
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
	public void onFragmentSelected(int viewResource, int titleResource) {
		if (isTablet()) {
			FragmentTransaction ft = getFragmentManager().beginTransaction();
			ft.replace(R.id.help_content, HelpContentFragment.newInstance(viewResource)).commit();
		} else {
			getSupportActionBar().setTitle(titleResource);
			FragmentTransaction ft = getFragmentManager().beginTransaction();
			ft.replace(R.id.help_menu, HelpContentFragment.newInstance(viewResource)).commit();
			mIsMenuShown = false;
		}

	}

	public void onFeedback() {
		Analytics.sendEvent(this, "feedback", "from_help_menu");		
		Intent send = new Intent(Intent.ACTION_SENDTO);
		String subject = getString(R.string.help_about_app_feedback_prefix) + " " + VersionUtils.getVersion(this);
		String uriText = "mailto:" + Uri.encode(getString(R.string.feedback_email)) + 
		          "?subject=" + Uri.encode(subject) + 
		          "&body=" + Uri.encode("");
		Uri uri = Uri.parse(uriText);
		send.setData(uri);
		try {
		startActivity(Intent.createChooser(send, getString(R.string.feedback_chooseEmailClient)));		
		} catch (android.content.ActivityNotFoundException ex) {
			Toast.makeText(HelpActivity.this, getString(R.string.feedback_noEmailClient), Toast.LENGTH_SHORT).show();
		}
		
	}

}