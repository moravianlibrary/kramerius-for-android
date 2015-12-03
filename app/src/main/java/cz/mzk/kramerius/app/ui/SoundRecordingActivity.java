package cz.mzk.kramerius.app.ui;

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
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import cz.mzk.kramerius.app.BaseActivity;
import cz.mzk.kramerius.app.R;
import cz.mzk.kramerius.app.adapter.TrackLvAdapter;
import cz.mzk.kramerius.app.adapter.TrackRvAdapter;
import cz.mzk.kramerius.app.api.K5Api;
import cz.mzk.kramerius.app.api.K5ConnectorFactory;
import cz.mzk.kramerius.app.metadata.Author;
import cz.mzk.kramerius.app.metadata.Metadata;
import cz.mzk.kramerius.app.model.Item;
import cz.mzk.kramerius.app.model.SoundRecording;
import cz.mzk.kramerius.app.model.Track;
import cz.mzk.kramerius.app.service.MediaPlayerService;
import cz.mzk.kramerius.app.service.MediaPlayerServiceBinder;
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

    private static final String TAG = SoundRecordingActivity.class.getSimpleName();

    private static final String EXTRA_SOUND_RECORDING = "sound_recording";
    public static final String EXTRA_SOUND_RECORDING_ITEM = "sound_recording_item";

    // data
    private String mPid;
    private Item mSoundRecordingItem;
    private SoundRecording mSoundRecording;
    // views
    private ImageView mActionbarThumb;
    private TextView mActionbarTitle;
    private TextView mActionbarAuthor;
    private ImageView mActionbarBtnInfo;

    private ProgressBar mProgressBar;
    private View mContent;
    private ImageView mBtnPlayAll;
    private RecyclerView mTracksRv;
    private ListView mTracksLv;
    private PlayerFragment mPlayerFragment;
    // other
    private TrackRvAdapter mTracksRvAdapter;
    private TrackLvAdapter mTracksLvAdapter;

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
        Log.i(TAG, "onCreate");
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
        mActionbarBtnInfo = (ImageView) findViewById(R.id.btnInfo);
        mActionbarBtnInfo.setOnClickListener(this);
        mProgressBar = (ProgressBar) findViewById(R.id.progressBar);
        mContent = findViewById(R.id.content);
        mBtnPlayAll = (ImageView) findViewById(R.id.btnPlayAll);
        mBtnPlayAll.setOnClickListener(this);
        View tracksView = findViewById(R.id.tracks);
        if (tracksView != null) {
            if (tracksView instanceof RecyclerView) {
                mTracksRv = (RecyclerView) tracksView;
            } else if (tracksView instanceof ListView) {
                mTracksLv = (ListView) tracksView;
            }
        }

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
            fetchDataAsync();
        }
    }

    private void fetchDataAsync() {
        new AsyncTask<Item, Void, SoundRecording>() {

            @Override
            protected SoundRecording doInBackground(Item... params) {
                Item srItem = params[0];
                if (srItem == null) {
                    // item obtained like this does not include author(s)
                    // see http://kramerius.mzk.cz/search/api/v5.0/item/uuid%3A6f840b85-e6b3-49f1-9710-71aed0beca47
                    srItem = K5ConnectorFactory.getConnector().getItem(SoundRecordingActivity.this, mPid);
                }
                String srTitle = srItem.getTitle();
                String srAuthor = srItem.getAuthor();
                if (srAuthor == null) {
                    Metadata modsMetadata = K5ConnectorFactory.getConnector().getModsMetadata(
                            SoundRecordingActivity.this, srItem.getPid());
                    List<Author> modsAuthors = modsMetadata.getAuthors();
                    if (modsAuthors != null && !modsAuthors.isEmpty()) {
                        Author author = modsAuthors.get(0);
                        srAuthor = author.getName();
                    }
                }
                List<Track> tracks = getAllTracks(srTitle, srItem.getPid());
                return new SoundRecording(srItem.getPid(), srTitle, srAuthor, tracks);
            }

            private List<Track> getAllTracks(String srTitle, String srPid) {
                List<Item> directTrackItems = K5ConnectorFactory.getConnector().getChildren(
                        SoundRecordingActivity.this, srPid, ModelUtil.TRACK);
                List<Track> tracks = toTracks(directTrackItems, srPid, srTitle, null, null);

                List<Item> soundUnitItems = K5ConnectorFactory.getConnector().getChildren(SoundRecordingActivity.this,
                        srPid, ModelUtil.SOUND_UNIT);
                for (Item suItem : soundUnitItems) {
                    String suTitle = suItem.getTitle();
                    String suPId = suItem.getPid();
                    List<Item> suTrackItems = K5ConnectorFactory.getConnector().getChildren(
                            SoundRecordingActivity.this, suItem.getPid(), ModelUtil.TRACK);
                    List<Track> suTracks = toTracks(suTrackItems, srPid, srTitle, suPId, suTitle);
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
            }

            ;

        }.execute(mSoundRecordingItem);
    }

    private void inflateViews() {
        mActionbarTitle.setText(mSoundRecording.getTitle());
        getSupportActionBar().setTitle(mSoundRecording.getTitle());
        if (mSoundRecording.getAuthor() != null) {
            mActionbarAuthor.setText(mSoundRecording.getAuthor());
        }
        getSupportActionBar().setSubtitle(mSoundRecording.getAuthor());
        if (mTracksRv != null) {
            mTracksRvAdapter = new TrackRvAdapter(this, mSoundRecording, new PlayerServiceHelper() {

                @Override
                public Track getCurrentTrackId() {
                    return mBinder != null ? mBinder.getCurrentTrack() : null;
                }

                @Override
                public boolean isPlayingNow() {
                    return State.STARTED == mBinder.getState();
                }

            });
            mTracksRv.setLayoutManager(new LinearLayoutManager(this));
            mTracksRv.setAdapter(mTracksRvAdapter);
        }
        if (mTracksLv != null) {
            mTracksLvAdapter = new TrackLvAdapter(this, mSoundRecording, new PlayerServiceHelper() {

                @Override
                public Track getCurrentTrackId() {
                    return mBinder != null ? mBinder.getCurrentTrack() : null;
                }

                @Override
                public boolean isPlayingNow() {
                    return State.STARTED == mBinder.getState();
                }

            });
            mTracksLv.setAdapter(mTracksLvAdapter);
            mTracksLv.setOnItemClickListener(new OnItemClickListener() {

                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Intent intent = new Intent(getApplicationContext(), MediaPlayerService.class);
                    intent.setAction(MediaPlayerService.ACTION_PLAY);
                    Bundle bundle = new Bundle();
                    bundle.putParcelable(MediaPlayerService.EXTRA_PLAY_SOUND_RECORDING, mSoundRecording);
                    bundle.putInt(MediaPlayerService.EXTRA_PLAY_POSITION, position);
                    intent.putExtras(bundle);
                    startService(intent);
                }
            });
        }

        mProgressBar.setVisibility(View.INVISIBLE);
        mContent.setVisibility(View.VISIBLE);
        fetchThumbnail();
    }

    private void loadData(Bundle bundle) {
        mPid = bundle.getString(EXTRA_PID);
        mSoundRecording = (SoundRecording) bundle.getParcelable(EXTRA_SOUND_RECORDING);
        mSoundRecordingItem = (Item) bundle.getParcelable(EXTRA_SOUND_RECORDING_ITEM);
    }

    @Override
    protected void onSaveInstanceState(Bundle bundle) {
        bundle.putString(EXTRA_PID, mPid);
        bundle.putParcelable(EXTRA_SOUND_RECORDING, mSoundRecording);
        bundle.putParcelable(EXTRA_SOUND_RECORDING_ITEM, mSoundRecordingItem);
        super.onSaveInstanceState(bundle);
    }

    protected void onStart() {
        super.onStart();
        EasyTracker.getInstance(this).activityStart(this);
        Intent intent = new Intent(this, MediaPlayerService.class);
        bindService(intent, mServiceConnection, Context.BIND_AUTO_CREATE);
        fetchThumbnail();
    }

    ;

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
                    if (activeSoundRecordingPid != null && activeSoundRecordingPid.equals(mSoundRecording.getPid())) {
                        isThisSoundRecording = true;
                        // update visulised track
                        Track currentTrack = mBinder.getCurrentTrack();
                        if (currentTrack == null) {
                            if (visulisedAsCurrent != null) {
                                visulisedAsCurrent = null;
                                invalidateRecyclerView();
                                // Log.i(TAG, "current track is null, visualisedAsCurrent!=null");
                            }
                        } else {
                            if (!currentTrack.equals(visulisedAsCurrent)) {// zmena hrajiciho tracku
                                visulisedAsCurrent = currentTrack;
                                lastState = mBinder.getState();
                                // Log.i(TAG, "zmena aktivniho tracku");
                                invalidateRecyclerView();
                            } else {
                                State state = mBinder.getState();
                                if (state != lastState) {// zmena stavu prehravace, potrebuju zastavit/spustit animaci
                                    // Log.i(TAG, "zmena stavu");
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
                Log.i(TAG, "invalidating");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (mTracksRvAdapter != null) {
                            mTracksRvAdapter.notifyDataSetChanged();
                        }
                        if (mTracksLvAdapter != null) {
                            mTracksLvAdapter.notifyDataSetChanged();
                        }
                    }
                });
            }
        };
        mPlayerTimer.scheduleAtFixedRate(mPlayerTask, 0, 200);// 5x za sekundu
    }

    @Override
    protected void onPause() {
        Log.i(TAG, "onPause");
        super.onPause();
        mPlayerTask.cancel();
        mPlayerTimer.cancel();
    }

    protected void onStop() {
        Log.i(TAG, "onStop");
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
    }

    @Override
    protected void onDestroy() {
        Log.i(TAG, "onDestroy");
        super.onDestroy();
        //making sure the notification disappears
        /*Intent intent = new Intent(getApplicationContext(), MediaPlayerService.class);
        intent.setAction(MediaPlayerService.ACTION_STOP);
        startService(intent);*/
    }

    @Override
    public void onClick(View v) {
        if (v == mBtnPlayAll) {
            Intent intent = new Intent(getApplicationContext(), MediaPlayerService.class);
            intent.setAction(MediaPlayerService.ACTION_PLAY);
            Bundle bundle = new Bundle();
            bundle.putParcelable(MediaPlayerService.EXTRA_PLAY_SOUND_RECORDING, mSoundRecording);
            bundle.putInt(MediaPlayerService.EXTRA_PLAY_POSITION, 0);
            intent.putExtras(bundle);
            startService(intent);
        } else if (v == mActionbarBtnInfo) {
            String pid = mPid != null ? mPid : (mSoundRecording != null ? mSoundRecording.getPid() : null);
            if (pid != null) {
                Intent intent = new Intent(this, MetadataActivity.class);
                intent.putExtra(EXTRA_PID, pid);
                startActivity(intent);
            }
        }
    }
}
