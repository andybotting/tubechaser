/*  
 * Copyright 2010 Andy Botting <andy@andybotting.com>  
 *  
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *  
 * This file is distributed in the hope that it will be useful, but  
 * WITHOUT ANY WARRANTY; without even the implied warranty of  
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU  
 * General Public License for more details.  
 *  
 * You should have received a copy of the GNU General Public License  
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.  
 *  
 * This file incorporates work covered by the following copyright and  
 * permission notice:
 * 
 * Copyright 2010 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.andybotting.tubechaser.provider;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.andybotting.tubechaser.utils.PreferenceHelper;

import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;
import android.util.Log;


public class TubeChaserDatabase extends SQLiteOpenHelper {
	
    private static final String TAG = "TubeChaserDatabase";
    private static final boolean LOGV = Log.isLoggable(TAG, Log.INFO);

	private static final String AUTHORITY = "com.andybotting.tubechaser";
	private static final String DATABASE_NAME = "tubechaser.db";
	private static final String DATABASE_INTERNAL_PATH = "/data/data/"+ AUTHORITY + "/databases/";
    
	// Match the DB version with the app version code
	private static final int DATABASE_VERSION = 394;
	
	private SQLiteDatabase mDB = null;
	private Context mContext;
	private boolean mIsInitializing = false;

	// Are we using the internal database?
	private boolean mIsDBInternal = true;


    interface Tables {
    	String LINES = "lines";
    	String STATIONS = "stations";
    	String LINES_STATIONS = "lines_stations";
    	
    	String STATIONS_JOIN_LINES = "stations "
    		+ "JOIN lines_stations ON lines_stations.station_id = stations._id "
    		+ "JOIN lines ON lines_stations.line_id = lines._id";
    }
    

    public TubeChaserDatabase(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
		mContext = context;
		if (LOGV) Log.v(TAG, "Instantiating TubeChaser database");
	}	
	
    
	public SQLiteDatabase getDatabase() {
		SQLiteDatabase db;  
		
		if (LOGV) Log.v(TAG, "Getting DB");
		db = getExternalStorageDB();
		if (db == null) {
			if (LOGV) Log.v(TAG, "DB from SD Card failed, using internal");
			db = getInternalStorageDB();
		}

		return db;
	}

	/**
	 * Return the SQL Database from the internal device storage
	 * @return
	 */
	private SQLiteDatabase getInternalStorageDB() {
		if (LOGV) Log.v(TAG, "Getting DB from device internal storage");

		SQLiteDatabase db = null;
		String dbFile = DATABASE_INTERNAL_PATH + DATABASE_NAME;

		try {		 	
			this.createDB(dbFile);
		} 
		catch (IOException ioe) {
			throw new Error("Unable to create database:" + ioe);
		}

		try {
			this.openDB(dbFile);
		}
		catch(SQLException sqle){
			throw sqle;
		}

		mIsDBInternal = true;
		db = getWritableDatabase(dbFile);
		return db;
	}


	/**
	 * Return the SQL Database from external storage
	 */
	private SQLiteDatabase getExternalStorageDB() {
		if (LOGV) Log.v(TAG, "Getting DB from external storage (SD Card)");

		SQLiteDatabase db = null;

        if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED))
            return null;

        // Build the directory on the SD Card, if it doesn't exist
        File appDbDir = new File(Environment.getExternalStorageDirectory(), "Android/data/" + AUTHORITY + "/files");
        if (!appDbDir.exists()) {
        	if (LOGV) Log.v(TAG, "Making dirs");
        	appDbDir.mkdirs();
        }

        // Our dbFile at /mnt/sdcard/Android/data/com.andybotting.tubechaser/files/tubechaser.db
        File dbFileObj = new File(appDbDir, DATABASE_NAME);
        String dbFile = dbFileObj.getAbsolutePath();
        
		try {	 	
			this.createDB(dbFile);
		} 
		catch (IOException ioe) {
			throw new Error("Unable to create database:" + ioe);
		}

		try {
			this.openDB(dbFile);
		}
		catch(SQLException sqle){
			throw sqle;
		}

		mIsDBInternal = false;
		db = getWritableDatabase(dbFile);
		return db;
	}

	/**
	 * Create the initial database at a given file path
	 * @param dbFile - a String representing the absolute file name
	 * @throws IOException
	 */
	public void createDB(String dbFile) throws IOException{

		boolean dbExist = checkDB(dbFile);
 
		if (dbExist) {
			if (LOGV) Log.v(TAG, "Found existing DB at " + dbFile);

			mDB = SQLiteDatabase.openDatabase(dbFile, null, SQLiteDatabase.OPEN_READONLY);
			int thisDBVersion = mDB.getVersion();
			mDB.close();
			
			if (LOGV) Log.v(TAG, "Current DB Version: v" + thisDBVersion + " - Shipped DB Version is v" + DATABASE_VERSION);
			if (thisDBVersion != DATABASE_VERSION) {							
				try {
					copyDB(dbFile);
				} catch (IOException e) {
					throw new Error("Error copying database");
				}
			}	
		}
		else {
			if (LOGV) Log.v(TAG, "Creating a new DB at " + dbFile);
			// By calling this method and empty database will be created into the default system path
			// of your application so we are gonna be able to overwrite that database with our database.
			mDB = getReadableDatabase(dbFile);
			
			try {
				copyDB(dbFile);
			} catch (IOException e) {
				throw new Error("Error copying database: " + e);
			}
		}
	}
 
	
	/**
	 * Check if the database already exist to avoid re-copying the file each time you open the application.
	 * @return true if it exists, false if it doesn't
	 */
	private boolean checkDB(String dbFile){
 		SQLiteDatabase checkDB = null;
 		if (LOGV) Log.v(TAG, "Checking for an existing DB at " + dbFile);
 		
		try {
			checkDB = SQLiteDatabase.openDatabase(dbFile, null, SQLiteDatabase.OPEN_READONLY);
		}
		catch(SQLiteException e){
			//database does't exist yet.
		}
 
		if(checkDB != null)
			checkDB.close();
 
		return checkDB != null ? true : false;
	}
 
	/**
	 * Copies your database from your local assets-folder to the just created empty database in the
	 * system folder, from where it can be accessed and handled.
	 * This is done by transfering bytestream.
	 * */
	private void copyDB(String dbFile) throws IOException {
		
		if (LOGV) Log.v(TAG, "Resetting last fetched timestamp");
		
		// Reset out last fetched status date
		PreferenceHelper preferenceHelper = new PreferenceHelper();
		preferenceHelper.resetLastUpdateTimestamp();
		
		if (LOGV) Log.v(TAG, "Copying packaged DB to " + dbFile);
		
		// Open your local db as the input stream
		InputStream is = mContext.getAssets().open(DATABASE_NAME);

		// Open the empty db as the output stream
		OutputStream os = new FileOutputStream(dbFile);
		
		// Transfer bytes from the inputfile to the outputfile
		byte[] buffer = new byte[1024];
		int length;
		while ( (length = is.read(buffer) ) > 0) {
			os.write(buffer, 0, length);
		}

		// Close the streams
		os.flush();
		os.close();
		is.close();
		
		if (LOGV) Log.v(TAG, "DB copying completed");
	}

	public void openDB(String dbFile) throws SQLException {
	 	// Open the database
		mDB = SQLiteDatabase.openDatabase(dbFile, null, SQLiteDatabase.OPEN_READONLY);
		// Close the DB to prevent a leak
		mDB.close();
	}

	@Override
	public synchronized void close() {
		if(mDB != null)
			mDB.close();
		super.close();
	}
 
	@Override
	public void onCreate(SQLiteDatabase db) {
		if (LOGV) Log.v(TAG, "DB onCreate() called");
		// Do nothing here
	}
 
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		if (LOGV) Log.v(TAG, "DB onUpgrade() called");
		// Do nothing here
	}
	

	public synchronized SQLiteDatabase getReadableDatabase(String dbFile) {
		if(mIsDBInternal) {
			return super.getReadableDatabase();
		}
		
		
		if (mDB != null && mDB.isOpen()) {
			return mDB; // The database is already open for business
		}

		if (mIsInitializing) {
			throw new IllegalStateException("getReadableDatabase called recursively");
		}

		try {
			return getWritableDatabase();
		} catch (SQLiteException e) {
			Log.e(TAG, "Couldn't open " + DATABASE_NAME + " for writing (will try read-only):", e);
		}

		SQLiteDatabase db = null;
		try {
			mIsInitializing = true;
			db = SQLiteDatabase.openDatabase(dbFile, null, SQLiteDatabase.OPEN_READONLY);
			if (db.getVersion() != DATABASE_VERSION) {
				throw new SQLiteException("Can't upgrade read-only database from version " + db.getVersion() + " to " + DATABASE_VERSION + ": " + dbFile);
			}

			onOpen(db);
			if (LOGV) Log.v(TAG, "Opened " + DATABASE_NAME + " in read-only mode");
			mDB = db;
			return mDB;
		} finally {
			mIsInitializing = false;
			if (db != null && db != mDB)
				db.close();
		}
	}


	public synchronized SQLiteDatabase getWritableDatabase(String dbFile) {
		if(mIsDBInternal) {
			return super.getWritableDatabase();
		}
		if (mDB != null && mDB.isOpen() && !mDB.isReadOnly()) {
			return mDB; // The database is already open for business
		}

		if (mIsInitializing) {
			throw new IllegalStateException("getWritableDatabase called recursively");
		}

		// If we have a read-only database open, someone could be using it
		// (though they shouldn't), which would cause a lock to be held on
		// the file, and our attempts to open the database read-write would
		// fail waiting for the file lock. To prevent that, we acquire the
		// lock on the read-only database, which shuts out other users.

		boolean success = false;
		SQLiteDatabase db = null;
		// if (mDatabase != null) mDatabase.lock(); //can't call the locks for
		// some reason. beginTransaction does lock it though
		try {
			mIsInitializing = true;
			db = SQLiteDatabase.openOrCreateDatabase(dbFile, null);
			int version = db.getVersion();
			if (version != DATABASE_VERSION) {
				db.beginTransaction();
				try {
					if (version == 0) {
						onCreate(db);
					} else {
						onUpgrade(db, version, DATABASE_VERSION);
					}
					db.setVersion(DATABASE_VERSION);
					db.setTransactionSuccessful();
				} finally {
					db.endTransaction();
				}
			}

			onOpen(db);
			success = true;
			return db;
		} finally {
			mIsInitializing = false;
			if (success) {
				if (mDB != null) {
					try {
						mDB.close();
					} catch (Exception e) {
					}
					// mDatabase.unlock();
				}
				mDB = db;
			} else {
				// if (mDatabase != null) mDatabase.unlock();
				if (db != null)
					db.close();
			}
		}
	}
}

