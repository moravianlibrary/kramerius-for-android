package cz.mzk.kramerius.app.ui;

import java.util.List;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.google.analytics.tracking.android.EasyTracker;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

import cz.mzk.kramerius.app.BaseActivity;
import cz.mzk.kramerius.app.R;
import cz.mzk.kramerius.app.adapter.TrackArrayAdapter;
import cz.mzk.kramerius.app.api.K5Api;
import cz.mzk.kramerius.app.api.K5ConnectorFactory;
import cz.mzk.kramerius.app.model.Item;
import cz.mzk.kramerius.app.ui.PlayerFragment.PlayerListener;
import cz.mzk.kramerius.app.util.TextUtil;
import cz.mzk.kramerius.app.util.VersionUtils;

public class SoundUnitActivity extends BaseActivity implements PlayerListener {

	public static final String LOG_TAG = SoundUnitActivity.class.getSimpleName();

	private PlayerFragment mPlayerFragment;
	private ListView mList;
	private ImageView mSoundUnitImage;
	private TextView mSoundRecordingTitle;
	private TextView mSoundUnitTitle;
	private TextView mSoundUnitInfo;

	private TrackArrayAdapter mAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_sound_unit);

		ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(getApplicationContext()).build();
		ImageLoader.getInstance().init(config);

		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);
		getSupportActionBar().setHomeButtonEnabled(true);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		mList = (ListView) findViewById(R.id.soundunit_list);
		mSoundUnitImage = (ImageView) findViewById(R.id.soundunit_image);
		mSoundRecordingTitle = (TextView) findViewById(R.id.soundunit_recordingTitle);
		mSoundUnitTitle = (TextView) findViewById(R.id.soundunit_unitTitle);
		mSoundUnitInfo = (TextView) findViewById(R.id.soundunit_info);

		mList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view, int position, long i) {
				if (mAdapter == null) {
					return;
				}
				onTrackSelected(position, true);
			}
		});

		mPlayerFragment = (PlayerFragment) getFragmentManager().findFragmentById(R.id.soundunit_playerFragment);
		mPlayerFragment.setCallback(this);
		String pid = getIntent().getStringExtra(EXTRA_PID);

		new GetTracksTask(this).execute(pid);
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

	class GetTracksTask extends AsyncTask<String, Void, ParentChildrenPair> {

		private Context tContext;

		public GetTracksTask(Context context) {
			tContext = context;
		}

		@Override
		protected void onPreExecute() {

		}

		@Override
		protected ParentChildrenPair doInBackground(String... params) {
			Item item = K5ConnectorFactory.getConnector().getItem(tContext, params[0]);
			return new ParentChildrenPair(item, K5ConnectorFactory.getConnector().getChildren(tContext, item.getPid()));
		}

		@Override
		protected void onPostExecute(ParentChildrenPair result) {
			if (tContext == null || result.getParent() == null) {
				return;
			}
			Item parent = result.getParent();
			List<Item> children = result.getChildren();
			mSoundRecordingTitle.setText(parent.getRootTitle());
			String title = TextUtil.shortenforActionBar(parent.getTitle());
			getSupportActionBar().setTitle(title);

			mSoundUnitTitle.setText(title);
			ImageLoader.getInstance().displayImage(K5Api.getThumbnailPath(tContext, parent.getPid()), mSoundUnitImage);
			if (children == null) {
				return;
			}
			mSoundUnitInfo.setText(getString(R.string.sound_unit_tracks) + " " + children.size());
			mAdapter = new TrackArrayAdapter(tContext, children);
			mList.setAdapter(mAdapter);
			onTrackSelected(0, false);

		}
	}

	private void onTrackSelected(int position, boolean play) {
		if (mAdapter == null || mAdapter.getCount() < position + 1) {
			return;
		}
		Item item = mAdapter.getItem(position);
		String link = K5Api.getMp3StreamPath(this, item.getPid());
		if(VersionUtils.Debuggable()) {
			Log.d(LOG_TAG, "MP3 path:" + link);
		}
		mPlayerFragment.play(link, play);
		mAdapter.setSelection(position);

	}

	private void addFakeTrack(List<Item> list, String title, String pid) {
		Item item = new Item();
		item.setTitle(title);
		item.setPid(pid);
		list.add(item);
	}

	private class ParentChildrenPair {
		private Item parent;
		private List<Item> children;

		public ParentChildrenPair(Item parent, List<Item> children) {
			this.parent = parent;
			this.children = children;
		}

		public Item getParent() {
			return parent;
		}

		public List<Item> getChildren() {
			return children;
		}
	}

	@Override
	public void onPlayerPlay() {
		mAdapter.setPlaying(true);
	}

	@Override
	public void onPlayerPause() {
		mAdapter.setPlaying(false);

	}

	@Override
	public void onPlayerNext() {
		mAdapter.setPlaying(false);
		int selection = mAdapter.getSelection() + 1;
		if (selection >= mAdapter.getCount()) {
			selection = 0;
		}
		onTrackSelected(selection, true);
	}

	@Override
	public void onPlayerPrevious() {
		mAdapter.setPlaying(false);
		int selection = mAdapter.getSelection() - 1;
		if (selection < 0) {
			selection = mAdapter.getCount() - 1;
		}
		onTrackSelected(selection, true);
	}

	@Override
	public void onPlayerComplete() {
		onPlayerNext();
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
	protected void onPause() {
		if (mPlayerFragment != null) {
			mPlayerFragment.pause();
		}
		if (mAdapter != null) {
			mAdapter.setPlaying(false);
		}
		super.onPause();
	}

}
