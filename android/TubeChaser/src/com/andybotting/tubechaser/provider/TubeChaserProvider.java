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

import java.util.ArrayList;

import java.util.HashMap;
import java.util.List;

import com.andybotting.tubechaser.objects.Line;
import com.andybotting.tubechaser.objects.Station;
import com.andybotting.tubechaser.provider.TubeChaserContract.LineStationsColumns;
import com.andybotting.tubechaser.provider.TubeChaserContract.Lines;
import com.andybotting.tubechaser.provider.TubeChaserContract.LinesColumns;
import com.andybotting.tubechaser.provider.TubeChaserContract.Stations;
import com.andybotting.tubechaser.provider.TubeChaserContract.StationsColumns;
import com.andybotting.tubechaser.provider.TubeChaserDatabase.Tables;
import com.andybotting.tubechaser.utils.PreferenceHelper;

import android.app.SearchManager;
import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.provider.BaseColumns;
import android.util.Log;

public class TubeChaserProvider extends ContentProvider {

    private static final String TAG = "TubeChaserProvider";
    private static final boolean LOGV = Log.isLoggable(TAG, Log.INFO);
    
    private SQLiteDatabase mDB;

    private static final UriMatcher sUriMatcher = buildUriMatcher();

    // Database columns
    private static final int SEARCH_SUGGEST = 0;
    
    private static final int LINES = 100;
    private static final int LINES_ID = 101;
    private static final int LINES_ID_STATIONS = 102;
    
    private static final int STATIONS = 200;
    private static final int STATIONS_ID = 201;
    private static final int STATIONS_ID_LINES = 202;
    private static final int STATIONS_STARRED = 203;
    private static final int STATIONS_SEARCH = 204;
    
    private static final int STATIONS_ID_LINES_ID_CODE = 301;
    
    // Projection Maps
    public static final HashMap<String, String> sLinesProjection;
    public static final HashMap<String, String> sStationsProjection;
    public static final HashMap<String, String> sLineStationsProjection;
    public static final HashMap<String, String> sSearchProjection; 
    
    /**
     * Matcher for Uri's
     */
    private static UriMatcher buildUriMatcher() {
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = TubeChaserContract.CONTENT_AUTHORITY;

        matcher.addURI(authority, "lines", LINES);
        matcher.addURI(authority, "lines/*/stations", LINES_ID_STATIONS);
        matcher.addURI(authority, "lines/*", LINES_ID);

        matcher.addURI(authority, "stations", STATIONS);
        matcher.addURI(authority, "stations/starred", STATIONS_STARRED);
        matcher.addURI(authority, "stations/search/*", STATIONS_SEARCH);
        matcher.addURI(authority, "stations/*/lines/*/code", STATIONS_ID_LINES_ID_CODE);
        matcher.addURI(authority, "stations/*/lines", STATIONS_ID_LINES);
        matcher.addURI(authority, "stations/*", STATIONS_ID);
        
    	matcher.addURI(authority, SearchManager.SUGGEST_URI_PATH_QUERY, SEARCH_SUGGEST);
    	matcher.addURI(authority, SearchManager.SUGGEST_URI_PATH_QUERY + "/*", SEARCH_SUGGEST);

    	return matcher;
    }
    
