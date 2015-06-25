package cz.mzk.kramerius.app.data;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;
import android.text.TextUtils;
import cz.mzk.kramerius.app.R;
import cz.mzk.kramerius.app.data.KrameriusContract.HistoryEntry;
import cz.mzk.kramerius.app.data.KrameriusContract.InstitutuinEntry;
import cz.mzk.kramerius.app.data.KrameriusContract.LanguageEntry;
import cz.mzk.kramerius.app.data.KrameriusContract.RelatorEntry;

public class KrameriusDatabase extends SQLiteOpenHelper {

	private static final int DATABASE_VERSION_INITIAL = 1;
	private static final int DATABASE_VERSION_RELATORS = 2;
	private static final int DATABASE_VERSION_HISTORY = 3;
	private static final int DATABASE_VERSION_LOCALE_LANGUAGES = 4;
	private static final int DATABASE_VERSION_LOCALE_RELATORS = 5;

	private static final int DATABASE_VERSION = DATABASE_VERSION_LOCALE_RELATORS;

	private static final String DATABASE_NAME_INTERNAL = "kramerius.db";
	private static final String DATABASE_NAME_EXTERNAL = Environment
			.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/kramerius4.db";

	private static final String INDEX_INSTITUTION_SIGLA = InstitutuinEntry.TABLE_NAME + "_"
			+ InstitutuinEntry.COLUMN_SIGLA;
	private static final String INDEX_LANGUAGE_CODE = LanguageEntry.TABLE_NAME + "_" + LanguageEntry.COLUMN_CODE;
	private static final String INDEX_RELATOR_CODE = RelatorEntry.TABLE_NAME + "_" + RelatorEntry.COLUMN_CODE;

	private Context mContext;

	public KrameriusDatabase(Context context) {
		super(context, DATABASE_NAME_INTERNAL, null, DATABASE_VERSION);
		mContext = context;
	}

	@Override
	public void onCreate(SQLiteDatabase db) {

		db.execSQL(buildStatementCreateTable(InstitutuinEntry.TABLE_NAME, DATABASE_VERSION));
		db.execSQL(buildStatementCreateIndex(INDEX_INSTITUTION_SIGLA, DATABASE_VERSION));

		db.execSQL(buildStatementCreateTable(LanguageEntry.TABLE_NAME, DATABASE_VERSION));
		db.execSQL(buildStatementCreateIndex(INDEX_LANGUAGE_CODE, DATABASE_VERSION));

		db.execSQL(buildStatementCreateTable(RelatorEntry.TABLE_NAME, DATABASE_VERSION));
		db.execSQL(buildStatementCreateIndex(INDEX_RELATOR_CODE, DATABASE_VERSION));

		db.execSQL(buildStatementCreateTable(HistoryEntry.TABLE_NAME, DATABASE_VERSION));

		populateFrom(db, R.raw.institution);
		populateFrom(db, R.raw.languages);
		populateFrom(db, R.raw.relators);
	}

	private String buildStatementCreateIndex(String indexName, int dbVersion) {
		if (indexName.equals(INDEX_INSTITUTION_SIGLA)) {
			return "CREATE INDEX " + indexName + " on "//
					+ InstitutuinEntry.TABLE_NAME + "("//
					+ InstitutuinEntry.COLUMN_SIGLA//
					+ ");";
		} else if (indexName.equals(INDEX_LANGUAGE_CODE)) {
			return "CREATE INDEX " + indexName + " on "//
					+ LanguageEntry.TABLE_NAME + "(" + LanguageEntry.COLUMN_CODE + ");";
		} else if (indexName.equals(INDEX_RELATOR_CODE)) {
			return "CREATE INDEX " + indexName + " on "//
					+ RelatorEntry.TABLE_NAME + "(" + RelatorEntry.COLUMN_CODE + ");";
		} else {
			throw new IllegalArgumentException("unknown index " + indexName);
		}

	}

