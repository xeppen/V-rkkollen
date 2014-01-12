package com.xeppen.varkkollen.sql;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import com.xeppen.varkkollen.model.Contraction;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class FeedReaderDbHelper extends SQLiteOpenHelper {
	// If you change the database schema, you must increment the database
	// version.
	static final String tName = "Contractions";
	static final String colID = "ContractionID";
	static final String colStartTime = "StartTime";
	static final String colStopTime = "StopTime";
	static final String colDurMin = "DurationMin";
	static final String colDurSec = "DurationSec";
	static final String colIntencity = "ContractionIntencity";
	static final String colNote = "ContractionNote";
	static final String colCreatedTime = "CreatedTime";

	public static final int DATABASE_VERSION = 1;
	public static final String DATABASE_NAME = "VarkKollen.db";
	static final String SQL_CREATE_ENTRIES = "CREATE TABLE IF NOT EXISTS "
			+ tName + " (" + colID + " integer primary key autoincrement, "
			+ colStartTime + " TEXT, " + colStopTime + " TEXT, " + colDurMin
			+ " INTEGER, " + colDurSec + " INTEGER, " + colIntencity
			+ " TEXT, " + colNote + " TEXT, " + colCreatedTime + " TEXT)";

	private static final String TEXT_TYPE = " TEXT";
	private static final String COMMA_SEP = ",";

	private static final String SQL_DELETE_ENTRIES = "DROP TABLE IF EXISTS "
			+ tName;

	public FeedReaderDbHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	public void onCreate(SQLiteDatabase db) {
		db.execSQL(SQL_CREATE_ENTRIES);
	}

	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// This database is only a cache for online data, so its upgrade policy
		// is
		// to simply to discard the data and start over
		db.execSQL(SQL_DELETE_ENTRIES);
		onCreate(db);
	}

	public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		onUpgrade(db, oldVersion, newVersion);
	}

	public void AddContraction(Contraction cont) {

		SQLiteDatabase db = this.getWritableDatabase();
		ContentValues cv = new ContentValues();

		cv.put(colStartTime, cont.getStartTime());
		cv.put(colStopTime, cont.getStopTime());
		cv.put(colDurMin, cont.getDurationMin());
		cv.put(colDurSec, cont.getDurationSec());
		cv.put(colIntencity, cont.getIntencity());
		cv.put(colNote, cont.getNote());
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Date date = new Date();
		cv.put(colCreatedTime, dateFormat.format(date));

		long newRowId;
		newRowId = db.insert(tName, colStartTime, cv);
		db.close();

	}

	public void updateContraction(String note, Integer i, Integer id) {
		SQLiteDatabase db = this.getWritableDatabase();

		ContentValues updatedValues = new ContentValues();

		// Assign values for each row.
		updatedValues.put(colIntencity, i);
		updatedValues.put(colNote, note);

		String where = colID + "=" + id;

		db.update(tName, updatedValues, where, null);
	}

	public void deleteContraction(Integer id) {
		SQLiteDatabase db = this.getWritableDatabase();

		String where = colID + "=" + id;

		db.delete(tName, where, null);
	}

	public List<Contraction> GetLatestCont() {
		SQLiteDatabase db = this.getReadableDatabase();
		List<Contraction> items = new ArrayList<Contraction>();
		
		String[] params = new String[] {};
		Cursor c = db.rawQuery("SELECT * FROM " + tName + " ORDER BY "
				+ colCreatedTime + " DESC", params);
		int i = 0;
		if (c.moveToFirst()) {
			int count = c.getCount();
			while (i < count && i < 2) {
				Contraction cur = new Contraction();

				cur.setId(Integer.parseInt(c.getString(0)));
				cur.setStartTime(c.getString(1));
				cur.setStopTime(c.getString(2));
				cur.setDurationMin(Integer.parseInt(c.getString(3)));
				cur.setDurationSec(Integer.parseInt(c.getString(4)));
				cur.setIntencity(Integer.parseInt(c.getString(5)));
				cur.setNote(c.getString(6));
				
				items.add(cur);

				c.moveToNext();
				i++;
			}
			c.close();
			return items;
		} else
			return null;
	}

	public int getNumberOfContractions() {
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor cur = db.rawQuery("Select * from " + tName, null);
		int x = cur.getCount();
		cur.close();
		return x;
	}

	public List<Contraction> getAllContractions() {
		List<Contraction> contractions = new ArrayList<Contraction>();
		SQLiteDatabase db = this.getReadableDatabase();

		String[] params = new String[] {};
		String query = "SELECT * from " + tName + " ORDER BY " + colCreatedTime
				+ " DESC";
		Cursor cursor = db.rawQuery(query, params);

		if (cursor.moveToFirst()) {
			while (!cursor.isAfterLast()) {
				Contraction cont = new Contraction();

				cont.setId(Integer.parseInt(cursor.getString(0)));
				cont.setStartTime(cursor.getString(1));
				cont.setStopTime(cursor.getString(2));
				cont.setDurationMin(Integer.parseInt(cursor.getString(3)));
				cont.setDurationSec(Integer.parseInt(cursor.getString(4)));
				cont.setIntencity(Integer.parseInt(cursor.getString(5)));
				cont.setNote(cursor.getString(6));

				contractions.add(cont);

				cursor.moveToNext();
			}
		}
		cursor.close();
		return contractions;
	}

	public List<Contraction> getPreviousHoursContractions(int hours) {
		List<Contraction> contractions = new ArrayList<Contraction>();
		SQLiteDatabase db = this.getReadableDatabase();

		SimpleDateFormat dateFormat = new SimpleDateFormat(
				"yyyy-MM-dd HH:mm:ss", Locale.ENGLISH);
		Calendar c = Calendar.getInstance();
		c.setTimeInMillis(System.currentTimeMillis());
		c.add(Calendar.HOUR_OF_DAY, -hours);

		String[] params = new String[] {};
		String query = "SELECT * from " + tName + " WHERE " + colCreatedTime
				+ " >= " + "'" + dateFormat.format(c.getTime()) + "'";
		Cursor cursor = db.rawQuery(query, params);

		if (cursor.moveToFirst()) {
			while (!cursor.isAfterLast()) {
				Contraction cont = new Contraction();

				cont.setId(Integer.parseInt(cursor.getString(0)));
				cont.setStartTime(cursor.getString(1));
				cont.setStopTime(cursor.getString(2));
				cont.setDurationMin(Integer.parseInt(cursor.getString(3)));
				cont.setDurationSec(Integer.parseInt(cursor.getString(4)));
				cont.setIntencity(Integer.parseInt(cursor.getString(5)));
				cont.setNote(cursor.getString(6));

				contractions.add(cont);

				cursor.moveToNext();
			}
		}
		cursor.close();
		return contractions;

	}

	public void clearDB() {
		SQLiteDatabase db = this.getReadableDatabase();
		db.execSQL("delete from " + tName);
	}

	public void populateTestData() {

	}
}