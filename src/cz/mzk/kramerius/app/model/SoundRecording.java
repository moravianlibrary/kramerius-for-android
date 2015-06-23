package cz.mzk.kramerius.app.model;

import java.io.Serializable;
import java.util.List;

public class SoundRecording implements Serializable {

	private static final long serialVersionUID = 3250806601186245219L;
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

}