    /**
     * Definitions for projections
     */
    static {
    	// Lines
	    HashMap<String, String> map = new HashMap<String, String>();
	    map.put(BaseColumns._ID, 			Tables.LINES + "." + BaseColumns._ID);
	    map.put(LinesColumns.NAME, 			Tables.LINES + "." + LinesColumns.NAME);
	    map.put(LinesColumns.SHORTNAME, 	Tables.LINES + "." + LinesColumns.SHORTNAME);
	    map.put(LinesColumns.TFL_ID, 		Tables.LINES + "." + LinesColumns.TFL_ID);
	    map.put(LinesColumns.CODE, 			Tables.LINES + "." + LinesColumns.CODE);
	    map.put(LinesColumns.TYPE, 			Tables.LINES + "." + LinesColumns.TYPE);
	    map.put(LinesColumns.COLOUR, 		Tables.LINES + "." + LinesColumns.COLOUR);
	    map.put(LinesColumns.STATUS, 		Tables.LINES + "." + LinesColumns.STATUS);
	    map.put(LinesColumns.STATUS_CODE, 	Tables.LINES + "." + LinesColumns.STATUS_CODE);
	    map.put(LinesColumns.STATUS_DESC, 	Tables.LINES + "." + LinesColumns.STATUS_DESC);
	    map.put(LinesColumns.STATUS_CLASS, 	Tables.LINES + "." + LinesColumns.STATUS_CLASS);
	    sLinesProjection = map;

	    // Stations
	    map = new HashMap<String, String>();
	    map.put(BaseColumns._ID, 			Tables.STATIONS + "." + BaseColumns._ID);
	    map.put(StationsColumns.NAME, 		Tables.STATIONS + "." + StationsColumns.NAME);
	    map.put(StationsColumns.LINES, 		Tables.STATIONS + "." + StationsColumns.LINES);
	    map.put(StationsColumns.TFL_ID,		Tables.STATIONS + "." + StationsColumns.TFL_ID);	    
	    map.put(StationsColumns.LATITUDE, 	Tables.STATIONS + "." + StationsColumns.LATITUDE);
	    map.put(StationsColumns.LONGITUDE, 	Tables.STATIONS + "." + StationsColumns.LONGITUDE);
	    map.put(StationsColumns.STATUS, 	Tables.STATIONS + "." + StationsColumns.STATUS);
	    map.put(StationsColumns.STATUS_CODE,Tables.STATIONS + "." + StationsColumns.STATUS_CODE);
	    map.put(StationsColumns.STATUS_DESC,Tables.STATIONS + "." + StationsColumns.STATUS_DESC);
	    map.put(StationsColumns.STEPFREE, 	Tables.STATIONS + "." + StationsColumns.STEPFREE);
	    sStationsProjection = map;

	    // Line Stations
	    map = new HashMap<String, String>();
	    map.put(BaseColumns._ID, 				Tables.LINES_STATIONS + "." + BaseColumns._ID);
	    map.put(LineStationsColumns.STATION_ID, Tables.LINES_STATIONS + "." + LineStationsColumns.STATION_ID);
	    map.put(LineStationsColumns.LINE_ID, 	Tables.LINES_STATIONS + "." + LineStationsColumns.LINE_ID);
	    map.put(LineStationsColumns.CODE, 		Tables.LINES_STATIONS + "." + LineStationsColumns.CODE);	
	    sLineStationsProjection = map;
	    
	    // Search
	    map = new HashMap<String, String>();
	    map.put(BaseColumns._ID, 							BaseColumns._ID);
	    map.put(StationsColumns.NAME, 						StationsColumns.NAME + " AS " + SearchManager.SUGGEST_COLUMN_TEXT_1);
	    map.put(StationsColumns.LINES, 						StationsColumns.LINES + " AS " + SearchManager.SUGGEST_COLUMN_TEXT_2);
	    map.put(SearchManager.SUGGEST_COLUMN_INTENT_DATA, 	BaseColumns._ID + " AS " + SearchManager.SUGGEST_COLUMN_INTENT_DATA);
	    sSearchProjection = map;
	    
    }
    
    /**
     * On DB create
     */
    @Override
    public boolean onCreate() {
    	// TODO: context should be set globally for this class 
    	// not given for each method
        final Context context = getContext();
        TubeChaserDatabase mDBHelper = new TubeChaserDatabase(context);
        mDB = mDBHelper.getDatabase();
        return true;
    }

    /**
     * Get the mime type from a given Uri
     */
    @Override
    public String getType(Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case LINES:
                return Lines.CONTENT_TYPE;
            case LINES_ID:
                return Lines.CONTENT_ITEM_TYPE;
            case LINES_ID_STATIONS:
                return Stations.CONTENT_TYPE;
            case STATIONS:
                return Stations.CONTENT_TYPE;
            case STATIONS_ID:
                return Stations.CONTENT_ITEM_TYPE;
            case STATIONS_ID_LINES:
                return Lines.CONTENT_TYPE;
            case STATIONS_STARRED:
            	return Stations.CONTENT_TYPE;
            case STATIONS_SEARCH:
            	return Stations.CONTENT_TYPE;
            case STATIONS_ID_LINES_ID_CODE:
                return Stations.CONTENT_ITEM_TYPE;	
            case SEARCH_SUGGEST:
            	return Stations.CONTENT_ITEM_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }
    
    
    /**
     * Database query
     */
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        if (LOGV) Log.v(TAG, "query(uri:" + uri + ")");
        
