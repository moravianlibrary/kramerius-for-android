package cz.mzk.kramerius.app.data;

import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;

import cz.mzk.kramerius.app.BuildConfig;

public class KrameriusContract {

    public static final String AUTHORITY_URI =  BuildConfig.APPLICATION_ID + ".provider";

    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + AUTHORITY_URI);

    public static final String PATH_INSTITUTION = "institution";
    public static final String PATH_LANGUAGE = "language";
    public static final String PATH_RELATOR = "relator";
    public static final String PATH_HISTORY = "history";
    public static final String PATH_SEARCH = "search";
    public static final String PATH_CACHE= "cache";
    public static final String PATH_LIBRARY = "library";

    public static final class InstitutionEntry implements BaseColumns {

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
        public static final String COLUMN_LANG = "lang";

    }

    public static final class CacheEntry implements BaseColumns {

        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_CACHE).build();

        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/" + AUTHORITY_URI + "/" + PATH_CACHE;

        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/" + AUTHORITY_URI + "/" + PATH_CACHE;

        public static final String TABLE_NAME = "cache";

        public static final String COLUMN_URL = "url";
        public static final String COLUMN_RESPONSE = "response";

        public static Uri buildCacheUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

        public static long getId(Uri uri) {
            return Long.parseLong(uri.getPathSegments().get(1));
        }

    }


    public static final class RelatorEntry implements BaseColumns {

        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_RELATOR).build();

        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/" + AUTHORITY_URI + "/" + PATH_RELATOR;

        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/" + AUTHORITY_URI + "/" + PATH_RELATOR;

        public static final String TABLE_NAME = "relator";

        public static final String COLUMN_CODE = "relator_code";
        public static final String COLUMN_NAME = "name";
        public static final String COLUMN_LANG = "lang";
    }

    public static final class HistoryEntry implements BaseColumns {

        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_HISTORY).build();

        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/" + AUTHORITY_URI + "/" + PATH_HISTORY;

        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/" + AUTHORITY_URI + "/" + PATH_HISTORY;

        public static final String TABLE_NAME = "history";

        public static final String COLUMN_DOMAIN = "domain";
        public static final String COLUMN_PID = "pid";
        public static final String COLUMN_PARENT_PID = "parent_pid";
        public static final String COLUMN_TITLE = "title";
        public static final String COLUMN_SUBTITLE = "subtitle";
        public static final String COLUMN_TIMESTAMP = "timestamp";
        public static final String COLUMN_MODEL = "model";

        public static Uri buildHistoryUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

        public static long getId(Uri uri) {
            return Long.parseLong(uri.getPathSegments().get(1));
        }


        public static final String[] PROJECTION = {
                COLUMN_DOMAIN, COLUMN_PID, COLUMN_PARENT_PID, COLUMN_TITLE,
                COLUMN_SUBTITLE, COLUMN_TIMESTAMP, COLUMN_MODEL
        };
    }

    public static final class LibraryEntry implements BaseColumns {

        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_LIBRARY).build();

        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/" + AUTHORITY_URI + "/" + PATH_LIBRARY;

        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/" + AUTHORITY_URI + "/"
                + PATH_LIBRARY;

        public static final String TABLE_NAME = "library";

        public static final String COLUMN_NAME = "name";
        public static final String COLUMN_DOMAIN = "domain";
        public static final String COLUMN_PROTOCOL = "protocol";
        public static final String COLUMN_CODE = "code";
        public static final String COLUMN_LOCKED = "locked";

        public static Uri buildLibraryUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }
    }


    public static final class SearchEntry implements BaseColumns {

        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_SEARCH).build();

        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/" + AUTHORITY_URI + "/" + PATH_SEARCH;

        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/" + AUTHORITY_URI + "/" + PATH_SEARCH;

        public static final String TABLE_NAME = "search";

        public static final String COLUMN_QUERY = "query";
        public static final String COLUMN_TIMESTAMP = "timestamp";


        public static Uri buildSearchUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

        public static final String[] PROJECTION = {
                COLUMN_QUERY, COLUMN_TIMESTAMP
        };
    }

}
