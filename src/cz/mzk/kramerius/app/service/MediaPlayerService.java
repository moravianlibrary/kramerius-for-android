package cz.mzk.kramerius.app.service;

import java.io.IOException;
import java.util.logging.Logger;
import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.net.http.HttpResponseCache;
import android.os.IBinder;
import android.support.v7.app.NotificationCompat;
import android.support.v7.app.NotificationCompat.Builder;
import cz.mzk.kramerius.app.BaseActivity;
import cz.mzk.kramerius.app.R;
import cz.mzk.kramerius.app.model.SoundRecording;
import cz.mzk.kramerius.app.model.Track;
import cz.mzk.kramerius.app.service.MediaPlayerWithState.State;
import cz.mzk.kramerius.app.service.NotificationThumbnailManager.DownloadHandler;
import cz.mzk.kramerius.app.ui.SoundRecordingActivity;

public class MediaPlayerService extends Service {

	private static final Logger LOGGER = Logger.getLogger(MediaPlayerService.class.getSimpleName());
	public static final int SERVICE_PI_REQ_CODE = 4321;
	public static final int ACTIVITY_PI_REQ_CODE = 1234;
	private static final int NOTIFICATION_ID = 1;

	private static final int NOTIFICATION_ICON_SMALL = R.drawable.ic_stat_notification_small;
	private static final int NOTIFICATION_ACTION_ICON_PLAY = R.drawable.ic_play_arrow_white_36dp;
	private static final int NOTIFICATION_ACTION_ICON_PAUSE = R.drawable.ic_pause_white_36dp;
	private static final int NOTIFICATION_ACTION_ICON_EMPTY = R.drawable.ic_empty_36dp;
	private static final int NOTIFICATION_ACTION_ICON_NEXT = R.drawable.ic_skip_next_white_36dp;
	private static final int NOTIFICATION_ACTION_ICON_PREVIOUS = R.drawable.ic_skip_previous_white_36dp;

	public static final String ACTION_PLAY = "action_play";
	public static final String ACTION_PAUSE = "action_pause";
	public static final String ACTION_RESUME = "action_resume";
	public static final String ACTION_NEXT = "action_next";
	public static final String ACTION_PREVIOUS = "action_previous";
	public static final String ACTION_STOP = "action_stop";
	public static final String ACTION_REWIND = "action_rewing";
	public static final String EXTRA_PLAY_SOUND_RECORDING = "extra_play_sound_recording";
	public static final String EXTRA_PLAY_POSITION = "extra_play_position";
	public static final String EXTRA_REWIND_TIME = "extra_rewind_time";

	// helpers
	private MediaPlayerWithState mMediaPlayer;
	private NotificationThumbnailManager mThumbMgr;
	private IBinder mBinder;
	// data
	private SoundRecording mSoundRecording = null;
	private int mCurrentTrackIndex = -1;
	private boolean mFailed = false;

	@Override
	public IBinder onBind(Intent intent) {
		// Note: Unlike the activity lifecycle callback methods, you are not required to call the superclass implementation of
		// these callback methods.
		LOGGER.fine("onBind");
		return mBinder;
	}

	public boolean consumeFailed() {
		if (mFailed) {
			mFailed = false;
			return true;
		} else {
			return false;
		}
	}

	@Override
	public void onCreate() {
		super.onCreate();
		mThumbMgr = new NotificationThumbnailManager(this);
		mBinder = new MediaPlayerServiceBinder(this);
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		if (mMediaPlayer == null) {
			initMediaPlayer();
		}
		handleIntent(intent);
		return super.onStartCommand(intent, flags, startId);
	}

