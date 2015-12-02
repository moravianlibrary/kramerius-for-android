package cz.mzk.kramerius.app.service;

import android.os.Binder;

import cz.mzk.kramerius.app.model.Track;
import cz.mzk.kramerius.app.service.MediaPlayerWithState.State;

public class MediaPlayerServiceBinder extends Binder {

    private final MediaPlayerService service;

    public MediaPlayerServiceBinder(MediaPlayerService service) {
        super();
        this.service = service;
    }

    public String getSoundRecordingId() {
        return service.getSoundRecordingId();
    }

    public Track getCurrentTrack() {
        return service.getCurrentTrack();
    }

    public Integer getDuration() {
        return service.getDuration();
    }

    public Integer getCurrentPosition() {
        return service.getCurrentPosition();
    }

    public State getState() {
        return service.getState();
    }

    public boolean hasFailed() {
        return service.consumeFailed();
    }

}
