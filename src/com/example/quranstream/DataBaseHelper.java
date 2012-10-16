package com.example.quranstream;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;
import android.util.Log;

public class DataBaseHelper extends SQLiteOpenHelper {

	// The Android's default system path of your application database.
	private static String DB_PATH = "/data/data/com.example.quranstream/databases/";
	private static String DB_NAME = "streamingquran.sqlite3";
	// private static String DB_NAME = "db";
	private SQLiteDatabase myDataBase;

	private final Context myContext;

	private static final String TABLE_RECITERS = "Reciters";
	private static final String ROW_ID_RECITERS = "_id";
	private static final String ROW_NAMA_RECITERS = "Name";
	private static final String ROW_NAMAARAB_RECITERS = "NameAr";
	private static final String ROW_LINK_RECITERS = "Link";
	private static final String ROW_FAV_RECITERS = "Fav";

	private static final String TABLE_SURAH = "Soras";
	private static final String ROW_ID_SURAH = "_id";
	private static final String ROW_NAMA_SURAH = "Name";
	private static final String ROW_NAMAARAB_SURAH = "NameAr";
	private static final String ROW_LINK_SURAH = "Link";
	private static final String ROW_FAV_SURAH = "Fav";

	/**
	 * Constructor Takes and keeps a reference of the passed context in order to
	 * access to the application assets and resources.
	 * 
	 * @param context
	 */
	public DataBaseHelper(Context context) {

		super(context, DB_NAME, null, 1);
		this.myContext = context;
	}

	/**
	 * Creates a empty database on the system and rewrites it with your own
	 * database.
	 * */
	public void createDataBase() throws IOException {

		boolean dbExist = checkDataBase();

		if (dbExist) {
			// do nothing - database already exist

		} else {

			// By calling this method and empty database will be created into
			// the default system path
			// of your application so we are gonna be able to overwrite that
			// database with our database.

			this.getReadableDatabase();
			this.close();
			try {

				copyDataBase();

			} catch (IOException e) {

				throw new Error("Error copying database");

			}
		}

	}

	/**
	 * Check if the database already exist to avoid re-copying the file each
	 * time you open the application.
	 * 
	 * @return true if it exists, false if it doesn't
	 */
	private boolean checkDataBase() {

		SQLiteDatabase checkDB = null;

		try {
			// String FOLDER_PATH = Environment.getExternalStorageDirectory()
			// .getAbsolutePath() + File.separator + "Pubkey";
			String myPath = DB_PATH + DB_NAME;
			checkDB = SQLiteDatabase.openDatabase(myPath, null,
					SQLiteDatabase.OPEN_READONLY);
			Log.d("checkdb", myContext.getAssets().toString());
		} catch (SQLiteException e) {
			Log.d("checkdb", "tidak ada");
			// database does't exist yet.

		}

		if (checkDB != null) {
			Log.d("checkdb", "close");
			checkDB.close();

		}

		return checkDB != null ? true : false;
	}

	/**
	 * Copies your database from your local assets-folder to the just created
	 * empty database in the system folder, from where it can be accessed and
	 * handled. This is done by transfering bytestream.
	 * */
	private void copyDataBase() throws IOException {

		// Open your local db as the input stream
		InputStream myInput = myContext.getAssets().open(DB_NAME);

		// Path to the just created empty db
		String outFileName = DB_PATH + DB_NAME;

		// Open the empty db as the output stream
		OutputStream myOutput = new FileOutputStream(outFileName);

		// transfer bytes from the inputfile to the outputfile
		byte[] buffer = new byte[1024];
		int length;
		while ((length = myInput.read(buffer)) > 0) {
			myOutput.write(buffer, 0, length);
		}

		// Close the streams
		myOutput.flush();
		myOutput.close();
		myInput.close();

	}

	public void openDataBase() throws SQLException {

		// Open the database
		String myPath = DB_PATH + DB_NAME;
		myDataBase = SQLiteDatabase.openDatabase(myPath, null,
				SQLiteDatabase.OPEN_READWRITE);

	}