        String limit = null;
        Uri notificationUri = uri;

        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();

        final int match = sUriMatcher.match(uri);
        switch (match) {
	        case LINES: {
	            qb.setTables(Tables.LINES);
	            qb.setProjectionMap(sLinesProjection);
	            break;
	        }
	        case LINES_ID: {
	        	final long lineId = Lines.getLineId(uri);
	        	qb.setTables(Tables.LINES);
	            qb.setProjectionMap(sLinesProjection);
	            qb.appendWhere(BaseColumns._ID + "=" + lineId);
	            break;
	        }
	        case LINES_ID_STATIONS: {
                final List<String> segments = uri.getPathSegments();
                final long lineId = Long.parseLong(segments.get(1));
	        	qb.setTables(Tables.STATIONS_JOIN_LINES);
	            qb.setProjectionMap(sStationsProjection);	
	            qb.appendWhere(Tables.LINES + "." + BaseColumns._ID + " = " + lineId);
	            break;
	        }
	        case STATIONS: {
	            qb.setTables(Tables.STATIONS);
	            qb.setProjectionMap(sStationsProjection);
	            sortOrder = StationsColumns.NAME;
	            break;
	        }
	        case STATIONS_ID: {
	        	final long stationId = Stations.getStationId(uri);
	        	qb.setTables(Tables.STATIONS);
	            qb.setProjectionMap(sStationsProjection);
	            qb.appendWhere(BaseColumns._ID + " = " + stationId);
	            break;
	        }
	        case STATIONS_ID_LINES: {
                final List<String> segments = uri.getPathSegments();
                final long stationId = Long.parseLong(segments.get(1));
	        	qb.setTables(Tables.STATIONS_JOIN_LINES);
	            qb.setProjectionMap(sLinesProjection);
	            qb.appendWhere(Tables.STATIONS + "." + BaseColumns._ID + " = " + stationId);
	            break;
	        }
	        case STATIONS_ID_LINES_ID_CODE: {
                final List<String> segments = uri.getPathSegments();
                final long stationId = Long.parseLong(segments.get(1));
                final long lineId = Long.parseLong(segments.get(3));
	        	qb.setTables(Tables.LINES_STATIONS);
	            qb.setProjectionMap(sLineStationsProjection);
	            qb.appendWhere(LineStationsColumns.STATION_ID + " = " + stationId + " AND " + LineStationsColumns.LINE_ID + " = " + lineId);
	            break;	        	
	        }
	        case STATIONS_SEARCH: {
	        	String query = null;
                if (uri.getPathSegments().size() > 1)
                    query = uri.getLastPathSegment().toLowerCase();

	        	qb.setTables(Tables.STATIONS);
	        	qb.setProjectionMap(sStationsProjection);
	            qb.appendWhere(Stations.NAME + " LIKE '%" + query +"%'");
	            sortOrder = StationsColumns.NAME;
	            break;
	        }
	        case SEARCH_SUGGEST: {
            	String query = null;
            	// If no search text yet, show all stations
                if (uri.getPathSegments().size() > 1) {
                    query = uri.getLastPathSegment().toLowerCase();
    	            qb.appendWhere(Stations.NAME + " LIKE '%" + query +"%'");
                }
	        	qb.setTables(Tables.STATIONS);
	        	qb.setProjectionMap(sSearchProjection);
	        	sortOrder = StationsColumns.NAME;
	            break;
	        }
        }

