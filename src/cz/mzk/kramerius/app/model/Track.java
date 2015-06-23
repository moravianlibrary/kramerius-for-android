package cz.mzk.kramerius.app.model;

import android.os.Parcel;
import android.os.Parcelable;

public class Track implements Parcelable {

	public static enum AudioFormat {
		OGG, MP3, WAV
	}

	private final String mPid;
	private final String mTitle;
	private final String mSoundRecordingPid;
	private final String mSoundRecordingTitle;
	private final String mSoundUnitPid;
	private final String mSoundUnitTitle;

	public Track(String pid, String title, String srPid, String srTitle, String suPid, String suTitle) {
		this.mPid = pid;
		this.mTitle = title;
		this.mSoundRecordingPid = srPid;
		this.mSoundRecordingTitle = srTitle;
		this.mSoundUnitPid = suPid;
		this.mSoundUnitTitle = suTitle;
	}

	public String getPid() {
		return mPid;
	}

	public String getTitle() {
		return mTitle;
	}

	public String getSoundRecordingPid() {
		return mSoundRecordingPid;
	}

	public String getSoundRecordingTitle() {
		return mSoundRecordingTitle;
	}

	public String getSoundUnitPid() {
		return mSoundUnitPid;
	}

	public String getSoundUnitTitle() {
		return mSoundUnitTitle;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((mPid == null) ? 0 : mPid.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Track other = (Track) obj;
		if (mPid == null) {
			if (other.mPid != null)
				return false;
		} else if (!mPid.equals(other.mPid))
			return false;
		return true;
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(mPid);
		dest.writeString(mTitle);
		dest.writeString(mSoundRecordingPid);
		dest.writeString(mSoundRecordingTitle);
		dest.writeString(mSoundUnitPid);
		dest.writeString(mSoundRecordingTitle);
	}

	public static final Parcelable.Creator<Track> CREATOR = new Parcelable.Creator<Track>() {
		public Track createFromParcel(Parcel in) {
			return new Track(in.readString(), in.readString(), in.readString(), in.readString(), in.readString(),
					in.readString());
		}

		public Track[] newArray(int size) {
			return new Track[size];
		}
	};

}
