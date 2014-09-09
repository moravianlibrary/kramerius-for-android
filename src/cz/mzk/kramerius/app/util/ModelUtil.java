package cz.mzk.kramerius.app.util;

import cz.mzk.kramerius.app.R;

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

}