        Cursor c = qb.query(mDB, projection, selection, selectionArgs, null, null, sortOrder, limit);
        c.setNotificationUri(getContext().getContentResolver(), notificationUri);
        return c;
    }
    
    
    /**
     * Update the database
     */
    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
    	if (LOGV) Log.v(TAG, "update-> uri(" + uri + ") values(" + values.toString() + ")");
    	
        int count;
        Uri notifyUri = uri;
        switch (sUriMatcher.match(uri)) {
            case LINES_ID: {
                long lineId = ContentUris.parseId(uri);
                count = mDB.update(Tables.LINES, values, BaseColumns._ID + "=" + lineId, null);
                getContext().getContentResolver().notifyChange(Stations.CONTENT_URI, null);
                break;
            }
            case STATIONS_ID: {
                long stationId = ContentUris.parseId(uri);
                count = mDB.update(Tables.STATIONS, values, BaseColumns._ID + "=" + stationId, null);
                getContext().getContentResolver().notifyChange(Stations.CONTENT_URI, null);
                break;
            }
            default: {
                throw new IllegalArgumentException("Unknown URI " + uri);
            }
        }

        getContext().getContentResolver().notifyChange(notifyUri, null, false);
        return count;
    }
    
    
    /**
     * Database delete is not implemented
     */
	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		return 0;
	}

	
	/** 
	 * Database insert is not implemented
	 */
	@Override
	public Uri insert(Uri uri, ContentValues values) {
		return null;
	}
	
	
    /**
     * Build a line object from a given cursor
     */
    private Line buildLine(Cursor c) {

    	int col_id = c.getColumnIndexOrThrow(LinesColumns.ID);
		int col_name = c.getColumnIndexOrThrow(LinesColumns.NAME);
		int col_code = c.getColumnIndexOrThrow(LinesColumns.CODE);
		int col_tfl_id = c.getColumnIndexOrThrow(LinesColumns.TFL_ID);
		int col_short_name = c.getColumnIndexOrThrow(LinesColumns.SHORTNAME);
		int col_colour = c.getColumnIndexOrThrow(LinesColumns.COLOUR);
		int col_type = c.getColumnIndexOrThrow(LinesColumns.TYPE);
		int col_status = c.getColumnIndexOrThrow(LinesColumns.STATUS);
		int col_status_code = c.getColumnIndexOrThrow(LinesColumns.STATUS_CODE);
		int col_status_desc = c.getColumnIndexOrThrow(LinesColumns.STATUS_DESC);
		int col_status_class = c.getColumnIndexOrThrow(LinesColumns.STATUS_CLASS);
		
		Line line = new Line();
		
		line.setId(c.getInt(col_id));
		line.setName(c.getString(col_name));
		line.setShortName(c.getString(col_short_name));
		line.setCode(c.getString(col_code));
		line.setTfLID(c.getString(col_tfl_id));
		line.setColour(c.getString(col_colour));
		line.setType(c.getInt(col_type));
		line.setStatus(c.getString(col_status));
		line.setStatusCode(c.getString(col_status_code));
		line.setStatusDesc(c.getString(col_status_desc));
		line.setStatusClass(c.getString(col_status_class));

		return line;
    }
    
    
    /**
     * Build a station object from a given cursor
     */
    private Station buildStation(Cursor c) {
    	
		int col_id = c.getColumnIndexOrThrow(StationsColumns.ID);
		int col_name = c.getColumnIndexOrThrow(StationsColumns.NAME);
		int col_lines = c.getColumnIndexOrThrow(StationsColumns.LINES);
		int col_tfl_id = c.getColumnIndexOrThrow(StationsColumns.TFL_ID);
		int col_latitude = c.getColumnIndexOrThrow(StationsColumns.LATITUDE);
		int col_longitude = c.getColumnIndexOrThrow(StationsColumns.LONGITUDE);
		int col_status = c.getColumnIndexOrThrow(StationsColumns.STATUS);
		int col_status_code = c.getColumnIndexOrThrow(StationsColumns.STATUS_CODE);
		int col_status_desc = c.getColumnIndexOrThrow(StationsColumns.STATUS_DESC);
		int col_stepfree = c.getColumnIndexOrThrow(StationsColumns.STEPFREE);

		Station station = new Station();
	
		station.setId(c.getInt(col_id));
		station.setName(c.getString(col_name));
		station.setTflId(c.getString(col_tfl_id));
		station.setLinesString(c.getString(col_lines));
		station.setLatitude(c.getDouble(col_latitude));
		station.setLongitude(c.getDouble(col_longitude));
		station.setStatus(c.getString(col_status));
		station.setStatusCode(c.getString(col_status_code));
		station.setStatusDesc(c.getString(col_status_desc));
		station.setStepFree(c.getInt(col_stepfree));

		return station;
    }
    

    /**
     * Get lines from a given Uri
     */
	public List<Line> getLines(Context context, Uri uri) {
		List<Line> tubeLines = new ArrayList<Line>();
		Cursor c = context.getContentResolver().query(uri, null, null, null, null);
	
		if (c.moveToFirst()) {
			do {
				Line tubeLine = buildLine(c);
				tubeLines.add(tubeLine);
			} while(c.moveToNext());
		}
		
		c.close();
		return tubeLines;
	}
    
    
	/**
	 * Get all lines
	 */
	public List<Line> getLines(Context context) {
		Uri uri = Lines.CONTENT_URI;
		List<Line> tubeLines = new ArrayList<Line>();

		Cursor c = context.getContentResolver().query(uri, null, null, null, null);
		if (c.moveToFirst()) {
			do {
				Line tubeLine = buildLine(c);
				tubeLines.add(tubeLine);
			} while(c.moveToNext());
		}
		
		c.close();
		return tubeLines;
	}
	
	
	/**
	 * Get a line from a given Uri
	 */
	public Line getLine(Context context, Uri uri) {
		Line tubeLine = null;
		Cursor c = context.getContentResolver().query(uri, null, null, null, null);

		if (c.moveToFirst())		
			tubeLine = buildLine(c);

		c.close();
		return tubeLine;
	}	

	
	/**
	 * Get a list of all stations
	 */
	public List<Station> getStations(Context context) {
		Uri uri = Stations.CONTENT_URI;
		List<Station> stations = new ArrayList<Station>();
		Cursor c = context.getContentResolver().query(uri, null, null, null, null); // add projection later

		if (c.moveToFirst()) {	
			do {
				Station tubeStation = buildStation(c);
				stations.add(tubeStation);
			} while(c.moveToNext());
		}
		
		c.close();
		return stations;
	}	
	
	
	/**
	 * Get a list of stations for a given line Uri
	 */
	public List<Station> getStationsForLine(Context context, Uri uri) {
		List<Station> stations = new ArrayList<Station>();
		Cursor c = context.getContentResolver().query(uri, null, null, null, null);

		if (c.moveToFirst()) {		
			do {
				Station tubeStation = buildStation(c);
				stations.add(tubeStation);
			} while(c.moveToNext());
		}

		c.close();
		return stations;
	}

	
	/**
	 * Get a list of stations from a given Uri
	 */
	public List<Station> getStations(Context context, Uri uri) {
		List<Station> stations = new ArrayList<Station>();
		Cursor c = context.getContentResolver().query(uri, null, null, null, null);

		if (c.moveToFirst()) {		
			do {
				Station tubeStation = buildStation(c);
				stations.add(tubeStation);
			} while(c.moveToNext());
		}

		c.close();
		return stations;
	}
	
	
	/**
	 * Get a station from a given Uri
	 */
	public Station getStation(Context context, Uri uri) {
		Station station = null;		
		Cursor c = context.getContentResolver().query(uri, null, null, null, null);

		if (c.moveToFirst())
			station = buildStation(c);

		c.close();
		return station;
	}
	
	
	/**
	 * Get a list of stations out of the preferences
	 */
	public List<Station> getStarredStations(Context context) {
		List<Station> stations = new ArrayList<Station>();
		PreferenceHelper preferenceHelper = new PreferenceHelper();	

		String starredStationsString = preferenceHelper.getStarredStationsString();
		if (!starredStationsString.matches("")) {
			String[] items = starredStationsString.split(",");
		
			for (String item : items) {
				String[] kv = item.split(":");

				long stationId = Long.parseLong(kv[0]); 
				long lineId = Long.parseLong(kv[1]);
				
				Uri stationUri = Stations.buildStationUri(stationId);
				Uri lineUri = Lines.buildLineUri(lineId);
				
				Station station = getStation(context, stationUri);
				List<Line> line = getLines(context, lineUri);
				station.setLines(line);
				
				stations.add(station);
			}
			
		}
		
		return stations;
	}
	
	
	/**
	 * Get the 3-letter station code for the Line/Station combination.
	 * Each service (tube, overground, dlr) has it's own code.
	 */
	public String getLineStationCode(Context context, Uri uri) {
		
		String station_code = null;
		Cursor c = context.getContentResolver().query(uri, null, null, null, null);

		if (c.moveToFirst()) {		
			int col_station_code = c.getColumnIndexOrThrow(LineStationsColumns.CODE);
			station_code = c.getString(col_station_code);
			
			// If the code is blank, return null
			if (station_code.length() < 2)
				return null;
		}
		c.close();
		return station_code;
	}

}
