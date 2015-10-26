package cz.mzk.kramerius.app.model;

import java.util.ArrayList;
import java.util.List;

import android.os.Parcel;
import android.os.Parcelable;

public class SoundRecording implements Parcelable {

	private final String mPid;
	private final String mTitle;
	private final String mAuthor;
	private final List<Track> mTracks;

	public SoundRecording(String pid, String title, String author, List<Track> tracks) {
		this.mPid = pid;
		this.mTitle = title;
		this.mAuthor = author;
		this.mTracks = tracks;
	}

	public String getPid() {
		return mPid;
	}

	public String getTitle() {
		return mTitle;
	}

	public String getAuthor() {
		return mAuthor;
	}

	public List<Track> getTracks() {
		return mTracks;
	}

	public Track getTrack(int index) {
		return mTracks.get(index);
	}

	public int getSize() {
		return mTracks.size();
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(mPid);
		dest.writeString(mTitle);
		dest.writeString(mAuthor);
		dest.writeTypedList(mTracks);
	}

	public static final Parcelable.Creator<SoundRecording> CREATOR = new Parcelable.Creator<SoundRecording>() {
		public SoundRecording createFromParcel(Parcel in) {
			String pid = in.readString();
			String title = in.readString();
			String author = in.readString();
			List<Track> tracks = new ArrayList<Track>();
			in.readTypedList(tracks, Track.CREATOR);
			return new SoundRecording(pid, title, author, tracks);
		}

		public SoundRecording[] newArray(int size) {
			return new SoundRecording[size];
		}
	};

}
