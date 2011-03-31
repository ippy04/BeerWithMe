package com.BeerMessenger;

import java.util.ArrayList;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Simple picture database access helper class. Defines the basic CRUD operations
 * for the picture info, and gives the ability to list all pictures as well as
 * retrieve or modify a specific picture.
 * 
 */
public class PictureInfoDbHelper {

	public static final String KEY_PICTURE_PATH = "picturePath";
	public static final String KEY_MESSAGE = "message";
	public static final String KEY_ROWID = "_id";
	public static final String KEY_URL = "url";
	public static final String KEY_CONTACT_NAME = "contactName";
	public static final String KEY_CONTACT_NUMBER = "contactNumber";
	public static final String KEY_SENDERS_NAME = "sendersName";
	
	private DatabaseHelper mDbHelper;
	private SQLiteDatabase mDb;

	/**
	 * Database creation sql statement
	 */
	private static final String DATABASE_CREATE =
		"create table pictureInfo (_id integer primary key autoincrement, "
		+ KEY_PICTURE_PATH + " text not null, "
		+ KEY_MESSAGE + " text not null, "
		+ KEY_URL + " text not null, "
		+ KEY_CONTACT_NAME + " text not null, "
		+ KEY_CONTACT_NUMBER + " text not null, "
		+ KEY_SENDERS_NAME + " text not null)";

	private static final String DATABASE_NAME = "data";
	private static final String DATABASE_TABLE = "pictureInfo";
	private static final int DATABASE_VERSION = 1;

	private final Context mCtx;

	private static class DatabaseHelper extends SQLiteOpenHelper {

		DatabaseHelper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {

			db.execSQL(DATABASE_CREATE);
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			db.execSQL("DROP TABLE IF EXISTS "+DATABASE_TABLE);
			onCreate(db);
		}
	}

	/**
	 * Constructor - takes the context to allow the database to be
	 * opened/created
	 * 
	 * @param ctx the Context within which to work
	 */
	public PictureInfoDbHelper(Context ctx) {
		this.mCtx = ctx;
	}

	/**
	 * Open the picture database. If it cannot be opened, try to create a new
	 * instance of the database. If it cannot be created, throw an exception to
	 * signal the failure
	 * 
	 * @return this (self reference, allowing this to be chained in an
	 *         initialization call)
	 * @throws SQLException if the database could be neither opened or created
	 */
	public PictureInfoDbHelper open() throws SQLException {
		mDbHelper = new DatabaseHelper(mCtx);
		mDb = mDbHelper.getWritableDatabase();
		return this;
	}

	public void close() {
		mDbHelper.close();
	}


	/**
	 * Create a new picture using the info. If the picture is
	 * successfully created return the new rowId for that picture, otherwise return
	 * a -1 to indicate failure.
	 * 
	 * @return rowId or -1 if failed
	 */
	public long createPicture(String _picturePath, 
			String _message, 
			String _url, 
			String _contactName, 
			String _contactNumber, 
			String _sendersName){
		
		ContentValues initialValues = new ContentValues();
		initialValues.put(KEY_PICTURE_PATH, _picturePath);
		initialValues.put(KEY_MESSAGE, _message);
		initialValues.put(KEY_URL, _url);
		initialValues.put(KEY_CONTACT_NAME, _contactName);
		initialValues.put(KEY_CONTACT_NUMBER, _contactNumber);
		initialValues.put(KEY_SENDERS_NAME, _sendersName);
				

		return mDb.insert(DATABASE_TABLE, null, initialValues);
	}

	/**
	 * Delete the picture with the given rowId
	 * 
	 * @param rowId id of picture to delete
	 * @return true if deleted, false otherwise
	 */
	public boolean deletePicture(long rowId) {

		return mDb.delete(DATABASE_TABLE, KEY_ROWID + "=" + rowId, null) > 0;
	}

	/**
	 * Return a Cursor over the list of all pictures in the database
	 * 
	 * @return Cursor over all pictures
	 */
	public Cursor fetchAllPictures() {

		return mDb.query(DATABASE_TABLE, null, null, null, null, null, null);
	}	
	
	/**
	 * Return an arraylist of all the picture file names
	 * 
	 * @return ArrayList of all picture names
	 */
	public ArrayList<String> fetchAllPictureNames() {

		// grab cursor to pictures
		Cursor cursor = fetchAllPictures();
		
		// initialize array
		ArrayList<String> pictures = new ArrayList<String>();
		
		// null cursor, just return empty arraylist
		if (cursor==null)
			return pictures;
		
		// if empty cursor then return empty arraylist
		if (!cursor.moveToFirst()){
			cursor.close();
			return pictures;
		}
		
		// loop across cursor grabbing picture names
		while (true){
			pictures.add(cursor.getString(cursor.getColumnIndex(KEY_PICTURE_PATH)));
			if (!cursor.moveToNext())
				break;
		}
			
		cursor.close();
		return pictures;
	}
	
	/**
	 * Return an arraylist of all the picture file names
	 * 
	 * @return ArrayList of all messages
	 */
	public ArrayList<String> fetchAllMessages() {

		// grab cursor to pictures
		Cursor cursor = fetchAllPictures();
		
		// initialize array
		ArrayList<String> pictures = new ArrayList<String>();
		
		// null cursor, just return empty arraylist
		if (cursor==null)
			return pictures;
		
		// if empty cursor then return empty arraylist
		if (!cursor.moveToFirst()){
			cursor.close();
			return pictures;
		}
		
		// loop across cursor grabbing picture names
		while (true){
			pictures.add(cursor.getString(cursor.getColumnIndex(KEY_MESSAGE)));
			if (!cursor.moveToNext())
				break;
		}
			
		cursor.close();
		return pictures;
	}
	

	/**
	 * Return a Cursor positioned at the picture that matches the given rowId
	 * 
	 * @param rowId id of picture to retrieve
	 * @return Cursor positioned to matching picture, if found
	 * @throws SQLException if picture could not be found/retrieved
	 */
	public Cursor fetchPicture(long rowId) throws SQLException {

		Cursor mCursor =

			mDb.query(true, DATABASE_TABLE, null, KEY_ROWID + "=" + rowId, null,
					null, null, null, null);
		if (mCursor != null) {
			mCursor.moveToFirst();
		}
		return mCursor;

	}

	/**
	 * Update the picture using the details provided. The picture to be updated is
	 * specified using the rowId, and it is altered to use the values passed in
	 * 
	 * @param rowId id of picture to update
	 * @return true if the picture was successfully updated, false otherwise
	 */
	public boolean updatePicture(long rowId, 
			String _picturePath, 
			String _message, 
			String _url, 
			String _contactName, 
			String _contactNumber, 
			String _sendersName){
		
		ContentValues args = new ContentValues();
		args.put(KEY_PICTURE_PATH, _picturePath);
		args.put(KEY_MESSAGE, _message);
		args.put(KEY_URL, _url);
		args.put(KEY_CONTACT_NAME, _contactName);
		args.put(KEY_CONTACT_NUMBER, _contactNumber);
		args.put(KEY_SENDERS_NAME, _sendersName);

		return mDb.update(DATABASE_TABLE, args, KEY_ROWID + "=" + rowId, null) > 0;
	}
}
