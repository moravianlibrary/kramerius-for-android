package cz.mzk.kramerius.app.data;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.net.Uri;

public class KrameriusProvider extends ContentProvider {

	private static final int INSTITUTION = 100;
	private static final int LANGUAGE = 110;

	private static final UriMatcher sUriMatcher = buildUriMatcher();

	private KrameriusDatabase mOpenHelper;

	private static UriMatcher buildUriMatcher() {
		final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
		final String authority = KrameriusContract.AUTHORITY_URI;
		matcher.addURI(authority, KrameriusContract.PATH_INSTITUTION, INSTITUTION);
		matcher.addURI(authority, KrameriusContract.PATH_LANGUAGE, LANGUAGE);
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
			return KrameriusContract.InstitutuinEntry.CONTENT_TYPE;
		case LANGUAGE:
			return KrameriusContract.LanguageEntry.CONTENT_TYPE;
		default:
			throw new UnsupportedOperationException("Unknown uri:" + uri);
		}
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		throw new UnsupportedOperationException("Unknown uri: " + uri);
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
			cursor = mOpenHelper.getReadableDatabase().query(KrameriusContract.InstitutuinEntry.TABLE_NAME, projection,
					selection, selectionArgs, null, null, sortOrder);
			break;
		case LANGUAGE:
			cursor = mOpenHelper.getReadableDatabase().query(KrameriusContract.LanguageEntry.TABLE_NAME, projection,
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
		throw new UnsupportedOperationException("Unknown uri: " + uri);
	}

}