	private void handleIntent(Intent intent) {
		if (intent == null || intent.getAction() == null) {
			LOGGER.warning("intent null or no action, ignoring");
		} else {
			String action = intent.getAction();
			// LOGGER.info("action: " + action);
			if (action.equals(ACTION_PLAY)) {
				Track currentTrack = getCurrentTrack();
				SoundRecording newSoundRecording = (SoundRecording) intent
						.getParcelableExtra(EXTRA_PLAY_SOUND_RECORDING);
				int newIndex = intent.getIntExtra(EXTRA_PLAY_POSITION, 0);
				if (!newSoundRecording.equals(mSoundRecording)) {// sound recording switched
					mSoundRecording = newSoundRecording;
				}
				if (currentTrack == null) {
					mCurrentTrackIndex = newIndex;
					play();
				} else {
					Track newTrack = mSoundRecording.getTrack(newIndex);
					if (!newTrack.equals(currentTrack)) {
						mCurrentTrackIndex = newIndex;
						mMediaPlayer.reset();
						play();
					} else {// same track
						if (getState() == State.IDLE) {// somehow not playing nor loading
							prepareMediaPlayerAndPlay();
						}
					}
				}
			} else if (action.equals(ACTION_PAUSE)) {
				pause();
			} else if (action.equals(ACTION_RESUME)) {
				resume();
			} else if (action.equals(ACTION_REWIND)) {
				int time = intent.getIntExtra(EXTRA_REWIND_TIME, 0);
				rewind(time);
			} else if (action.equalsIgnoreCase(ACTION_PREVIOUS)) {
				skipToPrevious();
			} else if (action.equalsIgnoreCase(ACTION_NEXT)) {
				skipToNext();
			} else if (action.equalsIgnoreCase(ACTION_STOP)) {
				if (mMediaPlayer.canStop()) {
					mMediaPlayer.stop();
				}
				stopServiceAndCleanNotifications();
			}
		}
	}

	private void rewind(int time) {
		mMediaPlayer.seekTo(time);
	}

	private void resume() {
		if (mMediaPlayer.canStart()) {
			LOGGER.fine("resume: resuming");
			mMediaPlayer.start();
			showNotification(NOTIFICATION_ACTION_ICON_PAUSE, R.string.audioplayer_notification_btn_pause, ACTION_PAUSE);
		} else {
			LOGGER.warning("resume: cannot resume, state: " + mMediaPlayer.getState());
		}
	}

	private void play() {
		Track currentTrack = getCurrentTrack();
		if (currentTrack == null) {
			LOGGER.warning("play: cannot play - no track available");
		} else {
			if (currentTrack.getPid().equals(mMediaPlayer.getTrackPid())) {
				LOGGER.info("play: already playing, ignoring");
			} else {// other track
				LOGGER.fine("play: playing");
				mMediaPlayer.reset();
				prepareMediaPlayerAndPlay();
			}
		}
	}

	private void pause() {
		if (mMediaPlayer.canPause()) {
			LOGGER.fine("pause: pausing");
			mMediaPlayer.pause();
			showNotification(NOTIFICATION_ACTION_ICON_PLAY, R.string.audioplayer_notification_btn_play, ACTION_RESUME);
		} else {
			LOGGER.warning("pause: cannot pause, state: " + mMediaPlayer.getState());
		}
	}

	private void skipToPrevious() {
		if (canSkipToPrevious()) {
			LOGGER.fine("skipToPrevious: skipping");
			mCurrentTrackIndex--;
			mMediaPlayer.reset();
			prepareMediaPlayerAndPlay();
		} else {
			LOGGER.warning("skipToPrevious: no previous track");
		}
	}

	private boolean canSkipToNext() {
		return (mCurrentTrackIndex + 1) < mSoundRecording.getSize();
	}

	private boolean canSkipToPrevious() {
		return (mCurrentTrackIndex - 1) >= 0;
	}

	private void skipToNext() {
		if (canSkipToNext()) {
			LOGGER.fine("skipToNext: skipping");
			mCurrentTrackIndex++;
			mMediaPlayer.reset();
			prepareMediaPlayerAndPlay();
		} else {
			LOGGER.warning("skipToNext: no next track");
		}
	}

	private void stopServiceAndCleanNotifications() {
		LOGGER.info("canceling notifications");
		NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		notificationManager.cancel(NOTIFICATION_ID);
		LOGGER.fine("stoping thumb manager");
		mThumbMgr.stop();
		LOGGER.fine("flushing http cache");
		HttpResponseCache cache = HttpResponseCache.getInstalled();
		if (cache != null) {
			cache.flush();
		}
		LOGGER.fine("releasing media player");
		mMediaPlayer.release();
		LOGGER.fine("clearing data");
		mMediaPlayer = null;
		mSoundRecording = null;
		LOGGER.info("stopping service");
		stopSelf();
	}

