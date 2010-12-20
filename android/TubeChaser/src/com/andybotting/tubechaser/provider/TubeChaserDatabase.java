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
import android.util.Log;


public class TubeChaserDatabase extends SQLiteOpenHelper {
	
    private static final String TAG = "TubeChaserDatabase";
    private static final boolean LOGV = Log.isLoggable(TAG, Log.INFO);

	private static final String AUTHORITY = "com.andybotting.tubechaser";
	private static final String DATABASE_NAME = "tubechaser.db";
	private static final String DATABASE_PATH = "/data/data/"+ AUTHORITY + "/databases/";
    
	// Update this with the App Version (App Version x 10)
	// E.g. 
	// 	App Version v0.1 = DB Version 1
	// 	App Version v1.2 = DB Version 12
	private static final int DATABASE_VERSION = 2;
	
	private SQLiteDatabase db; 
	private Context context;

	
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
		this.context = context;
		if (LOGV) Log.v(TAG, "Instantiating TubeChaser database");
	}	
	
    
	public SQLiteDatabase getDatabase() {
		SQLiteDatabase db;  
		
		try {		 	
			this.createDataBase();
		} 
		catch (IOException ioe) {
			throw new Error("Unable to create database");
		}
	
		try {
			this.openDataBase();
		}
		catch(SQLException sqle){
			throw sqle;
		}

		db = this.getWritableDatabase();
		return db;
	}
	
	public void createDataBase() throws IOException{
 
		boolean dbExist = checkDataBase();
 
		if(dbExist){
			String myPath = DATABASE_PATH + DATABASE_NAME;
			db = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READONLY);
			int thisDBVersion = db.getVersion();
			db.close();
			
			if (thisDBVersion < DATABASE_VERSION) {							
				try {
					copyDataBase();
				} catch (IOException e) {
					throw new Error("Error copying database");
				}
			}	
		}
		else{
			// By calling this method and empty database will be created into the default system path
			// of your application so we are gonna be able to overwrite that database with our database.
			this.getReadableDatabase();
 
			try {
				copyDataBase();
			} catch (IOException e) {
				throw new Error("Error copying database");
			}
		}
	}
 
	
	/**
	 * Check if the database already exist to avoid re-copying the file each time you open the application.
	 * @return true if it exists, false if it doesn't
	 */
	private boolean checkDataBase(){
 
		SQLiteDatabase checkDB = null;
 
		try {
			String myPath = DATABASE_PATH + DATABASE_NAME;
			checkDB = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READONLY);
		}
		catch(SQLiteException e){
			//database does't exist yet.
		}
 
		if(checkDB != null){
			checkDB.close();
		}
 
		return checkDB != null ? true : false;
	}
 
	/**
	 * Copies your database from your local assets-folder to the just created empty database in the
	 * system folder, from where it can be accessed and handled.
	 * This is done by transfering bytestream.
	 * */
	private void copyDataBase() throws IOException {
		
		if (LOGV) Log.v(TAG, "Resetting last fetched timestamp");
		
		// Reset out last fetched status date
		PreferenceHelper preferenceHelper = new PreferenceHelper(context);
		preferenceHelper.resetLastUpdateTimestamp();
		
		if (LOGV) Log.v(TAG, "Copying packaged database into position");
		
		// Open your local db as the input stream
		InputStream myInput = context.getAssets().open(DATABASE_NAME);

		// Path to the just created empty db
		String outFileName = DATABASE_PATH + DATABASE_NAME;
 
		// Open the empty db as the output stream
		OutputStream myOutput = new FileOutputStream(outFileName);
 
		// Transfer bytes from the inputfile to the outputfile
		byte[] buffer = new byte[1024];
		int length;
		while ( (length = myInput.read(buffer) ) > 0) {
			myOutput.write(buffer, 0, length);
		}
 
		// Close the streams
		myOutput.flush();
		myOutput.close();
		myInput.close();
	}

	
	public void openDataBase() throws SQLException {
	 	// Open the database
		String myPath = DATABASE_PATH + DATABASE_NAME;
		db = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READONLY);
		// Close the DB to prevent a leak
		db.close();
	}
 
	@Override
	public synchronized void close() {
		if(db != null)
			db.close();
		super.close();
	}
 
	@Override
	public void onCreate(SQLiteDatabase db) {
		// Do nothing here
	}
 
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// Do nothing here
	}
    
    
}


