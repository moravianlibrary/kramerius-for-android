package cz.mzk.kramerius.app.data;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;
import android.text.TextUtils;
import cz.mzk.kramerius.app.R;
import cz.mzk.kramerius.app.data.KrameriusContract.HistoryEntry;
import cz.mzk.kramerius.app.data.KrameriusContract.InstitutionEntry;
import cz.mzk.kramerius.app.data.KrameriusContract.LanguageEntry;
import cz.mzk.kramerius.app.data.KrameriusContract.RelatorEntry;
import cz.mzk.kramerius.app.util.ModelUtil;

public class KrameriusDatabase extends SQLiteOpenHelper {

	private static final int DATABASE_VERSION_INITIAL = 1;
	private static final int DATABASE_VERSION_RELATORS = 2;
	private static final int DATABASE_VERSION_HISTORY = 3;
	private static final int DATABASE_VERSION_LOCALE_LANGUAGES = 4;
	private static final int DATABASE_VERSION_LOCALE_RELATORS = 5;
	private static final int DATABASE_VERSION_HISTORY_MODEL = 6;

	private static final int DATABASE_VERSION = DATABASE_VERSION_HISTORY_MODEL;

	private static final String DATABASE_NAME_INTERNAL = "kramerius.db";
	private static final String DATABASE_NAME_EXTERNAL = Environment
			.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/kramerius4.db";

	private static final String TABLE_TMP = "tmp";
	private static final String INDEX_INSTITUTION_SIGLA = InstitutionEntry.TABLE_NAME + "_"
			+ InstitutionEntry.COLUMN_SIGLA;
	private static final String INDEX_LANGUAGE_CODE = LanguageEntry.TABLE_NAME + "_" + LanguageEntry.COLUMN_CODE;
	private static final String INDEX_RELATOR_CODE = RelatorEntry.TABLE_NAME + "_" + RelatorEntry.COLUMN_CODE;

	private Context mContext;

	public KrameriusDatabase(Context context) {
		super(context, DATABASE_NAME_INTERNAL, null, DATABASE_VERSION);
		mContext = context;
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		// institution
		db.execSQL(buildStatementCreateTable(InstitutionEntry.TABLE_NAME, DATABASE_VERSION));
		db.execSQL(buildStatementCreateIndex(INDEX_INSTITUTION_SIGLA, DATABASE_VERSION));
		populateFrom(db, R.raw.institution);
		// language
		db.execSQL(buildStatementCreateTable(LanguageEntry.TABLE_NAME, DATABASE_VERSION));
		db.execSQL(buildStatementCreateIndex(INDEX_LANGUAGE_CODE, DATABASE_VERSION));
		populateFrom(db, R.raw.languages);
		// relator
		db.execSQL(buildStatementCreateTable(RelatorEntry.TABLE_NAME, DATABASE_VERSION));
		db.execSQL(buildStatementCreateIndex(INDEX_RELATOR_CODE, DATABASE_VERSION));
		populateFrom(db, R.raw.relators);
		// history
		db.execSQL(buildStatementCreateTable(HistoryEntry.TABLE_NAME, DATABASE_VERSION));
	}