	@SuppressLint("InlinedApi")
	private Notification buildNotification(NotificationCompat.Action primaryAction) {
		Track currentTrack = getCurrentTrack();
		NotificationCompat.Builder builder = (Builder) new NotificationCompat.Builder(this)//
				.setContentTitle(currentTrack.getTitle())//
				.setContentText(currentTrack.getSoundRecordingTitle())//
				.setDeleteIntent(buildStopServicePI())//
				.setContentIntent(buildOpenSoundRecordingActivityPI())//
				.setCategory(Notification.CATEGORY_TRANSPORT)//
				.setVisibility(NotificationCompat.VISIBILITY_PUBLIC)//
				.setPriority(Notification.PRIORITY_HIGH)//
				.setOngoing(false)// can be removed (swipe/remove all notifications)
				.setWhen(0) // not showing time
		;
		// actions
		boolean primaryActionEnabled = primaryAction != null;
		boolean nextEnabled = canSkipToNext();
		boolean previousEnabled = canSkipToPrevious();

		NotificationCompat.Action emptyAction = new NotificationCompat.Action.Builder(NOTIFICATION_ACTION_ICON_EMPTY,
				"", null).build();
		if (previousEnabled) {
			builder.addAction(buildNotificationAction(NOTIFICATION_ACTION_ICON_PREVIOUS,
					R.string.audioplayer_notification_btn_previous, ACTION_PREVIOUS));
		} else {
			builder.addAction(emptyAction);
		}
		if (primaryActionEnabled) {
			builder.addAction(primaryAction);
		} else {
			builder.addAction(emptyAction);
		}
		if (nextEnabled) {
			builder.addAction(buildNotificationAction(NOTIFICATION_ACTION_ICON_NEXT,
					R.string.audioplayer_notification_btn_next, ACTION_NEXT));
		} else {
			builder.addAction(emptyAction);
		}
		// style
		NotificationCompat.MediaStyle style = new NotificationCompat.MediaStyle();
		style.setShowCancelButton(false);// noop in Lollipop+
		// style.setCancelButtonIntent(buildStopServicePI());// doesn't seem to be working
		// style.setShowActionsInCompactView(0, 1, 2); //previous, pause/resume, next
		if (previousEnabled && primaryActionEnabled && nextEnabled) {
			style.setShowActionsInCompactView(0, 1, 2);
		} else if (previousEnabled && primaryActionEnabled) {
			style.setShowActionsInCompactView(0, 1);
		} else if (previousEnabled && nextEnabled) {
			style.setShowActionsInCompactView(0, 2);
		} else if (primaryActionEnabled && nextEnabled) {
			style.setShowActionsInCompactView(1, 2);
		} else if (previousEnabled) {
			style.setShowActionsInCompactView(0);
		} else if (primaryActionEnabled) {
			style.setShowActionsInCompactView(1);
		} else if (nextEnabled) {
			style.setShowActionsInCompactView(2);
		} else {
			style.setShowActionsInCompactView();
		}
		// if (primaryActionEnabled) {
		// style.setShowActionsInCompactView(1);// resume only
		// }
		builder.setStyle(style);
		// icons
		builder.setLargeIcon(getThumbnail(currentTrack));
		builder.setSmallIcon(NOTIFICATION_ICON_SMALL);
		return builder.build();
	}

	private Bitmap getThumbnail(Track currentTrack) {
		return mThumbMgr.getBitmap(this, currentTrack, new DownloadHandler() {

			@Override
			public void onDownloaded() {
				State state = getState();
				LOGGER.info("onDownloaded, state: " + state);
				if (state != null) {
					switch (state) {
					case STARTED:
						showNotification(NOTIFICATION_ACTION_ICON_PAUSE, R.string.audioplayer_notification_btn_pause,
								ACTION_PAUSE);
						break;
					case PAUSED:
						showNotification(NOTIFICATION_ACTION_ICON_PLAY, R.string.audioplayer_notification_btn_play,
								ACTION_RESUME);
						break;
					default:
						showNotificationWithoutPrimaryAction();
						LOGGER.info("bitmap downloades, state " + state + ", not reloading notification");
					}
				}
			}

		});
	}

