package cz.mzk.kramerius.app.data;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

import cz.mzk.kramerius.app.data.KrameriusContract.HistoryEntry;
import cz.mzk.kramerius.app.data.KrameriusContract.SearchEntry;

public class KrameriusProvider extends ContentProvider {

    private static final int INSTITUTION = 100;
    private static final int LANGUAGE = 110;
    private static final int RELATOR = 120;
    private static final int HISTORY = 130;
    private static final int CACHE = 140;
    private static final int LIBRARY = 150;
    private static final int SEARCH = 160;

    private static final UriMatcher sUriMatcher = buildUriMatcher();

    private KrameriusDatabase mOpenHelper;

    private static UriMatcher buildUriMatcher() {
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = KrameriusContract.AUTHORITY_URI;
        matcher.addURI(authority, KrameriusContract.PATH_INSTITUTION, INSTITUTION);
        matcher.addURI(authority, KrameriusContract.PATH_LANGUAGE, LANGUAGE);
        matcher.addURI(authority, KrameriusContract.PATH_RELATOR, RELATOR);
        matcher.addURI(authority, KrameriusContract.PATH_HISTORY, HISTORY);
        matcher.addURI(authority, KrameriusContract.PATH_CACHE, CACHE);
        matcher.addURI(authority, KrameriusContract.PATH_LIBRARY, LIBRARY);
        matcher.addURI(authority, KrameriusContract.PATH_SEARCH, SEARCH);
        return matcher;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int delCount = 0;
        switch (match) {
            case CACHE:
                delCount = db.delete(
                        KrameriusContract.CacheEntry.TABLE_NAME,
                        selection,
                        selectionArgs);
                break;
            case LIBRARY:
                delCount = db.delete(
                        KrameriusContract.LibraryEntry.TABLE_NAME,
                        selection,
                        selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        return delCount;
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
            case CACHE:
                return KrameriusContract.CacheEntry.CONTENT_TYPE;
            case LIBRARY:
                return KrameriusContract.LibraryEntry.CONTENT_TYPE;
            case SEARCH:
                return KrameriusContract.SearchEntry.CONTENT_TYPE;
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
            case SEARCH: {
                long id = db.insert(SearchEntry.TABLE_NAME, null, values);
                if (id > 0) {
                    returnUri = SearchEntry.buildSearchUri(id);
                } else {
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                }
                break;
            }
            case CACHE: {
                long id = db.insert(KrameriusContract.CacheEntry.TABLE_NAME, null, values);
                if (id > 0) {
                    returnUri = KrameriusContract.CacheEntry.buildCacheUri(id);
                } else {
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                }
                break;
            }
            case LIBRARY: {
                long id = db.insert(KrameriusContract.LibraryEntry.TABLE_NAME, null, values);
                if (id > 0) {
                    returnUri = KrameriusContract.LibraryEntry.buildLibraryUri(id);
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
            case CACHE:
                cursor = mOpenHelper.getReadableDatabase().query(KrameriusContract.CacheEntry.TABLE_NAME, projection,
                        selection, selectionArgs, null, null, sortOrder);
                break;
            case LIBRARY:
                cursor = mOpenHelper.getReadableDatabase().query(KrameriusContract.LibraryEntry.TABLE_NAME, projection,
                        selection, selectionArgs, null, null, sortOrder);
                break;
            case SEARCH:
                cursor = mOpenHelper.getReadableDatabase().query(true, KrameriusContract.SearchEntry.TABLE_NAME, projection,
                        selection, selectionArgs, null, null, sortOrder, "5");
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
