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

package com.andybotting.tubechaser.utils;

import java.util.ArrayList;
import java.util.Date;

import com.andybotting.tubechaser.TubeChaser;
import com.andybotting.tubechaser.activity.Settings;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.preference.PreferenceManager;
import android.util.Log;

public class PreferenceHelper {
	
    private static final String TAG = "PreferenceHelper";
    private static final boolean LOGV = Log.isLoggable(TAG, Log.INFO);
    
	private static final String KEY_STARRED_STATIONS_STRING = "starred_stations_string";
	private static final String KEY_FIRST_LAUNCH_VERSION = "first_launch_version";
	private static final String KEY_LAST_UPDATE = "last_update";
	private static final String KEY_STATS_TIMESTAMP = "stats_timestamp";	
	
	private final SharedPreferences mPreferences;
	private final Context mContext;
	
	public PreferenceHelper() {
		this.mContext = TubeChaser.getContext();
		this.mPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
	}
	
	
    /**
     * Return a string representing the default launch activity
     */
	public String defaultLaunchActivity() {
		return mPreferences.getString(Settings.KEY_DEFAULT_LAUNCH_ACTIVITY, "HomeActivity");
	}
	
    /**
     * Return an integer representing the number of departures to show 
     */
	public int numberOfDepartures() {
		
		String val = mPreferences.getString(Settings.KEY_NUMBER_OF_DEPARTURES, "3");
		
		if (val.matches("All")) {
			return 99;
		}
		else {
			return Integer.parseInt(val);
		}
		
	}
		
	
    /**
     * Return a boolean for whether stats sending is enabled
     */
	public boolean isSendStatsEnabled() {
		return mPreferences.getBoolean(Settings.KEY_SEND_STATS, Settings.KEY_SEND_STATS_DEFAULT_VALUE);
	}
	
    /**
     * Return a boolean for whether the National Rail API code is set
     */
	public boolean isNationalRailAPIEnabled() {
		boolean enabled = false;
		String val = mPreferences.getString(Settings.KEY_NAT_RAIL_API, null);
		
		if (val != null) {
			if (val.length() > 1) {
				enabled = true;
			}
		}
		
		return enabled;
	}
	
	
    /**
     * Return an string representing the stored National Rail API code
     */
	public String getNationalRailAPICode() {
		return mPreferences.getString(Settings.KEY_NAT_RAIL_API, null);
	}	
	
	
    /**
     * Return a boolean representing that this is the first launch for this version
     */
	public boolean isFirstLaunchThisVersion() {
		long lastVersion = mPreferences.getLong(KEY_FIRST_LAUNCH_VERSION, 0);
		
		try {
			PackageInfo pi = mContext.getPackageManager().getPackageInfo(mContext.getPackageName(), 0);
			if (lastVersion < pi.versionCode) {
				return true;
			}
		} catch (NameNotFoundException e) {
			// Nothing
		}
		
		return false;
	}
	
    /**
     * Set a long signalling the lastest version of the application launched
     */	
	public void setFirstLaunchThisVersion() {
		
		try {
			PackageInfo pi = mContext.getPackageManager().getPackageInfo(mContext.getPackageName(), 0);
			SharedPreferences.Editor editor = mPreferences.edit();
			editor.putLong(KEY_FIRST_LAUNCH_VERSION, pi.versionCode);
			editor.commit();
		} catch (NameNotFoundException e) {
			// Nothing
		}
	}
		
		

    /**
     * Return a long representing the last update
     */
	public long getLastUpdateTimestamp() {
		return mPreferences.getLong(KEY_LAST_UPDATE, 0);
	}
	
    /**
     * Set a long representing the last stats send date
     */	
	public void setLastUpdateTimestamp() {
		Date now = new Date();
		SharedPreferences.Editor editor = mPreferences.edit();	
		editor.putLong(KEY_LAST_UPDATE, now.getTime());
		editor.commit();
	}
	
    /**
     * Reset the last stats send date
     */	
	public void resetLastUpdateTimestamp() {
		SharedPreferences.Editor editor = mPreferences.edit();	
		editor.putLong(KEY_LAST_UPDATE, 0);
		editor.commit();
	}	
	
	/**
     * Return a long representing the last stats send date
     */
	public long getStatsTimestamp() {
		return mPreferences.getLong(KEY_STATS_TIMESTAMP, 0);
	}
	
    /**
     * Set a long representing the last update
     */	
	public void setStatsTimestamp() {
		Date now = new Date();
		SharedPreferences.Editor editor = mPreferences.edit();
		editor.putLong(KEY_STATS_TIMESTAMP, now.getTime());
		editor.commit();
	}	

	/**
     * Return a string representing the starred station/lines
     */
	public String getStarredStationsString() {
		return mPreferences.getString(KEY_STARRED_STATIONS_STRING, "");
	}	
	
    /**
     * Set a string representing the starred station/lines
     */	
	public void setStarredStationsString(String stationsString) {
		SharedPreferences.Editor editor = mPreferences.edit();
		editor.putString(KEY_STARRED_STATIONS_STRING, stationsString);
		editor.commit();
	}
	
    /**
     * Set a long representing the last update
     */		
	public boolean isStarred(long stationId, long lineId) {
		String starredStationsString = getStarredStationsString();
		if (!starredStationsString.equalsIgnoreCase("")) {
			ArrayList<String> list = StringUtil.parseString(starredStationsString);
			for (String item : list) {
				String[] kv = item.split(":");
				if ( kv[0].matches(String.valueOf(stationId)) && (kv[1].matches(String.valueOf(lineId))) ) {
					return true;
				}
			}
		}
		return false;
	}
	
    /**
     * Append the new station to the end of the station string
     * and set it in the mPreferences.
     */	
	public void starStation(long stationId, long lineId) {
		if (!isStarred(stationId, lineId)) {
			if (LOGV) Log.v(TAG, "Starring Station: " + String.valueOf(stationId) + ":" + String.valueOf(lineId));
			String givenItem = String.format("%s:%s", stationId, lineId);
			ArrayList<String> list = StringUtil.parseString(getStarredStationsString());
			list.add(givenItem);
			setStarredStationsString(StringUtil.makeString(list));
		}
	}
	
    /**
     * Generate a new station string, removing the given station/line
     * and set it in the mPreferences.
     */	
	public void unstarStation(long stationId, long lineId) {
		if (isStarred(stationId, lineId)) {
			String givenItem = String.format("%s:%s", stationId, lineId);
			if (LOGV) Log.v(TAG, "Unstarring Station: " + givenItem);
			String starredStationsString = getStarredStationsString();
			ArrayList<String> list = StringUtil.parseString(starredStationsString);
			
			for (int i=0; i < list.size(); i++) {
				if (list.get(i).matches(givenItem))
					list.remove(i);
			}
			setStarredStationsString(StringUtil.makeString(list));
		}
	
	}
	
}
