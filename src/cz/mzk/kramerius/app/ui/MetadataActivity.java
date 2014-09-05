package cz.mzk.kramerius.app.ui;

import com.google.analytics.tracking.android.EasyTracker;

import android.os.Bundle;
import android.view.MenuItem;
import cz.mzk.kramerius.app.BaseActivity;
import cz.mzk.kramerius.app.R;
import cz.mzk.kramerius.app.R.id;

public class MetadataActivity extends BaseActivity {

	public static final String TAG = MetadataActivity.class.getName();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_metadata);

		getActionBar().setDisplayUseLogoEnabled(false);
		getActionBar().setDisplayShowHomeEnabled(false);
		getActionBar().setDisplayHomeAsUpEnabled(true);


		MetadataFragment metadataFragment = (MetadataFragment) getFragmentManager().findFragmentById(
				id.metadata_metadataFragment);
		String pid = getIntent().getStringExtra(EXTRA_PID);
		metadataFragment.assignPid(pid);
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
