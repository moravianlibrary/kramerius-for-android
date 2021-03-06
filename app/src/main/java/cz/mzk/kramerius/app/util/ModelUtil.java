package cz.mzk.kramerius.app.util;

import android.app.Activity;
import android.content.Intent;

import cz.mzk.kramerius.app.BaseActivity;
import cz.mzk.kramerius.app.R;
import cz.mzk.kramerius.app.model.Item;
import cz.mzk.kramerius.app.ui.PageActivity;
import cz.mzk.kramerius.app.ui.PeriodicalActivity;
import cz.mzk.kramerius.app.ui.SoundRecordingActivity;

public class ModelUtil {

    public static final String PERIODICAL = "periodical";
    public static final String PERIODICAL_VOLUME = "periodicalvolume";
    public static final String PERIODICAL_ITEM = "periodicalitem";
    public static final String PAGE = "page";
    public static final String MANUSCRIPT = "manuscript";
    public static final String MONOGRAPH = "monograph";
    public static final String SOUND_RECORDING = "soundrecording";
    public static final String SOUND_UNIT = "soundunit";
    public static final String TRACK = "track";
    public static final String MAP = "map";
    public static final String GRAPHIC = "graphic";
    public static final String SHEET_MUSIC = "sheetmusic";
    public static final String ARCHIVE = "archive";

    public static int getLabel(String value) {
        if (value == null || value.isEmpty()) {
            return R.string.document_unknown;
        }
        if (value.equals(PERIODICAL)) {
            return R.string.document_periodical;
        } else if (value.equals(PERIODICAL_VOLUME)) {
            return R.string.document_periodical_volume;
        } else if (value.equals(PERIODICAL_ITEM)) {
            return R.string.document_periodical_item;
        } else if (value.equals(PAGE)) {
            return R.string.document_page;
        } else if (value.equals(MANUSCRIPT)) {
            return R.string.document_manuscript;
        } else if (value.equals(MONOGRAPH)) {
            return R.string.document_monograph;
        } else if (value.equals(SOUND_RECORDING)) {
            return R.string.document_sound_recording;
        } else if (value.equals(SOUND_UNIT)) {
            return R.string.document_sound_unit;
        } else if (value.equals(TRACK)) {
            return R.string.document_track;
        } else if (value.equals(MAP)) {
            return R.string.document_map;
        } else if (value.equals(GRAPHIC)) {
            return R.string.document_graphic;
        } else if (value.equals(SHEET_MUSIC)) {
            return R.string.document_sheetmusic;
        } else if (value.equals(ARCHIVE)) {
            return R.string.document_archive;
        }

        return R.string.document_unknown;
    }

    public static int getIcon(String value) {
        if (value == null) {
            return R.drawable.ic_help_green;
        }
        if (value.equals(PERIODICAL)) {
            return R.drawable.ic_periodical_green;
        } else if (value.equals(PERIODICAL_VOLUME)) {
            return R.drawable.ic_periodical_green;
        } else if (value.equals(PERIODICAL_ITEM)) {
            return R.drawable.ic_periodical_green;
        } else if (value.equals(PAGE)) {
            return R.drawable.ic_page_green;
        } else if (value.equals(MANUSCRIPT)) {
            return R.drawable.ic_manuscript_green;
        } else if (value.equals(MONOGRAPH)) {
            return R.drawable.ic_book_green;
        } else if (value.equals(SOUND_RECORDING)) {
            return R.drawable.ic_music_green;
        } else if (value.equals(SOUND_UNIT)) {
            return R.drawable.ic_music_green;
        } else if (value.equals(TRACK)) {
            return R.drawable.ic_music_green;
        } else if (value.equals(MAP)) {
            return R.drawable.ic_map_green;
        } else if (value.equals(GRAPHIC)) {
            return R.drawable.ic_graphic_green;
        } else if (value.equals(SHEET_MUSIC)) {
            return R.drawable.ic_sheetmusic_green;
        } else if (value.equals(ARCHIVE)) {
            return R.drawable.ic_archive_green;
        }
        return R.drawable.ic_help_green;
    }

    public static void startActivityByModel(Activity activity, Item item) {
        Intent intent = null;
        if (TRACK.equals(item.getModel())) {
            intent = new Intent(activity, SoundRecordingActivity.class);
            intent.putExtra(BaseActivity.EXTRA_PID, item.getRootPid());
        } else {
            if (SOUND_RECORDING.equals(item.getModel())) {
                intent = new Intent(activity, SoundRecordingActivity.class);
                intent.putExtra(SoundRecordingActivity.EXTRA_SOUND_RECORDING_ITEM, item);
            } else if (PERIODICAL.equals(item.getModel())) {
                intent = new Intent(activity, PeriodicalActivity.class);
            } else if (PERIODICAL_VOLUME.equals(item.getModel())) {
                intent = new Intent(activity, PeriodicalActivity.class);
            } else {
                intent = new Intent(activity, PageActivity.class);
                // if(item.isPrivate()) {
                // intent.putExtra(PageActivity.EXTRA_SECURE, true);
                // }
            }
            intent.putExtra(BaseActivity.EXTRA_PID, item.getPid());
        }
        activity.startActivity(intent);
    }

}
