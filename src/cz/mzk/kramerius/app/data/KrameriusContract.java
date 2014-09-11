package cz.mzk.kramerius.app.data;

import android.net.Uri;
import android.provider.BaseColumns;

public class KrameriusContract {

	public static final String AUTHORITY_URI = "cz.mzk.kramerius.app.kramerius";

	public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + AUTHORITY_URI);

	public static final String PATH_INSTITUTION = "institution";
	public static final String PATH_LANGUAGE = "language";

	public static final class InstitutuinEntry implements BaseColumns {

		public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_INSTITUTION).build();

		public static final String CONTENT_TYPE = "vnd.android.cursor.dir/" + AUTHORITY_URI + "/" + PATH_INSTITUTION;

		public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/" + AUTHORITY_URI + "/"
				+ PATH_INSTITUTION;

		public static final String TABLE_NAME = "institution";

		public static final String COLUMN_SIGLA = "sigla";
		public static final String COLUMN_NAME = "name";

	}

	public static final class LanguageEntry implements BaseColumns {

		public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_LANGUAGE).build();

		public static final String CONTENT_TYPE = "vnd.android.cursor.dir/" + AUTHORITY_URI + "/" + PATH_LANGUAGE;

		public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/" + AUTHORITY_URI + "/" + PATH_LANGUAGE;

		public static final String TABLE_NAME = "language";

		public static final String COLUMN_CODE = "lang_code";
		public static final String COLUMN_NAME = "name";

	}

}
