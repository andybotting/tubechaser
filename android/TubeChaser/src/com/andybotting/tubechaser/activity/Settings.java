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

package com.andybotting.tubechaser.activity;

import com.andybotting.tubechaser.R;

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.PreferenceActivity;

public class Settings extends PreferenceActivity implements OnSharedPreferenceChangeListener {
	public static final String KEY_DEFAULT_LAUNCH_ACTIVITY = "defaultLaunchActivity";
	public static final String KEY_NUMBER_OF_DEPARTURES = "numberOfDepartures";
	public static final String KEY_SEND_STATS = "sendUsageStats";
	public static final String KEY_NAT_RAIL_API = "nationalRailAPICode";
	
	public static final boolean KEY_SEND_STATS_DEFAULT_VALUE = false;
	
	private ListPreference mDefaultLaunchActivity;
	private ListPreference mNumberOfDepartures;
	private CheckBoxPreference mSendStats;
	//private EditTextPreference mNationalRailAPICode;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);        
        // Load the preferences from an XML resource
        addPreferencesFromResource(R.layout.settings);
        
        // Get a reference to the preferences
        mDefaultLaunchActivity = (ListPreference)getPreferenceScreen().findPreference(KEY_DEFAULT_LAUNCH_ACTIVITY);
        mSendStats = (CheckBoxPreference)getPreferenceScreen().findPreference(KEY_SEND_STATS);
        mNumberOfDepartures = (ListPreference)getPreferenceScreen().findPreference(KEY_NUMBER_OF_DEPARTURES);
        //mNationalRailAPICode = (EditTextPreference)getPreferenceScreen().findPreference(KEY_NUMBER_OF_DEPARTURES);
    }
    
    @Override
    protected void onResume() {
        super.onResume();

        // Setup the initial values
        setPreferenceSummary(getPreferenceScreen().getSharedPreferences(), KEY_DEFAULT_LAUNCH_ACTIVITY);
        setPreferenceSummary(getPreferenceScreen().getSharedPreferences(), KEY_NUMBER_OF_DEPARTURES);
        setPreferenceSummary(getPreferenceScreen().getSharedPreferences(), KEY_SEND_STATS);
        setPreferenceSummary(getPreferenceScreen().getSharedPreferences(), KEY_NAT_RAIL_API);
        
        // Set up a listener whenever a key changes            
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Unregister the listener whenever a key changes            
        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);    
    }
      
	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		setPreferenceSummary(sharedPreferences, key);
	}
    
    private void setPreferenceSummary(SharedPreferences sharedPreferences, String key) {
    	
		if(key.equals(KEY_DEFAULT_LAUNCH_ACTIVITY)){
			// Default launch activity
			mDefaultLaunchActivity.setSummary("Open " + getFriendlyDefaultActivityName(sharedPreferences) + " on launch.");
		}
		else if (key.equals(KEY_NUMBER_OF_DEPARTURES)) {
			// Number of departures
			String val = sharedPreferences.getString(KEY_NUMBER_OF_DEPARTURES, "3");
			if (val.matches("All")) {
				mNumberOfDepartures.setSummary("Showing all available departures.");
			}
			else {
				mNumberOfDepartures.setSummary("Show a maxiumum of " + sharedPreferences.getString(KEY_NUMBER_OF_DEPARTURES, "3") + " departures per platform.");
			}
		}
		else if(key.equals(KEY_SEND_STATS)) {
			// Send Stats
			mSendStats.setSummary(sharedPreferences.getBoolean(key, KEY_SEND_STATS_DEFAULT_VALUE) ? 
					"Send anonymous usage statistics." : "Don't send anonymous usage statistics.");
		}	
		
    }
    
    private String getFriendlyDefaultActivityName(SharedPreferences sharedPreferences){
    	// Because the default activity setting is saved as an activity name we need to get the friendly name to show the user
    	// in the summary of the setting
		String[] defaultActivityEntries = getResources().getStringArray(R.array.defaultActivityEntries);
		String[] defaultActivityEntryValues = getResources().getStringArray(R.array.defaultActivityEntryValues);
		String currentDefaultActivityValue = sharedPreferences.getString(KEY_DEFAULT_LAUNCH_ACTIVITY, "Home");
    	
		return defaultActivityEntries[findStringItemIndex(defaultActivityEntryValues, currentDefaultActivityValue)];
    }
    
    private int findStringItemIndex(String[] array, String item){
    	
    	for(int i = 0; i < array.length; i++){
    		if(array[i].equals(item))
    			return i;
    	}
    	
    	return -1;
    }

	
}