	private NotificationCompat.Action buildNotificationAction(int iconRes, int titleRes, String intentAction) {
		Intent intent = new Intent(this, MediaPlayerService.class);
		intent.setAction(intentAction);
		PendingIntent pendingIntent = PendingIntent.getService(this, SERVICE_PI_REQ_CODE, intent,
				PendingIntent.FLAG_CANCEL_CURRENT);
		String title = getResources().getString(titleRes);
		return new NotificationCompat.Action.Builder(iconRes, title, pendingIntent).build();
	}

	public static PendingIntent buildStopServicePendingIntent(Context context) {
		Intent intent = new Intent(context, MediaPlayerService.class);
		intent.setAction(ACTION_STOP);
		PendingIntent pendingIntent = PendingIntent.getService(context, SERVICE_PI_REQ_CODE, intent,
				PendingIntent.FLAG_CANCEL_CURRENT);
		return pendingIntent;
	}

	private PendingIntent buildStopServicePI() {
		Intent intent = new Intent(this, MediaPlayerService.class);
		intent.setAction(ACTION_STOP);
		PendingIntent pendingIntent = PendingIntent.getService(this, SERVICE_PI_REQ_CODE, intent,
				PendingIntent.FLAG_CANCEL_CURRENT);
		return pendingIntent;
	}

	private PendingIntent buildOpenSoundRecordingActivityPI() {
		Intent intent = new Intent(getApplicationContext(), SoundRecordingActivity.class);
		intent.putExtra(BaseActivity.EXTRA_PID, mSoundRecording.getPid());
		// TaskStackBuilder stackBuilder = TaskStackBuilder.create(getApplicationContext());
		// // Adds the back stack
		// stackBuilder.addParentStack(SoundRecordingActivity.class);
		// // Adds the Intent to the top of the stack
		// stackBuilder.addNextIntent(intent);
		// // Gets a PendingIntent containing the entire back stack
		// PendingIntent pendingIntent = stackBuilder.getPendingIntent(ACTIVITY_PI_REQ_CODE,
		// PendingIntent.FLAG_UPDATE_CURRENT);

		// intent.setAction(Intent.ACTION_VIEW);
		// intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
		// intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

		// intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);// na KITKAT tohle staci
		// PendingIntent pendingIntent = PendingIntent.getActivity(this, ACTIVITY_PI_REQ_CODE, intent,
		// PendingIntent.FLAG_UPDATE_CURRENT);

		PendingIntent pendingIntent = PendingIntent.getActivity(this, ACTIVITY_PI_REQ_CODE, intent,
				PendingIntent.FLAG_UPDATE_CURRENT);
		return pendingIntent;
	}

	private void initMediaPlayer() {
		LOGGER.info("initializing media player");
		mMediaPlayer = new MediaPlayerWithState();
		mMediaPlayer.setOnCompletionListener(new OnCompletionListener() {

			@Override
			public void onCompletion(MediaPlayer mp) {
				LOGGER.info("onCompletion");
				if (canSkipToNext()) {
					skipToNext();
				} else {
					LOGGER.info("no more tracks in playlist");
					stopServiceAndCleanNotifications();
				}
			}

		});
		mMediaPlayer.setOnErrorListener(new OnErrorListener() {

			@Override
			public boolean onError(MediaPlayer mp, int what, int extra) {
				String errorType = toErrorType(what);
				String extraCode = toExtraCode(extra);
				LOGGER.severe("error: " + errorType + ": " + extraCode);
				// return false;
				mMediaPlayer.release();
				mMediaPlayer = null;
				mFailed = true;
				return true;
			}

			private String toErrorType(int what) {
				switch (what) {
				case MediaPlayer.MEDIA_ERROR_UNKNOWN:
					return "MEDIA_ERROR_UNKNOWN";
				case MediaPlayer.MEDIA_ERROR_SERVER_DIED:
					return "MEDIA_ERROR_SERVER_DIED";
				default:
					return Integer.toString(what);
				}
			}

			private String toExtraCode(int extra) {
				switch (extra) {
				case MediaPlayer.MEDIA_ERROR_IO:
					return "MEDIA_ERROR_IO";
				case MediaPlayer.MEDIA_ERROR_MALFORMED:
					return "MEDIA_ERROR_MALFORMED";
				case MediaPlayer.MEDIA_ERROR_UNSUPPORTED:
					return "MEDIA_ERROR_UNSUPPORTED";
				case MediaPlayer.MEDIA_ERROR_TIMED_OUT:
					return "MEDIA_ERROR_TIMED_OUT";
				default:
					return Integer.toString(extra);
				}
			}

		});
	}

