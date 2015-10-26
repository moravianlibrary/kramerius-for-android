package cz.mzk.kramerius.app.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import cz.mzk.kramerius.app.R;
import cz.mzk.kramerius.app.model.SoundRecording;
import cz.mzk.kramerius.app.ui.SoundRecordingActivity.PlayerServiceHelper;

public class TrackRvAdapter extends RecyclerView.Adapter<TrackRvViewholder> {

	private final Context mContext;
	private final SoundRecording mSoundRecording;
	private final PlayerServiceHelper mServiceHelper;
	private final Animation mVinylAnimation;

	public TrackRvAdapter(Context context, SoundRecording mSoundRecording, PlayerServiceHelper mServiceHelper) {
		super();
		this.mContext = context;
		this.mSoundRecording = mSoundRecording;
		this.mServiceHelper = mServiceHelper;
		this.mVinylAnimation = AnimationUtils.loadAnimation(context, R.anim.rotation);
		this.mVinylAnimation.setRepeatCount(Animation.INFINITE);
	}

	@Override
	public int getItemCount() {
		return mSoundRecording.getSize();
	}

	@Override
	public void onBindViewHolder(TrackRvViewholder viewHolder, int position) {
		viewHolder.bind(mSoundRecording, position);
	}

	@Override
	public TrackRvViewholder onCreateViewHolder(ViewGroup parent, int viewType) {
		View root = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_track_rv, parent, false);
		return new TrackRvViewholder(mContext, root, mServiceHelper);
	}

}