	private String buildStatementCreateIndex(String indexName, int dbVersion) {
		if (indexName.equals(INDEX_INSTITUTION_SIGLA)) {
			return "CREATE INDEX " + indexName + " on "//
					+ InstitutionEntry.TABLE_NAME + "("//
					+ InstitutionEntry.COLUMN_SIGLA//
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
			if (dbVersion < DATABASE_VERSION_HISTORY_MODEL) {
				return "CREATE TABLE " + HistoryEntry.TABLE_NAME + " (" //
						+ HistoryEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"//
						+ HistoryEntry.COLUMN_DOMAIN + " TEXT NOT NULL, "//
						+ HistoryEntry.COLUMN_PID + " TEXT NOT NULL, "//
						+ HistoryEntry.COLUMN_PARENT_PID + " TEXT NOT NULL, "//
						+ HistoryEntry.COLUMN_TITLE + " TEXT, "//
						+ HistoryEntry.COLUMN_SUBTITLE + " TEXT, "//
						+ HistoryEntry.COLUMN_TIMESTAMP + " INTEGER NOT NULL"//
						+ ");";
			} else {
				return "CREATE TABLE " + HistoryEntry.TABLE_NAME + " (" //
						+ HistoryEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"//
						+ HistoryEntry.COLUMN_DOMAIN + " TEXT NOT NULL, "//
						+ HistoryEntry.COLUMN_PID + " TEXT NOT NULL, "//
						+ HistoryEntry.COLUMN_PARENT_PID + " TEXT NOT NULL, "//
						+ HistoryEntry.COLUMN_TITLE + " TEXT, "//
						+ HistoryEntry.COLUMN_SUBTITLE + " TEXT, "//
						+ HistoryEntry.COLUMN_TIMESTAMP + " INTEGER NOT NULL, "//
						+ HistoryEntry.COLUMN_MODEL + " TEXT NOT NULL "//
						+ ");";
			}
		} else if (tableName.equals(RelatorEntry.TABLE_NAME)) {
			if (dbVersion < DATABASE_VERSION_LOCALE_RELATORS) {
				return "CREATE TABLE " + RelatorEntry.TABLE_NAME + " (" //
						+ RelatorEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"//
						+ RelatorEntry.COLUMN_CODE + " TEXT NOT NULL, "//
						+ RelatorEntry.COLUMN_NAME + " TEXT NOT NULL," //
						+ ");";
			} else {
				return "CREATE TABLE " + RelatorEntry.TABLE_NAME + " (" //
						+ RelatorEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"//
						+ RelatorEntry.COLUMN_CODE + " TEXT NOT NULL, "//
						+ RelatorEntry.COLUMN_NAME + " TEXT NOT NULL," //
						+ RelatorEntry.COLUMN_LANG + " TEXT NOT NULL" //
						+ ");";
			}
		} else if (tableName.equals(InstitutionEntry.TABLE_NAME)) {
			return "CREATE TABLE " + KrameriusContract.InstitutionEntry.TABLE_NAME + " (" + InstitutionEntry._ID
					+ " INTEGER PRIMARY KEY AUTOINCREMENT," //
					+ InstitutionEntry.COLUMN_SIGLA + " TEXT NOT NULL, "//
					+ InstitutionEntry.COLUMN_NAME + " TEXT NOT NULL"//
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

			// relators
			db.execSQL(buildStatementCreateTable(RelatorEntry.TABLE_NAME, version + 1));
			db.execSQL(buildStatementCreateIndex(INDEX_RELATOR_CODE, version + 1));
			populateFrom(db, R.raw.relators_old_v2);

			version++;

		case DATABASE_VERSION_RELATORS:

			// histories
			db.execSQL(buildStatementCreateTable(HistoryEntry.TABLE_NAME, version + 1));

			version++;

		case DATABASE_VERSION_HISTORY:

			// is this really necessary? table & index don't seem to be created in previous versions
			db.execSQL("DROP INDEX IF EXISTS " + INDEX_LANGUAGE_CODE);
			db.execSQL("DROP TABLE IF EXISTS " + LanguageEntry.TABLE_NAME);

			// languages
			db.execSQL(buildStatementCreateTable(LanguageEntry.TABLE_NAME, version + 1));
			db.execSQL(buildStatementCreateIndex(INDEX_LANGUAGE_CODE, version + 1));
			populateFrom(db, R.raw.languages);

			version++;

		case DATABASE_VERSION_LOCALE_LANGUAGES:

			// relators - adding attribute 'lang'
			db.execSQL("DROP INDEX IF EXISTS " + INDEX_RELATOR_CODE);
			db.execSQL("DROP TABLE IF EXISTS " + RelatorEntry.TABLE_NAME);

			db.execSQL(buildStatementCreateTable(RelatorEntry.TABLE_NAME, version + 1));
			db.execSQL(buildStatementCreateIndex(INDEX_RELATOR_CODE, version + 1));
			populateFrom(db, R.raw.relators);

			version++;

		case DATABASE_VERSION_LOCALE_RELATORS:
			// history - adding attribute 'model' (present data transformed)
			// SQLite doesn't implement ALTER TABLE ADD CONSTRAINT (http: // www.sqlite.org/omitted.html)
			// So I can't add nullable column model, update all records with "page" and set column model to not nullable.
			db.execSQL("ALTER TABLE " + HistoryEntry.TABLE_NAME + " RENAME TO " + TABLE_TMP + ";");
			db.execSQL(buildStatementCreateTable(HistoryEntry.TABLE_NAME, version + 1));
			String[] columns = new String[] {//
			HistoryEntry.COLUMN_DOMAIN,//
					HistoryEntry.COLUMN_PARENT_PID,//
					HistoryEntry.COLUMN_PID,//
					HistoryEntry.COLUMN_SUBTITLE,//
					HistoryEntry.COLUMN_TIMESTAMP,//
					HistoryEntry.COLUMN_TITLE };
			Cursor cursor = db.query(TABLE_TMP, columns, null, null, null, null, null);
			boolean notEmpty = cursor.moveToFirst();
			if (notEmpty) {
				while (!cursor.isAfterLast()) {
					ContentValues values = new ContentValues();
					int index = 0;
					values.put(HistoryEntry.COLUMN_DOMAIN, cursor.getString(index++));
					values.put(HistoryEntry.COLUMN_MODEL, ModelUtil.PAGE);
					values.put(HistoryEntry.COLUMN_PARENT_PID, cursor.getString(index++));
					values.put(HistoryEntry.COLUMN_PID, cursor.getString(index++));
					values.put(HistoryEntry.COLUMN_SUBTITLE, cursor.getString(index++));
					values.put(HistoryEntry.COLUMN_TIMESTAMP, cursor.getInt(index++));
					values.put(HistoryEntry.COLUMN_TITLE, cursor.getString(index++));
					db.insert(HistoryEntry.TABLE_NAME, null, values);
					cursor.moveToNext();
				}
			}
			db.execSQL("DROP TABLE IF EXISTS " + TABLE_TMP);
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