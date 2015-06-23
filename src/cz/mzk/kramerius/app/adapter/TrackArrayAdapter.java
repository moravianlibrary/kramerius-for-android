package cz.mzk.kramerius.app.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import cz.mzk.kramerius.app.R;
import cz.mzk.kramerius.app.model.SoundRecording;
import cz.mzk.kramerius.app.model.Track;
import cz.mzk.kramerius.app.ui.SoundRecordingActivity.PlayerServiceHelper;

public class TrackArrayAdapter extends ArrayAdapter<Track> {

	private final SoundRecording mSoundRecording;
	private final PlayerServiceHelper mServiceHelper;
	private final Animation mVinylAnimation;

	public TrackArrayAdapter(Context context, SoundRecording soundRecording, PlayerServiceHelper serviceHelper) {
		super(context, R.layout.item_track, soundRecording.getTracks());
		this.mSoundRecording = soundRecording;
		this.mServiceHelper = serviceHelper;
		mVinylAnimation = AnimationUtils.loadAnimation(context, R.anim.rotation);
		mVinylAnimation.setRepeatCount(Animation.INFINITE);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View rowView = convertView;
		if (rowView == null) {
			LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			rowView = inflater.inflate(R.layout.item_track, parent, false);
			TrackArrayViewholder viewHolder = new TrackArrayViewholder(getContext(), rowView, mServiceHelper);
			rowView.setTag(viewHolder);
		}
		TrackArrayViewholder holder = (TrackArrayViewholder) rowView.getTag();
		holder.bind(mSoundRecording, position);
		return rowView;
	}

}
