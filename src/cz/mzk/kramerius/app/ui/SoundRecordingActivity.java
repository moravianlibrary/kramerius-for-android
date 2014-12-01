package cz.mzk.kramerius.app.ui;

import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.google.analytics.tracking.android.EasyTracker;

import cz.mzk.kramerius.app.BaseActivity;
import cz.mzk.kramerius.app.OnItemSelectedListener;
import cz.mzk.kramerius.app.R;
import cz.mzk.kramerius.app.model.Item;
import cz.mzk.kramerius.app.util.ModelUtil;

public class SoundRecordingActivity extends BaseActivity implements OnItemSelectedListener {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_sound_recording);
		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);
		getSupportActionBar().setTitle(R.string.app_name);
		getSupportActionBar().setHomeButtonEnabled(true);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		String pid = getIntent().getExtras().getString(EXTRA_PID);
		SoundRecordingFragment fragment = SoundRecordingFragment.newInstance(pid);
		fragment.setOnItemSelectedListener(this);
		FragmentTransaction ft = getFragmentManager().beginTransaction();
		ft.replace(R.id.soundrecording_fragment_container, fragment).commit();
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
	public void onItemSelected(Item item) {
		if (ModelUtil.SOUND_UNIT.equals(item.getModel())) {
			Intent intent = new Intent(SoundRecordingActivity.this, SoundUnitActivity.class);
			intent.putExtra(EXTRA_PID, item.getPid());
			startActivity(intent);
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