	private String buildStatementCreateTable(String tableName, int dbVersion) {
		if (tableName.equals(HistoryEntry.TABLE_NAME)) {
			return "CREATE TABLE " + HistoryEntry.TABLE_NAME + " (" //
					+ HistoryEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"//
					+ HistoryEntry.COLUMN_DOMAIN + " TEXT NOT NULL, "//
					+ HistoryEntry.COLUMN_PID + " TEXT NOT NULL, "//
					+ HistoryEntry.COLUMN_PARENT_PID + " TEXT NOT NULL, "//
					+ HistoryEntry.COLUMN_TITLE + " TEXT, "//
					+ HistoryEntry.COLUMN_SUBTITLE + " TEXT, "//
					+ HistoryEntry.COLUMN_TIMESTAMP + " INTEGER NOT NULL"//
					+ ");";
		} else if (tableName.equals(RelatorEntry.TABLE_NAME)) {
			return "CREATE TABLE " + RelatorEntry.TABLE_NAME + " (" //
					+ RelatorEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"//
					+ RelatorEntry.COLUMN_CODE + " TEXT NOT NULL, "//
					+ RelatorEntry.COLUMN_NAME + " TEXT NOT NULL," //
					+ RelatorEntry.COLUMN_LANG + " TEXT NOT NULL" //
					+ ");";
		} else if (tableName.equals(InstitutuinEntry.TABLE_NAME)) {
			return "CREATE TABLE " + KrameriusContract.InstitutuinEntry.TABLE_NAME + " (" + InstitutuinEntry._ID
					+ " INTEGER PRIMARY KEY AUTOINCREMENT," //
					+ InstitutuinEntry.COLUMN_SIGLA + " TEXT NOT NULL, "//
					+ InstitutuinEntry.COLUMN_NAME + " TEXT NOT NULL"//
					+ ");";
		} else if (tableName.equals(LanguageEntry.TABLE_NAME)) {
			return "CREATE TABLE " + LanguageEntry.TABLE_NAME + " ("//
					+ LanguageEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"//
					+ LanguageEntry.COLUMN_CODE + " TEXT NOT NULL, "//
					+ LanguageEntry.COLUMN_NAME + " TEXT NOT NULL, " //
					+ LanguageEntry.COLUMN_LANG + " TEXT NOT NULL" //
					+ ");";
		}
		throw new IllegalArgumentException("unknown table " + tableName);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		int version = oldVersion;
		switch (version) {
		case DATABASE_VERSION_INITIAL:
			db.execSQL(buildStatementCreateTable(RelatorEntry.TABLE_NAME, DATABASE_VERSION_RELATORS));
			db.execSQL(buildStatementCreateIndex(INDEX_RELATOR_CODE, DATABASE_VERSION_RELATORS));
			populateFrom(db, R.raw.relators_old_v2);

			version = DATABASE_VERSION_RELATORS;

		case DATABASE_VERSION_RELATORS:
			db.execSQL(buildStatementCreateTable(HistoryEntry.TABLE_NAME, DATABASE_VERSION_LOCALE_LANGUAGES));

			version = DATABASE_VERSION_HISTORY;

		case DATABASE_VERSION_HISTORY:
			db.execSQL("DROP INDEX IF EXISTS " + INDEX_LANGUAGE_CODE);
			db.execSQL("DROP TABLE IF EXISTS " + LanguageEntry.TABLE_NAME);

			db.execSQL(buildStatementCreateTable(LanguageEntry.TABLE_NAME, DATABASE_VERSION_LOCALE_LANGUAGES));
			db.execSQL(buildStatementCreateIndex(INDEX_LANGUAGE_CODE, DATABASE_VERSION_LOCALE_LANGUAGES));
			populateFrom(db, R.raw.languages);

			version = DATABASE_VERSION_LOCALE_LANGUAGES;

		case DATABASE_VERSION_LOCALE_LANGUAGES:
			db.execSQL("DROP INDEX IF EXISTS " + INDEX_RELATOR_CODE);
			db.execSQL("DROP TABLE IF EXISTS " + RelatorEntry.TABLE_NAME);

			db.execSQL(buildStatementCreateTable(RelatorEntry.TABLE_NAME, DATABASE_VERSION_RELATORS));
			db.execSQL(buildStatementCreateIndex(INDEX_RELATOR_CODE, DATABASE_VERSION_RELATORS));
			populateFrom(db, R.raw.relators);

			version = DATABASE_VERSION_LOCALE_RELATORS;
		}
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
