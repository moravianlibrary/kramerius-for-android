package cz.mzk.kramerius.app.ui;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Logger;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Response;
import com.google.analytics.tracking.android.EasyTracker;

import cz.mzk.kramerius.app.BaseActivity;
import cz.mzk.kramerius.app.R;
import cz.mzk.kramerius.app.adapter.TrackArrayAdapter;
import cz.mzk.kramerius.app.api.K5Api;
import cz.mzk.kramerius.app.api.K5ConnectorFactory;
import cz.mzk.kramerius.app.model.Item;
import cz.mzk.kramerius.app.model.SoundRecording;
import cz.mzk.kramerius.app.model.Track;
import cz.mzk.kramerius.app.service.MediaPlayerServiceBinder;
import cz.mzk.kramerius.app.service.MediaPlayerService;
import cz.mzk.kramerius.app.service.MediaPlayerWithState.State;
import cz.mzk.kramerius.app.util.BitmapUtil;
import cz.mzk.kramerius.app.util.ModelUtil;
import cz.mzk.kramerius.app.viewer.RedirectingImageRequest;
import cz.mzk.kramerius.app.viewer.VolleyRequestManager;

public class SoundRecordingActivity extends BaseActivity implements OnClickListener {

	public static interface PlayerServiceHelper {
		public Track getCurrentTrackId();

		boolean isPlayingNow();
	}

	private static final Logger LOGGER = Logger.getLogger(SoundRecordingActivity.class.getSimpleName());

	private static final String EXTRA_SOUND_RECORDING = "sound_recording";

	// data
	private String mPid;
	private SoundRecording mSoundRecording;
	// views
	private ImageView mActionbarThumb;
	private TextView mActionbarTitle;
	private TextView mActionbarAuthor;
	private ProgressBar mProgressBar;
	private View mContent;
	private ImageView mBtnPlayAll;
	private ListView mTracks;
	private PlayerFragment mPlayerFragment;
	// other
	private TrackArrayAdapter mTracksAdapter;
	private AsyncTask<String, Void, Bitmap> mFetchThumbBitmapTask;
	private Timer mPlayerTimer;
	private TimerTask mPlayerTask;
	private MediaPlayerServiceBinder mBinder = null;
	private ServiceConnection mServiceConnection = new ServiceConnection() {

		@Override
		public void onServiceConnected(ComponentName className, IBinder service) {
			mBinder = (MediaPlayerServiceBinder) service;
		}

		@Override
		public void onServiceDisconnected(ComponentName arg0) {
			mBinder = null;
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_sound_recording);
		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);
		getSupportActionBar().setTitle(R.string.app_name);
		getSupportActionBar().setHomeButtonEnabled(false);
		getSupportActionBar().setDisplayHomeAsUpEnabled(false);