	@Override
	public synchronized void close() {

		if (myDataBase != null)
			myDataBase.close();

		super.close();

	}

	@Override
	public void onCreate(SQLiteDatabase db) {

	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

	}

	// Add your public helper methods to access and get content from the
	// database.
	// You could return cursors by doing "return myDataBase.query(....)" so it'd
	// be easy
	// to you to create adapters for your views.
	// ------------------------------------------------------ //
	public ArrayList<ArrayList<Object>> AmbilReciters() {

		ArrayList<ArrayList<Object>> dataArray = new ArrayList<ArrayList<Object>>();
		Cursor cur;
		try {
			// cur = myDataBase.query(TABLE_RECITERS, new String[] {
			// ROW_ID_RECITERS, ROW_NAMA_RECITERS, ROW_LINK_RECITERS },
			// null, null, null, null, null);
			// SELECT * FROM Reciters ORDER BY Fav desc
			cur = myDataBase.rawQuery(
					"SELECT * FROM Reciters ORDER BY Fav desc", null);
			cur.moveToFirst();
			if (!cur.isAfterLast()) {
				do {
					ArrayList<Object> dataList = new ArrayList<Object>();
					dataList.add(cur.getInt(0));
					dataList.add(cur.getString(1));
					// Log.d("nama", cur.getString(1));
					dataList.add(cur.getString(2));
					// Log.d("nama", cur.getString(2));
					dataList.add(cur.getString(3));

					dataList.add(cur.getString(4));

					dataArray.add(dataList);

				} while (cur.moveToNext());

			}

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.e("DEBE ERROR", e.toString());

		}
		return dataArray;
	}

	public ArrayList<Object> ambilBaris(int rowId) {
		ArrayList<Object> arrbaris = new ArrayList<Object>();
		Cursor cursor;
		try {
			cursor = myDataBase
					.query(TABLE_RECITERS, new String[] { ROW_ID_RECITERS,
							ROW_NAMA_RECITERS, ROW_LINK_RECITERS ,ROW_FAV_RECITERS},
							ROW_ID_RECITERS + "=" + rowId, null, null, null,
							null, null);
			cursor.moveToFirst();
			if (!cursor.isAfterLast()) {
				do {
					arrbaris.add(cursor.getLong(0));
					arrbaris.add(cursor.getString(1));
					arrbaris.add(cursor.getString(2));
					arrbaris.add(cursor.getString(3));
				} while (cursor.moveToNext());
			}
			cursor.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.e("error", e.toString());
		}

		return arrbaris;
	}

	public ArrayList<ArrayList<Object>> AmbilSurat() {

		ArrayList<ArrayList<Object>> dataArray = new ArrayList<ArrayList<Object>>();
		Cursor cur;
		try {
			cur = myDataBase.query(TABLE_SURAH, new String[] { ROW_ID_SURAH,
					ROW_NAMA_SURAH, ROW_NAMAARAB_SURAH, ROW_LINK_SURAH }, null,
					null, null, null, null);
			cur.moveToFirst();
			if (!cur.isAfterLast()) {
				do {
					ArrayList<Object> dataList = new ArrayList<Object>();
					// dataList.add(cur.getLong(0));
					dataList.add(cur.getInt(0));
					dataList.add(cur.getString(1));
					dataList.add(cur.getString(2));
					dataList.add(cur.getString(3));

					dataArray.add(dataList);

				} while (cur.moveToNext());

			}

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.e("DEBE ERROR", e.toString());

		}
		return dataArray;
	}

	public void setFavorit(int rowId, int st) {
		ContentValues cv = new ContentValues();
		Log.d("id row", String.valueOf(rowId));
		Log.d("int", String.valueOf(st));
		cv.put(ROW_FAV_RECITERS, st);
		try {
			myDataBase.update(TABLE_RECITERS, cv,
					ROW_ID_RECITERS + "=" + rowId, null);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.e("Db Error", e.toString());
		}
	}
}