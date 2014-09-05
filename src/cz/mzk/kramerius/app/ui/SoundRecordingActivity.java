package cz.mzk.kramerius.app.ui;

import java.util.List;

import com.google.analytics.tracking.android.EasyTracker;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import cz.mzk.kramerius.app.BaseActivity;
import cz.mzk.kramerius.app.R;
import cz.mzk.kramerius.app.adapter.SoundUnitArrayAdapter;
import cz.mzk.kramerius.app.api.K5Connector;
import cz.mzk.kramerius.app.model.Item;
import cz.mzk.kramerius.app.model.ParentChildrenPair;
import cz.mzk.kramerius.app.util.ModelUtil;
import cz.mzk.kramerius.app.util.TextUtil;

public class SoundRecordingActivity extends BaseActivity {

	public static final String TAG = SoundRecordingActivity.class.getName();

	private ListView mList;

	private SoundUnitArrayAdapter mAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_sound_recording);

		
		
		getActionBar().setDisplayUseLogoEnabled(false);
		getActionBar().setDisplayShowHomeEnabled(false);
		getActionBar().setDisplayHomeAsUpEnabled(true);
		getActionBar().setTitle("K5 - Digitální knihovna");

		mList = (ListView) findViewById(R.id.soundrecording_list);

		mList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view, int position, long i) {
				if (mAdapter == null) {
					return;
				}
				onSoundUnitSelected(position);
			}
		});
		String pid = getIntent().getStringExtra(EXTRA_PID);
		new GetSoundUnitsTask(this).execute(pid);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    switch(item.getItemId()) {
	    case android.R.id.home:
	        finish();
	    	return true;
	    default:
	        return super.onOptionsItemSelected(item);
	    }
	}	

	class GetSoundUnitsTask extends AsyncTask<String, Void, ParentChildrenPair> {

		private Context tContext;

		public GetSoundUnitsTask(Context context) {
			tContext = context;
		}

		@Override
		protected void onPreExecute() {

		}

		@Override
		protected ParentChildrenPair doInBackground(String... params) {
			Item item = K5Connector.getInstance().getItem(tContext, params[0]);
			if(item == null) {
				return null;
			}
			return new ParentChildrenPair(item, K5Connector.getInstance().getChildren(tContext, item.getPid()));
		}

		@Override
		protected void onPostExecute(ParentChildrenPair result) {
			if (tContext == null || result.getParent() == null) {
				return;
			}
			Item parent = result.getParent();
			List<Item> children = result.getChildren();
			getActionBar().setTitle(TextUtil.shortenforActionBar(parent.getTitle()));
			if (children == null) {
				return;
			}
			mAdapter = new SoundUnitArrayAdapter(tContext, children);
			mList.setAdapter(mAdapter);
		}
	}

	private void onSoundUnitSelected(int position) {
		if (mAdapter == null || mAdapter.getCount() < position + 1) {
			return;
		}
		Item item = mAdapter.getItem(position);
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