		// load views
		mActionbarThumb = (ImageView) findViewById(R.id.thumb);
		mActionbarTitle = (TextView) findViewById(R.id.title);
		mActionbarAuthor = (TextView) findViewById(R.id.author);
		mProgressBar = (ProgressBar) findViewById(R.id.progressBar);
		mContent = findViewById(R.id.content);
		mBtnPlayAll = (ImageView) findViewById(R.id.btnPlayAll);
		mBtnPlayAll.setOnClickListener(this);
		mTracks = (ListView) findViewById(R.id.tracks);
		mPlayerFragment = (PlayerFragment) getFragmentManager().findFragmentById(R.id.playerFragment);
		// load data
		if (savedInstanceState != null) {
			loadData(savedInstanceState);
		} else {
			loadData(getIntent().getExtras());
		}
		if (mSoundRecording != null) {
			inflateViews();
		} else {
			loadDataAsync();
		}
	}

	private void loadDataAsync() {
		new AsyncTask<Void, Void, SoundRecording>() {

			@Override
			protected SoundRecording doInBackground(Void... params) {
				Item srItem = K5ConnectorFactory.getConnector().getItem(SoundRecordingActivity.this, mPid);
				String srAuthor = srItem.getAuthor();
				String srTitle = srItem.getTitle();
				if (srAuthor == null) {
					srAuthor = "TODO: author missing";
				}
				List<Track> tracks = getAllTracks(srTitle);
				return new SoundRecording(mPid, srTitle, srAuthor, tracks);
			}

			private List<Track> getAllTracks(String srTitle) {
				List<Item> directTrackItems = K5ConnectorFactory.getConnector().getChildren(
						SoundRecordingActivity.this, mPid, ModelUtil.TRACK);
				List<Track> tracks = toTracks(directTrackItems, mPid, srTitle, null, null);

				List<Item> soundUnitItems = K5ConnectorFactory.getConnector().getChildren(SoundRecordingActivity.this,
						mPid, ModelUtil.SOUND_UNIT);
				for (Item suItem : soundUnitItems) {
					String suTitle = suItem.getTitle();
					String suPId = suItem.getPid();
					List<Item> suTrackItems = K5ConnectorFactory.getConnector().getChildren(
							SoundRecordingActivity.this, suItem.getPid(), ModelUtil.TRACK);
					List<Track> suTracks = toTracks(suTrackItems, mPid, srTitle, suPId, suTitle);
					for (Track track : suTracks) {
						if (!tracks.contains(track)) {
							tracks.add(track);
						}
					}
				}
				return tracks;
			}

			private List<Track> toTracks(List<Item> trackItems, String srPid, String srTitle, String suPid,
					String suTitle) {
				List<Track> result = new ArrayList<Track>(trackItems.size());
				for (Item item : trackItems) {
					result.add(new Track(item.getPid(), item.getTitle(), srPid, srTitle, suPid, suTitle));
				}
				return result;
			}

			protected void onPostExecute(SoundRecording result) {
				mSoundRecording = result;
				inflateViews();
			};

		}.execute();
	}

	private void inflateViews() {
		mActionbarTitle.setText(mSoundRecording.getTitle());
		getSupportActionBar().setTitle(mSoundRecording.getTitle());
		mActionbarAuthor.setText(mSoundRecording.getAuthor());
		getSupportActionBar().setSubtitle(mSoundRecording.getAuthor());
		mTracksAdapter = new TrackArrayAdapter(this, mSoundRecording, new PlayerServiceHelper() {

			@Override
			public Track getCurrentTrackId() {
				return mBinder != null ? mBinder.getCurrentTrack() : null;
			}

			@Override
			public boolean isPlayingNow() {
				return State.STARTED == mBinder.getState();
			}

		});
		mTracks.setAdapter(mTracksAdapter);
		mTracks.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				Intent intent = new Intent(getApplicationContext(), MediaPlayerService.class);
				intent.setAction(MediaPlayerService.ACTION_PLAY);
				Bundle bundle = new Bundle();
				bundle.putSerializable(MediaPlayerService.EXTRA_PLAY_SOUND_RECORDING, mSoundRecording);
				bundle.putInt(MediaPlayerService.EXTRA_PLAY_POSITION, position);
				intent.putExtras(bundle);
				startService(intent);
			}
		});
		mProgressBar.setVisibility(View.INVISIBLE);
		mContent.setVisibility(View.VISIBLE);
		fetchThumbnail();
	}

	private void loadData(Bundle bundle) {
		mPid = bundle.getString(EXTRA_PID);
		mSoundRecording = (SoundRecording) bundle.getSerializable(EXTRA_SOUND_RECORDING);
	}

	@Override
	protected void onSaveInstanceState(Bundle bundle) {
		bundle.putString(EXTRA_PID, mPid);
		if (mSoundRecording != null) {
			bundle.putSerializable(EXTRA_SOUND_RECORDING, mSoundRecording);
		}
		super.onSaveInstanceState(bundle);
	}

	protected void onStart() {
		super.onStart();
		EasyTracker.getInstance(this).activityStart(this);
		Intent intent = new Intent(this, MediaPlayerService.class);
		bindService(intent, mServiceConnection, Context.BIND_AUTO_CREATE);
		fetchThumbnail();
	};

	private void fetchThumbnail() {
		if (mActionbarThumb.getDrawable() == null && mSoundRecording != null) {
			String url = K5Api.getThumbnailPath(this, mSoundRecording.getPid());
			VolleyRequestManager.addToRequestQueue(new RedirectingImageRequest(url, new Response.Listener<Bitmap>() {
				@Override
				public void onResponse(Bitmap bitmap) {
					inflateImage(bitmap);
				}

				private void inflateImage(Bitmap fetched) {
					if (fetched != null) {
						Bitmap scaled = BitmapUtil.scaleBitmap(fetched,
								getResources().getDimensionPixelSize(R.dimen.sound_recording_thumb_width),
								getResources().getDimensionPixelSize(R.dimen.sound_recording_thumb_height));
						if (scaled != null) {
							mActionbarThumb.setImageBitmap(scaled);
							Drawable drawable = new BitmapDrawable(getResources(), scaled);
							getSupportActionBar().setIcon(drawable);
						}
					}
				}
			}, null));
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		mPlayerTimer = new Timer();
		mPlayerTask = new TimerTask() {
			boolean isThisSoundRecording = false;
			private Track visulisedAsCurrent;
			private State lastState = null;

			@Override
			public void run() {
				if (mBinder != null && mSoundRecording != null) {
					String activeSoundRecordingPid = mBinder.getSoundRecordingId();
					if (activeSoundRecordingPid != null && activeSoundRecordingPid.equals(mPid)) {
						isThisSoundRecording = true;
						// update visulised track
						Track currentTrack = mBinder.getCurrentTrack();
						if (currentTrack == null) {
							if (visulisedAsCurrent != null) {
								visulisedAsCurrent = null;
								invalidateRecyclerView();
								// LOGGER.info("current track is null, visualisedAsCurrent!=null");
							}
						} else {
							if (!currentTrack.equals(visulisedAsCurrent)) {// zmena hrajiciho tracku
								visulisedAsCurrent = currentTrack;
								lastState = mBinder.getState();
								// LOGGER.info("zmena aktivniho tracku");
								invalidateRecyclerView();
							} else {
								State state = mBinder.getState();
								if (state != lastState) {// zmena stavu prehravace, potrebuju zastavit/spustit animaci
									// LOGGER.info("zmena stavu");
									lastState = state;
									invalidateRecyclerView();
								}
							}
						}
						// update player
						updatePlayer(true, mBinder.getState(), mBinder.getCurrentPosition(), mBinder.getDuration());
						// toast in case of error
						if (mBinder.hasFailed()) {
							runOnUiThread(new Runnable() {
								@Override
								public void run() {
									Toast.makeText(SoundRecordingActivity.this, "error playing track",
											Toast.LENGTH_SHORT).show();
								}
							});
						}
					} else {
						updatePlayer(false, null, null, null);
						if (isThisSoundRecording) {
							isThisSoundRecording = false;
							visulisedAsCurrent = null;
						}
					}
				}
			}

			private void updatePlayer(final boolean enabled, final State state, final Integer currentTime,
					final Integer totalTime) {
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						mPlayerFragment.update(enabled, state, currentTime, totalTime);
						mBtnPlayAll.setVisibility(enabled ? View.GONE : View.VISIBLE);
					}
				});
			}

			private void invalidateRecyclerView() {
				LOGGER.info("invalidating");
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						mTracksAdapter.notifyDataSetChanged();
					}
				});
			}
		};
		mPlayerTimer.scheduleAtFixedRate(mPlayerTask, 0, 200);// 5x za sekundu
	}

	@Override
	protected void onPause() {
		super.onPause();
		mPlayerTask.cancel();
		mPlayerTimer.cancel();
	}

	protected void onStop() {
		super.onStop();
		EasyTracker.getInstance(this).activityStart(this);
		// cancel loading thumbnail
		if (mFetchThumbBitmapTask != null) {
			mFetchThumbBitmapTask.cancel(true);
			mFetchThumbBitmapTask = null;
		}
		// unbind from the service
		if (mBinder != null) {
			unbindService(mServiceConnection);
			mBinder = null;
		}
	};

	@Override
	public void onClick(View v) {
		if (v == mBtnPlayAll) {
			Intent intent = new Intent(getApplicationContext(), MediaPlayerService.class);
			intent.setAction(MediaPlayerService.ACTION_PLAY);
			Bundle bundle = new Bundle();
			bundle.putSerializable(MediaPlayerService.EXTRA_PLAY_SOUND_RECORDING, mSoundRecording);
			bundle.putInt(MediaPlayerService.EXTRA_PLAY_POSITION, 0);
			intent.putExtras(bundle);
			startService(intent);
		}
	}

}