	protected void prepareMediaPlayerAndPlay() {
		try {
			Track track = getCurrentTrack();
			if (track != null) {
				mMediaPlayer.setDataSource(getApplicationContext(), track.getPid());
			}
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalStateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (mMediaPlayer.canPrepare()) {

			mMediaPlayer.setOnPreparedListener(new OnPreparedListener() {

				@Override
				public void onPrepared(MediaPlayer mp) {
					LOGGER.fine("media player prepared, starting");
					mMediaPlayer.start();
					showNotification(NOTIFICATION_ACTION_ICON_PAUSE, R.string.audioplayer_notification_btn_pause,
							ACTION_PAUSE);
				}
			});
			LOGGER.info("preparing media player (async)");
			mMediaPlayer.prepareAsync();
			showNotificationWithoutPrimaryAction();
		} else {
			mFailed = true;
			LOGGER.info("prepareMediaPlayerAndPlay: cannot prepare now, canceling (state=" + getState() + ")");
			// stopServiceAndCleanNotifications();
			showNotificationWithoutPrimaryAction();
		}
	}

	private void showNotification(Integer centralActionIcon, int centralActionTitleResource, String centralAction) {
		Notification notification = buildNotification(buildNotificationAction(centralActionIcon,
				centralActionTitleResource, centralAction));
		// TODO: zvolit mezi backgroundService a foregroundService
		// background service
		// muze byt zabita systemem a casto se tak stava pri opusteni aplikace
		// notifikace neni svazana a tak je mozne ji swipem odstranit a pri tom zavolat zabitli sluzby pres intent
		// foreground service
		// musi byt svazana s notifikaci
		// musi se zabit sama stopForeground(true);
		// notifikaci nelze odstranit swipem
		// ale je mozne pres MediaStyle.setShowCancelButton(true); pridat krizek a hanlder na jeho kliknuti
		// jenze ten krizek se nezobrazi u Lollipop+
		// startForeground(NOTIFICATION_ID, notification);
		// takze zatim vitezi background service
		NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		notificationManager.notify(NOTIFICATION_ID, notification);
	}

	private void showNotificationWithoutPrimaryAction() {
		Notification notification = buildNotification(null);
		NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		notificationManager.notify(NOTIFICATION_ID, notification);
	}

	@Override
	public boolean onUnbind(Intent intent) {
		LOGGER.fine("onUnbind");
		return super.onUnbind(intent);
	}

	@Override
	public void onDestroy() {
		// LOGGER.info("onDestroy");
		super.onDestroy();
		if (mMediaPlayer != null) {
			mMediaPlayer.release();
		}
	}

	Track getCurrentTrack() {
		if (mSoundRecording != null && mCurrentTrackIndex >= 0 && mCurrentTrackIndex < mSoundRecording.getSize()) {
			return mSoundRecording.getTrack(mCurrentTrackIndex);
		} else {
			return null;
		}
	}

	String getSoundRecordingId() {
		return mSoundRecording != null ? mSoundRecording.getPid() : null;
	}

	Integer getDuration() {
		return mMediaPlayer != null ? mMediaPlayer.getDuration() : null;
	}

	Integer getCurrentPosition() {
		return mMediaPlayer != null ? mMediaPlayer.getCurrentPosition() : null;
	}

	State getState() {
		return mMediaPlayer != null ? mMediaPlayer.getState() : null;
	}

}