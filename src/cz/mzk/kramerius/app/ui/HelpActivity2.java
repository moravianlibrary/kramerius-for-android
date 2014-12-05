package cz.mzk.kramerius.app.ui;

import com.google.analytics.tracking.android.EasyTracker;

import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import cz.mzk.kramerius.app.BaseActivity;
import cz.mzk.kramerius.app.R;

public class HelpActivity2 extends BaseActivity {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_help2);
		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);
		//getSupportActionBar().setTitle(R.string.settings_title);
		getSupportActionBar().setHomeButtonEnabled(true);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
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