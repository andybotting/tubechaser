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

import android.app.SearchManager;
import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;


public class TubeChaserContract {

    public static final String CONTENT_AUTHORITY = "com.andybotting.tubechaser";
    private static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);
	

    public static final String PATH_LINES = "lines";
    public static final String PATH_STATIONS = "stations";
    public static final String PATH_LINE_STATIONS = "line_stations";
    public static final String PATH_SEARCH = "search";    
    public static final String PATH_STARRED = "starred";
	
	// Database column definitions
	interface LinesColumns {
		String ID = "_id";
		String NAME = "name";
		String SHORTNAME = "shortname";
		String CODE = "code";
		String TFL_ID = "tfl_id";
		String TYPE = "type";
		String COLOUR = "colour";
		String STATUS = "status";
		String STATUS_DESC = "status_desc";
		String STATUS_CODE = "status_code";
		String STATUS_CLASS = "status_class";
	}
	 
	interface StationsColumns {
		String ID = "_id";
		String NAME = "name";
		String CODE = "code";
		String LINES = "lines";
		String TFL_ID = "tfl_id";
		String LATITUDE = "latitude";
		String LONGITUDE = "longitude";
		String STATUS = "status";
		String STATUS_CODE = "status_code";
		String STATUS_DESC = "status_desc";
		String STEPFREE = "stepfree";
	 }
	 

    /**
     * Lines
     */
    public static class Lines implements LinesColumns, BaseColumns {
        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_LINES).build();
        public static final Uri CONTENT_EXPORT_URI = CONTENT_URI.buildUpon().appendPath(PATH_LINES).build();

        /** Default "ORDER BY" clause. */
        public static final String DEFAULT_SORT = LinesColumns.NAME;

        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.tubechaser.line";
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.tubechaser.line";

        public static Uri buildLineUri(long lineId) {
            return ContentUris.withAppendedId(CONTENT_URI, lineId);
        }

        public static long getLineId(Uri uri) {
            return ContentUris.parseId(uri);
        }
        
        public static Uri buildStationsUri(long lineId) {
            return CONTENT_URI.buildUpon().appendPath(String.valueOf(lineId)).appendPath(PATH_STATIONS).build();
        }

        public static Uri buildStationsUri(Uri linesUri) {
        	return linesUri.buildUpon().appendPath(PATH_STATIONS).build();
        }
    }
	
    /**
     * Stations
     */
    public static class Stations implements StationsColumns, BaseColumns {
        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_STATIONS).build();
        public static final Uri CONTENT_EXPORT_URI = CONTENT_URI.buildUpon().appendPath(PATH_STATIONS).build();

        /** Default "ORDER BY" clause. */
        public static final String DEFAULT_SORT = StationsColumns.NAME;

        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.tubechaser.station";
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.tubechaser.station";

        public static Uri buildStationUri(long stationId) {
            return ContentUris.withAppendedId(CONTENT_URI, stationId);
        }

        public static long getStationId(Uri uri) {
            return ContentUris.parseId(uri);
        }
        
        public static Uri buildStarredUri() {
            return CONTENT_URI.buildUpon().appendPath(PATH_STARRED).build();
        }        
        
        public static Uri buildSearchUri(String query) {
            return CONTENT_URI.buildUpon().appendPath(PATH_SEARCH).appendPath(query).build();
        }
        
        // Build "stations/*/lines" uri for getting all stations for a line
        public static Uri buildLinesUri(long stationId) {
            return CONTENT_URI.buildUpon().appendPath(String.valueOf(stationId)).appendPath(PATH_LINES).build();
        }

        // Return a "stations/*/lines" uri for a given "/stations/*" Uri
        public static Uri buildLinesUri(Uri stationUri) {
        	return stationUri.buildUpon().appendPath(PATH_LINES).build();
        }
        
        public static String getSearchQuery(Uri uri) {
            return uri.getPathSegments().get(2);
        }
        
        // Return a "stations/*/lines" uri for a given "/stations/*" Uri
        public static Uri buildStarUri(long stationId, long lineId) {
        	return CONTENT_URI.buildUpon()
        		.appendPath(String.valueOf(stationId))
        		.appendPath(PATH_LINES)
        		.appendPath(String.valueOf(lineId))
        		.appendPath(PATH_STARRED)
        		.build();
        }
        
    }	
    
    /**
     * Search Suggest
     */    
    public static class SearchSuggest {
        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(SearchManager.SUGGEST_URI_PATH_QUERY).build();

        public static final String DEFAULT_SORT = SearchManager.SUGGEST_COLUMN_TEXT_1 + " COLLATE NOCASE ASC";
    }
	
}


