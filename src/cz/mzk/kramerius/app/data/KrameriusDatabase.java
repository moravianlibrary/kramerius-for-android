package cz.mzk.kramerius.app.data;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;
import cz.mzk.kramerius.app.R;
import cz.mzk.kramerius.app.data.KrameriusContract.InstitutuinEntry;
import cz.mzk.kramerius.app.data.KrameriusContract.LanguageEntry;

public class KrameriusDatabase extends SQLiteOpenHelper {

	private static final int DATABASE_VERSION_INITIAL = 1;

	private static final int DATABASE_VERSION = DATABASE_VERSION_INITIAL;

	private static final String DATABASE_NAME_INTERNAL = "kramerius.db";
	private static final String DATABASE_NAME_EXTERNAL = Environment
			.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/kramerius.db";

	private Context mContext;

	public KrameriusDatabase(Context context) {
		super(context, DATABASE_NAME_INTERNAL, null, DATABASE_VERSION);
		mContext = context;
	}

	@Override
	public void onCreate(SQLiteDatabase db) {

		final String SQL_CREATE_INSTITUTION_TABLE = "CREATE TABLE " + KrameriusContract.InstitutuinEntry.TABLE_NAME
				+ " (" + InstitutuinEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," + InstitutuinEntry.COLUMN_SIGLA
				+ " TEXT NOT NULL, " + InstitutuinEntry.COLUMN_NAME + " TEXT NOT NULL" + ");";

		final String SQL_CREATE_INSTITUTION_SIGLA_INDEX = "CREATE INDEX " + InstitutuinEntry.TABLE_NAME + "_"
				+ InstitutuinEntry.COLUMN_SIGLA + " on " + InstitutuinEntry.TABLE_NAME + "("
				+ InstitutuinEntry.COLUMN_SIGLA + ");";

		final String SQL_CREATE_LANGUAGE_TABLE = "CREATE TABLE " + LanguageEntry.TABLE_NAME + " (" + LanguageEntry._ID
				+ " INTEGER PRIMARY KEY AUTOINCREMENT," + LanguageEntry.COLUMN_CODE + " TEXT NOT NULL, "
				+ LanguageEntry.COLUMN_NAME + " TEXT NOT NULL" + ");";

		final String SQL_CREATE_LANGUAGE_CODE_INDEX = "CREATE INDEX " + LanguageEntry.TABLE_NAME + "_"
				+ LanguageEntry.COLUMN_CODE + " on " + LanguageEntry.TABLE_NAME + "(" + LanguageEntry.COLUMN_CODE
				+ ");";

		db.execSQL(SQL_CREATE_INSTITUTION_TABLE);
		db.execSQL(SQL_CREATE_INSTITUTION_SIGLA_INDEX);

		db.execSQL(SQL_CREATE_LANGUAGE_TABLE);
		db.execSQL(SQL_CREATE_LANGUAGE_CODE_INDEX);

		populateFrom(db, R.raw.institution);
		populateFrom(db, R.raw.languages);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		//TODO
	}
	
	
	private void populateFrom(SQLiteDatabase db, int resourceId) {		
		if (db == null || !db.isOpen()) {
			throw new IllegalStateException("Failed populate table");
		}

		InputStream is = mContext.getResources().openRawResource(resourceId);
		BufferedReader reader = new BufferedReader(new InputStreamReader(is));
		String line;
		try {
			db.beginTransaction();
			while ((line = reader.readLine()) != null) {
				if (!TextUtils.isEmpty(line) && !line.startsWith("--")) {					
					db.execSQL(line);
				}
			}
			is.close();
			reader.close();
			db.setTransactionSuccessful();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			db.endTransaction();
		}
	}
	
	
	

	
	
	
}
