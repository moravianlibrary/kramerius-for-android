package cz.mzk.kramerius.app.data;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

import cz.mzk.kramerius.app.data.KrameriusContract.HistoryEntry;

public class KrameriusProvider extends ContentProvider {

    private static final int INSTITUTION = 100;
    private static final int LANGUAGE = 110;
    private static final int RELATOR = 120;
    private static final int HISTORY = 130;

    private static final UriMatcher sUriMatcher = buildUriMatcher();

    private KrameriusDatabase mOpenHelper;

    private static UriMatcher buildUriMatcher() {
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = KrameriusContract.AUTHORITY_URI;
        matcher.addURI(authority, KrameriusContract.PATH_INSTITUTION, INSTITUTION);
        matcher.addURI(authority, KrameriusContract.PATH_LANGUAGE, LANGUAGE);
        matcher.addURI(authority, KrameriusContract.PATH_RELATOR, RELATOR);
        matcher.addURI(authority, KrameriusContract.PATH_HISTORY, HISTORY);
        return matcher;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        throw new UnsupportedOperationException("Unknown uri: " + uri);
    }

    @Override
    public String getType(Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case INSTITUTION:
                return KrameriusContract.InstitutionEntry.CONTENT_TYPE;
            case LANGUAGE:
                return KrameriusContract.LanguageEntry.CONTENT_TYPE;
            case RELATOR:
                return KrameriusContract.RelatorEntry.CONTENT_TYPE;
            case HISTORY:
                return KrameriusContract.HistoryEntry.CONTENT_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown uri:" + uri);
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        Uri returnUri;
        switch (match) {
            case HISTORY: {
                long id = db.insert(HistoryEntry.TABLE_NAME, null, values);
                if (id > 0) {
                    returnUri = HistoryEntry.buildHistoryUri(id);
                } else {
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                }
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        return returnUri;
    }

    @Override
    public boolean onCreate() {
        mOpenHelper = new KrameriusDatabase(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        Cursor cursor;
        switch (sUriMatcher.match(uri)) {
            case INSTITUTION:
                cursor = mOpenHelper.getReadableDatabase().query(KrameriusContract.InstitutionEntry.TABLE_NAME, projection,
                        selection, selectionArgs, null, null, sortOrder);
                break;
            case LANGUAGE:
                cursor = mOpenHelper.getReadableDatabase().query(true, KrameriusContract.LanguageEntry.TABLE_NAME, projection,
                        selection, selectionArgs, null, null, sortOrder, null);
                break;
            case RELATOR:
                cursor = mOpenHelper.getReadableDatabase().query(KrameriusContract.RelatorEntry.TABLE_NAME, projection,
                        selection, selectionArgs, null, null, sortOrder);
                break;
            case HISTORY:
                cursor = mOpenHelper.getReadableDatabase().query(KrameriusContract.HistoryEntry.TABLE_NAME, projection,
                        selection, selectionArgs, null, null, sortOrder);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int rowsUpdated;
        switch (match) {
            case HISTORY:
                rowsUpdated = db.update(HistoryEntry.TABLE_NAME, values, selection, selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsUpdated;
    }
}
